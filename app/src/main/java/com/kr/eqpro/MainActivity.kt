package com.kr.eqpro

import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
        if (sessionId == 0) {
            Toast.makeText(this, "Buka lewat SmartTube: Audio > Equalizer", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        try {
            equalizer = Equalizer(0, sessionId)
            equalizer?.enabled = true
        } catch (e: Exception) {
            Toast.makeText(this, "App ini ga support EQ", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupBand(R.id.seek_60, R.id.db_60, 0)
        setupBand(R.id.seek_230, R.id.db_230, 1)
        setupBand(R.id.seek_910, R.id.db_910, 2)
        setupBand(R.id.seek_3k6, R.id.db_3k6, 3)
        setupBand(R.id.seek_14k, R.id.db_14k, 4)
        setupPresets()
        
        findViewById<Chip>(R.id.chip_flat).requestFocus()
    }

    private fun setupPresets() {
        findViewById<Chip>(R.id.chip_flat).setOnClickListener { applyPreset(0,0,0) }
        findViewById<Chip>(R.id.chip_bass).setOnClickListener { applyPreset(8,6,0,-2,-4) }
        findViewById<Chip>(R.id.chip_full_bass).setOnClickListener { applyPreset(12,9,3,0,-3) }
        findViewById<Chip>(R.id.chip_bass_treble).setOnClickListener { applyPreset(7,3,0,3,7) }
        findViewById<Chip>(R.id.chip_vocal).setOnClickListener { applyPreset(-2,2,6,4,0) }
        findViewById<Chip>(R.id.chip_headphones).setOnClickListener { applyPreset(4,2,0,2,5) }
    }

    private fun applyPreset(b1: Int, b2: Int, b3: Int, b4: Int, b5: Int) {
        setBand(R.id.seek_60, R.id.db_60, 0, b1)
        setBand(R.id.seek_230, R.id.db_230, 1, b2)
        setBand(R.id.seek_910, R.id.db_910, 2, b3)
        setBand(R.id.seek_3k6, R.id.db_3k6, 3, b4)
        setBand(R.id.seek_14k, R.id.db_14k, 4, b5)
    }

    private fun setBand(seekId: Int, txtId: Int, band: Int, db: Int) {
        findViewById<SeekBar>(seekId).progress = db + 15
        findViewById<TextView>(txtId).text = "${db}dB"
        try { equalizer?.setBandLevel(band.toShort(), (db * 100).toShort()) } catch (_: Exception) {}
    }

    private fun setupBand(seekId: Int, txtId: Int, band: Int) {
        val seek = findViewById<SeekBar>(seekId)
        val txt = findViewById<TextView>(txtId)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val db = p - 15
                txt.text = "${db}dB"
                try { equalizer?.setBandLevel(band.toShort(), (db * 100).toShort()) } catch (_: Exception) {}
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        equalizer?.release()
        super.onDestroy()
    }
}
