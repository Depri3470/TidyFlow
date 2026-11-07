package com.depri.tidyflow.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.depri.tidyflow.R
import com.depri.tidyflow.databinding.ActivityAchievementsBinding
import com.depri.tidyflow.util.Achievements
import com.depri.tidyflow.util.Game
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Экран достижений (ачивок). */
class AchievementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private val adapter = AchievementAdapter()
    private var userId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets(binding.root, includeBottom = false)

        userId = requireUserId() ?: return
        setupBottomNav(binding.bottomNav, R.id.nav_achievements)

        binding.rvAchievements.layoutManager = LinearLayoutManager(this)
        binding.rvAchievements.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (userId != -1L) load()
    }

    private fun load() {
        lifecycleScope.launch {
            val habits = db.habitDao().getAll(userId)
            val logs = db.logDao().getAll(userId)
            val dates = logs.map { LocalDate.parse(it.date) }.toSet()
            val xp = logs.size * Game.XP_PER_COMPLETION

            val list = Achievements.build(
                totalDone = logs.size,
                bestStreak = Game.bestStreak(dates),
                habitsCount = habits.size,
                level = Game.level(xp)
            )
            binding.tvOpened.text = getString(R.string.ach_opened, list.count { it.unlocked }, list.size)
            adapter.submitList(list)
        }
    }
}
