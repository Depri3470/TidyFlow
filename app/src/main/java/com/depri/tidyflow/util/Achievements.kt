package com.depri.tidyflow.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.depri.tidyflow.R

data class Achievement(
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    @DrawableRes val iconRes: Int,
    val current: Int,
    val target: Int
) {
    val unlocked: Boolean get() = current >= target
}

/**
 * Иконки достижений — Material Icons (векторы в res/drawable).
 * Compose-эквиваленты: Flag, Bolt, EmojiEvents, Star, ThumbUp, FormatListBulleted, TrendingUp
 * (часть из них — в androidx.compose.material:material-icons-extended).
 */
object Achievements {

    fun build(totalDone: Int, bestStreak: Int, habitsCount: Int, level: Int): List<Achievement> = listOf(
        Achievement(R.string.ach_first_title, R.string.ach_first_desc, R.drawable.ic_flag, totalDone, 1),
        Achievement(R.string.ach_streak3_title, R.string.ach_streak3_desc, R.drawable.ic_bolt, bestStreak, 3),
        Achievement(R.string.ach_streak7_title, R.string.ach_streak7_desc, R.drawable.ic_nav_trophy, bestStreak, 7),
        Achievement(R.string.ach_ten_title, R.string.ach_ten_desc, R.drawable.ic_star, totalDone, 10),
        Achievement(R.string.ach_fifty_title, R.string.ach_fifty_desc, R.drawable.ic_thumb_up, totalDone, 50),
        Achievement(R.string.ach_planner_title, R.string.ach_planner_desc, R.drawable.ic_list, habitsCount, 3),
        Achievement(R.string.ach_level_title, R.string.ach_level_desc, R.drawable.ic_trending_up, level, 3)
    )
}
