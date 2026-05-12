package com.kr.eqpro

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.os.IBinder
import androidx.core.app.NotificationCompat

class EqService : Service() {
    companion object {
        var eqInstance: Equalizer? = null
        const val ACTION_UPDATE_EQ = "com.kr.eqpro.UPDATE_EQ"
        const val EXTRA_BAND = "band"
        const val EXTRA_LEVEL = "level"
        const val EXTRA_PRESET = "preset"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_EQ -> {
                if (intent.hasExtra(EXTRA_PRESET)) {
                    val arr = intent.getIntArrayExtra(EXTRA_PRESET)!!
                    applyPreset(arr)
                } else {
                    val band = intent.getIntExtra(EXTRA_BAND, 0)
                    val level = intent.getIntExtra(EXTRA_LEVEL, 0)
                    try { eqInstance?.setBandLevel(band.toShort(), level.toShort()) } catch (_: Exception) {}
                }
                return START_STICKY
            }
            else -> {
                val sessionId = intent?.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)?: 0
                if (sessionId == 0) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForeground(1, createNotification())
                try {
                    eqInstance = Equalizer(0, sessionId).apply { enabled = true }
                    loadLastPreset()
                } catch (_: Exception) { stopSelf() }
                return START_STICKY
            }
        }
    }

    private fun applyPreset(dbValues: IntArray) {
        for (i in dbValues.indices) {
            try { eqInstance?.setBandLevel(i.toShort(), (dbValues[i] * 100).toShort()) } catch (_: Exception) {}
        }
        savePreset(dbValues)
    }

    private fun savePreset(dbValues: IntArray) {
        getSharedPreferences("eq", Context.MODE_PRIVATE).edit()
          .putString("last_preset", dbValues.joinToString(",")).apply()
    }

    private fun loadLastPreset() {
        val last = getSharedPreferences("eq", Context.MODE_PRIVATE).getString("last_preset", "0,0,0,0,0")
        val dbValues = last!!.split(",").map { it.toInt() }.toIntArray()
        applyPreset(dbValues)
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel("eq", "KR EQ", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "eq")
          .setContentTitle("KR Equalizer Aktif")
          .setContentText("Ketuk untuk buka setting")
          .setSmallIcon(android.R.drawable.ic_media_play)
          .setContentIntent(pending)
          .setOngoing(true)
          .build()
    }

    override fun onDestroy() {
        eqInstance?.release()
        eqInstance = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
