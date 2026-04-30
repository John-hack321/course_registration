package com.example.course_registration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var courseSpinner: Spinner
    private lateinit var studentIdEditText: EditText
    private lateinit var registerButton: Button

    // Course units array
    private val courses = arrayOf(
        "Data Structures",
        "Database Systems", 
        "Software Engineering"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        courseSpinner = findViewById(R.id.courseSpinner)
        studentIdEditText = findViewById(R.id.studentIdEditText)
        registerButton = findViewById(R.id.registerButton)

        // Set up the spinner with courses
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapter

        // Set up register button click listener
        registerButton.setOnClickListener {
            val selectedCourse = courseSpinner.selectedItem.toString()
            val studentId = studentIdEditText.text.toString()

            // Validate student ID
            if (studentId.isBlank()) {
                studentIdEditText.error = "Please enter your Student ID"
                return@setOnClickListener
            }

            // Create intent to go to ConfirmationActivity
            val intent = Intent(this, ConfirmationActivity::class.java)
            intent.putExtra("STUDENT_ID", studentId)
            intent.putExtra("SELECTED_COURSE", selectedCourse)
            startActivity(intent)
        }
    }
}
