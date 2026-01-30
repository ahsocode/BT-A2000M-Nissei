package com.example.bt_a2000m_nissei.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SeedDao {

    @Query("SELECT COUNT(*) FROM machines")
    suspend fun countMachines(): Int

    @Query("SELECT COUNT(*) FROM checklist_templates")
    suspend fun countTemplates(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: MachineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<ChecklistTemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBindings(bindings: List<MachineChecklistBindingEntity>)
}
