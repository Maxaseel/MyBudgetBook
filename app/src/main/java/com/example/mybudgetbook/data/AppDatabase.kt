package com.example.mybudgetbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mybudgetbook.data.dao.*
import com.example.mybudgetbook.data.entity.*

@Database(
    entities = [
        Buchung::class, Konto::class, Kategorie::class,
        Zahlungsart::class, Person::class, Gruppe::class,
        Budget::class, Regel::class, Sparziel::class
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buchungDao(): BuchungDao
    abstract fun kontoDao(): KontoDao
    abstract fun kategorieDao(): KategorieDao
    abstract fun zahlungsartDao(): ZahlungsartDao
    abstract fun personDao(): PersonDao
    abstract fun gruppeDao(): GruppeDao
    abstract fun statDao(): StatDao
    abstract fun budgetDao(): BudgetDao
    abstract fun regelDao(): RegelDao
    abstract fun sparzielDao(): SparzielDao

    companion object {
        const val DATABASE_NAME = "mybudgetbook.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }

        fun resetInstance() {
            INSTANCE = null
        }
    }
}
