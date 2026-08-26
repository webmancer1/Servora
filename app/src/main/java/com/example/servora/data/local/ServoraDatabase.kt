package com.example.servora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.servora.data.model.AlertSeverity

class AlertSeverityConverter {

    @TypeConverter
    fun fromSeverity(severity: AlertSeverity): String = severity.name

    @TypeConverter
    fun toSeverity(value: String): AlertSeverity =
        runCatching { AlertSeverity.valueOf(value) }.getOrDefault(AlertSeverity.INFO)
}

@Database(
    entities = [
        ServerEntity::class,
        MetricSnapshotEntity::class,
        AlertEntity::class,
        RemoteActionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AlertSeverityConverter::class)
abstract class ServoraDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun metricDao(): MetricDao
    abstract fun alertDao(): AlertDao
    abstract fun remoteActionDao(): RemoteActionDao
}
