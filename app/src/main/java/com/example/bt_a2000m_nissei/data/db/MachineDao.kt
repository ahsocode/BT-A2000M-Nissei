package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MachineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(machine: MachineEntity)

    @Query("SELECT * FROM machines WHERE machineCode = :code")
    suspend fun findByCode(code: String): MachineEntity?

    @Query("SELECT * FROM machines WHERE id = :id")
    suspend fun findById(id: String): MachineEntity?
}
