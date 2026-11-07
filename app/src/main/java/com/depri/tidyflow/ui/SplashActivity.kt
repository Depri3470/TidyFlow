package com.depri.tidyflow.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.lifecycleScope
import com.depri.tidyflow.databinding.ActivitySplashBinding
import com.depri.tidyflow.util.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Экран загрузки с анимацией логотипа. Затем — вход или главный экран. */
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logo = binding.logoCard
        logo.scaleX = 0.4f
        logo.scaleY = 0.4f
        logo.alpha = 0f
        logo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(700)
            .setInterpolator(OvershootInterpolator())
            .start()

        for (view in listOf(binding.tvAppName, binding.tvTagline)) {
            view.alpha = 0f
            view.animate().alpha(1f).setStartDelay(450).setDuration(500).start()
        }

        lifecycleScope.launch {
            delay(1800)
            val uid = session.userId
            val loggedIn = uid != Session.NO_USER && db.userDao().findById(uid) != null
            val next = if (loggedIn) MainActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this@SplashActivity, next))
            finish()
        }
    }
}
