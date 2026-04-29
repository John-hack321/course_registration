package com.example.course_registration

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.course_registration.ui.theme.Course_registrationTheme
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder

// ── Brand Colors ──────────────────────────────────────────────────────────────
val UoNBlue       = Color(0xFF003580)
val UoNLightBlue  = Color(0xFF0057B8)
val UoNSky        = Color(0xFFE8F0FB)
val UoNGold       = Color(0xFFF5A623)
val SurfaceWhite  = Color(0xFFFFFFFF)
val TextPrimary   = Color(0xFF0A1628)
val TextSecondary = Color(0xFF5A6A85)
val DividerColor  = Color(0xFFE2E8F4)
val ErrorRed      = Color(0xFFD32F2F)
val SuccessGreen  = Color(0xFF2E7D32)
val PageBg        = Color(0xFFF4F7FC)

// ── SharedPrefs keys ─────────────────────────────────────────────────────────
private const val PREFS_NAME        = "uon_app"
private const val KEY_USERS         = "users_json"        // JSONArray of user objects
private const val KEY_LOGGED_IN     = "logged_in_username"
private const val KEY_REGISTRATIONS = "registrations_json" // JSONObject: username -> JSONArray of course codes

// ── Data Classes ──────────────────────────────────────────────────────────────
data class Course(
    val code: String,
    val name: String,
    val department: String,
    val credits: Int,
    val semester: String,
    val description: String
)

data class AppUser(
    val username: String,
    val fullName: String,
    val studentId: String,
    val passwordHash: String // simple hash for demo
)

// ── Course Catalogue ──────────────────────────────────────────────────────────
val COURSE_CATALOGUE = listOf(
    Course("SCS 3308", "Embedded Systems & Mobile Programming", "Computing & Informatics", 3, "Sem II 2025/26",
        "Covers microcontrollers, RTOS fundamentals, and Android/iOS mobile development patterns."),
    Course("SCS 3201", "Data Structures & Algorithms", "Computing & Informatics", 3, "Sem II 2025/26",
        "In-depth study of trees, graphs, dynamic programming, and complexity analysis."),
    Course("SCS 3305", "Database Systems", "Computing & Informatics", 3, "Sem II 2025/26",
        "Relational models, SQL, normalization, transactions, and NoSQL paradigms."),
    Course("SCS 3302", "Software Engineering", "Computing & Informatics", 3, "Sem II 2025/26",
        "SDLC, Agile/Scrum, design patterns, testing strategies, and project management."),
    Course("SCS 3306", "Computer Networks", "Computing & Informatics", 3, "Sem II 2025/26",
        "OSI model, TCP/IP, routing algorithms, network security, and cloud infrastructure."),
    Course("SCS 3310", "Artificial Intelligence", "Computing & Informatics", 3, "Sem II 2025/26",
        "Search algorithms, machine learning fundamentals, neural networks, and NLP basics."),
    Course("SCS 3304", "Operating Systems", "Computing & Informatics", 3, "Sem II 2025/26",
        "Process management, memory management, file systems, and concurrency control."),
    Course("SCS 3309", "Human-Computer Interaction", "Computing & Informatics", 3, "Sem II 2025/26",
        "User research, prototyping, usability testing, and accessibility standards."),
    Course("SCS 3401", "Information Security", "Computing & Informatics", 3, "Sem II 2025/26",
        "Cryptography, network security, ethical hacking, and security policy frameworks."),
    Course("SCS 3402", "Cloud Computing", "Computing & Informatics", 3, "Sem II 2025/26",
        "Cloud architecture, IaaS/PaaS/SaaS models, AWS/GCP services, and DevOps practices."),
    Course("MAT 3201", "Numerical Methods", "Mathematics", 3, "Sem II 2025/26",
        "Numerical solutions to equations, interpolation, numerical integration and differentiation."),
    Course("STA 3201", "Probability & Statistics", "Statistics & Actuarial", 3, "Sem II 2025/26",
        "Probability distributions, hypothesis testing, regression, and statistical modelling."),
)

// ── Storage Helper ────────────────────────────────────────────────────────────
class AppStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Users ---
    fun getUsers(): List<AppUser> {
        val json = prefs.getString(KEY_USERS, "[]") ?: "[]"
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            AppUser(obj.getString("username"), obj.getString("fullName"),
                obj.getString("studentId"), obj.getString("passwordHash"))
        }
    }

    fun saveUser(user: AppUser) {
        val users = getUsers().toMutableList()
        users.add(user)
        val arr = JSONArray()
        users.forEach { u ->
            arr.put(JSONObject().apply {
                put("username", u.username)
                put("fullName", u.fullName)
                put("studentId", u.studentId)
                put("passwordHash", u.passwordHash)
            })
        }
        prefs.edit().putString(KEY_USERS, arr.toString()).apply()
    }

    fun findUser(username: String): AppUser? = getUsers().find {
        it.username.equals(username, ignoreCase = true)
    }

    fun usernameExists(username: String): Boolean = findUser(username) != null

    // --- Session ---
    fun setLoggedIn(username: String?) {
        prefs.edit().putString(KEY_LOGGED_IN, username).apply()
    }

    fun getLoggedInUsername(): String? = prefs.getString(KEY_LOGGED_IN, null)

    // --- Registrations ---
    private fun getRegData(): JSONObject {
        val json = prefs.getString(KEY_REGISTRATIONS, "{}") ?: "{}"
        return JSONObject(json)
    }

    fun getRegisteredCourses(username: String): List<String> {
        val data = getRegData()
        if (!data.has(username)) return emptyList()
        val arr = data.getJSONArray(username)
        return (0 until arr.length()).map { arr.getString(it) }
    }

    fun registerCourse(username: String, courseCode: String) {
        val data = getRegData()
        val arr = if (data.has(username)) data.getJSONArray(username) else JSONArray()
        // Avoid duplicates
        val existing = (0 until arr.length()).map { arr.getString(it) }
        if (courseCode !in existing) arr.put(courseCode)
        data.put(username, arr)
        prefs.edit().putString(KEY_REGISTRATIONS, data.toString()).apply()
    }

    fun unregisterCourse(username: String, courseCode: String) {
        val data = getRegData()
        if (!data.has(username)) return
        val arr = data.getJSONArray(username)
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            if (arr.getString(i) != courseCode) newArr.put(arr.getString(i))
        }
        data.put(username, newArr)
        prefs.edit().putString(KEY_REGISTRATIONS, data.toString()).apply()
    }

    fun simpleHash(input: String): String = input.hashCode().toString()
}

// ── MainActivity ──────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge with LIGHT icons on the (transparent) status bar.
        // Each screen draws its own gradient/header behind the status bar so
        // the icons stay readable. SystemBarStyle.dark(...) here means
        // "the bar background is dark, so render light foreground icons".
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        setContent {
            Course_registrationTheme {
                AppRoot()
            }
        }
    }
}

// ── App Root: decides auth vs main ───────────────────────────────────────────
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val storage = remember { AppStorage(context) }
    var loggedInUser by remember { mutableStateOf(storage.getLoggedInUsername()) }

    if (loggedInUser == null) {
        AuthFlow(storage) { username ->
            loggedInUser = username
        }
    } else {
        val user = storage.findUser(loggedInUser!!)
        if (user == null) {
            storage.setLoggedIn(null)
            loggedInUser = null
        } else {
            MainAppShell(storage, user) {
                storage.setLoggedIn(null)
                loggedInUser = null
            }
        }
    }
}

// ── Auth Flow ─────────────────────────────────────────────────────────────────
@Composable
fun AuthFlow(storage: AppStorage, onLoggedIn: (String) -> Unit) {
    var showRegister by remember { mutableStateOf(false) }
    if (showRegister) {
        RegisterScreen(storage, onRegistered = { showRegister = false }, onBack = { showRegister = false })
    } else {
        LoginScreen(storage, onLoggedIn = onLoggedIn, onGoRegister = { showRegister = true })
    }
}

// ── Login Screen ──────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(storage: AppStorage, onLoggedIn: (String) -> Unit, onGoRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(UoNBlue, Color(0xFF1565C0), PageBg)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // Logo Area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite.copy(alpha = 0.15f))
                    .border(2.dp, SurfaceWhite.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("UoN", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("University of Nairobi", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Student Portal", color = UoNGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                    Text("Welcome back", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))

                    AuthTextField(
                        value = username,
                        onValueChange = { username = it; error = null },
                        label = "Username",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(Modifier.height(14.dp))
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = ErrorRed, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val user = storage.findUser(username.trim())
                            if (user == null) {
                                error = "Username not found"
                            } else if (user.passwordHash != storage.simpleHash(password)) {
                                error = "Incorrect password"
                            } else {
                                storage.setLoggedIn(user.username)
                                onLoggedIn(user.username)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UoNBlue)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No account? ", color = TextSecondary, fontSize = 13.sp)
                        TextButton(onClick = onGoRegister, contentPadding = PaddingValues(0.dp)) {
                            Text("Create one", color = UoNLightBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Register Screen ───────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(storage: AppStorage, onRegistered: () -> Unit, onBack: () -> Unit) {
    var fullName   by remember { mutableStateOf("") }
    var studentId  by remember { mutableStateOf("") }
    var username   by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var confirm    by remember { mutableStateOf("") }
    var pwVisible  by remember { mutableStateOf(false) }
    var error      by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(UoNBlue, Color(0xFF1565C0), PageBg)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = SurfaceWhite)
                }
                Text("Create Account", color = SurfaceWhite, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            }

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite.copy(alpha = 0.15f))
                    .border(2.dp, SurfaceWhite.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("UoN", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Text("Register", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary)
                    Text("Set up your student account", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))

                    AuthTextField(fullName, { fullName = it; error = null }, "Full Name", Icons.Default.Badge)
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(studentId, { studentId = it; error = null }, "Student ID (e.g. C02-0000/2022)", Icons.Default.CreditCard)
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(username, { username = it; error = null }, "Username", Icons.Default.Person)
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = password, onValueChange = { password = it; error = null },
                        label = "Password", leadingIcon = Icons.Default.Lock,
                        isPassword = true, passwordVisible = pwVisible,
                        onTogglePassword = { pwVisible = !pwVisible }
                    )
                    Spacer(Modifier.height(12.dp))
                    AuthTextField(
                        value = confirm, onValueChange = { confirm = it; error = null },
                        label = "Confirm Password", leadingIcon = Icons.Default.Lock,
                        isPassword = true, passwordVisible = pwVisible,
                        onTogglePassword = { pwVisible = !pwVisible }
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, color = ErrorRed, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            error = when {
                                fullName.isBlank()    -> "Full name is required"
                                studentId.isBlank()   -> "Student ID is required"
                                username.isBlank()    -> "Username is required"
                                username.length < 3   -> "Username must be at least 3 characters"
                                password.length < 6   -> "Password must be at least 6 characters"
                                password != confirm   -> "Passwords do not match"
                                storage.usernameExists(username.trim()) -> "Username already taken"
                                else -> null
                            }
                            if (error == null) {
                                val user = AppUser(
                                    username = username.trim(),
                                    fullName = fullName.trim(),
                                    studentId = studentId.trim(),
                                    passwordHash = storage.simpleHash(password)
                                )
                                storage.saveUser(user)
                                onRegistered()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UoNBlue)
                    ) {
                        Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = TextSecondary, fontSize = 13.sp)
                        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                            Text("Sign in", color = UoNLightBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Reusable Auth Text Field ──────────────────────────────────────────────────
@Composable
fun AuthTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    leadingIcon: ImageVector, isPassword: Boolean = false,
    passwordVisible: Boolean = false, onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(leadingIcon, null, tint = UoNLightBlue, modifier = Modifier.size(20.dp)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null, tint = TextSecondary, modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UoNLightBlue,
            unfocusedBorderColor = DividerColor,
            focusedContainerColor = UoNSky,
            unfocusedContainerColor = Color(0xFFF9FAFC),
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary)
    )
}

// ── Main App Shell (Bottom Nav) ───────────────────────────────────────────────
sealed class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    object Home       : BottomTab("home",       "Home",       Icons.Default.Home)
    object Courses    : BottomTab("courses",    "Courses",    Icons.Default.MenuBook)
    object MyCourses  : BottomTab("my_courses", "My Courses", Icons.Default.School)
    object Profile    : BottomTab("profile",    "Profile",    Icons.Default.Person)
}

val BOTTOM_TABS = listOf(BottomTab.Home, BottomTab.Courses, BottomTab.MyCourses, BottomTab.Profile)

@Composable
fun MainAppShell(storage: AppStorage, user: AppUser, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Refresh trigger for registration changes
    var refreshKey by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = PageBg,
        // IMPORTANT: don't let Scaffold reserve the status-bar area as padding
        // — each screen's gradient header draws BEHIND the status bar itself
        // (via .statusBarsPadding()). Otherwise the system icons end up sitting
        // on the page background and become invisible.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            ) {
                BOTTOM_TABS.forEach { tab ->
                    val selected = currentRoute == tab.route ||
                            (currentRoute?.startsWith("course_detail") == true && tab.route == "courses")
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(tab.icon, null, modifier = Modifier.size(22.dp))
                        },
                        label = {
                            Text(
                                tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UoNBlue,
                            selectedTextColor = UoNBlue,
                            indicatorColor = UoNSky,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            // Only consume the BOTTOM inset (the nav bar) — let each screen
            // draw under the status bar so headers extend behind it.
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(storage, user, navController, refreshKey)
            }
            composable(BottomTab.Courses.route) {
                CoursesScreen(storage, user, navController, refreshKey) { refreshKey++ }
            }
            composable(BottomTab.MyCourses.route) {
                MyCoursesScreen(storage, user, navController, refreshKey) { refreshKey++ }
            }
            composable(BottomTab.Profile.route) {
                ProfileScreen(user, onLogout)
            }
            composable("course_detail/{courseCode}") { backStack ->
                val code = backStack.arguments?.getString("courseCode") ?: ""
                val course = COURSE_CATALOGUE.find { it.code == code }
                if (course != null) {
                    CourseDetailScreen(storage, user, course, navController, refreshKey) { refreshKey++ }
                }
            }
        }
    }
}

// ── Home Screen ───────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(storage: AppStorage, user: AppUser, navController: NavHostController, refreshKey: Int) {
    val registered = remember(refreshKey) { storage.getRegisteredCourses(user.username) }
    val registeredCourses = COURSE_CATALOGUE.filter { it.code in registered }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header — gradient extends behind the status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Brush.verticalGradient(listOf(UoNBlue, UoNLightBlue)))
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite.copy(alpha = 0.2f))
                            .border(1.5.dp, SurfaceWhite.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.fullName.firstOrNull()?.uppercase() ?: "U",
                            color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 19.sp
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Welcome back,",
                            color = SurfaceWhite.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            letterSpacing = 0.3.sp
                        )
                        Text(
                            user.fullName.split(" ").firstOrNull() ?: user.fullName,
                            color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 19.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(UoNGold.copy(alpha = 0.18f))
                            .border(1.dp, UoNGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Sem II", color = UoNGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(22.dp))
                // Stats Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip("${registered.size}", "Registered", Modifier.weight(1f))
                    StatChip("${COURSE_CATALOGUE.size}", "Available", Modifier.weight(1f))
                    StatChip("${registered.sumOf { code -> COURSE_CATALOGUE.find { it.code == code }?.credits ?: 0 }}", "Credits", Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick Actions
        SectionLabel("QUICK ACTIONS", modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.MenuBook,
                label = "Browse\nCourses",
                color = UoNBlue,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(BottomTab.Courses.route) }

            QuickActionCard(
                icon = Icons.Default.School,
                label = "My\nCourses",
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            ) { navController.navigate(BottomTab.MyCourses.route) }

            QuickActionCard(
                icon = Icons.Default.Person,
                label = "My\nProfile",
                color = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f)
            ) { navController.navigate(BottomTab.Profile.route) }
        }

        Spacer(Modifier.height(24.dp))

        // Recent Courses
        if (registeredCourses.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("MY COURSES")
                TextButton(onClick = { navController.navigate(BottomTab.MyCourses.route) }) {
                    Text("See all", color = UoNLightBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            registeredCourses.take(3).forEach { course ->
                HomeCourseCard(course, modifier = Modifier.padding(horizontal = 20.dp)) {
                    navController.navigate("course_detail/${course.code}")
                }
                Spacer(Modifier.height(8.dp))
            }
        } else {
            // Empty state
            Card(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MenuBook, null, tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No courses yet", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Browse courses and register for units\nthis semester.",
                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate(BottomTab.Courses.route) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UoNBlue)
                    ) {
                        Text("Browse Courses", fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        // Academic Notice
        Card(
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            border = BorderStroke(1.dp, UoNGold.copy(alpha = 0.4f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = UoNGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Registration Deadline", fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp, color = Color(0xFF7A5500))
                    Text("Semester II course registration closes on 30th May 2026. Register before the deadline.",
                        fontSize = 12.sp, color = Color(0xFF7A5500).copy(alpha = 0.8f), lineHeight = 18.sp)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = TextSecondary,
        letterSpacing = 1.2.sp,
        modifier = modifier
    )
}

@Composable
fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceWhite.copy(alpha = 0.16f))
            .border(1.dp, SurfaceWhite.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                color = SurfaceWhite.copy(alpha = 0.78f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp
            )
        }
    }
}

@Composable
fun QuickActionCard(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun HomeCourseCard(course: Course, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(UoNSky),
                contentAlignment = Alignment.Center
            ) {
                Text(course.code.take(3), color = UoNBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${course.code} · ${course.credits} Credits", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary.copy(alpha = 0.5f))
        }
    }
}

// ── Courses Catalogue Screen ───────────────────────────────────────────────────
@Composable
fun CoursesScreen(
    storage: AppStorage, user: AppUser,
    navController: NavHostController, refreshKey: Int,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val registered = remember(refreshKey) { storage.getRegisteredCourses(user.username) }

    val filtered = COURSE_CATALOGUE.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header — extends behind the status bar for proper edge-to-edge look
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Brush.verticalGradient(listOf(UoNBlue, UoNLightBlue)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column {
                Text(
                    "Course Catalogue",
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${COURSE_CATALOGUE.size} units available · Sem II 2025/26",
                    color = SurfaceWhite.copy(alpha = 0.78f), fontSize = 12.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search by name, code or department",
                            fontSize = 13.sp, color = SurfaceWhite.copy(alpha = 0.65f))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null,
                            tint = SurfaceWhite.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null,
                                    tint = SurfaceWhite.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SurfaceWhite.copy(alpha = 0.55f),
                        unfocusedBorderColor = SurfaceWhite.copy(alpha = 0.28f),
                        focusedContainerColor = SurfaceWhite.copy(alpha = 0.16f),
                        unfocusedContainerColor = SurfaceWhite.copy(alpha = 0.1f),
                        focusedTextColor = SurfaceWhite,
                        unfocusedTextColor = SurfaceWhite,
                        cursorColor = SurfaceWhite,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, null, tint = TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("No courses found", color = TextSecondary)
                        }
                    }
                }
            } else {
                items(filtered) { course ->
                    val isRegistered = course.code in registered
                    CatalogueCard(
                        course = course,
                        isRegistered = isRegistered,
                        onClick = { navController.navigate("course_detail/${course.code}") },
                        onRegister = {
                            if (isRegistered) storage.unregisterCourse(user.username, course.code)
                            else storage.registerCourse(user.username, course.code)
                            onRefresh()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogueCard(course: Course, isRegistered: Boolean, onClick: () -> Unit, onRegister: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        border = if (isRegistered) BorderStroke(1.5.dp, SuccessGreen.copy(alpha = 0.4f)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isRegistered) Color(0xFFE8F5E9) else UoNSky),
                    contentAlignment = Alignment.Center
                ) {
                    Text(course.code.take(3), color = if (isRegistered) SuccessGreen else UoNBlue,
                        fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.height(2.dp))
                    Text(course.code, fontSize = 12.sp, color = UoNLightBlue, fontWeight = FontWeight.Medium)
                }
                if (isRegistered) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(course.description, fontSize = 12.sp, color = TextSecondary, maxLines = 2,
                overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoursePill("${course.credits} Credits")
                Spacer(Modifier.width(6.dp))
                CoursePill(course.department.split(" & ").firstOrNull() ?: course.department)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onRegister,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegistered) Color(0xFFEEEEEE) else UoNBlue,
                        contentColor = if (isRegistered) TextPrimary else SurfaceWhite
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(if (isRegistered) "Drop" else "Register", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun CoursePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PageBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

// ── My Courses Screen ─────────────────────────────────────────────────────────
@Composable
fun MyCoursesScreen(
    storage: AppStorage, user: AppUser,
    navController: NavHostController, refreshKey: Int,
    onRefresh: () -> Unit
) {
    val registered = remember(refreshKey) { storage.getRegisteredCourses(user.username) }
    val myCourses = COURSE_CATALOGUE.filter { it.code in registered }
    val totalCredits = myCourses.sumOf { it.credits }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header — gradient extends behind the status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceWhite.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, null,
                        tint = SurfaceWhite, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Courses", color = SurfaceWhite,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp,
                        letterSpacing = (-0.3).sp)
                    Text(
                        "${myCourses.size} units · $totalCredits credits enrolled",
                        color = SurfaceWhite.copy(alpha = 0.78f), fontSize = 12.sp
                    )
                }
            }
        }

        if (myCourses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.School, null, tint = TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No courses yet", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 18.sp)
                    Text("Register for courses from the catalogue",
                        color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate(BottomTab.Courses.route) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UoNBlue)
                    ) { Text("Browse Courses") }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Summary card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryItem("${myCourses.size}", "Units")
                            VerticalDivider(modifier = Modifier.height(36.dp), color = SuccessGreen.copy(alpha = 0.3f))
                            SummaryItem("$totalCredits", "Credits")
                            VerticalDivider(modifier = Modifier.height(36.dp), color = SuccessGreen.copy(alpha = 0.3f))
                            SummaryItem("Sem II", "Period")
                        }
                    }
                }
                items(myCourses) { course ->
                    MyCoursesCard(course,
                        onClick = { navController.navigate("course_detail/${course.code}") },
                        onDrop = {
                            storage.unregisterCourse(user.username, course.code)
                            onRefresh()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SuccessGreen)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
fun MyCoursesCard(course: Course, onClick: () -> Unit, onDrop: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Drop Course?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to drop ${course.name}?", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDrop() }) {
                    Text("Drop", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(course.code.take(3), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(course.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    color = TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
                Spacer(Modifier.height(2.dp))
                Text("${course.code} · ${course.credits} Credits", fontSize = 11.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("✓ Enrolled", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                }
            }
            IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = ErrorRed.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Course Detail Screen ──────────────────────────────────────────────────────
@Composable
fun CourseDetailScreen(
    storage: AppStorage, user: AppUser,
    course: Course, navController: NavHostController, refreshKey: Int,
    onRefresh: () -> Unit
) {
    val isRegistered = remember(refreshKey) {
        course.code in storage.getRegisteredCourses(user.username)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar — extends behind the status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Brush.verticalGradient(listOf(UoNBlue, UoNLightBlue)))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, null,
                        tint = SurfaceWhite, modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Course Details",
                    color = SurfaceWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Course header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(UoNSky),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(course.code.take(3), color = UoNBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(course.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, lineHeight = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(course.code, fontSize = 13.sp, color = UoNLightBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(16.dp))
                    DetailRow("Department", course.department)
                    Spacer(Modifier.height(12.dp))
                    DetailRow("Credit Hours", "${course.credits} Credits")
                    Spacer(Modifier.height(12.dp))
                    DetailRow("Offered", course.semester)
                    Spacer(Modifier.height(12.dp))
                    DetailRow("Status", if (isRegistered) "✓  Enrolled" else "Open for registration")
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Overview", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(course.description, fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (isRegistered) storage.unregisterCourse(user.username, course.code)
                    else storage.registerCourse(user.username, course.code)
                    onRefresh()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegistered) Color(0xFFEEEEEE) else UoNBlue,
                    contentColor = if (isRegistered) ErrorRed else SurfaceWhite
                )
            ) {
                Icon(
                    if (isRegistered) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                    null, modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isRegistered) "Drop this Course" else "Register for this Course",
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

// ── Profile Screen ────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(user: AppUser, onLogout: () -> Unit) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", fontWeight = FontWeight.Bold) },
            text = { Text("You'll need to sign in again to access your account.", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header — UoN brand gradient extends behind the status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Brush.verticalGradient(listOf(UoNBlue, UoNLightBlue)))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite.copy(alpha = 0.18f))
                        .border(2.dp, SurfaceWhite.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.fullName.firstOrNull()?.uppercase() ?: "U",
                        color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 34.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    user.fullName,
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.2).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "@${user.username}",
                    color = SurfaceWhite.copy(alpha = 0.78f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(UoNGold.copy(alpha = 0.18f))
                        .border(1.dp, UoNGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        "Student · ${user.studentId}",
                        color = UoNGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Account Info
        ProfileSection("ACCOUNT INFORMATION") {
            ProfileInfoRow(Icons.Default.Badge, "Full Name", user.fullName)
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(Icons.Default.Person, "Username", user.username)
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(Icons.Default.CreditCard, "Student ID", user.studentId)
        }

        Spacer(Modifier.height(16.dp))

        ProfileSection("ACADEMIC INFO") {
            ProfileInfoRow(Icons.Default.School, "Department", "Computing & Informatics")
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(Icons.Default.CalendarToday, "Academic Year", "2025/2026")
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileInfoRow(Icons.Default.LocationCity, "Institution", "University of Nairobi")
        }

        Spacer(Modifier.height(24.dp))

        // Sign out
        Card(
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                .clickable { showLogoutDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Logout, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionLabel(title, modifier = Modifier.padding(bottom = 10.dp, start = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = UoNLightBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
fun generateRef(studentId: String): String {
    val ts = System.currentTimeMillis().toString().takeLast(6)
    return "UON-REG-${studentId.takeLast(4).uppercase()}-$ts"
}