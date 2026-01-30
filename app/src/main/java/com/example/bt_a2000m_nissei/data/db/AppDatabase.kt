package com.example.bt_a2000m_nissei.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class, MachineEntity::class, ChecklistTemplateEntity::class,
        ChecklistItemEntity::class, MachineChecklistBindingEntity::class,
        ChecksheetEntity::class, ChecksheetItemResultEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun machineDao(): MachineDao
    abstract fun seedDao(): SeedDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun runnerDao(): RunnerDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nissei-db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
