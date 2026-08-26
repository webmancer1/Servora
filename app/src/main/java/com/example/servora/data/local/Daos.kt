package com.example.servora.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers ORDER BY createdAt ASC")
    fun observeServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers ORDER BY createdAt ASC")
    suspend fun getServers(): List<ServerEntity>

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServer(id: String): ServerEntity?

    @Query("SELECT * FROM servers WHERE id = :id")
    fun observeServer(id: String): Flow<ServerEntity?>

    @Query("SELECT COUNT(*) FROM servers")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServer(server: ServerEntity)

    @Delete
    suspend fun deleteServer(server: ServerEntity)

    @Query("DELETE FROM metric_snapshots WHERE serverId = :serverId")
    suspend fun deleteSnapshotsFor(serverId: String)

    @Query("DELETE FROM alerts WHERE serverId = :serverId")
    suspend fun deleteAlertsFor(serverId: String)

    @Query("DELETE FROM remote_actions WHERE serverId = :serverId")
    suspend fun deleteActionsFor(serverId: String)
}

@Dao
interface MetricDao {

    @Insert
    suspend fun insertSnapshots(snapshots: List<MetricSnapshotEntity>)

    @Query("SELECT * FROM metric_snapshots WHERE serverId = :serverId AND timestamp >= :since ORDER BY timestamp ASC")
    fun observeHistory(serverId: String, since: Long): Flow<List<MetricSnapshotEntity>>

    @Query(
        "SELECT * FROM metric_snapshots WHERE serverId = :serverId AND timestamp >= :since " +
            "AND id IN (SELECT MAX(id) FROM metric_snapshots GROUP BY serverId) LIMIT 1"
    )
    suspend fun getLatestSnapshot(serverId: String, since: Long): MetricSnapshotEntity?

    @Query("DELETE FROM metric_snapshots WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}

@Dao
interface AlertDao {

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT :limit")
    fun observeAlerts(limit: Int = 200): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE serverId = :serverId ORDER BY timestamp DESC LIMIT :limit")
    fun observeAlertsFor(serverId: String, limit: Int = 50): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert
    suspend fun insertAlert(alert: AlertEntity): Long

    @Query("SELECT COUNT(*) FROM alerts WHERE dedupeKey = :dedupeKey AND timestamp >= :since")
    suspend fun countRecentWithKey(dedupeKey: String, since: Long): Int

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM alerts WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}

@Dao
interface RemoteActionDao {

    @Insert
    suspend fun insertAction(action: RemoteActionEntity): Long

    @Query("UPDATE remote_actions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT * FROM remote_actions ORDER BY timestamp DESC LIMIT :limit")
    fun observeActions(limit: Int = 50): Flow<List<RemoteActionEntity>>

    @Query("SELECT * FROM remote_actions WHERE serverId = :serverId ORDER BY timestamp DESC LIMIT :limit")
    fun observeActionsFor(serverId: String, limit: Int = 20): Flow<List<RemoteActionEntity>>

    @Query("DELETE FROM remote_actions WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
