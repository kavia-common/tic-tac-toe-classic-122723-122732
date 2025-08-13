package org.example.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

/**
 * MainActivity renders the Tic Tac Toe board, manages local 2-player gameplay,
 * displays the scoreboard, and offers a reset option.
 */
class MainActivity : Activity() {

    private lateinit var engine: GameEngine

    private lateinit var scoreboardText: TextView
    private lateinit var statusText: TextView

    private lateinit var cells: Array<Array<Button>>
    private lateinit var resetButton: Button

    // PUBLIC_INTERFACE
    /**
     * Android lifecycle: Initialize the UI and game state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout("activity_main"))

        engine = GameEngine()
        scoreboardText = findViewById(getId("scoreboardText"))
        statusText = findViewById(getId("statusText"))
        resetButton = findViewById(getId("resetButton"))

        cells = arrayOf(
            arrayOf(findViewById(getId("cell_0_0")), findViewById(getId("cell_0_1")), findViewById(getId("cell_0_2"))),
            arrayOf(findViewById(getId("cell_1_0")), findViewById(getId("cell_1_1")), findViewById(getId("cell_1_2"))),
            arrayOf(findViewById(getId("cell_2_0")), findViewById(getId("cell_2_1")), findViewById(getId("cell_2_2")))
        )

        // Restore state if available
        if (savedInstanceState != null) {
            val state = HashMap<String, Any>()
            savedInstanceState.getString("board")?.let { state["board"] = it }
            savedInstanceState.getString("currentPlayer")?.let { state["currentPlayer"] = it }
            state["isGameOver"] = savedInstanceState.getBoolean("isGameOver", false)
            state["scoreX"] = savedInstanceState.getInt("scoreX", 0)
            state["scoreO"] = savedInstanceState.getInt("scoreO", 0)
            engine.fromState(state)
        }

        wireUpBoard()
        updateUIFromEngine()

        resetButton.setOnClickListener {
            engine.resetBoard()
            updateUIFromEngine()
        }
    }

    private fun wireUpBoard() {
        for (r in 0..2) {
            for (c in 0..2) {
                cells[r][c].setOnClickListener {
                    if (engine.makeMove(r, c)) {
                        updateUIFromEngine()
                    }
                }
            }
        }
    }

    private fun updateUIFromEngine() {
        // Update board cells
        for (r in 0..2) {
            for (c in 0..2) {
                val value = engine.getCell(r, c)
                cells[r][c].text = if (value == ' ') "" else value.toString()
                cells[r][c].isEnabled = value == ' ' && !engine.isGameOver
            }
        }

        // Update scoreboard
        scoreboardText.text = "Score  X: ${engine.scoreX}  |  O: ${engine.scoreO}"

        // Update status
        val winner = engine.getWinner()
        statusText.text = when (winner) {
            'X' -> getStringByName("status_x_wins")
            'O' -> getStringByName("status_o_wins")
            'D' -> getStringByName("status_draw")
            else -> if (engine.currentPlayer == 'X') getStringByName("status_player_x_turn") else getStringByName("status_player_o_turn")
        }
    }

    // Helpers to resolve resources dynamically to avoid compile-time R references
    private fun getLayout(name: String): Int = resources.getIdentifier(name, "layout", packageName)
    private fun getId(name: String): Int = resources.getIdentifier(name, "id", packageName)
    private fun getStringByName(name: String): String = resources.getString(resources.getIdentifier(name, "string", packageName))

    // PUBLIC_INTERFACE
    /**
     * Persist game state across configuration changes (e.g., rotation).
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val state = engine.toState()
        outState.putString("board", state["board"] as String)
        outState.putString("currentPlayer", state["currentPlayer"] as String)
        outState.putBoolean("isGameOver", state["isGameOver"] as Boolean)
        outState.putInt("scoreX", state["scoreX"] as Int)
        outState.putInt("scoreO", state["scoreO"] as Int)
    }
}
