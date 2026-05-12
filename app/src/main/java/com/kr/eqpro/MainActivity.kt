package com.kr.eqpro

import android.content.Intent
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_eq)

        val serviceIntent = Intent(this, EqService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        try {
            equalizer = Equalizer(0, 0)
            equalizer?.enabled = true
        } catch (e: Exception) { }

        setupBand(findViewById(R.id.band_60), 0, "60Hz")
        setupBand(findViewById(R.id.band_230), 1, "230Hz")
        setupBand(findViewById(R.id.band_910), 2, "910Hz")
        setupBand(findViewById(R.id.band_3k6), 3, "3.6kHz")
        setupBand(findViewById(R.id.band_14k), 4, "14kHz")

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
