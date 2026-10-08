package com.archi.dreaming.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DreamEntity::class], version = 1, exportSchema = false)
abstract class DreamDatabase : RoomDatabase() {

    abstract fun dreamDao(): DreamDao

    companion object {
        @Volatile
        private var instance: DreamDatabase? = null

        fun get(context: Context): DreamDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DreamDatabase::class.java,
                    "dreams.db",
                ).build().also { instance = it }
            }
    }
}
