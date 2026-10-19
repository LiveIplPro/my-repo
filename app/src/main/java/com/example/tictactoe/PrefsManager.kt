package com.example.tictactoe

import android.content.Context
import android.content.SharedPreferences

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tictactoe_prefs", Context.MODE_PRIVATE)

    // Scores
    var playerWins: Int
        get() = prefs.getInt("player_wins", 0)
        set(v) = prefs.edit().putInt("player_wins", v).apply()

    var aiWins: Int
        get() = prefs.getInt("ai_wins", 0)
        set(v) = prefs.edit().putInt("ai_wins", v).apply()

    var ties: Int
        get() = prefs.getInt("ties", 0)
        set(v) = prefs.edit().putInt("ties", v).apply()

    // Stats
    var totalGames: Int
        get() = prefs.getInt("total_games", 0)
        set(v) = prefs.edit().putInt("total_games", v).apply()

    // Settings
    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound", true)
        set(v) = prefs.edit().putBoolean("sound", v).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration", true)
        set(v) = prefs.edit().putBoolean("vibration", v).apply()

    var difficulty: String
        get() = prefs.getString("difficulty", Difficulty.MEDIUM.name) ?: Difficulty.MEDIUM.name
        set(v) = prefs.edit().putString("difficulty", v).apply()

    fun resetStats() {
        prefs.edit().clear().apply()
    }
}