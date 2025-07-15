package com.amazic.example.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amazic.petalamazic.callback.SplashAdsCallback
import com.amazic.petalamazic.petal.Petal
import com.amazic.example.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Petal.getInstance().loadSplashAds(this, binding.splashAdView, mutableListOf("testq6zq98hecj"), object : SplashAdsCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }, "splash_ads")
    }
}