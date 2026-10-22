package com.archi.dreaming.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DreamEntity::class, StepEntity::class], version = 5, exportSchema = false)
abstract class DreamDatabase : RoomDatabase() {

    abstract fun dreamDao(): DreamDao

    abstract fun stepDao(): StepDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dreams ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE dreams SET position = " +
                        "(SELECT COUNT(*) FROM dreams AS newer WHERE newer.createdAt > dreams.createdAt)",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE dreams_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "imagePath TEXT, " +
                        "createdAt INTEGER NOT NULL, " +
                        "position INTEGER NOT NULL)",
                )
                db.execSQL(
                    "INSERT INTO dreams_new (id, title, imagePath, createdAt, position) " +
                        "SELECT id, title, imagePath, createdAt, position FROM dreams",
                )
                db.execSQL("DROP TABLE dreams")
                db.execSQL("ALTER TABLE dreams_new RENAME TO dreams")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS steps (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "dreamId INTEGER NOT NULL, " +
                        "text TEXT NOT NULL, " +
                        "done INTEGER NOT NULL, " +
                        "position INTEGER NOT NULL, " +
                        "FOREIGN KEY(dreamId) REFERENCES dreams(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_steps_dreamId ON steps (dreamId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dreams ADD COLUMN fulfilledAt INTEGER")
            }
        }

        @Volatile
        private var instance: DreamDatabase? = null

        fun get(context: Context): DreamDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DreamDatabase::class.java,
                    "dreams.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }
    }
}
