package com.depri.tidyflow.ui

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.depri.tidyflow.R
import com.depri.tidyflow.databinding.ActivityStatsBinding
import com.depri.tidyflow.util.DateUtils
import com.depri.tidyflow.util.Game
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle

/** Статистика: общие показатели и столбчатая диаграмма за 7 дней. */
class StatsActivity : BaseActivity() {

    private lateinit var binding: ActivityStatsBinding
    private var userId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets(binding.root, includeBottom = false)

        userId = requireUserId() ?: return
        setupBottomNav(binding.bottomNav, R.id.nav_stats)
    }

    override fun onResume() {
        super.onResume()
        if (userId != -1L) load()
    }

    private fun load() {
        lifecycleScope.launch {
            val habits = db.habitDao().getAll(userId)
            val logs = db.logDao().getAll(userId)
            val today = LocalDate.now()

            val allDates = logs.map { LocalDate.parse(it.date) }
            val uniqueDates = allDates.toSet()
            val xp = logs.size * Game.XP_PER_COMPLETION

            binding.tvLevelInfo.text = getString(R.string.level_xp_fmt, Game.level(xp), xp)
            binding.tvTotal.text = logs.size.toString()
            binding.tvStreak.text = getString(R.string.days_fmt, Game.currentStreak(uniqueDates, today))
            binding.tvBest.text = getString(R.string.days_fmt, Game.bestStreak(uniqueDates))

            // последние 7 дней (включая сегодня)
            val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
            val counts = days.map { day -> allDates.count { it == day } }
            val labels = days.map {
                it.dayOfWeek.getDisplayName(TextStyle.SHORT, DateUtils.RU)
                    .replaceFirstChar { c -> c.titlecase(DateUtils.RU) }
            }
            binding.barChart.setData(counts, labels)

            // процент выполнения за 7 дней
            var scheduled = 0
            var done = 0
            for (day in days) {
                for (habit in habits) {
                    if (DateUtils.isDue(habit.daysMask, day) &&
                        !LocalDate.parse(habit.createdAt).isAfter(day)
                    ) {
                        scheduled++
                        if (logs.any { it.habitId == habit.id && it.date == day.toString() }) {
                            done++
                        }
                    }
                }
            }
            val rate = if (scheduled == 0) 0 else (done * 100 / scheduled)
            binding.tvWeekRate.text = getString(R.string.percent_fmt, rate)
        }
    }
}
