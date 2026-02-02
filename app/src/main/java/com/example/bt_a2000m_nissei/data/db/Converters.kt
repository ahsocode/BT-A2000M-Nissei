package com.example.bt_a2000m_nissei.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromInspectionPeriod(value: InspectionPeriod?): String? = value?.name

    @TypeConverter
    fun toInspectionPeriod(value: String?): InspectionPeriod? =
        value?.let { InspectionPeriod.valueOf(it) }

    @TypeConverter
    fun fromChecklistItemType(value: ChecklistItemType?): String? = value?.name

    @TypeConverter
    fun toChecklistItemType(value: String?): ChecklistItemType? =
        value?.let { ChecklistItemType.valueOf(it) }
}
