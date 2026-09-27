package com.petmorph.ai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.petmorph.ai.data.model.CompanionEntity

@Database(entities = [CompanionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CompanionDatabase : RoomDatabase() {
    abstract fun companionDao(): CompanionDao

    companion object {
        @Volatile private var instance: CompanionDatabase? = null
        fun get(context: Context): CompanionDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CompanionDatabase::class.java,
                    "petmorph.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
