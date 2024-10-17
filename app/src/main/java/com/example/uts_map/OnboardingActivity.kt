package com.example.uts_map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding) // Ensure this layout exists in res/layout

        // Find the Button and other views if necessary
        val getStartedButton = findViewById<Button>(R.id.btnGetStarted)
    }
}
