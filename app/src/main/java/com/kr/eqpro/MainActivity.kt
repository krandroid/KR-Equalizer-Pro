package com.kr.eqpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null
    private val bandViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_eq)

        // Minta permission notifikasi Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
               != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Cek Equalizer support, kalo gagal jangan crash
        try {
            equalizer = Equalizer(0, 0)
            equalizer?.enabled = true
        } catch (e: Exception) {
            Toast.makeText(this, "HP ini block Global EQ. Coba pake app musik internal.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        // Start service cuma kalo Equalizer berhasil
        if (equalizer!= null) {
            val serviceIntent = Intent(this, EqService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }

        bandViews.add(findViewById(R.id.band_60))
        bandViews.add(findViewById(R.id.band_230))
        bandViews.add(findViewById(R.id.band_910))
        bandViews.add(findViewById(R.id.band_3k6))
        bandViews.add(findViewById(R.id.band_14k))

        setupBand(bandViews[0], 0, "60Hz")
        setupBand(bandViews[1], 1, "230Hz")
        setupBand(bandViews[2], 2, "910Hz")
        setupBand(bandViews[3], 3, "3.6kHz")
        setupBand(bandViews[4], 4, "14kHz")

        setupPresets()

        val switchDolby = findViewById<Switch>(R.id.switch_dolby)
        val layoutSpread = findViewById<View>(R.id.layout_spread)
        val seekSpread = findViewById<SeekBar>(R.id.seek_spread)
        val txtSpread = findViewById<TextView>(R.id.txt_spread)

        switchDolby.setOnCheckedChangeListener { _, isChecked ->
            layoutSpread.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        seekSpread.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtSpread.text = "SPREAD: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupPresets() {
        findViewById<Chip>(R.id.chip_flat).setOnClickListener { applyPreset(intArrayOf(0,0,0)) }
        findViewById<Chip>(R.id.chip_bass).setOnClickListener { applyPreset(intArrayOf(8,6,0,-2,-4)) }
        findViewById<Chip>(R.id.chip_full_bass).setOnClickListener { applyPreset(intArrayOf(12,9,3,0,-3)) }
        findViewById<Chip>(R.id.chip_bass_treble).setOnClickListener { applyPreset(intArrayOf(7,3,0,3,7)) }
        findViewById<Chip>(R.id.chip_vocal).setOnClickListener { applyPreset(intArrayOf(-2,2,6,4,0)) }
        findViewById<Chip>(R.id.chip_headphones).setOnClickListener { applyPreset(intArrayOf(4,2,0,2,5)) }
    }

    private fun applyPreset(dbValues: IntArray) {
        for (i in dbValues.indices) {
            val db = dbValues[i]
            val progress = db + 15
            val seekBand = bandViews[i].findViewById<SeekBar>(R.id.seek_band)
            seekBand.progress = progress

            try {
                equalizer?.setBandLevel(i.toShort(), (db * 100).toShort())
                val intent = Intent(this, EqService::class.java)
                intent.putExtra("band", i)
                intent.putExtra("level", (db * 100))
                startService(intent)
            } catch (e: Exception) { }
        }
    }

    private fun setupBand(view: View, bandIndex: Int, hz: String) {
        val txtHz = view.findViewById<TextView>(R.id.txt_hz)
        val seekBand = view.findViewById<SeekBar>(R.id.seek_band)
        val txtDb = view.findViewById<TextView>(R.id.txt_db)

        txtHz.text = hz

        seekBand.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val db = progress - 15
                txtDb.text = "${db}dB"
                try {
                    equalizer?.setBandLevel(bandIndex.toShort(), (db * 100).toShort())

                    val intent = Intent(this@MainActivity, EqService::class.java)
                    intent.putExtra("band", bandIndex)
                    intent.putExtra("level", (db * 100))
                    startService(intent)

                } catch (e: Exception) { }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        equalizer?.release()
        super.onDestroy()
    }
}
