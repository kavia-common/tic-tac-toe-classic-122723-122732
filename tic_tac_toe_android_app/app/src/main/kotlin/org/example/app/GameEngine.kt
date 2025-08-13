package org.example.app

/**
 * GameEngine manages the Tic Tac Toe game state for a local 2-player game.
 * It tracks the board, current player, game over state and accumulated scores.
 */
class GameEngine {

    /** Board is a 3x3 grid represented by characters: 'X', 'O', or ' ' (empty). */
    private val board: Array<CharArray> = Array(3) { CharArray(3) { ' ' } }

    /** Current player's mark: 'X' or 'O'. */
    var currentPlayer: Char = 'X'
        private set

    /** If true, the current board is in a terminal state (win or draw). */
    var isGameOver: Boolean = false
        private set

    /** Score values accumulated across rounds. */
    var scoreX: Int = 0
        private set
    var scoreO: Int = 0
        private set

    // PUBLIC_INTERFACE
    /**
     * Attempt to make a move at the given cell. Returns true if the move is applied.
     * Does nothing if the cell is not empty or the game is already over.
     */
    fun makeMove(row: Int, col: Int): Boolean {
        if (isGameOver) return false
        if (row !in 0..2 || col !in 0..2) return false
        if (board[row][col] != ' ') return false

        board[row][col] = currentPlayer
        evaluateGameState()
        if (!isGameOver) {
            togglePlayer()
        }
        return true
    }

    // PUBLIC_INTERFACE
    /**
     * Resets the board for a new round while preserving scores.
     * Sets current player to 'X' and clears the terminal state flag.
     */
    fun resetBoard() {
        for (r in 0..2) {
            for (c in 0..2) {
                board[r][c] = ' '
            }
        }
        currentPlayer = 'X'
        isGameOver = false
    }

    // PUBLIC_INTERFACE
    /**
     * Returns the character at the board cell (row, col).
     */
    fun getCell(row: Int, col: Int): Char = board[row][col]

    // PUBLIC_INTERFACE
    /**
     * Returns the winner character if the game is over: 'X' or 'O'.
     * Returns 'D' for draw, or ' ' if the game is still ongoing.
     */
    fun getWinner(): Char {
        val win = checkWinner()
        return when {
            win == 'X' || win == 'O' -> win
            isBoardFull() && win == ' ' -> 'D'
            else -> ' '
        }
    }

    // PUBLIC_INTERFACE
    /**
     * Serialize the engine state to a Bundle-like map for persistence.
     */
    fun toState(): Map<String, Any> {
        val flat = CharArray(9)
        var idx = 0
        for (r in 0..2) {
            for (c in 0..2) {
                flat[idx++] = board[r][c]
            }
        }
        return mapOf(
            "board" to String(flat),
            "currentPlayer" to currentPlayer.toString(),
            "isGameOver" to isGameOver,
            "scoreX" to scoreX,
            "scoreO" to scoreO
        )
    }

    // PUBLIC_INTERFACE
    /**
     * Restore the engine state from a map previously produced by toState().
     */
    fun fromState(state: Map<String, Any>) {
        val boardStr = state["board"] as? String
        val cp = (state["currentPlayer"] as? String)?.firstOrNull()
        val over = state["isGameOver"] as? Boolean
        val sx = state["scoreX"] as? Int
        val so = state["scoreO"] as? Int

        if (boardStr != null && boardStr.length == 9) {
            var idx = 0
            for (r in 0..2) {
                for (c in 0..2) {
                    board[r][c] = boardStr[idx++]
                }
            }
        }
        if (cp != null) currentPlayer = cp
        if (over != null) isGameOver = over
        if (sx != null) scoreX = sx
        if (so != null) scoreO = so
    }

    private fun togglePlayer() {
        currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
    }

    private fun evaluateGameState() {
        val w = checkWinner()
        when (w) {
            'X' -> {
                scoreX += 1
                isGameOver = true
            }
            'O' -> {
                scoreO += 1
                isGameOver = true
            }
            else -> {
                if (isBoardFull()) {
                    isGameOver = true
                }
            }
        }
    }

    private fun isBoardFull(): Boolean {
        for (r in 0..2) for (c in 0..2) if (board[r][c] == ' ') return false
        return true
    }

    private fun checkWinner(): Char {
        // Rows
        for (r in 0..2) {
            if (board[r][0] != ' ' && board[r][0] == board[r][1] && board[r][1] == board[r][2]) {
                return board[r][0]
            }
        }
        // Columns
        for (c in 0..2) {
            if (board[0][c] != ' ' && board[0][c] == board[1][c] && board[1][c] == board[2][c]) {
                return board[0][c]
            }
        }
        // Diagonals
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }
        return ' '
    }
}
