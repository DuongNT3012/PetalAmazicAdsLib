package com.zoomcamera.x30zoom.hdcamera

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zoomcamera.x30zoom.hdcamera.databinding.ActivitySplashBinding
import com.zoomcamera.petalamazic.callback.SplashAdsCallback
import com.zoomcamera.petalamazic.petal.Petal
import com.zoomcamera.petalamazic.utils.RemoteConfigHelper

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RemoteConfigHelper.getInstance().fetchData(this, 0, object : RemoteConfigHelper.IOnFetchDone {
            override fun onFetchDone() {
                loadSplashAds()
            }

            override fun onFetchFail() {
                loadSplashAds()
            }
        })
    }

    private fun loadSplashAds() {
        Petal.getInstance().loadSplashAds(this, binding.splashAdView, mutableListOf("testq6zq98hecj"), object : SplashAdsCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }, "splash_ads")
    }
}