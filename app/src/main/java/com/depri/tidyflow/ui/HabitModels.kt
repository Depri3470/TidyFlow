package com.depri.tidyflow.ui

import com.depri.tidyflow.data.Habit
import java.time.LocalDate

/** Элемент сетки День. */
data class HabitDayItem(
    val habit: Habit,
    val done: Boolean,
    val streak: Int
)

/** Элемент таблицы Неделя: даты выполнения в виде ISO-строк. */
data class HabitWeekItem(
    val habit: Habit,
    val weekStart: LocalDate,
    val doneDates: Set<String>
)
