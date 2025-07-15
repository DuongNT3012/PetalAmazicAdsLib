package com.amazic.example.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amazic.example.databinding.ActivityMainBinding
import com.amazic.petalamazic.banner_ads.PetalBannerBuilder
import com.amazic.petalamazic.banner_ads.PetalBannerManager
import com.amazic.petalamazic.callback.InterAdsCallback
import com.amazic.petalamazic.callback.RewardAdsCallback
import com.amazic.petalamazic.inter_ads.InterManager
import com.amazic.petalamazic.native_ads.PetalNativeBuilder
import com.amazic.petalamazic.native_ads.PetalNativeManager
import com.amazic.petalamazic.reward_ads.RewardManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bannerBuilder = PetalBannerBuilder(this, binding.frBanner)
        bannerBuilder.listIdAdMain = mutableListOf("testw6vs28auh3")
        val bannerManager = PetalBannerManager(this, bannerBuilder, this, "banner_all")
        //bannerManager.setAlwaysReloadOnResume(true)
        //bannerManager.setIntervalReloadBanner(10000)

        val nativeBuilder = PetalNativeBuilder(this, binding.frNative, com.amazic.petalamazic.R.layout.layout_native_large, com.amazic.petalamazic.R.layout.shimmer_native_large)
        nativeBuilder.listIdAdMain = mutableListOf("testy63txaom86")
        val nativeManager = PetalNativeManager(this, nativeBuilder, this, "native_all")
        //nativeManager.setAlwaysReloadOnResume(true)
        //nativeManager.setIntervalReloadBanner(10000)

        RewardManager.loadRewardAds(this, mutableListOf("testx9dtjwj8hp"), "reward_all", "reward_all")
        InterManager.loadInterAds(this, mutableListOf("testb4znbuh3n2"), "inter_all", "inter_all")
        binding.tvReward.setOnClickListener {
            RewardManager.showRewardAds(this, "reward_all", "reward_all", object : RewardAdsCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    finish()
                }
            }, true)
        }
        binding.tvInter.setOnClickListener {
            InterManager.showInterAds(this, "inter_all", "inter_all", object : InterAdsCallback() {
                override fun onNextAction() {
                    super.onNextAction()
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                    finish()
                }
            }, true)
        }
    }
}