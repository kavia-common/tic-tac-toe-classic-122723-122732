package org.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup

/**
 * OnboardingActivity shows a brief introduction and allows both players
 * to choose their game icons (chess pieces) before starting the match.
 */
class OnboardingActivity : Activity() {

    // PUBLIC_INTERFACE
    /**
     * Android lifecycle: Initialize onboarding screen and navigation.
     * Provides chess piece selection for both players and enables the start button
     * only after both selections are made.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout("activity_onboarding"))

        val start: Button = findViewById(getId("startGameButton"))
        val groupP1: RadioGroup = findViewById(getId("radioGroupP1"))
        val groupP2: RadioGroup = findViewById(getId("radioGroupP2"))

        // Initially disable start until both players pick an icon
        start.isEnabled = groupP1.checkedRadioButtonId != -1 && groupP2.checkedRadioButtonId != -1

        val updateStartEnabled = {
            start.isEnabled = groupP1.checkedRadioButtonId != -1 && groupP2.checkedRadioButtonId != -1
        }

        groupP1.setOnCheckedChangeListener { _, _ -> updateStartEnabled() }
        groupP2.setOnCheckedChangeListener { _, _ -> updateStartEnabled() }

        start.setOnClickListener {
            val p1Icon = getSelectedIcon(groupP1) ?: "X"
            val p2Icon = getSelectedIcon(groupP2) ?: "O"

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("player1Icon", p1Icon)
            intent.putExtra("player2Icon", p2Icon)
            startActivity(intent)
            // Optionally finish so onboarding isn't returned to on back press
            finish()
        }
    }

    /**
     * Resolve a selected RadioButton's icon from its tag (or text as a fallback).
     */
    private fun getSelectedIcon(group: RadioGroup): String? {
        val checkedId = group.checkedRadioButtonId
        if (checkedId == -1) return null
        val rb = findViewById<RadioButton>(checkedId)
        val tag = rb.tag as? String
        return tag ?: rb.text?.toString()
    }

    // Helpers to resolve resources dynamically to avoid compile-time R references
    private fun getLayout(name: String): Int = resources.getIdentifier(name, "layout", packageName)
    private fun getId(name: String): Int = resources.getIdentifier(name, "id", packageName)
}
