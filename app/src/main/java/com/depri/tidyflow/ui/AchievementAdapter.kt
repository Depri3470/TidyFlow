package com.depri.tidyflow.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.depri.tidyflow.R
import com.depri.tidyflow.databinding.ItemAchievementBinding
import com.depri.tidyflow.util.Achievement

class AchievementAdapter : ListAdapter<Achievement, AchievementAdapter.VH>(AchievementDiff) {

    class VH(val b: ItemAchievementBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val b = holder.b
        val value = minOf(item.current, item.target)

        b.ivIcon.setImageResource(if (item.unlocked) item.iconRes else R.drawable.ic_lock)
        b.root.alpha = if (item.unlocked) 1f else 0.65f
        b.tvTitle.setText(item.titleRes)
        b.tvDesc.setText(item.descRes)
        b.progressBar.max = item.target
        b.progressBar.setProgressCompat(value, false)
        b.tvProgress.text = "$value/${item.target}"
    }
}

private object AchievementDiff : DiffUtil.ItemCallback<Achievement>() {
    override fun areItemsTheSame(old: Achievement, new: Achievement) = old.titleRes == new.titleRes
    override fun areContentsTheSame(old: Achievement, new: Achievement) = old == new
}
