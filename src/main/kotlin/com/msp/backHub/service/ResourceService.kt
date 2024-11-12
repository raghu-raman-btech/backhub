package com.msp.backHub.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.msp.backHub.Repo.*
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
class ResourceService {

    @Autowired
    private lateinit var resourceRepo: ResourceEntityRepo
    @Autowired
    private lateinit var clientRepo: CompanyRepository

    @Autowired
    private lateinit var backupConfigService: BackupConfigService
    @Autowired
    private lateinit var backupConfigRepo: BackupConfigRepo
    @Autowired
    private lateinit var syncConfigRepo: SyncConfigRepo
    @Autowired
    private lateinit var resourceIdConfigIdMappingRepo: ResourceIdConfigIdMappingRepo
    @Autowired
    private lateinit var s3Service: S3ServiceTest


    fun registerResource(registerResourceInput: Pojo.RegisterResourceInput) {
        val resource = ResourceEntity()
        resource.resourceId = registerResourceInput.resourceId!!
        resource.name = registerResourceInput.name
        resource.clientId = registerResourceInput.clientId!!


        val savedResource = resourceRepo.saveAndFlush(resource)


        val company = clientRepo.findById(resource.clientId!!).get()

        //println("cpy: ${company.name!!}")


        val companyBuckey = "${company.name!!}-${company.id!!}"

        println("cpyBkt : $companyBuckey")

        s3Service.createFolder(resource.resourceId!!,companyBuckey)


        // create schedules for newly registered resource
        postProcessUpdateResource(savedResource, null, Pojo.ConfigOperation.ADD)

    }


    fun updateResource(updateResourceInput: Pojo.UpdateResourceInput) {
        val resource = resourceRepo.findById(updateResourceInput.resourceId!!).get()
        val oldClientId = resource.clientId!!
        //resource.resourceId = Random.nextLong()
        resource.name = updateResourceInput.name

        //  resource.clientId = updateResourceInput.clientId

        resourceRepo.saveAndFlush(resource)

        // update schedules for resources client update

        // THIS IS BULL SHIT FOR THE CURRENT INTERNAL IT HIERARCHY .
//        if (updateResourceInput.clientId != null) {
//            postProcessUpdateResource(resource, oldClientId, Pojo.ConfigOperation.UPDATE)
//        }

    }



    fun removeConfigForResource(updateResourceInput: Pojo.RemoveConfigForResource) {

        val resource = resourceRepo.findById(updateResourceInput.resourceId!!).get()
        val config = backupConfigRepo.findById(updateResourceInput.configId!!).get()

        if(config.resourceId == null){
            throw RuntimeException("REsource level config cant be removed. it can only deleted or disabled")
        }

        backupConfigService.deleteCustomerBackupConfigScheduleOnResource(resource,config)
        resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, config.id!!)

    }


    fun getConfigForResource(updateConfigBody: Pojo.GetConfigForResourceInput): MutableMap<String, Map<String, String?>> {

//        val configIds = resourceIdConfigIdMappingRepo.findAllByResourceId(updateConfigBody.resourceId!!)?.map {
//            it.configId!!
//        }

         println("came inside getConfigForResource")

         val configs = backupConfigRepo.findByCompanyIdOrResourceId(updateConfigBody.clientId!!,updateConfigBody.resourceId!!)?.filter { it.enable }?.associateBy { it.id!! }


        val responseMap:MutableMap<String,Map<String,String?>> = mutableMapOf()
        // configID -> map(paths ,lastBackUp)




        configs?.forEach {(configId,config)  ->

            println("confiGId : $configId")

            val lambdaUrl = "https://apkuzwo7xe.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

            val httpPost = HttpPost(lambdaUrl)
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

            val requestData = JsonObject()

            requestData.addProperty("resourceId",updateConfigBody.resourceId )
            requestData.addProperty("configId", configId.toString())
            requestData.addProperty("action", "GET_LAST_BACKUP_TIME")




            httpPost.entity = StringEntity(requestData.toString(), ContentType.APPLICATION_JSON)

            HttpClients.createDefault().use { httpClient ->
                httpClient.execute(httpPost).use { response ->
                    val statusCode = response.statusLine.statusCode
                    val responseEntity = response.entity
                    val responseJsonStr = EntityUtils.toString(responseEntity)
                    println("LAMBDA RESPONSE CAME WITH  statusCOde : $statusCode , res String : $responseJsonStr")

                    val lastBackupTime = Gson().fromJson(responseJsonStr, String::class.java)

                    println("config : $configId has paths ${config.pathDetails} and lastSyncTime $lastBackupTime")


                    responseMap[configId.toString()] = mapOf("paths" to config.pathDetails , "lastSyncTime" to lastBackupTime )

                }
            }
        }
        println("returning map : $responseMap")
        println("*************************")
        return responseMap
    }




    fun getSyncConfigForResource(updateConfigBody: Pojo.GetConfigForResourceInput): MutableMap<String, Map<String, String?>> {

//        val configIds = resourceIdConfigIdMappingRepo.findAllByResourceId(updateConfigBody.resourceId!!)?.map {
//            it.configId!!
//        }

        println("came inside getConfigForResource")

        val configs = syncConfigRepo.findByCompanyIdOrResourceId(updateConfigBody.clientId!!,updateConfigBody.resourceId!!)?.filter { it.enable }?.associateBy { it.id }


        val responseMap:MutableMap<String,Map<String,String?>> = mutableMapOf()
        // configID -> map(paths ,lastBackUp)


        configs?.forEach {(configId,config)  ->

            println("confiGId : $configId")

            val lambdaUrl = "https://apkuzwo7xe.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

            val httpPost = HttpPost(lambdaUrl)
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

            val requestData = JsonObject()

            requestData.addProperty("resourceId",updateConfigBody.resourceId )
            requestData.addProperty("configId", configId.toString())
            requestData.addProperty("action", "GET_LAST_SYNC_TIME")




            httpPost.entity = StringEntity(requestData.toString(), ContentType.APPLICATION_JSON)

            HttpClients.createDefault().use { httpClient ->
                httpClient.execute(httpPost).use { response ->
                    val statusCode = response.statusLine.statusCode
                    val responseEntity = response.entity
                    val responseJsonStr = EntityUtils.toString(responseEntity)
                    println("LAMBDA RESPONSE CAME WITH  statusCOde : $statusCode , res String : $responseJsonStr")

                    val lastBackupTime = Gson().fromJson(responseJsonStr, String::class.java)

                    println("config : $configId has paths ${config.pathDetails} and lastSyncTime $lastBackupTime")


                    responseMap[configId.toString()] = mapOf("paths" to config.pathDetails , "lastSyncTime" to lastBackupTime )

                }
            }
        }
        println("returning map : $responseMap")
        println("*************************")
        return responseMap
    }






    fun deRegisterResource(deRegisterResourceInput: Pojo.DeRegisterResourceInput) {
        // todo delete multiple
        val resource = resourceRepo.findById(deRegisterResourceInput.resourceId!!).get()
        resource.deleted = true
        resourceRepo.saveAndFlush(resource)

        // delete schedules for resources
        postProcessUpdateResource(resource, null, Pojo.ConfigOperation.DELETE)


    }


//    private fun postProcessRegisterResource(resource: ResourceEntity) {
//        val clientLevelConfigs = backupConfigRepo.findAllByClientIdAndResourceIdIsNull(resource.clientId!!)
//        clientLevelConfigs?.forEach {config->
//            config.resourceId = resource.resourceId
//            backupConfigService.updateBackupConfigSchedule(config,config.id!!,Pojo.ConfigOperation.ADD)
//        }
//    }


//    private fun postProcessUpdateResource(resource: ResourceEntity, oldClientId: Long?, operation: Pojo.ConfigOperation) {
//        val resourceLevelConfigs = backupConfigRepo.findByResourceId(resource.resourceId!!)
//        resourceLevelConfigs?.forEach {config->
//            backupConfigService.updateBackupConfigSchedule(config,config.id!!,operation)
//        }
//    }


    private fun postProcessUpdateResource(resource: ResourceEntity, oldClientId: Long?, operation: Pojo.ConfigOperation) {

        if (operation == Pojo.ConfigOperation.ADD) {
            println("came inside postProcessUpdateResource add")
            val companyLevelConfigs = backupConfigRepo.findByCompanyIdAndResourceId(resource.clientId!!)?.filter { it.enable }
            companyLevelConfigs?.forEach { clientLevelConfig ->

                println("came inside clientLevelConfig add")


                backupConfigService.updateBackupConfigScheduleOnResourceUpdate(resource, clientLevelConfig, operation)


                // ADD MAPPING
                val resourceConfigMapping = ResourceIdConfigIdMapping()
                resourceConfigMapping.configId = clientLevelConfig.id
                resourceConfigMapping.resourceId = resource.resourceId
                resourceIdConfigIdMappingRepo.save(resourceConfigMapping)

            }


        }
//        else if (operation == Pojo.ConfigOperation.UPDATE) {
//
//            // THIS IS BULLSHIT FOR THE CURRENT INTERNAL IT HIERARCHY
//
//            println("came inside postProcessUpdateResource update")
//
//            val oldCompanyLevelConfigs = backupConfigRepo.findByCompanyIdAndResourceId(oldClientId!!)
//            oldCompanyLevelConfigs?.forEach { oldClientLevelConfig ->
//                backupConfigService.deleteCustomerBackupConfigScheduleOnResource(resource, oldClientLevelConfig)
//
//                // DELETE OLD MAPPING
//                resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, oldClientLevelConfig.id!!)
//
//            }
//
//
//            val newCompanyLevelConfigsToapply = backupConfigRepo.findByCompanyIdAndResourceId(resource.clientId!!)
//            newCompanyLevelConfigsToapply?.forEach { clientLevelConfig ->
//                if (clientLevelConfig.enable) {
//                    backupConfigService.updateBackupConfigScheduleOnResourceUpdate(resource, clientLevelConfig, operation)
//                }
//                // ADD MAPPING
//                val resourceConfigMapping = ResourceIdConfigIdMapping()
//                resourceConfigMapping.configId = clientLevelConfig.id
//                resourceConfigMapping.resourceId = resource.resourceId
//                resourceIdConfigIdMappingRepo.save(resourceConfigMapping)
//
//            }
//
//        }
        else {
            println("came inside postProcessUpdateResource delete")
            val companyLevelConfigs = backupConfigRepo.findByCompanyIdAndResourceId(resource.clientId!!)?.filter { it.enable }
            companyLevelConfigs?.forEach { clientLevelConfig ->
                backupConfigService.deleteCustomerBackupConfigScheduleOnResource(resource, clientLevelConfig)

                // DELETE  MAPPING
                resourceIdConfigIdMappingRepo.deleteByResourceIdAndConfigId(resource.resourceId!!, clientLevelConfig.id!!)

            }
        }
    }

}