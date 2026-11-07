package com.depri.tidyflow.util

import android.content.Context

/** Хранит id текущего пользователя между запусками приложения. */
class Session(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("tidyflow_session", Context.MODE_PRIVATE)

    var userId: Long
        get() = prefs.getLong(KEY_USER_ID, NO_USER)
        set(value) = prefs.edit().putLong(KEY_USER_ID, value).apply()

    fun logout() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    companion object {
        const val NO_USER = -1L
        private const val KEY_USER_ID = "user_id"
    }
}
