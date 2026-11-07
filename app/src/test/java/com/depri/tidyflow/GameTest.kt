package com.depri.tidyflow

import com.depri.tidyflow.util.Game
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GameTest {

    private val day = LocalDate.of(2026, 11, 1)

    @Test
    fun levelGrowsEveryHundredXp() {
        assertEquals(1, Game.level(0))
        assertEquals(1, Game.level(90))
        assertEquals(2, Game.level(100))
        assertEquals(3, Game.level(250))
    }

    @Test
    fun bestStreakCountsConsecutiveDays() {
        val dates = setOf(day, day.plusDays(1), day.plusDays(2), day.plusDays(5))
        assertEquals(3, Game.bestStreak(dates))
    }

    @Test
    fun currentStreakAllowsTodayToBeEmpty() {
        val today = day.plusDays(3)
        val dates = setOf(day.plusDays(1), day.plusDays(2))
        assertEquals(2, Game.currentStreak(dates, today))
    }

    @Test
    fun currentStreakBreaksAfterGap() {
        val today = day.plusDays(5)
        val dates = setOf(day, day.plusDays(1))
        assertEquals(0, Game.currentStreak(dates, today))
    }

    @Test
    fun habitStreakSkipsNonScheduledDays() {
        // Привычка по будням (Пн-Пт): 2026-11-02 — понедельник
        val mask = 0b0011111
        val monday = LocalDate.of(2026, 11, 2)
        val friday = monday.minusDays(3) // 2026-10-30
        val thursday = friday.minusDays(1)
        val dates = setOf(thursday, friday, monday)
        assertEquals(3, Game.habitStreak(mask, dates, monday))
    }
}
