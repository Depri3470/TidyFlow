package com.depri.tidyflow.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.depri.tidyflow.R
import com.depri.tidyflow.data.Habit
import com.depri.tidyflow.databinding.ActivityHabitEditBinding
import com.depri.tidyflow.util.HabitColors
import com.depri.tidyflow.util.HabitIcons
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Создание и редактирование привычки. */
class HabitEditActivity : BaseActivity() {

    private lateinit var binding: ActivityHabitEditBinding
    private var userId = -1L
    private var habitId = -1L
    private var existing: Habit? = null
    private var colorIndex = 0
    private val colorViews = mutableListOf<View>()

    private val dayChips: List<Chip> by lazy {
        with(binding) { listOf(chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets(binding.root)

        userId = requireUserId() ?: return
        habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)

        buildColorPicker()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        if (habitId != -1L) {
            lifecycleScope.launch {
                val habit = db.habitDao().getById(habitId)
                if (habit == null || habit.userId != userId) {
                    finish()
                    return@launch
                }
                existing = habit
                fill(habit)
            }
        }
    }

    private fun fill(habit: Habit) {
        binding.tvScreenTitle.setText(R.string.title_edit)
        binding.btnDelete.visibility = View.VISIBLE
        binding.etTitle.setText(habit.title)
        binding.etNote.setText(habit.note)

        for (child in binding.iconGroup.children) {
            if (child is Chip && child.tag == habit.icon) {
                binding.iconGroup.check(child.id)
            }
        }
        colorIndex = habit.colorIndex
        refreshColors()
        dayChips.forEachIndexed { i, chip -> chip.isChecked = ((habit.daysMask shr i) and 1) == 1 }
    }

    private fun buildColorPicker() {
        val density = resources.displayMetrics.density
        val size = (40 * density).toInt()
        val margin = (8 * density).toInt()
        HabitColors.palette.forEachIndexed { index, _ ->
            val view = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = margin }
                setOnClickListener {
                    colorIndex = index
                    refreshColors()
                }
            }
            colorViews.add(view)
            binding.colorRow.addView(view)
        }
        refreshColors()
    }

    private fun refreshColors() {
        colorViews.forEachIndexed { i, view ->
            val selected = i == colorIndex
            view.background = circle(HabitColors.palette[i])
            view.alpha = if (selected) 1f else 0.4f
            view.scaleX = if (selected) 1.15f else 0.9f
            view.scaleY = if (selected) 1.15f else 0.9f
        }
    }

    private fun circle(color: Int): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    }

    private fun save() {
        val title = binding.etTitle.text.toString().trim()
        val note = binding.etNote.text.toString().trim()
        binding.tilTitle.error = null
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.err_empty)
            return
        }

        var mask = 0
        dayChips.forEachIndexed { i, chip -> if (chip.isChecked) mask = mask or (1 shl i) }
        if (mask == 0) {
            Toast.makeText(this, R.string.err_pick_day, Toast.LENGTH_SHORT).show()
            return
        }

        val icon = binding.iconGroup
            .findViewById<Chip>(binding.iconGroup.checkedChipId)?.tag as? String ?: HabitIcons.DEFAULT

        lifecycleScope.launch {
            val current = existing
            if (current == null) {
                db.habitDao().insert(
                    Habit(
                        userId = userId,
                        title = title,
                        note = note,
                        icon = icon,
                        colorIndex = colorIndex,
                        daysMask = mask,
                        createdAt = LocalDate.now().toString()
                    )
                )
            } else {
                db.habitDao().update(
                    current.copy(
                        title = title,
                        note = note,
                        icon = icon,
                        colorIndex = colorIndex,
                        daysMask = mask
                    )
                )
            }
            finish()
        }
    }

    private fun confirmDelete() {
        val habit = existing ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    db.habitDao().delete(habit)
                    finish()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
