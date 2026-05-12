package com.kr.eqpro

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class EqService : Service() {
    private var equalizer: Equalizer? = null
    private val CHANNEL_ID = "KR_EQ_CHANNEL"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KR Equalizer Pro Aktif")
            .setContentText("Dolby Spread berjalan di background")
            .setSmallIcon(R.drawable.ic_equalizer_gold)
            .setOngoing(true)
            .build()
        startForeground(1, notification)

        // Global audio session
        try {
            equalizer = Equalizer(0, 0)
            equalizer?.enabled = true
            // Set band default di sini kalo mau
        } catch (e: Exception) { }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Terima update dari MainActivity buat ubah band
        val band = intent?.getIntExtra("band", -1) ?: -1
        val level = intent?.getIntExtra("level", 0) ?: 0
        if (band >= 0) {
            try {
                equalizer?.setBandLevel(band.toShort(), level.toShort())
            } catch (e: Exception) { }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        equalizer?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "KR Equalizer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
