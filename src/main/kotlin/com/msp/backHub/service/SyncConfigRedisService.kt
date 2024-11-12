package com.msp.backHub.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class SyncConfigRedisService(@Autowired private val redisTemplate: RedisTemplate<String, Any?>) {

    private val backupConfigHash: HashOperations<String, String, String> = redisTemplate.opsForHash()



    // JobGroup , JobName , pathDetails


    // ideally , it should be a map(path and destinationResources) , since path details are sent in body of "BACKUP" and "SYNC" ,
    // it is not necessary till webapp to aws cache connection.

    fun getBackupConfigCache(jobGroup:String,jobName:String): String? {
        return backupConfigHash.get(jobGroup,jobName)
    }

    fun updateBackupConfigCache(jobGroup:String,jobName:String,pathDetails:String){
        backupConfigHash.put(jobGroup,jobName,pathDetails)
    }


    fun deleteBackupConfigCache(jobGroup:String,jobName:String): Long {
        return backupConfigHash.delete(jobGroup,jobName)
    }



    fun getSyncConfigDestinationResourcesCache(resourceId:String,configId:String): String? {
        return backupConfigHash.get(resourceId,configId)
    }

    fun updateSyncConfigDestinationResourcesCache(resourceId:String,configId:String,destinationResources:String){
        backupConfigHash.put(resourceId,configId,destinationResources)
    }


    fun deleteSyncConfigDestinationResourcesCache(resourceId:String,configId:String): Long {
        return backupConfigHash.delete(resourceId,configId)
    }







}