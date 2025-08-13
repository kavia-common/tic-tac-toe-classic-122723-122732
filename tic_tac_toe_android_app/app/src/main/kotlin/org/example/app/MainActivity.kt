package org.example.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

/**
 * MainActivity renders the Tic Tac Toe board, manages local 2-player gameplay,
 * displays the scoreboard, and offers a reset option.
 *
 * It uses the selected chess piece icons from onboarding as the tokens
 * for Player 1 (engine 'X') and Player 2 (engine 'O').
 */
class MainActivity : Activity() {

    private lateinit var engine: GameEngine

    private lateinit var scoreboardText: TextView
    private lateinit var statusText: TextView

    private lateinit var cells: Array<Array<Button>>
    private lateinit var resetButton: Button
    private lateinit var trashTalkText: TextView

    // Selected player icons (default fallbacks if onboarding didn't set them)
    private var player1Icon: String = "X" // maps to engine 'X'
    private var player2Icon: String = "O" // maps to engine 'O'

    // OpenAI trash talker
    private lateinit var trashTalker: OpenAiTrashTalker

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
        trashTalkText = findViewById(getId("trashTalkText"))
        trashTalker = OpenAiTrashTalker(applicationContext)

        // Get player icons from onboarding (extras) first
        intent.getStringExtra("player1Icon")?.let { player1Icon = it }
        intent.getStringExtra("player2Icon")?.let { player2Icon = it }

        // Restore state if available (both engine and chosen icons)
        if (savedInstanceState != null) {
            val state = HashMap<String, Any>()
            savedInstanceState.getString("board")?.let { state["board"] = it }
            savedInstanceState.getString("currentPlayer")?.let { state["currentPlayer"] = it }
            state["isGameOver"] = savedInstanceState.getBoolean("isGameOver", false)
            state["scoreX"] = savedInstanceState.getInt("scoreX", 0)
            state["scoreO"] = savedInstanceState.getInt("scoreO", 0)
            engine.fromState(state)

            savedInstanceState.getString("p1Icon")?.let { player1Icon = it }
            savedInstanceState.getString("p2Icon")?.let { player2Icon = it }
        }

        cells = arrayOf(
            arrayOf(findViewById(getId("cell_0_0")), findViewById(getId("cell_0_1")), findViewById(getId("cell_0_2"))),
            arrayOf(findViewById(getId("cell_1_0")), findViewById(getId("cell_1_1")), findViewById(getId("cell_1_2"))),
            arrayOf(findViewById(getId("cell_2_0")), findViewById(getId("cell_2_1")), findViewById(getId("cell_2_2")))
        )

        wireUpBoard()
        updateUIFromEngine()

        resetButton.setOnClickListener {
            engine.resetBoard()
            updateUIFromEngine()
            trashTalkText.text = ""
        }
    }

    private fun wireUpBoard() {
        for (r in 0..2) {
            for (c in 0..2) {
                cells[r][c].setOnClickListener {
                    if (engine.makeMove(r, c)) {
                        updateUIFromEngine()
                        // After every valid move, generate playful trash talk
                        requestTrashTalk(r, c)
                    }
                }
            }
        }
    }

    private fun updateUIFromEngine() {
        // Update board cells with selected icons
        for (r in 0..2) {
            for (c in 0..2) {
                val value = engine.getCell(r, c)
                val display = when (value) {
                    'X' -> player1Icon
                    'O' -> player2Icon
                    else -> ""
                }
                cells[r][c].text = display
                cells[r][c].isEnabled = value == ' ' && !engine.isGameOver
            }
        }

        // Update scoreboard (show icons instead of X/O)
        scoreboardText.text = "Score  $player1Icon: ${engine.scoreX}  |  $player2Icon: ${engine.scoreO}"

        // Update status with icons
        val winner = engine.getWinner()
        statusText.text = when (winner) {
            'X' -> "$player1Icon wins!"
            'O' -> "$player2Icon wins!"
            'D' -> "Draw game."
            else -> {
                val turnIcon = if (engine.currentPlayer == 'X') player1Icon else player2Icon
                "Player $turnIcon turn"
            }
        }
    }

    /**
     * Build a small ASCII representation of the board for the prompt.
     */
    private fun buildBoardAscii(): String {
        val sb = StringBuilder()
        for (r in 0..2) {
            for (c in 0..2) {
                val ch = engine.getCell(r, c)
                val shown = when (ch) {
                    'X' -> player1Icon
                    'O' -> player2Icon
                    else -> "·"
                }
                sb.append(shown)
                if (c < 2) sb.append(" | ")
            }
            if (r < 2) sb.append("\n---------\n")
        }
        return sb.toString()
    }

    /**
     * Trigger a background fetch to OpenAI to get a playful trash talk message and show it in a Toast.
     */
    private fun requestTrashTalk(row: Int, col: Int) {
        val justPlayedChar = engine.getCell(row, col)
        val justPlayedIcon = when (justPlayedChar) {
            'X' -> player1Icon
            'O' -> player2Icon
            else -> "?"
        }
        val isOver = engine.isGameOver
        val winner = engine.getWinner()
        val nextIcon = if (!isOver) {
            if (engine.currentPlayer == 'X') player1Icon else player2Icon
        } else ""

        val boardAscii = buildBoardAscii()

        Thread {
            val line = trashTalker.generateTrashTalk(
                boardAscii = boardAscii,
                row = row,
                col = col,
                justPlayedIcon = justPlayedIcon,
                nextPlayerIcon = nextIcon,
                isGameOver = isOver,
                winnerChar = winner
            )
            runOnUiThread {
                trashTalkText.text = line
                Toast.makeText(this, line, Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    // Helpers to resolve resources dynamically to avoid compile-time R references
    private fun getLayout(name: String): Int = resources.getIdentifier(name, "layout", packageName)
    private fun getId(name: String): Int = resources.getIdentifier(name, "id", packageName)

    // PUBLIC_INTERFACE
    /**
     * Persist game state and chosen icons across configuration changes (e.g., rotation).
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val state = engine.toState()
        outState.putString("board", state["board"] as String)
        outState.putString("currentPlayer", state["currentPlayer"] as String)
        outState.putBoolean("isGameOver", state["isGameOver"] as Boolean)
        outState.putInt("scoreX", state["scoreX"] as Int)
        outState.putInt("scoreO", state["scoreO"] as Int)
        outState.putString("p1Icon", player1Icon)
        outState.putString("p2Icon", player2Icon)
    }
}
