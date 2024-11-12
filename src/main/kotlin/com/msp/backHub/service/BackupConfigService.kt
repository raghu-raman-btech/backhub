package com.msp.backHub.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.msp.backHub.Repo.BackupConfigRepo
import com.msp.backHub.Repo.ResourceEntityRepo
import com.msp.backHub.Repo.ResourceIdConfigIdMappingRepo
import com.msp.backHub.entity.BackupConfigEntity
import com.msp.backHub.entity.ResourceEntity
import com.msp.backHub.entity.ResourceIdConfigIdMapping
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
class BackupConfigService {

    @Autowired
    private lateinit var backupConfigRepo: BackupConfigRepo
    @Autowired
    private lateinit var resourceRepo: ResourceEntityRepo
    @Autowired
    private lateinit var schedulerService: SchedulerService
    @Autowired
    private lateinit var resourceIdConfigIdMappingRepo: ResourceIdConfigIdMappingRepo
    @Autowired
    private lateinit var backupConfigRedisService: BackupConfigRedisService

    fun addBackupConfig(addConfigInput: Pojo.Config): Boolean {
        try {
            val newConfig = BackupConfigEntity()
            newConfig.name = addConfigInput.name
            newConfig.enable = addConfigInput.enable
            newConfig.resourceId = addConfigInput.resourceId
            newConfig.companyId = addConfigInput.companyId!!
            newConfig.backupAll = addConfigInput.backupAll

            val scheduleDetails = Gson().toJson(addConfigInput.scheduleDetails)
            newConfig.scheduleDetails = scheduleDetails

            newConfig.pathDetails = if (addConfigInput.backupAll) {
                "/"
            } else {
                Gson().toJson(addConfigInput.pathDetails.includePaths)
            }
            val addedConfig = backupConfigRepo.saveAndFlush(newConfig)

            // *****************************************
            // TODO : DO IN BACKGROUND BY PULSAR
            updateBackupConfigScheduleForAddOperation(addConfigInput, addedConfig)
            // *****************************************

        } catch (ex: Exception) {
            println("ex while addBackupConfig , ${ex.printStackTrace()}")
            return false
        }

        return true
    }


    fun updateBackupConfig(updateConfigList: List<Pojo.Config>): Boolean {
        updateConfigList.forEach { updateConfigInput->

            val configId = updateConfigInput.id!!

            val existingConfig = backupConfigRepo.findById(configId).get()
            existingConfig.name = updateConfigInput.name

            updateBackupConfigScheduleForUpdateOperation(updateConfigInput,existingConfig)

            val pathDetailsInput = if (updateConfigInput.backupAll) {
                "/"
            } else {
                Gson().toJson(updateConfigInput.pathDetails.includePaths)
            }

            existingConfig.scheduleDetails = Gson().toJson(updateConfigInput.scheduleDetails)
            existingConfig.pathDetails = pathDetailsInput
            existingConfig.enable = updateConfigInput.enable

            backupConfigRepo.saveAndFlush(existingConfig)

        }
        return true
    }


    fun deleteBackupConfig(deleteConfigInput: Pojo.Config): Boolean {
        val existingConfig = backupConfigRepo.findById(deleteConfigInput.id!!).get()
        existingConfig.enable = false
        backupConfigRepo.saveAndFlush(existingConfig)
        updateBackupConfigScheduleForDeleteOperation(deleteConfigInput,existingConfig)
        return true
    }




    fun updateBackupConfigScheduleForAddOperation(configInput: Pojo.Config, configEntity: BackupConfigEntity) {
        if(!configInput.enable){
            throw RuntimeException("Invalid Input")
        }

        val scheduleType = configInput.scheduleDetails.scheduleType
        var scheduleInterval = configInput.scheduleDetails.scheduleInterval

        val configId = configEntity.id!!
        val pathDetailsInput = configEntity.pathDetails!!
        if (scheduleType == Pojo.BackupConfigScheduleType.DAYS) {
             // convert days to hours
             scheduleInterval *= 24
        }

        val jobName = "JobName_backupConfig_${configId}"
        val triggerName = "TriggerName_backupConfig_${configId}"

        if (configInput.resourceId != null) {

            val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
            val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

            val resourceConfigmapping = ResourceIdConfigIdMapping()
            resourceConfigmapping.configId = configId
            resourceConfigmapping.resourceId = configInput.resourceId
            resourceIdConfigIdMappingRepo.save(resourceConfigmapping)

            backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)

            notifyAgent(jobName,jobGroup,configInput.resourceId,configId,"ADD_CONFIG",pathDetailsInput)

            schedulerService.scheduleBackupJob(jobName, jobGroup, triggerName, triggerGroup, scheduleInterval, pathDetailsInput, configInput.resourceId, configId)

        } else if (configInput.companyId != null) {
            val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
            if (!resourcesList.isNullOrEmpty()) {
                resourcesList.forEach { resource ->
                   // println("came insode for resource : ${resource.resourceId}")

                    val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId!!}"
                    val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${resource.resourceId}"

                    val resourceConfigmapping = ResourceIdConfigIdMapping()
                    resourceConfigmapping.configId = configId
                    resourceConfigmapping.resourceId = resource.resourceId
                    resourceIdConfigIdMappingRepo.save(resourceConfigmapping)


                    backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)

                    notifyAgent(jobName,jobGroup,resource.resourceId!! ,configId,"ADD_CONFIG",pathDetailsInput)

                    schedulerService.scheduleBackupJob(jobName, jobGroup, triggerName, triggerGroup, scheduleInterval, pathDetailsInput, resource.resourceId!!, configId)
                }
            }
        } else {
            throw RuntimeException("Invalid Input")
        }
    }

    fun updateBackupConfigScheduleForUpdateOperation(configInput: Pojo.Config, configEntity: BackupConfigEntity) {
        val enabled = configInput.enable

        val enabledNow = (!configEntity.enable && enabled)

        if (enabled) {

            val scheduleType = configInput.scheduleDetails.scheduleType
            var scheduleInterval = configInput.scheduleDetails.scheduleInterval

            val configId = configEntity.id!!

            if (scheduleType == Pojo.BackupConfigScheduleType.DAYS) {
                // convert days to hours
                scheduleInterval *= 24
            }

            val scheduleDetailsInput = Gson().toJson(configInput.scheduleDetails)
            val existingScheduleDetails = configEntity.scheduleDetails

            val pathDetailsInput = if (configInput.backupAll) {
                "/"
            } else {
                Gson().toJson(configInput.pathDetails.includePaths)
            }
            val existingPathDetails = configEntity.pathDetails



            if ((scheduleDetailsInput != existingScheduleDetails) || enabledNow) {

                val jobName = "JobName_backupConfig_${configId}"
                val triggerName = "TriggerName_backupConfig_${configId}"

                if (configInput.resourceId != null) {

                    val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                    val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"


                    schedulerService.deleteBackupJobSchedule(jobName, jobGroup)
                    schedulerService.scheduleBackupJob(jobName, jobGroup, triggerName, triggerGroup, scheduleInterval, pathDetailsInput, configInput.resourceId, configId)


                } else if (configInput.companyId != null) {

                    // println("came inside for clientLevel ${configInput.companyId}")

                    val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
                    if (!resourcesList.isNullOrEmpty()) {
                        resourcesList.forEach { resource ->

                            println("came inside for client.resourceLevel ${configInput.resourceId}")

                            val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}"
                            val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

                            schedulerService.deleteBackupJobSchedule(jobName, jobGroup)

                            schedulerService.scheduleBackupJob(jobName, jobGroup, triggerName, triggerGroup, scheduleInterval, pathDetailsInput, resource.resourceId!!, configId)

                        }

                    }

                }

            }

            if ((pathDetailsInput != existingPathDetails) || enabledNow) {
                val jobName = "JobName_backupConfig_${configId}"

                if (configInput.resourceId != null) {
                    if (enabledNow) {
                        val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                        backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                        notifyAgent(jobName, jobGroup, configInput.resourceId, configEntity.id!!, "ADD_CONFIG", pathDetailsInput)
                    } else {
                        val jobNameRemoveConfig = "JobName_backupConfig_${configId}_RemoveConfig"
                        val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
                        backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)
                        notifyAgent("null", "null", configInput.resourceId, configEntity.id!!, "REMOVE_CONFIG")

                        //TODO : PATHS_REMOVE & PATHS_UPDATE

                        val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                        backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                        notifyAgent(jobName, jobGroup, configInput.resourceId, configEntity.id!!, "ADD_CONFIG", pathDetailsInput)
                    }
                } else if (configInput.companyId != null) {

                    val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
                    if (!resourcesList.isNullOrEmpty()) {
                        resourcesList.forEach { resource ->

                            if (enabledNow) {
                                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}"
                                backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                                notifyAgent(jobName, jobGroup, resource.resourceId!!, configEntity.id!!, "ADD_CONFIG", pathDetailsInput)
                            } else {
                                val jobNameRemoveConfig = "JobName_backupConfig_${configId}_RemoveConfig"
                                val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}_RemoveConfig"
                                backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)
                                notifyAgent("null", "null", resource.resourceId!!, configEntity.id!!, "REMOVE_CONFIG")

                                //TODO : PATHS_REMOVE & PATHS_UPDATE

                                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}"
                                backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)
                                notifyAgent(jobName, jobGroup, resource.resourceId!!, configEntity.id!!, "ADD_CONFIG", pathDetailsInput)

                            }
                        }
                    }
                }
            }

        } else {

            // do only  delete config related stuff here

            val jobName = "JobName_backupConfig_${configInput.id}"
            val triggerName = "TriggerName_backupConfig_${configInput.id}"

            if (configInput.resourceId != null) {
                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
                resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(configInput.resourceId, configInput.id!!)
                schedulerService.deleteBackupJobSchedule(jobName, jobGroup)
                backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)


//                val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//                val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
//                backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)


                notifyAgent("null", "null", configInput.resourceId, configEntity.id!!, "REMOVE_CONFIG")


            } else if (configInput.companyId != null) {
                val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
                if (!resourcesList.isNullOrEmpty()) {
                    resourcesList.forEach { resource ->

                        val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}"

                        resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, configInput.id!!)

                        schedulerService.deleteBackupJobSchedule(jobName, jobGroup)

                        backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)

//                        val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//                        val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
//                        backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

                        notifyAgent("null", "null", resource.resourceId!!, configEntity.id!!, "REMOVE_CONFIG")

                    }
                }
            }
        }
    }


    fun updateBackupConfigScheduleForDeleteOperation(configInput: Pojo.Config, configEntity: BackupConfigEntity) {
        val jobName = "JobName_backupConfig_${configInput.id!!}"
        if (configInput.resourceId != null) {

            val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
            resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(configInput.resourceId, configInput.id!!)
            schedulerService.deleteBackupJobSchedule(jobName, jobGroup)
            backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)

//            val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
//            val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//            backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

            notifyAgent("null", "null", configInput.resourceId, configEntity.id!!, "REMOVE_CONFIG")


        } else if (configInput.companyId != null) {
            val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
            if (!resourcesList.isNullOrEmpty()) {
                resourcesList.forEach { resource ->
                    val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId!!}"
                    resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, configInput.id)
                    schedulerService.deleteBackupJobSchedule(jobName, jobGroup)
                    backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)

//                    val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}_RemoveConfig"
//                    val jobNameRemoveConfig = "JobName_backupConfig_${configInput.id}_RemoveConfig"
//                    backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

                    notifyAgent("null", "null", resource.resourceId!!, configEntity.id!!, "REMOVE_CONFIG")
                }
            }
        }
    }









/*    fun updateBackupConfigSchedule(configInput: Pojo.Config, configEntity: BackupConfigEntity, enabled: Boolean, operation: Pojo.ConfigOperation): Boolean {
        println("came inside updateBackupConfigSchedule for $operation")
        val scheduleType = configInput.scheduleDetails.scheduleType
        val scheduleInterval = configInput.scheduleDetails.scheduleInterval

        val configId = configEntity.id!!

        val pathDetailsInput = if (configInput.backupAll) {
            "/"
        } else {
            Gson().toJson(configInput.pathDetails)
        }


        if (scheduleType == Pojo.BackupConfigScheduleType.DAYS) {
            // convert days to hours
            //scheduleInterval *= 24
        }

        val jobName = "JobName_backupConfig_${configId}"
        val triggerName = "TriggerName_backupConfig_${configId}"

        if (configInput.resourceId != null) {

            println("came inside for resourceLevel ${configInput.resourceId}")

            val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
            val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"

            //deleting by checking done commonly because this method deals only with config related  flow
            // if(operation == Pojo.ConfigOperation.UPDATE || operation == Pojo.ConfigOperation.DELETE){
            //}

            if (operation == Pojo.ConfigOperation.ADD) {
                val resourceConfigmapping = ResourceIdConfigIdMapping()
                resourceConfigmapping.configId = configId
                resourceConfigmapping.resourceId = configInput.resourceId
                resourceIdConfigIdMappingRepo.save(resourceConfigmapping)
            } else if (operation == Pojo.ConfigOperation.DELETE) {
                resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(configInput.resourceId, configId)
            }


            schedulerService.deleteBackupJobSchedule(jobName, jobGroup,operation)


            if ((operation == Pojo.ConfigOperation.ADD || operation == Pojo.ConfigOperation.UPDATE) && enabled) {

                    schedulerService.scheduleBackupJob(
                            jobName, jobGroup, triggerName, triggerGroup, scheduleInterval, pathDetailsInput, configInput.resourceId, configId
                    )


               // backupConfigRedisService.updateBackupConfigCache(jobGroup,jobName,pathDetailsInput)

            }




            if ((operation == Pojo.ConfigOperation.ADD || operation == Pojo.ConfigOperation.UPDATE) && enabled) {
                if (operation == Pojo.ConfigOperation.UPDATE && configEntity.pathDetails != null && configEntity.pathDetails != pathDetailsInput) {

                    *//*

                old : include , exclude
                        *  remove the path from watcher
                        *  add ""  ""  "'    "'   "'


                  *//*

                    val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
                    val jobNameRemoveConfig = "JobName_backupConfig_${configId}_RemoveConfig"
                    backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)

                    // TODO  hit lambda api and notify agent with action somthing like  "REMOVE_BACKUP_CONFIG"

                    *//*      * new : include , exclude
                            * add
                            * remove
                  *//*

//                val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
//                val jobName = "JobName_backupConfig_${configId}"
                  backupConfigRedisService.updateBackupConfigCache(jobGroup, jobName, pathDetailsInput)

                  // TODO hit lambda api and notify agent with action somthing like  "UPDATE_BACKUP_CONFIG"



                }

            }else {

                if(operation == Pojo.ConfigOperation.DELETE) {
                    backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)
                }

                // config is deleted or disabled


                val jobGroupRemoveConfig = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}_RemoveConfig"
                val jobNameRemoveConfig = "JobName_backupConfig_${configId}_RemoveConfig"
                backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, configEntity.pathDetails!!)
                // hit lambda api and notify agent with action somthing like  "REMOVE_BACKUP_CONFIG"

            }


        }
        else if (configInput.companyId != null) {

            println("came inside for clientLevel ${configInput.companyId}")

            val resourcesList = resourceRepo.findAllByClientId(configInput.companyId)
            if (!resourcesList.isNullOrEmpty()) {
                resourcesList.forEach { resource ->

                    println("came insode for resource : ${resource.resourceId}")

                    val jobGroup = "JobGroup_client:${configInput.companyId}_resource:${resource.resourceId}"
                    val triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${resource.resourceId}"


                    if (operation == Pojo.ConfigOperation.ADD) {
                        val resourceConfigmapping = ResourceIdConfigIdMapping()
                        resourceConfigmapping.configId = configId
                        resourceConfigmapping.resourceId = resource.resourceId
                        resourceIdConfigIdMappingRepo.save(resourceConfigmapping)
                    } else if (operation == Pojo.ConfigOperation.DELETE) {

                        println("DeleteMapping for ${resource.resourceId!!}, $configId")
                        resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, configId)
                    }

                    // TODO NEED TO IMPLEMENT PULSAR FOR BACKGROUND TASK
                    //  if(operation == Pojo.ConfigOperation.UPDATE || operation == Pojo.ConfigOperation.DELETE){
                    schedulerService.deleteBackupJobSchedule(jobName, jobGroup, operation)
                    //  }
                    if ((operation == Pojo.ConfigOperation.ADD || operation == Pojo.ConfigOperation.UPDATE) && enabled) {
                        schedulerService.scheduleBackupJob(
                                jobName, jobGroup, triggerName, triggerGroup, scheduleInterval, pathDetailsInput, resource.resourceId!!, configId
                        )

                        backupConfigRedisService.updateBackupConfigCache(jobGroup,jobName,pathDetailsInput)

                    }

                    //send to lambda with this :  operation ,resourceId , pathConfig - so that agent will reBuild watcher


                }
            }
        }
        return true
        // TODO need to return failure list
    }*/









    fun deleteCustomerBackupConfigScheduleOnResource(resource: ResourceEntity, clientLevelConfig: BackupConfigEntity): Boolean {
        println("came inside updateBackupConfigScheduleOnResourceDelete ")


        val jobName = "JobName_backupConfig_${clientLevelConfig.id}"

        val jobGroup = "JobGroup_backupConfig:${clientLevelConfig.id}_client:${clientLevelConfig.companyId}_resource:${resource.resourceId}"

        schedulerService.deleteBackupJobSchedule(jobName, jobGroup)


        backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)


//        val jobGroupRemoveConfig = "JobGroup_client:${clientLevelConfig.companyId}_resource:${resource.resourceId}_RemoveConfig"
//        val jobNameRemoveConfig = "JobName_backupConfig_${clientLevelConfig.id}_RemoveConfig"
//        backupConfigRedisService.updateBackupConfigCache(jobGroupRemoveConfig, jobNameRemoveConfig, clientLevelConfig.pathDetails!!)


      notifyAgent("null","null",resource.resourceId!!,clientLevelConfig.id!!,"REMOVE_CONFIG")


        return true
        //need to return failure list
    }


    fun updateBackupConfigScheduleOnResourceUpdate(resource: ResourceEntity, clientLevelConfig: BackupConfigEntity, operation: Pojo.ConfigOperation): Boolean {
        println("came inside updateBackupConfigScheduleOnResourceUpdate - for clientLevelConfigId:  ${clientLevelConfig.id} ")
        val scheduleDetails = Gson().fromJson(clientLevelConfig.scheduleDetails, Pojo.BackupConfigScheduleDetails::class.java)
        val scheduleType = scheduleDetails.scheduleType
        var scheduleInterval = scheduleDetails.scheduleInterval

        val pathDetailsString = if (!clientLevelConfig.backupAll!!) {
            "/"
        } else {
            Gson().toJson(clientLevelConfig.pathDetails)
        }

        if (scheduleType == Pojo.BackupConfigScheduleType.DAYS) {
            // convert days to hours
            //scheduleInterval *= 24
        }

        val jobName = "JobName_backupConfig_${clientLevelConfig.id}"
        val triggerName = "TriggerName_backupConfig_${clientLevelConfig.id}"



        if ((operation == Pojo.ConfigOperation.ADD || operation == Pojo.ConfigOperation.UPDATE) ) {
            val newJobGroup = "JobGroup_backupConfig:${clientLevelConfig.id}_client:${clientLevelConfig.companyId}_resource:${resource.resourceId}"
            val newTriggerGroup = "TriggerGroup_backupConfig:${clientLevelConfig.id}_client:${clientLevelConfig.companyId}_resource:${resource.resourceId}"

            schedulerService.scheduleBackupJob(
                    jobName, newJobGroup, triggerName, newTriggerGroup, scheduleInterval, pathDetailsString, resource.resourceId!!, clientLevelConfig.id!!
            )

            backupConfigRedisService.updateBackupConfigCache(newJobGroup,jobName,pathDetailsString)


            //notifyAgent(jobName,newJobGroup,resource.resourceId!!,clientLevelConfig.id!!,"ADD_CONFIG",pathDetailsString)


        }

        return true
        //  need to return failure list
    }



    fun notifyAgent(jobName: String, jobGroup: String, resourceId: Long, configId: Long, action: String,pathDetailsInput:String?=null) {

        val lambdaUrl = "https://xxxxxxx.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

        val httpPost = HttpPost(lambdaUrl)
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

        val requestData = JsonObject()

        requestData.addProperty("jobName", jobName)
        requestData.addProperty("jobGroup", jobGroup)
        requestData.addProperty("resourceId", resourceId.toString())
        requestData.addProperty("configId", configId.toString())
        requestData.addProperty("action", action)
        requestData.addProperty("paths",pathDetailsInput )



        httpPost.entity = StringEntity(requestData.toString(), ContentType.APPLICATION_JSON)

        HttpClients.createDefault().use { httpClient ->
            httpClient.execute(httpPost).use { response ->
                val statusCode = response.statusLine.statusCode
                val responseEntity = response.entity
                val responseJsonStr = EntityUtils.toString(responseEntity)
                println("LAMBDA RESPONSE CAME for action : $action WITH  statusCOde : $statusCode , res String : $responseJsonStr")
            }
        }

    }

}