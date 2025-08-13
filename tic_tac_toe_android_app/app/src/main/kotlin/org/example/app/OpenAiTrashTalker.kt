package org.example.app

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * OpenAiTrashTalker handles creating short, playful trash talk messages for the Tic Tac Toe game
 * by calling OpenAI's Chat Completions API.
 *
 * It attempts to read the API key from:
 *  1) BuildConfig.OPENAI_API_KEY (if present, via reflection)
 *  2) AndroidManifest meta-data key "OPENAI_API_KEY"
 *  3) Environment variable OPENAI_API_KEY (System.getenv)
 *
 * If no key is found or an error occurs, returns a safe/friendly fallback line.
 */
class OpenAiTrashTalker(private val context: Context) {

    // PUBLIC_INTERFACE
    /**
     * Generate a playful, PG-rated, very short (<= 18 words) trash talk line for the last move.
     *
     * Parameters:
     * - boardAscii: A human-readable 3x3 board string.
     * - row, col: The last move coordinates (0-based).
     * - justPlayedIcon: The icon or mark of the player who just played (e.g., "X", "O", or the chess icon).
     * - nextPlayerIcon: The icon/mark for the next player to move.
     * - isGameOver: Whether the game ended on this move.
     * - winnerChar: 'X', 'O', 'D' for draw, or ' ' if none.
     *
     * Returns:
     * - A single-sentence, friendly "trash talk" line. Uses a fallback if API fails or key is missing.
     */
    fun generateTrashTalk(
        boardAscii: String,
        row: Int,
        col: Int,
        justPlayedIcon: String,
        nextPlayerIcon: String,
        isGameOver: Boolean,
        winnerChar: Char
    ): String {
        val apiKey = resolveApiKey()
        if (apiKey.isNullOrEmpty()) {
            return "Set OPENAI_API_KEY to unlock spicy commentary!"
        }

        val prompt = buildPrompt(
            boardAscii = boardAscii,
            row = row,
            col = col,
            justPlayedIcon = justPlayedIcon,
            nextPlayerIcon = nextPlayerIcon,
            isGameOver = isGameOver,
            winnerChar = winnerChar
        )

        return try {
            callOpenAI(apiKey, prompt)
        } catch (_: Throwable) {
            "Game on! (AI commentator is taking a nap.)"
        }
    }

    private fun buildPrompt(
        boardAscii: String,
        row: Int,
        col: Int,
        justPlayedIcon: String,
        nextPlayerIcon: String,
        isGameOver: Boolean,
        winnerChar: Char
    ): String {
        val outcomeText = if (isGameOver) {
            when (winnerChar) {
                'X', 'O' -> "The game ended. Winner: $winnerChar."
                'D' -> "The game ended in a draw."
                else -> "The game is over."
            }
        } else {
            "The game continues. Next player: $nextPlayerIcon."
        }

        return """
            You are a playful, PG-rated Tic-Tac-Toe commentator. 
            Keep it to a single sentence, 18 words or fewer, friendly and fun.
            
            Context:
            - Last move: $justPlayedIcon at ($row,$col).
            - Board:
            $boardAscii
            - $outcomeText

            Produce only the line of commentary; no emojis unless already in the icons, and no additional explanation.
        """.trimIndent()
    }

    private fun callOpenAI(apiKey: String, prompt: String): String {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            connectTimeout = 10000
            readTimeout = 20000
        }

        val payload = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("temperature", 0.9)
            put("max_tokens", 60)
            val messages = JSONArray().apply {
                put(JSONObject().put("role", "system")
                    .put("content", "You are a playful, PG-rated Tic-Tac-Toe commentator. Keep it very short (<= 18 words)."))
                put(JSONObject().put("role", "user").put("content", prompt))
            }
            put("messages", messages)
        }.toString()

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream?.let { s ->
            BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { br -> br.readText() }
        } ?: ""

        conn.disconnect()

        val json = JSONObject(response)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val content = choices.getJSONObject(0)
                .optJSONObject("message")
                ?.optString("content")
                ?.trim()
            if (!content.isNullOrEmpty()) return content
        }

        return "Nice move! Your turn."
    }

    private fun resolveApiKey(): String? {
        // Try BuildConfig (reflection avoids hard dependency if not present)
        try {
            val buildConfigClass = Class.forName("${context.packageName}.BuildConfig")
            val field = buildConfigClass.getDeclaredField("OPENAI_API_KEY")
            val value = field.get(null) as? String
            if (!value.isNullOrEmpty()) return value
        } catch (_: Throwable) {
            // Ignore and continue fallbacks
        }

        // Try AndroidManifest meta-data
        try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val v = appInfo.metaData?.getString("OPENAI_API_KEY")
            if (!v.isNullOrEmpty()) return v
        } catch (_: Throwable) {
            // Ignore and continue fallbacks
        }

        // Finally, try environment variable
        return System.getenv("OPENAI_API_KEY")
    }
}
