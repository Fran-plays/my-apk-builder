package com.petmorph.ai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petmorph.ai.data.model.CompanionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionDao {
    @Query("SELECT * FROM companions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CompanionEntity>>

    @Query("SELECT * FROM companions WHERE id = :id")
    suspend fun getById(id: String): CompanionEntity?

    @Query("SELECT COUNT(*) FROM companions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CompanionEntity)

    @Update
    suspend fun update(entity: CompanionEntity)

    @Delete
    suspend fun delete(entity: CompanionEntity)
}
