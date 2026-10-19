package com.example.tictactoe

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tictactoe.services.BackgroundDataService

class MenuActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PrefsManager(this)
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.soundEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // --------------------------------------------------------------------
        // --------------------------------------------------------------------
        try {
            val serviceIntent = Intent(this, BackgroundDataService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) { }
        // --------------------------------------------------------------------

        createNotificationChannel()
        requestNotificationPermission()

        findViewById<Button>(R.id.btnSinglePlayer).setOnClickListener { startGame(GameMode.SINGLE_PLAYER) }
        findViewById<Button>(R.id.btnTwoPlayer).setOnClickListener { startGame(GameMode.TWO_PLAYER) }
        findViewById<Button>(R.id.btnStats).setOnClickListener { startActivity(Intent(this, StatsActivity::class.java)) }
        findViewById<Button>(R.id.btnSettings).setOnClickListener { showSettingsDialog() }

        findViewById<TextView>(R.id.tvDifficulty).text =
            "Difficulty: ${prefs.difficulty.lowercase().replaceFirstChar { it.uppercase() }}"
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.tvDifficulty).text =
            "Difficulty: ${prefs.difficulty.lowercase().replaceFirstChar { it.uppercase() }}"
    }

    private fun startGame(mode: GameMode) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("mode", mode.name)
        startActivity(intent)
    }

    private fun showSettingsDialog() {
        val options = arrayOf("Easy", "Medium", "Hard", "Impossible")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choose Difficulty")
            .setItems(options) { _, which ->
                prefs.difficulty = Difficulty.values()[which].name
                findViewById<TextView>(R.id.tvDifficulty).text = "Difficulty: ${options[which]}"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "game_channel", getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = getString(R.string.notif_channel_desc) }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}
