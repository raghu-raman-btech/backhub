package com.backhubWebsocketLambda.service

import com.google.gson.JsonObject
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands

class RedisService {
    private val redisClient: RedisClient
    private val connection: StatefulRedisConnection<String, String>
    private val syncCommands: RedisCommands<String, String>


    init {
        println("REDIS INIT CALLED NOW")

        val redisHost = System.getenv("REDIS_HOST")!!
        val redisPort = System.getenv("REDIS_PORT")?.toInt() ?: 6379

        val redisURI = RedisURI.Builder.redis(redisHost, redisPort).build()
        redisClient = RedisClient.create(redisURI)
        synchronized(this){
            connection = redisClient.connect()
            syncCommands = connection.sync()
        }
    }

    fun set(key: String, value: String) {
        syncCommands.set(key, value)

    }

    fun get(key: String): String? {
        return syncCommands.get(key)
    }


    fun hset(key:String, map: Map<String,String>){
         syncCommands.hset(key,map)
    }

    fun hget(key: String,hkey:String):String {
        return syncCommands.hget(key,hkey)
    }


    fun updateResourceConnection(resourceId:String,connId:String){
        hset("ResourceIdConnId", mapOf(resourceId to connId))
    }

    fun updateClientConnection(clientToken:String,connId:String){
        hset("clientTokenConnId", mapOf(clientToken to connId))
    }

    fun getClientConnId(clientToken:String):String{
        return hget("clientTokenConnId",clientToken)
    }

    fun getResourceConnId(resourceId:String):String{
        return hget("ResourceIdConnId",resourceId)
    }



    fun pathsForJobKey(jobGroup: String, jobName: String): String {
        return hget(jobGroup,jobName)
    }


    fun updateBackupPathsByWatcher(resourceId: String, configId: String, path: String) {
        val key = "BackupWatcher_resource:${resourceId}_configId:${configId}"
        syncCommands.sadd(key,path)
        println("updated paths by watcher to cache")
    }

    fun deleteBackupPathsByWatcher(resourceId: String, configId: String) {
        val key = "BackupWatcher_resource:${resourceId}_configId:${configId}"
        syncCommands.del(key)
    }


    fun getBackupPathsByWatcher(resourceId: String, configId: String): MutableSet<String>? {
        val key = "BackupWatcher_resource:${resourceId}_configId:${configId}"
        return  syncCommands.smembers(key)
    }


    /////////////// SYNC ////////////

    fun updateSyncPathsByWatcher(resourceId: String, configId: String, path: String) {
        val key = "SyncWatcher_resource:${resourceId}_configId:${configId}"
        syncCommands.sadd(key,path)
        println("updated paths by watcher to cache")
    }

    fun deleteSyncPathsByWatcher(resourceId: String, configId: String) {
        val key = "SyncWatcher_resource:${resourceId}_configId:${configId}"
        syncCommands.del(key)
    }


    fun getSyncPathsByWatcher(resourceId: String, configId: String): MutableSet<String>? {
        val key = "SyncWatcher_resource:${resourceId}_configId:${configId}"
        return  syncCommands.smembers(key)
    }



    //////////////// LastBackupTime ////////////



    fun updateLastBackupTime(resourceId: String, configId: String){
        val key = "LastBackupTime_resource:${resourceId}_configId:${configId}"
        syncCommands.set(key,System.currentTimeMillis().toString())
    }


    fun getLastBackupTime(resourceId: String, configId: String): String? {
        val key = "LastBackupTime_resource:${resourceId}_configId:${configId}"
        return syncCommands.get(key)
    }



    //////////////// LastSyncTime ////////////

    fun updateLastSyncTime(resourceId: String, configId: String){
        val key = "LastSyncTime_resource:${resourceId}_configId:${configId}"
        syncCommands.set(key,System.currentTimeMillis().toString())
    }


    fun getLastSyncTime(resourceId: String, configId: String): String? {
        val key = "LastSyncTime_resource:${resourceId}_configId:${configId}"
        return syncCommands.get(key)
    }


    fun handleConnIdCache(headerJson: JsonObject?, connId: String, queryStringParameters: JsonObject?) {
        val resourceId =  headerJson?.get("Resourceid")?.asString
        if(resourceId != null) {
            updateResourceConnection(resourceId,connId)
        }else{
            val token = queryStringParameters!!.get("token").asString
            updateClientConnection(token,connId)
        }

    }



}