package com.kr.eqpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val bandViews = mutableListOf<View>()
    private var serviceStarted = false

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startEqService()
        else Toast.makeText(this, "Service background dimatikan.", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_eq)
        checkNotifPermissionAndStartService()

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

    private fun checkNotifPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= 33) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    startEqService()
                }
                else -> requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startEqService()
        }
    }

    private fun startEqService() {
        if (!serviceStarted) {
            val serviceIntent = Intent(this, EqService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            serviceStarted = true
        }
    }

    private fun setupPresets() {
        findViewById<Button>(R.id.btn_flat).setOnClickListener { applyPreset(intArrayOf(0,0,0)) }
        findViewById<Button>(R.id.btn_bass).setOnClickListener { applyPreset(intArrayOf(8,6,0,-2,-4)) }
        findViewById<Button>(R.id.btn_full_bass).setOnClickListener { applyPreset(intArrayOf(12,9,3,0,-3)) }
        findViewById<Button>(R.id.btn_bass_treble).setOnClickListener { applyPreset(intArrayOf(7,3,0,3,7)) }
    }

    private fun applyPreset(dbValues: IntArray) {
        for (i in dbValues.indices) {
            val db = dbValues[i]
            val progress = db + 15
            val seekBand = bandViews[i].findViewById<SeekBar>(R.id.seek_band)
            seekBand.progress = progress

            val intent = Intent(this, EqService::class.java)
            intent.putExtra("band", i)
            intent.putExtra("level", (db * 100))
            startService(intent)
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

                val intent = Intent(this@MainActivity, EqService::class.java)
                intent.putExtra("band", bandIndex)
                intent.putExtra("level", (db * 100))
                startService(intent)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
