package com.depri.tidyflow.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.depri.tidyflow.R
import kotlin.math.max

// Простая столбчатая диаграмма
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var values: List<Int> = emptyList()
    private var labels: List<String> = emptyList()
    private var progress = 1f

    private val density = resources.displayMetrics.density
    private val rect = RectF()

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bar_muted)
    }
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
    }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.background)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = sp(12f)
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = sp(13f)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    fun setData(values: List<Int>, labels: List<String>) {
        this.values = values
        this.labels = labels
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (values.isEmpty()) return

        val n = values.size
        val topPad = valuePaint.textSize + 8 * density
        val bottomPad = labelPaint.textSize + 12 * density
        val chartH = height - topPad - bottomPad
        val slot = width.toFloat() / n
        val barW = slot * 0.46f
        val radius = 10 * density
        val maxValue = max(values.maxOrNull() ?: 1, 1)

        for (i in 0 until n) {
            val left = slot * i + (slot - barW) / 2f
            val bottom = topPad + chartH

            rect.set(left, topPad, left + barW, bottom)
            canvas.drawRoundRect(rect, radius, radius, trackPaint)

            val value = values[i]
            if (value > 0) {
                val barH = max(chartH * value / maxValue * progress, radius * 1.2f)
                rect.set(left, bottom - barH, left + barW, bottom)
                canvas.drawRoundRect(rect, radius, radius, if (i == n - 1) todayPaint else barPaint)
                canvas.drawText(value.toString(), left + barW / 2f, bottom - barH - 6 * density, valuePaint)
            }

            if (i < labels.size) {
                canvas.drawText(labels[i], slot * i + slot / 2f, height - 6 * density, labelPaint)
            }
        }
    }

    private fun sp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
}
