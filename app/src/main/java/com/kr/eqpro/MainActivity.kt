package com.kr.eqpro
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.kr.eqpro.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.mainFrame.id, EqFragment())
                .commitNow()
        }
    }
}
