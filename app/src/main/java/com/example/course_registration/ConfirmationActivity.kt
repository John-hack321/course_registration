package com.example.course_registration

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val confirmationTextView: TextView = findViewById(R.id.confirmationTextView)
        val backButton: Button = findViewById(R.id.backButton)

        // Get the data from Intent
        val studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        val selectedCourse = intent.getStringExtra("SELECTED_COURSE") ?: ""

        // Display confirmation message
        val confirmationMessage = "Student ID: $studentId\nCourse: $selectedCourse\n\nRegistration Successful!"
        confirmationTextView.text = confirmationMessage

        // Handle back button click
        backButton.setOnClickListener {
            finish() // Go back to MainActivity
        }
    }
}
