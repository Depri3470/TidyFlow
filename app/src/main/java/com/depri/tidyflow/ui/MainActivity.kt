package com.depri.tidyflow.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.depri.tidyflow.R
import com.depri.tidyflow.data.Habit
import com.depri.tidyflow.data.HabitLog
import com.depri.tidyflow.databinding.ActivityMainBinding
import com.depri.tidyflow.util.DateUtils
import com.depri.tidyflow.util.Game
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Главный экран: сетка привычек на день или таблица на неделю. */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var userId = -1L
    private var selectedDate: LocalDate = LocalDate.now()
    private var weekMode = false
    private var totalLogs = 0

    private val dayAdapter = DayHabitAdapter(
        onClick = { item, view -> onDayHabitClick(item, view) },
        onLongClick = { item -> openEditor(item.habit.id) }
    )
    private val weekAdapter = WeekHabitAdapter(
        onCellClick = { item, date, view -> onWeekCellClick(item, date, view) },
        onLongClick = { item -> openEditor(item.habit.id) }
    )

    private val headerCells: List<TextView> by lazy {
        with(binding) { listOf(hdr0, hdr1, hdr2, hdr3, hdr4, hdr5, hdr6) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets(binding.root, includeBottom = false)

        userId = requireUserId() ?: return
        setupBottomNav(binding.bottomNav, R.id.nav_home)

        binding.rvHabits.layoutManager = GridLayoutManager(this, 2)
        binding.rvHabits.adapter = dayAdapter

        binding.toggleMode.check(R.id.btnDay)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                weekMode = checkedId == R.id.btnWeek
                applyMode()
            }
        }

        binding.btnPrev.setOnClickListener {
            selectedDate = selectedDate.minusDays(if (weekMode) 7 else 1)
            load()
        }
        binding.btnNext.setOnClickListener {
            val today = LocalDate.now()
            val candidate = selectedDate.plusDays(if (weekMode) 7 else 1)
            selectedDate = if (candidate.isAfter(today)) today else candidate
            load()
        }
        binding.fabAdd.setOnClickListener { openEditor(-1L) }
        binding.btnLogout.setOnClickListener {
            session.logout()
            goToLogin()
        }

        binding.tvDate.text = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, d MMMM", DateUtils.RU))
            .replaceFirstChar { it.titlecase(DateUtils.RU) }
    }

    override fun onResume() {
        super.onResume()
        if (userId != -1L) load()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun applyMode() {
        if (weekMode) {
            binding.rvHabits.layoutManager = LinearLayoutManager(this)
            binding.rvHabits.adapter = weekAdapter
        } else {
            binding.rvHabits.layoutManager = GridLayoutManager(this, 2)
            binding.rvHabits.adapter = dayAdapter
        }
        binding.weekHeader.isVisible = weekMode
        load()
    }

    private fun openEditor(habitId: Long) {
        startActivity(
            Intent(this, HabitEditActivity::class.java)
                .putExtra(HabitEditActivity.EXTRA_HABIT_ID, habitId)
        )
    }

    // ---------------------------------------------------------------- загрузка данных

    private fun load() {
        lifecycleScope.launch {
            val habits = db.habitDao().getAll(userId)
            val logs = db.logDao().getAll(userId)
            val user = db.userDao().findById(userId)
            if (user == null) {
                session.logout()
                goToLogin()
                return@launch
            }
            totalLogs = logs.size
            val datesByHabit: Map<Long, Set<LocalDate>> =
                logs.groupBy({ it.habitId }, { LocalDate.parse(it.date) })
                    .mapValues { it.value.toSet() }

            bindHeader(user.name, logs.size)
            if (weekMode) bindWeek(habits, datesByHabit) else bindDay(habits, datesByHabit)
        }
    }

    private fun bindHeader(name: String, logsCount: Int) {
        val xp = logsCount * Game.XP_PER_COMPLETION
        binding.tvGreeting.text = getString(R.string.greeting, name)
        binding.tvLevel.text = getString(R.string.level_fmt, Game.level(xp))
        binding.tvXp.text = getString(R.string.xp_fmt, Game.xpInLevel(xp), Game.XP_PER_LEVEL)
        binding.xpBar.setProgressCompat(Game.xpInLevel(xp), true)
    }

    private fun bindDay(habits: List<Habit>, datesByHabit: Map<Long, Set<LocalDate>>) {
        val today = LocalDate.now()
        val items = habits
            .filter {
                DateUtils.isDue(it.daysMask, selectedDate) &&
                    !LocalDate.parse(it.createdAt).isAfter(selectedDate)
            }
            .map { habit ->
                val dates = datesByHabit[habit.id].orEmpty()
                HabitDayItem(habit, selectedDate in dates, Game.habitStreak(habit.daysMask, dates, today))
            }

        val title = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", DateUtils.RU))
        binding.tvRange.text = if (selectedDate == today) {
            getString(R.string.today) + ", " + title
        } else {
            title
        }
        binding.tvProgress.text = getString(R.string.progress_fmt, items.count { it.done }, items.size)
        binding.tvEmpty.isVisible = items.isEmpty()
        dayAdapter.submitList(items)
    }

    private fun bindWeek(habits: List<Habit>, datesByHabit: Map<Long, Set<LocalDate>>) {
        val today = LocalDate.now()
        val start = DateUtils.weekStart(selectedDate)
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("d MMM", DateUtils.RU)
        binding.tvRange.text = start.format(fmt) + " – " + end.format(fmt)

        // заголовки дней
        val dayNames = listOf(
            R.string.day_mon, R.string.day_tue, R.string.day_wed, R.string.day_thu,
            R.string.day_fri, R.string.day_sat, R.string.day_sun
        )
        headerCells.forEachIndexed { i, cell ->
            val date = start.plusDays(i.toLong())
            cell.text = getString(dayNames[i]) + "\n" + date.dayOfMonth
            val isToday = date == today
            cell.setTextColor(getColor(if (isToday) R.color.primary else R.color.text_secondary))
            cell.setTypeface(null, if (isToday) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }

        // прогресс за неделю (только прошедшие дни)
        var scheduled = 0
        var done = 0
        for (i in 0..6) {
            val date = start.plusDays(i.toLong())
            if (date.isAfter(today)) break
            for (habit in habits) {
                if (DateUtils.isDue(habit.daysMask, date) &&
                    !LocalDate.parse(habit.createdAt).isAfter(date)
                ) {
                    scheduled++
                    if (date in datesByHabit[habit.id].orEmpty()) done++
                }
            }
        }
        binding.tvProgress.text = getString(R.string.progress_fmt, done, scheduled)

        val items = habits.map { habit ->
            HabitWeekItem(
                habit = habit,
                weekStart = start,
                doneDates = datesByHabit[habit.id].orEmpty().map { it.toString() }.toSet()
            )
        }
        binding.tvEmpty.isVisible = items.isEmpty()
        weekAdapter.submitList(items)
    }

    // ---------------------------------------------------------------- отметки

    private fun onDayHabitClick(item: HabitDayItem, card: View) {
        if (item.done) {
            toggle(item.habit.id, selectedDate, completing = false)
        } else {
            playCompleteAnimation(card) { toggle(item.habit.id, selectedDate, completing = true) }
        }
    }

    private fun onWeekCellClick(item: HabitWeekItem, date: LocalDate, cell: View) {
        val done = date.toString() in item.doneDates
        if (done) {
            toggle(item.habit.id, date, completing = false)
        } else {
            playCompleteAnimation(cell) { toggle(item.habit.id, date, completing = true) }
        }
    }

    /** «Приятная» анимация отметки: лёгкое увеличение и пружинистый возврат. */
    private fun playCompleteAnimation(target: View, onEnd: () -> Unit) {
        target.animate()
            .scaleX(1.12f).scaleY(1.12f)
            .setDuration(120)
            .withEndAction {
                target.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(260)
                    .setInterpolator(OvershootInterpolator(3f))
                    .withEndAction(onEnd)
                    .start()
            }
            .start()
    }

    private fun toggle(habitId: Long, date: LocalDate, completing: Boolean) {
        lifecycleScope.launch {
            if (completing) {
                db.logDao().insert(HabitLog(habitId = habitId, date = date.toString()))
                val oldLevel = Game.level(totalLogs * Game.XP_PER_COMPLETION)
                val newLevel = Game.level((totalLogs + 1) * Game.XP_PER_COMPLETION)
                val message = if (newLevel > oldLevel) {
                    getString(R.string.level_up, newLevel)
                } else {
                    getString(R.string.xp_earned, Game.XP_PER_COMPLETION)
                }
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            } else {
                db.logDao().delete(habitId, date.toString())
            }
            load()
        }
    }
}
