package com.example.tictactoe

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        prefs = PrefsManager(this)

        loadStats()

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Statistics?")
                .setMessage("This will clear all your scores.")
                .setPositiveButton("Reset") { _, _ ->
                    prefs.resetStats()
                    loadStats()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadStats() {
        val wins = prefs.playerWins
        val losses = prefs.aiWins
        val draws = prefs.ties
        val total = wins + losses + draws
        val winRate = if (total == 0) 0 else (wins * 100 / total)

        findViewById<TextView>(R.id.tvTotalGames).text = total.toString()
        findViewById<TextView>(R.id.tvWins).text = wins.toString()
        findViewById<TextView>(R.id.tvLosses).text = losses.toString()
        findViewById<TextView>(R.id.tvDraws).text = draws.toString()
        findViewById<TextView>(R.id.tvWinRate).text = "$winRate%"
    }
}