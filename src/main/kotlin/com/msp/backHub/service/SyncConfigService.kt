package com.msp.backHub.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.msp.backHub.Repo.*
import com.msp.backHub.entity.ResourceIdSyncConfigIdMapping
import com.msp.backHub.entity.SyncConfigEntity
import com.msp.backHub.pojo.Pojo
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class SyncConfigService {
    @Autowired
    private lateinit var syncConfigRepo: SyncConfigRepo
    @Autowired
    private lateinit var resourceIdSyncConfigIdMappingRepo: ResourceIdSyncConfigIdMappingRepo
    @Autowired
    private lateinit var schedulerService: SchedulerService
    @Autowired
    private lateinit var syncConfigRedisService: SyncConfigRedisService
    @Autowired
    private lateinit var resourceRepo: ResourceEntityRepo


    fun addSyncConfig(addConfigInput: Pojo.SyncConfig): Boolean {
        try {
            val newConfig = SyncConfigEntity()
            newConfig.name = addConfigInput.name
            newConfig.enable = addConfigInput.enable
            newConfig.resourceId = addConfigInput.resourceId
            newConfig.companyId = addConfigInput.companyId!!

            newConfig.destinationResources = addConfigInput.destinationResources!!.joinToString(",")
            newConfig.pathDetails = Gson().toJson(addConfigInput.pathDetails.includePaths)


            val addedConfig = syncConfigRepo.saveAndFlush(newConfig)

            // *****************************************
            // TODO : DO IN BACKGROUND BY PULSAR
            updateSyncConfigScheduleForAddOperation(addConfigInput, addedConfig)
            // *****************************************

        } catch (ex: Exception) {
            println("ex while addSyncConfig , ${ex.printStackTrace()}")
            return false
        }
        return true
    }


    fun updateSyncConfig(updateConfigList: List<Pojo.SyncConfig>): Boolean {
        updateConfigList.forEach { updateConfigInput ->

            val configId = updateConfigInput.id!!
            val existingConfig = syncConfigRepo.findById(configId).get()

            existingConfig.name = updateConfigInput.name

            updateSyncConfigScheduleForUpdateOperation(updateConfigInput, existingConfig)

            val pathDetailsInput = Gson().toJson(updateConfigInput.pathDetails.includePaths)

            val destinationResourcesString = updateConfigInput.destinationResources!!.joinToString(",")
            syncConfigRedisService.updateSyncConfigDestinationResourcesCache(updateConfigInput.resourceId.toString(), configId.toString(), destinationResourcesString)

            existingConfig.destinationResources = destinationResourcesString

            existingConfig.pathDetails = pathDetailsInput
            existingConfig.enable = updateConfigInput.enable

            syncConfigRepo.saveAndFlush(existingConfig)

        }
        return true
    }


    private fun updateSyncConfigScheduleForAddOperation(configInput: Pojo.SyncConfig, configEntity: SyncConfigEntity) {

        if (!configInput.enable) {
            throw RuntimeException("Invalid Input")
        }

        val configId = configEntity.id!!
        val pathDetailsInput = configEntity.pathDetails!!

        val jobName = "JobName_syncConfig_${configId}"
        val triggerName = "TriggerName_syncConfig_${configId}"

        if (configInput.resourceId != null) {

            val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
            val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

            val resourceAndSyncConfigMapping = ResourceIdSyncConfigIdMapping()
            resourceAndSyncConfigMapping.configId = configId
            resourceAndSyncConfigMapping.resourceId = configInput.resourceId
            resourceIdSyncConfigIdMappingRepo.save(resourceAndSyncConfigMapping)

            syncConfigRedisService.updateBackupConfigCache(configInput.resourceId.toString(), configId.toString(), pathDetailsInput)

            notifyAgent(jobName, jobGroup, configInput.resourceId, configId, "ADD_SYNC_CONFIG", pathDetailsInput)

            val destinationResourcesString = configInput.destinationResources!!.joinToString(",")
            syncConfigRedisService.updateSyncConfigDestinationResourcesCache(configInput.resourceId.toString(), configId.toString(), destinationResourcesString)


            schedulerService.scheduleSyncJob(jobName, jobGroup, triggerName, triggerGroup, pathDetailsInput, configInput.resourceId, configId, destinationResourcesString)

        } else {
            throw RuntimeException("Invalid Input")
        }
    }


    private fun updateSyncConfigScheduleForUpdateOperation(configInput: Pojo.SyncConfig, configEntity: SyncConfigEntity) {

        val enabled = configInput.enable
        val enabledNow = !configEntity.enable && enabled
        if (enabled) {
            val configId = configEntity.id!!
            val pathDetailsInput = Gson().toJson(configInput.pathDetails.includePaths)
            val existingPathDetails = configEntity.pathDetails


            if ((pathDetailsInput != existingPathDetails) || enabledNow) {

                val jobName = "JobName_syncConfig_${configId}"

                if (configInput.resourceId != null) {

                    if (enabledNow) {
                        val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                        syncConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                        notifyAgent(jobName, jobGroup, configInput.resourceId, configId, "ADD_SYNC_CONFIG", pathDetailsInput)

                    } else {

                        val jobNameRemoveConfig = "JobName_backupConfig_${configId}_RemoveConfig"
                        val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
                        syncConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)
                        notifyAgent("null", "null", configInput.resourceId, configEntity.id!!, "REMOVE_SYNC_CONFIG")

                        // PATHS_REMOVE & UPDATE

                        val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                        syncConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                        notifyAgent(jobName, jobGroup, configInput.resourceId, configEntity.id!!, "ADD_SYNC_CONFIG", pathDetailsInput)

                    }

                } else if (configInput.companyId != null) {

                    val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
                    if (!resourcesList.isNullOrEmpty()) {
                        resourcesList.forEach { resource ->

                            if (enabledNow) {
                                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                                syncConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                                notifyAgent(jobName, jobGroup, resource.resourceId!!, configId, "ADD_SYNC_CONFIG", pathDetailsInput)
                            } else {
                                val jobNameRemoveConfig = "JobName_backupConfig_${configId}_RemoveConfig"
                                val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}_RemoveConfig"
                                syncConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)
                                notifyAgent("null", "null", resource.resourceId!!, configEntity.id!!, "REMOVE_SYNC_CONFIG")

                                // PATHS_REMOVE & UPDATE

                                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                                syncConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                                notifyAgent(jobName, jobGroup, resource.resourceId!!, configEntity.id!!, "ADD_SYNC_CONFIG", pathDetailsInput)

                            }
                        }
                    }
                }
            }

        } else {

            // do only  delete config related stuff here

            val jobName = "JobName_syncConfig_${configInput.id}"
            val triggerName = "TriggerName_syncConfig_${configInput.id}"

            if (configInput.resourceId != null) {


                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

                resourceIdSyncConfigIdMappingRepo.deleteByResourceIdAndConfigId(configInput.resourceId, configInput.id!!)


                schedulerService.deleteBackupJobSchedule(jobName, jobGroup)

                syncConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)


//                val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//                val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
//                backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)


                notifyAgent("null", "null", configInput.resourceId, configEntity.id!!, "REMOVE_SYNC_CONFIG")

            } else if (configInput.companyId != null) {
                val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
                if (!resourcesList.isNullOrEmpty()) {
                    resourcesList.forEach { resource ->

                        val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                        val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

                        resourceIdSyncConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, configInput.id!!)

                        schedulerService.deleteBackupJobSchedule(jobName, jobGroup)

                        syncConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)

//                        val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//                        val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
//                        backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

                        notifyAgent("null", "null", resource.resourceId!!, configEntity.id!!, "REMOVE_SYNC_CONFIG")

                    }
                }
            }
        }
    }

    fun deleteSyncConfig(deleteConfigInput: Pojo.SyncConfig):Boolean {
        val existingConfig = syncConfigRepo.findById(deleteConfigInput.id!!).get()
        existingConfig.enable = false

        syncConfigRepo.saveAndFlush(existingConfig)

        updateBackupConfigScheduleForDeleteOperation(deleteConfigInput,existingConfig)

        syncConfigRedisService.deleteBackupConfigCache(deleteConfigInput.resourceId.toString(),deleteConfigInput.id.toString())

        return true
    }


    private fun updateBackupConfigScheduleForDeleteOperation(configInput: Pojo.SyncConfig, configEntity: SyncConfigEntity) {
        val jobName = "JobName_syncConfig_${configInput.id!!}"
        if (configInput.resourceId != null) {


            val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

            resourceIdSyncConfigIdMappingRepo.deleteByResourceIdAndConfigId(configInput.resourceId, configInput.id!!)


            schedulerService.deleteBackupJobSchedule(jobName, jobGroup)

            syncConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)


//            val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
//            val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//            backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

            notifyAgent("null","null",configInput.resourceId,configEntity.id!!,"REMOVE_SYNC_CONFIG")


        }
        else if (configInput.companyId != null) {
            val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
            if (!resourcesList.isNullOrEmpty()) {
                resourcesList.forEach { resource ->

                    val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

                    resourceIdSyncConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, configInput.id!!)
                    schedulerService.deleteBackupJobSchedule(jobName, jobGroup)

                    syncConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)

//                    val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}_RemoveConfig"
//                    val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//                    backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

                    notifyAgent("null","null",resource.resourceId!!,configEntity.id!!,"REMOVE_SYNC_CONFIG")

                }

            }
        }
    }




    private fun notifyAgent(jobName: String, jobGroup: String, resourceId: Long, configId: Long, action: String, pathDetailsInput:String?=null) {

        val lambdaUrl = "https://xxxxxxx.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

        val httpPost = HttpPost(lambdaUrl)
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

        val requestData = JsonObject()

        requestData.addProperty("jobName", jobName)
        requestData.addProperty("jobGroup", jobGroup)
        requestData.addProperty("resourceId", resourceId.toString())
        requestData.addProperty("configId", configId.toString())
        requestData.addProperty("action", action)
        requestData.addProperty("paths",pathDetailsInput)



        httpPost.entity = StringEntity(requestData.toString(), ContentType.APPLICATION_JSON)

        HttpClients.createDefault().use { httpClient ->
            httpClient.execute(httpPost).use { response ->
                val statusCode = response.statusLine.statusCode
                val responseEntity = response.entity
                val responseJsonStr = EntityUtils.toString(responseEntity)
                println("LAMBDA RESPONSE CAME for SYNC UPDATE NOTIFY AGENT action : $action WITH  statusCOde : $statusCode , res String : $responseJsonStr")
            }
        }
    }


}