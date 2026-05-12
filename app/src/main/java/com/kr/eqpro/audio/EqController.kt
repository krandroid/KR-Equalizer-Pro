package com.kr.eqpro.audio
import android.media.audiofx.*

class EqController {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var loudness: LoudnessEnhancer? = null

    fun init(sessionId: Int = 0): Boolean {
        release()
        return try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
            virtualizer = Virtualizer(0, sessionId)
            loudness = LoudnessEnhancer(sessionId)
            true
        } catch (e: Exception) { false }
    }

    fun setDolbyMode(enable: Boolean, spread: Int = 80) {
        try {
            virtualizer?.apply {
                enabled = enable
                if (strengthSupported) setStrength((spread * 10).toShort())
            }
            loudness?.apply {
                enabled = enable
                if (enable) setTargetGain(1000)
            }
        } catch (_: Exception) {}
    }

    fun isDolbySupported(): Boolean {
        return try { Virtualizer(0, 0).run { val s = strengthSupported; release(); s } } 
        catch (_: Exception) { false }
    }

    fun release() {
        equalizer?.release(); virtualizer?.release(); loudness?.release()
        equalizer = null; virtualizer = null; loudness = null
    }
}
