package com.depri.tidyflow.util

import androidx.annotation.StringRes
import com.depri.tidyflow.R

data class Achievement(
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val emoji: String,
    val current: Int,
    val target: Int
) {
    val unlocked: Boolean get() = current >= target
}

object Achievements {

    fun build(totalDone: Int, bestStreak: Int, habitsCount: Int, level: Int): List<Achievement> = listOf(
        Achievement(R.string.ach_first_title, R.string.ach_first_desc, "🌱", totalDone, 1),
        Achievement(R.string.ach_streak3_title, R.string.ach_streak3_desc, "🔥", bestStreak, 3),
        Achievement(R.string.ach_streak7_title, R.string.ach_streak7_desc, "🏆", bestStreak, 7),
        Achievement(R.string.ach_ten_title, R.string.ach_ten_desc, "🎯", totalDone, 10),
        Achievement(R.string.ach_fifty_title, R.string.ach_fifty_desc, "💎", totalDone, 50),
        Achievement(R.string.ach_planner_title, R.string.ach_planner_desc, "🗂️", habitsCount, 3),
        Achievement(R.string.ach_level_title, R.string.ach_level_desc, "⭐", level, 3)
    )
}
