package com.kr.eqpro

import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null
    private val bandViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_eq)

        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME)

        if (sessionId != 0) {
            try {
                equalizer = Equalizer(0, sessionId)
                equalizer?.enabled = true
                Toast.makeText(this, "EQ aktif untuk $packageName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "App ini ga support EQ eksternal", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Buka lewat app musik: Settings > Equalizer", Toast.LENGTH_LONG).show()
            finish()
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
    }

    private fun setupPresets() {
        findViewById<Chip>(R.id.chip_flat).setOnClickListener { applyPreset(intArrayOf(0,0,0,0,0)) }
        findViewById<Chip>(R.id.chip_bass).setOnClickListener { applyPreset(intArrayOf(8,6,0,-2,-4)) }
        findViewById<Chip>(R.id.chip_full_bass).setOnClickListener { applyPreset(intArrayOf(12,9,3,0,-3)) }
        findViewById<Chip>(R.id.chip_bass_treble).setOnClickListener { applyPreset(intArrayOf(7,3,0,3,7)) }
        findViewById<Chip>(R.id.chip_vocal).setOnClickListener { applyPreset(intArrayOf(-2,2,6,4,0)) }
        findViewById<Chip>(R.id.chip_headphones).setOnClickListener { applyPreset(intArrayOf(4,2,0,2,5)) }
    }

    private fun applyPreset(dbValues: IntArray) {
        for (i in dbValues.indices) {
            val db = dbValues[i]
            bandViews[i].findViewById<SeekBar>(R.id.seek_band).progress = db + 15
            try { equalizer?.setBandLevel(i.toShort(), (db * 100).toShort()) } catch (e: Exception) {}
        }
    }

    private fun setupBand(view: View, bandIndex: Int, hz: String) {
        view.findViewById<TextView>(R.id.txt_hz).text = hz
        val seekBand = view.findViewById<SeekBar>(R.id.seek_band)
        val txtDb = view.findViewById<TextView>(R.id.txt_db)

        seekBand.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val db = progress - 15
                txtDb.text = "${db}dB"
                try { equalizer?.setBandLevel(bandIndex.toShort(), (db * 100).toShort()) } catch (e: Exception) {}
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
