package com.depri.tidyflow.util

object HabitColors {

    val palette = intArrayOf(
        0xFFFFFFFF.toInt(),
        0xFFE0E0E0.toInt(),
        0xFFC4C4C4.toInt(),
        0xFFA8A8A8.toInt(),
        0xFF8C8C8C.toInt(),
        0xFF707070.toInt()
    )

    fun get(index: Int): Int = palette[index.coerceIn(0, palette.size - 1)]
}
