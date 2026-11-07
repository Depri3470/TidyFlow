package com.depri.tidyflow.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.depri.tidyflow.R
import com.depri.tidyflow.databinding.ItemHabitDayBinding
import com.depri.tidyflow.util.HabitColors
import com.depri.tidyflow.util.HabitIcons

/** Сетка привычек на выбранный день (2 колонки). */
class DayHabitAdapter(
    private val onClick: (HabitDayItem, View) -> Unit,
    private val onLongClick: (HabitDayItem) -> Unit
) : ListAdapter<HabitDayItem, DayHabitAdapter.VH>(DayHabitDiff) {

    class VH(val b: ItemHabitDayBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemHabitDayBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.b
        val ctx = b.root.context
        val color = HabitColors.get(item.habit.colorIndex)

        b.ivIcon.setImageResource(HabitIcons.res(item.habit.icon))
        b.ivIcon.imageTintList = ColorStateList.valueOf(color)
        b.tvTitle.text = item.habit.title
        b.tvStreak.text = if (item.streak > 0) {
            ctx.getString(R.string.streak_days, item.streak)
        } else {
            ctx.getString(R.string.no_streak)
        }

        if (item.done) {
            b.card.setCardBackgroundColor(ColorUtils.setAlphaComponent(color, 40))
            b.card.setStrokeColor(color)
            b.ivCheck.setBackgroundResource(R.drawable.bg_circle_filled)
            b.ivCheck.backgroundTintList = ColorStateList.valueOf(color)
            b.ivCheck.setImageResource(R.drawable.ic_check)
        } else {
            b.card.setCardBackgroundColor(ctx.getColor(R.color.surface))
            b.card.setStrokeColor(ctx.getColor(R.color.divider))
            b.ivCheck.setBackgroundResource(R.drawable.bg_circle_outline)
            b.ivCheck.backgroundTintList = null
            b.ivCheck.setImageDrawable(null)
        }

        b.card.setOnClickListener { onClick(item, b.card) }
        b.card.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }
}

private object DayHabitDiff : DiffUtil.ItemCallback<HabitDayItem>() {
    override fun areItemsTheSame(old: HabitDayItem, new: HabitDayItem) =
        old.habit.id == new.habit.id

    override fun areContentsTheSame(old: HabitDayItem, new: HabitDayItem) = old == new
}
