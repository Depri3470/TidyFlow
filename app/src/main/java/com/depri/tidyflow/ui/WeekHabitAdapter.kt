package com.depri.tidyflow.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.depri.tidyflow.R
import com.depri.tidyflow.databinding.ItemHabitWeekBinding
import com.depri.tidyflow.util.DateUtils
import com.depri.tidyflow.util.HabitColors
import com.depri.tidyflow.util.HabitIcons
import java.time.LocalDate

/** Таблица «привычка × 7 дней недели». */
class WeekHabitAdapter(
    private val onCellClick: (HabitWeekItem, LocalDate, View) -> Unit,
    private val onLongClick: (HabitWeekItem) -> Unit
) : ListAdapter<HabitWeekItem, WeekHabitAdapter.VH>(WeekHabitDiff) {

    class VH(val b: ItemHabitWeekBinding) : RecyclerView.ViewHolder(b.root) {
        val cells: List<ImageView> = listOf(b.d0, b.d1, b.d2, b.d3, b.d4, b.d5, b.d6)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemHabitWeekBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.b
        val color = HabitColors.get(item.habit.colorIndex)
        val today = LocalDate.now()

        b.ivIcon.setImageResource(HabitIcons.res(item.habit.icon))
        b.ivIcon.imageTintList = ColorStateList.valueOf(color)
        b.tvTitle.text = item.habit.title
        b.rowRoot.setOnLongClickListener {
            onLongClick(item)
            true
        }

        holder.cells.forEachIndexed { index, cell ->
            val date = item.weekStart.plusDays(index.toLong())
            val due = DateUtils.isDue(item.habit.daysMask, date)
            val future = date.isAfter(today)
            val done = date.toString() in item.doneDates

            if (done) {
                cell.setBackgroundResource(R.drawable.bg_circle_filled)
                cell.backgroundTintList = ColorStateList.valueOf(color)
                cell.setImageResource(R.drawable.ic_check)
            } else {
                cell.setBackgroundResource(R.drawable.bg_circle_outline)
                cell.backgroundTintList = null
                cell.setImageDrawable(null)
            }

            cell.alpha = when {
                done -> 1f
                !due -> 0.25f
                future -> 0.5f
                else -> 1f
            }

            if (due && !future) {
                cell.setOnClickListener { onCellClick(item, date, cell) }
            } else {
                cell.setOnClickListener(null)
                cell.isClickable = false
            }
        }
    }
}

private object WeekHabitDiff : DiffUtil.ItemCallback<HabitWeekItem>() {
    override fun areItemsTheSame(old: HabitWeekItem, new: HabitWeekItem) =
        old.habit.id == new.habit.id

    override fun areContentsTheSame(old: HabitWeekItem, new: HabitWeekItem) = old == new
}
