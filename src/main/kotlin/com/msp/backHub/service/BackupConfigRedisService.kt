package com.msp.backHub.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service


@Service
class BackupConfigRedisService(@Autowired private val redisTemplate: RedisTemplate<String, Any?>) {

    private val backupConfigHash: HashOperations<String, String, String> = redisTemplate.opsForHash()



    // JobGroup , JobName , pathDetails

    fun getBackupConfigCache(jobGroup:String,jobName:String): String? {
        return backupConfigHash.get(jobGroup,jobName)
    }

    fun updateBackupConfigCache(jobGroup:String,jobName:String,pathDetails:String){
         backupConfigHash.put(jobGroup,jobName,pathDetails)
    }


    fun deleteBackupConfigCache(jobGroup:String,jobName:String): Long {
        return backupConfigHash.delete(jobGroup,jobName)
    }

}