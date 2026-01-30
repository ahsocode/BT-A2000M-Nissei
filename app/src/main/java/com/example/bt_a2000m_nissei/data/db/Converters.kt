package com.example.bt_a2000m_nissei.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun toPeriod(value: String?): InspectionPeriod? =
        value?.let { InspectionPeriod.valueOf(it) }

    @TypeConverter
    fun fromPeriod(value: InspectionPeriod?): String? = value?.name

    @TypeConverter
    fun toItemType(value: String?): ChecklistItemType? =
        value?.let { ChecklistItemType.valueOf(it) }

    @TypeConverter
    fun fromItemType(value: ChecklistItemType?): String? = value?.name
}
