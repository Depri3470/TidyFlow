package com.depri.tidyflow.util

import java.time.LocalDate

/** Игровая логика: XP, уровни и серии. Чистый Kotlin — легко тестируется. */
object Game {

    const val XP_PER_COMPLETION = 10
    const val XP_PER_LEVEL = 100

    fun level(xp: Int): Int = xp / XP_PER_LEVEL + 1

    fun xpInLevel(xp: Int): Int = xp % XP_PER_LEVEL

    /** Текущая серия дней подряд (хотя бы одна отметка в день). Сегодняшний день не обязателен. */
    fun currentStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in dates) today else today.minusDays(1)
        var streak = 0
        while (day in dates) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun bestStreak(dates: Set<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sorted = dates.sorted()
        var best = 1
        var current = 1
        for (i in 1 until sorted.size) {
            current = if (sorted[i - 1].plusDays(1) == sorted[i]) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }

    /** Серия одной привычки: дни, на которые она не запланирована, серию не прерывают. */
    fun habitStreak(mask: Int, dates: Set<LocalDate>, today: LocalDate): Int {
        var streak = 0
        var day = today
        var guard = 0
        while (guard++ < 400) {
            if (day in dates) {
                streak++
            } else if (DateUtils.isDue(mask, day) && day != today) {
                break
            }
            day = day.minusDays(1)
        }
        return streak
    }
}
