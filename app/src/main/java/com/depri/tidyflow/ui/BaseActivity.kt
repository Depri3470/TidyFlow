package com.depri.tidyflow.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.depri.tidyflow.R
import com.depri.tidyflow.data.AppDatabase
import com.depri.tidyflow.util.Session

/**
 * Общая база для всех экранов: доступ к БД и сессии, обработка системных отступов
 * (на API 35 edge-to-edge включён принудительно) и нижняя панель навигации.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected val db: AppDatabase by lazy { AppDatabase.get(this) }
    protected val session: Session by lazy { Session(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Тёмная тема: светлые иконки в статус-баре и навигационной панели
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
    }

    /** Добавляет отступы под статус-бар, вырез и (опционально) нижнюю панель/клавиатуру. */
    protected fun applyInsets(root: View, includeBottom: Boolean = true) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                bars.left,
                bars.top,
                bars.right,
                if (includeBottom) maxOf(bars.bottom, ime.bottom) else 0
            )
            insets
        }
    }

    /** Возвращает id пользователя или отправляет на экран входа. */
    protected fun requireUserId(): Long? {
        val id = session.userId
        if (id == Session.NO_USER) {
            goToLogin()
            return null
        }
        return id
    }

    protected fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    /** Нижняя панель навигации: Главная / Статистика / Достижения. */
    protected fun setupBottomNav(nav: BottomNavigationView, selectedId: Int) {
        nav.selectedItemId = selectedId
        nav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedId) {
                true
            } else {
                val target = when (item.itemId) {
                    R.id.nav_home -> MainActivity::class.java
                    R.id.nav_stats -> StatsActivity::class.java
                    else -> AchievementsActivity::class.java
                }
                val intent = Intent(this, target)
                if (target == MainActivity::class.java) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                } else {
                    startActivity(intent)
                    if (this !is MainActivity) finish()
                }
                false
            }
        }
    }
}
