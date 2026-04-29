package com.example.course_registration

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.course_registration.ui.theme.Course_registrationTheme

// ── UoN Brand Colors ──────────────────────────────────────────────────────────
val UoNBlue       = Color(0xFF003580)   // primary deep blue
val UoNLightBlue  = Color(0xFF0057B8)   // interactive / accent
val UoNSky        = Color(0xFFE8F0FB)   // tinted background panels
val UoNGold       = Color(0xFFF5A623)   // secondary accent (UoN gold)
val SurfaceWhite  = Color(0xFFFFFFFF)
val TextPrimary   = Color(0xFF0A1628)
val TextSecondary = Color(0xFF5A6A85)
val DividerColor  = Color(0xFFE2E8F4)
val ErrorRed      = Color(0xFFD32F2F)
val SuccessGreen  = Color(0xFF2E7D32)

// Shared-Prefs key constants
private const val PREFS_NAME   = "uon_course_reg"
private const val KEY_LAST_ID  = "last_student_id"
private const val KEY_LAST_COURSE = "last_course"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Course_registrationTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable(
            "confirmation/{studentId}/{course}",
        ) { back ->
            val studentId = back.arguments?.getString("studentId") ?: ""
            val course    = back.arguments?.getString("course")    ?: ""
            ConfirmationScreen(navController, studentId, course)
        }
    }
}

// ── Course List ───────────────────────────────────────────────────────────────
val COURSES = listOf(
    "SCS 3308 – Embedded Systems & Mobile Programming",
    "SCS 3201 – Data Structures & Algorithms",
    "SCS 3305 – Database Systems",
    "SCS 3302 – Software Engineering",
    "SCS 3306 – Computer Networks",
    "SCS 3310 – Artificial Intelligence",
    "SCS 3304 – Operating Systems",
    "SCS 3309 – Human-Computer Interaction",
)

// ── Main Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var studentId      by remember { mutableStateOf(prefs.getString(KEY_LAST_ID, "") ?: "") }
    var selectedCourse by remember { mutableStateOf(
        prefs.getString(KEY_LAST_COURSE, COURSES[0]) ?: COURSES[0]
    ) }
    var spinnerExpanded by remember { mutableStateOf(false) }
    var idError        by remember { mutableStateOf<String?>(null) }

    // Entrance animation
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "entrance"
    )

    Scaffold(
        containerColor = Color(0xFFF6F8FC),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Header ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(UoNBlue, UoNLightBlue)
                        )
                    )
                    .padding(top = 56.dp, bottom = 36.dp, start = 24.dp, end = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {

                    // Logo placeholder  ← swap R.drawable.ic_launcher with your UoN logo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite.copy(alpha = 0.15f))
                            .border(1.5.dp, SurfaceWhite.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Replace with Image(painterResource(R.drawable.uon_logo), ...) once you add the logo
                        Text(
                            "UoN",
                            color = SurfaceWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Course Registration",
                        color = SurfaceWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        lineHeight = 32.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Department of Computing & Informatics",
                        color = SurfaceWhite.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "University of Nairobi",
                        color = UoNGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Form Card ───────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-20).dp)
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    SectionLabel("Select Course Unit")
                    Spacer(Modifier.height(8.dp))

                    // Spinner / Dropdown
                    ExposedDropdownMenuBox(
                        expanded = spinnerExpanded,
                        onExpandedChange = { spinnerExpanded = !spinnerExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCourse,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = UoNLightBlue
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UoNLightBlue,
                                unfocusedBorderColor = DividerColor,
                                focusedContainerColor = UoNSky,
                                unfocusedContainerColor = Color(0xFFF9FAFC),
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = spinnerExpanded,
                            onDismissRequest = { spinnerExpanded = false },
                            modifier = Modifier.background(SurfaceWhite)
                        ) {
                            COURSES.forEachIndexed { index, course ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            course,
                                            fontSize = 13.sp,
                                            color = if (course == selectedCourse) UoNLightBlue else TextPrimary,
                                            fontWeight = if (course == selectedCourse) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedCourse = course
                                        spinnerExpanded = false
                                    }
                                )
                                if (index < COURSES.lastIndex) {
                                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    SectionLabel("Student ID")
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = {
                            studentId = it
                            idError = null
                        },
                        placeholder = {
                            Text("e.g. C02-0000/2022", color = TextSecondary, fontSize = 14.sp)
                        },
                        isError = idError != null,
                        supportingText = {
                            if (idError != null)
                                Text(idError!!, color = ErrorRed, fontSize = 12.sp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UoNLightBlue,
                            unfocusedBorderColor = DividerColor,
                            errorBorderColor = ErrorRed,
                            focusedContainerColor = UoNSky,
                            unfocusedContainerColor = Color(0xFFF9FAFC),
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    // Register Button
                    Button(
                        onClick = {
                            idError = validateStudentId(studentId)
                            if (idError == null) {
                                // Persist to SharedPreferences
                                prefs.edit()
                                    .putString(KEY_LAST_ID, studentId)
                                    .putString(KEY_LAST_COURSE, selectedCourse)
                                    .apply()
                                val encodedCourse = java.net.URLEncoder.encode(selectedCourse, "UTF-8")
                                navController.navigate("confirmation/$studentId/$encodedCourse")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UoNBlue,
                            contentColor   = SurfaceWhite
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            "Register for Course",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            // ── Footer note ─────────────────────────────────────────────────
            Spacer(Modifier.height(4.dp))
            Text(
                "Academic Year 2025/2026 · Semester II",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Confirmation Screen ───────────────────────────────────────────────────────
@Composable
fun ConfirmationScreen(
    navController: NavHostController,
    studentId: String,
    encodedCourse: String
) {
    val course = java.net.URLDecoder.decode(encodedCourse, "UTF-8")

    // Animated check scale
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkScale"
    )
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val checkScale by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkAnim"
    )

    Scaffold(containerColor = Color(0xFFF6F8FC)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UoNBlue)
                    .padding(top = 48.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SurfaceWhite)
                }
                Text(
                    "Registration Confirmed",
                    modifier = Modifier.align(Alignment.Center),
                    color = SurfaceWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            // Animated check icon
            Box(
                modifier = Modifier
                    .size((88 * checkScale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "You're registered!",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your course registration has been\nsuccessfully submitted.",
                textAlign = TextAlign.Center,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            // Details Card
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ConfirmationRow(label = "Student ID", value = studentId)
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(16.dp))
                    ConfirmationRow(label = "Course Unit", value = course)
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(16.dp))
                    ConfirmationRow(label = "Department", value = "Computing & Informatics")
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(Modifier.height(16.dp))
                    ConfirmationRow(label = "Status", value = "✓  Registered", valueColor = SuccessGreen)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Reference tag
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(UoNSky)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text("Reference Number", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        generateRef(studentId),
                        fontSize = 15.sp,
                        color = UoNBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Back button
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, UoNBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = UoNBlue)
            ) {
                Text(
                    "Register Another Course",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Helper Composables ────────────────────────────────────────────────────────
@Composable
fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        letterSpacing = 0.8.sp
    )
}

@Composable
fun ConfirmationRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor, lineHeight = 20.sp)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
fun validateStudentId(id: String): String? {
    val trimmed = id.trim()
    return when {
        trimmed.isBlank()      -> "Student ID cannot be empty"
        trimmed.length < 5     -> "Student ID is too short"
        trimmed.length > 20    -> "Student ID is too long"
        !trimmed.any { it.isDigit() } -> "Student ID must contain numbers"
        else                   -> null
    }
}

fun generateRef(studentId: String): String {
    val ts = System.currentTimeMillis().toString().takeLast(6)
    return "UON-REG-${studentId.takeLast(4).uppercase()}-$ts"
}