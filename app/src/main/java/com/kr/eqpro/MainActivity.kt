package com.kr.eqpro

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // LANGSUNG START GLOBAL EQ
        startForegroundService(Intent(this, EqService::class.java))

        setupBand(R.id.seek_60, R.id.db_60, 0)
        setupBand(R.id.seek_230, R.id.db_230, 1)
        setupBand(R.id.seek_910, R.id.db_910, 2)
        setupBand(R.id.seek_3k6, R.id.db_3k6, 3)
        setupBand(R.id.seek_14k, R.id.db_14k, 4)
        setupPresets()
        loadCurrentValues()

        findViewById<Chip>(R.id.chip_flat).requestFocus()
    }

    private fun setupPresets() {
        findViewById<Chip>(R.id.chip_flat).setOnClickListener { sendPreset(0,0,0) }
        findViewById<Chip>(R.id.chip_bass).setOnClickListener { sendPreset(8,6,0,-2,-4) }
        findViewById<Chip>(R.id.chip_full_bass).setOnClickListener { sendPreset(12,9,3,0,-3) }
        findViewById<Chip>(R.id.chip_bass_treble).setOnClickListener { sendPreset(7,3,0,3,7) }
        findViewById<Chip>(R.id.chip_vocal).setOnClickListener { sendPreset(-2,2,6,4,0) }
        findViewById<Chip>(R.id.chip_headphones).setOnClickListener { sendPreset(4,2,0,2,5) }
    }

    private fun sendPreset(b1: Int, b2: Int, b3: Int, b4: Int, b5: Int) {
        val intent = Intent(this, EqService::class.java)
        intent.action = EqService.ACTION_UPDATE_EQ
        intent.putExtra(EqService.EXTRA_PRESET, intArrayOf(b1, b2, b3, b4, b5))
        startService(intent)

        findViewById<SeekBar>(R.id.seek_60).progress = b1 + 15
        findViewById<SeekBar>(R.id.seek_230).progress = b2 + 15
        findViewById<SeekBar>(R.id.seek_910).progress = b3 + 15
        findViewById<SeekBar>(R.id.seek_3k6).progress = b4 + 15
        findViewById<SeekBar>(R.id.seek_14k).progress = b5 + 15
    }

    private fun setupBand(seekId: Int, txtId: Int, band: Int) {
        val seek = findViewById<SeekBar>(seekId)
        val txt = findViewById<TextView>(txtId)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                val db = p - 15
                txt.text = "${db}dB"
                if (u) {
                    val intent = Intent(this@MainActivity, EqService::class.java)
                    intent.action = EqService.ACTION_UPDATE_EQ
                    intent.putExtra(EqService.EXTRA_BAND, band)
                    intent.putExtra(EqService.EXTRA_LEVEL, db * 100)
                    startService(intent)
                }
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    private fun loadCurrentValues() {
        val last = getSharedPreferences("eq", MODE_PRIVATE).getString("last_preset", "0,0,0,0,0")
        val db = last!!.split(",").map { it.toInt() }
        findViewById<SeekBar>(R.id.seek_60).progress = db[0] + 15
        findViewById<SeekBar>(R.id.seek_230).progress = db[1] + 15
        findViewById<SeekBar>(R.id.seek_910).progress = db[2] + 15
        findViewById<SeekBar>(R.id.seek_3k6).progress = db[3] + 15
        findViewById<SeekBar>(R.id.seek_14k).progress = db[4] + 15

        findViewById<TextView>(R.id.db_60).text = "${db[0]}dB"
        findViewById<TextView>(R.id.db_230).text = "${db[1]}dB"
        findViewById<TextView>(R.id.db_910).text = "${db[2]}dB"
        findViewById<TextView>(R.id.db_3k6).text = "${db[3]}dB"
        findViewById<TextView>(R.id.db_14k).text = "${db[4]}dB"
    }
}
