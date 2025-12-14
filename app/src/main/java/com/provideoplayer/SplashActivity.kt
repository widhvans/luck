package com.provideoplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.provideoplayer.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    companion object {
        private const val SPLASH_DELAY = 1500L // 1.5 seconds
        private const val PREFS_NAME = "pro_video_player_prefs"
        private const val KEY_THEME = "theme_mode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme
        applyAppTheme()
        
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        binding.splashLogo.alpha = 0f
        binding.splashLogo.animate()
            .alpha(1f)
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(800)
            .withEndAction {
                binding.splashLogo.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()

        // Animate app name
        binding.appName.alpha = 0f
        binding.appName.translationY = 50f
        binding.appName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(400)
            .setDuration(600)
            .start()

        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            // Smooth transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY)
    }

    private fun applyAppTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = prefs.getInt(KEY_THEME, 0)
        
        when (themeMode) {
            0 -> { /* System Default */ }
            1 -> setTheme(R.style.Theme_ProVideoPlayer_Light)
            2 -> setTheme(R.style.Theme_ProVideoPlayer)
            3 -> setTheme(R.style.Theme_ProVideoPlayer_Amoled)
            4 -> setTheme(R.style.Theme_ProVideoPlayer_Blue)
            5 -> setTheme(R.style.Theme_ProVideoPlayer_Pink)
        }
    }
}
