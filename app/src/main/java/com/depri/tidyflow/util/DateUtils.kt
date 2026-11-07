package com.depri.tidyflow.util

import java.time.LocalDate
import java.util.Locale

object DateUtils {

    val RU: Locale = Locale.forLanguageTag("ru")

    /** Понедельник недели, в которую попадает дата. */
    fun weekStart(date: LocalDate): LocalDate =
        date.minusDays((date.dayOfWeek.value - 1).toLong())

    /** Запланирована ли привычка на этот день недели (по битовой маске). */
    fun isDue(mask: Int, date: LocalDate): Boolean =
        ((mask shr (date.dayOfWeek.value - 1)) and 1) == 1
}
