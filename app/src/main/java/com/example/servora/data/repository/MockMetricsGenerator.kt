package com.example.servora.data.repository

import com.example.servora.data.model.ProcessInfo
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerMetrics
import kotlin.random.Random

/**
 * Produces believable random-walk metrics for servers without a live agent.
 * Histories are kept per server id so charts evolve instead of jumping.
 */
class MockMetricsGenerator {

    private val cpuHistories = mutableMapOf<String, MutableList<Float>>()
    private val netInHistories = mutableMapOf<String, MutableList<Float>>()
    private val netOutHistories = mutableMapOf<String, MutableList<Float>>()
    private val uptimeBases = mutableMapOf<String, Long>()

    fun nextMetrics(server: Server): ServerMetrics {
        val cpuBase = cpuBaseFor(server.id)
        val cpu = (cpuBase + Random.nextFloat() * 20f - 10f).coerceIn(5f, 99f)
        val memory = memoryBaseFor(server).coerceIn(5f, 97f)
        val disk = diskBaseFor(server).coerceIn(5f, 97f)
        val networkIn = Random.nextFloat() * 800f + 50f
        val networkOut = Random.nextFloat() * 400f + 30f
        val memTotal = if (server.id.hashCode() % 3 == 0) 64f else 32f
        val diskTotal = if (server.id.hashCode() % 2 == 0) 2000f else 500f

        val uptime = uptimeBases.getOrPut(server.id) { Random.nextLong(86_400L, 2_592_000L) } + 3L

        return ServerMetrics(
            cpuUsage = cpu,
            memoryUsage = memory,
            memoryTotal = memTotal,
            memoryUsed = memTotal * memory / 100f,
            diskUsage = disk,
            diskTotal = diskTotal,
            diskUsed = diskTotal * disk / 100f,
            networkIn = networkIn,
            networkOut = networkOut,
            uptime = uptime,
            responseTime = Random.nextInt(200) + 10,
            requestsPerSecond = Random.nextInt(500) + 50,
            activeConnections = Random.nextInt(200) + 10,
            cpuHistory = push(cpuHistories, server.id, cpu),
            networkInHistory = push(netInHistories, server.id, networkIn),
            networkOutHistory = push(netOutHistories, server.id, networkOut)
        )
    }

    fun clear(serverId: String) {
        cpuHistories.remove(serverId)
        netInHistories.remove(serverId)
        netOutHistories.remove(serverId)
        uptimeBases.remove(serverId)
    }

    private fun push(history: MutableMap<String, MutableList<Float>>, id: String, value: Float): List<Float> {
        val list = history.getOrPut(id) { mutableListOf() }
        list.add(value)
        if (list.size > 20) list.removeAt(0)
        return list.toList()
    }

    private fun cpuBaseFor(id: String): Float = when (id.hashCode() % 5) {
        0 -> 45f; 1 -> 72f; 2 -> 35f; 3 -> 25f; else -> 55f
    }

    private fun memoryBaseFor(server: Server): Float = when (server.type.lowercase()) {
        "postgresql", "redis" -> 75f + Random.nextFloat() * 12f
        "minio" -> 65f + Random.nextFloat() * 10f
        else -> 45f + Random.nextFloat() * 20f
    }

    private fun diskBaseFor(server: Server): Float = when (server.type.lowercase()) {
        "postgresql", "minio" -> 70f + Random.nextFloat() * 10f
        else -> 30f + Random.nextFloat() * 12f
    }

    companion object {
        fun demoProcesses(): List<ProcessInfo> = listOf(
            ProcessInfo("nginx", 1024, Random.nextFloat() * 15f + 5f, Random.nextFloat() * 10f + 2f),
            ProcessInfo("postgres", 2048, Random.nextFloat() * 25f + 10f, Random.nextFloat() * 20f + 10f),
            ProcessInfo("node", 3072, Random.nextFloat() * 12f + 3f, Random.nextFloat() * 8f + 4f),
            ProcessInfo("redis-server", 4096, Random.nextFloat() * 8f + 2f, Random.nextFloat() * 30f + 5f),
            ProcessInfo("python3", 5120, Random.nextFloat() * 10f + 1f, Random.nextFloat() * 6f + 1f)
        )
    }
}
