package org.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

/**
 * OnboardingActivity shows a brief introduction and a button to start the game.
 */
class OnboardingActivity : Activity() {

    // PUBLIC_INTERFACE
    /**
     * Android lifecycle: Initialize onboarding screen and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout("activity_onboarding"))

        val start: Button = findViewById(getId("startGameButton"))
        start.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            // Optionally finish so onboarding isn't returned to on back press
            finish()
        }
    }

    // Helpers to resolve resources dynamically to avoid compile-time R references
    private fun getLayout(name: String): Int = resources.getIdentifier(name, "layout", packageName)
    private fun getId(name: String): Int = resources.getIdentifier(name, "id", packageName)
}
