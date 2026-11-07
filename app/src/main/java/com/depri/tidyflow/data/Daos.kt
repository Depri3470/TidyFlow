package com.depri.tidyflow.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?
}

@Dao
interface HabitDao {
    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY id")
    suspend fun getAll(userId: Long): List<Habit>
}

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: Long, date: String)

    @Query(
        "SELECT l.* FROM habit_logs l " +
            "INNER JOIN habits h ON h.id = l.habitId " +
            "WHERE h.userId = :userId"
    )
    suspend fun getAll(userId: Long): List<HabitLog>
}
