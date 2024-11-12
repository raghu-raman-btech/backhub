package com.msp.backHub.controller

import com.google.gson.JsonObject
import com.msp.backHub.Repo.CompanyRepository
import com.msp.backHub.Repo.SyncConfigRepo
import com.msp.backHub.pojo.Pojo
import com.msp.backHub.service.*
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException


@RestController
@RequestMapping("/resource")
class ResourceController {


    @Autowired
    private lateinit var s3Service: S3ServiceTest
    @Autowired
    private lateinit var companyRepo: CompanyRepository
    @Autowired
    private lateinit var syncConfigRedisService: SyncConfigRedisService

    @Autowired
    private lateinit var syncConfigRepo: SyncConfigRepo


    @Autowired
    private lateinit var  resourceService: ResourceService


    @PostMapping("/register")
    fun registerResource(@RequestBody registerResourceInput: Pojo.RegisterResourceInput): Boolean {
        resourceService.registerResource(registerResourceInput)
        return true
    }

    @PostMapping("/getConfigForResource")
    fun getConfigForResource(@RequestBody getSyncConfigForResourceBody: Pojo.GetConfigForResourceInput): MutableMap<String, Map<String, String?>> {
        return resourceService.getConfigForResource(getSyncConfigForResourceBody)
    }


    @PostMapping("/getSyncConfigForResource")
    fun getSyncConfigForResource(@RequestBody getSyncConfigForResourceBody: Pojo.GetConfigForResourceInput): MutableMap<String, Map<String, String?>> {
        return resourceService.getSyncConfigForResource(getSyncConfigForResourceBody)
    }


//    @PostMapping("/upload")
//    fun handleFileUpload(@RequestParam("file") file: MultipartFile): String {
//        println("upload hitted")
//        if (file.isEmpty) {
//            return "File is empty"
//        }
//
//        return try {
//            // Use the original filename or specify a custom name
//            val fileName = "default-filename"
//            val tempFile = File.createTempFile("upload-", fileName)
//            file.inputStream.use { input ->
//                tempFile.outputStream().use { output ->
//                    input.copyTo(output)
//                }
//            }
//            s3Service.uploadFile(tempFile, fileName)
//            tempFile.delete() // Clean up temporary file
//            "File uploaded successfully: $fileName"
//        } catch (e: IOException) {
//            e.printStackTrace()
//            "Failed to upload file"
//        }
//    }


    @PostMapping("/uploadv2ForSync")
    fun handleFileUploadv2ForSync(@RequestParam("file") file: MultipartFile,
                                  @RequestHeader(value = "companyId") companyId: String,
                                  @RequestHeader(value = "resourceId") resourceId: String,
                                  @RequestHeader(value = "syncConfigId") syncConfigId: String
    ): String {
        println("Upload hit")
        if (file.isEmpty) {
            return "File is empty"
        }

        return try {

            // Use the original filename or specify a custom name
            var fileName = file.originalFilename!!


            val path = "$resourceId/$fileName"


            val companyName = companyRepo.findById(companyId.toLong()).get().name!!

            val bucketName = "$companyName-$companyId"

            //Upload the file directly to S3
            s3Service.uploadFileV2(file.inputStream, path, bucketName)

            val urlUnit = s3Service.gets3UrlTest(bucketName, path)

            val uploadedFileName = urlUnit?.path?.substringAfterLast('/') // Gets the filename from the path


            val url = urlUnit.toString()


            val destinationResourcesListSrt = syncConfigRedisService.getSyncConfigDestinationResourcesCache(resourceId, syncConfigId)


            val destinationAssetsStrToList = destinationResourcesListSrt?.split(",")?.map { it }
            println(destinationAssetsStrToList)


            val configName = syncConfigRepo.findById(syncConfigId.toLong()).get().name


            destinationAssetsStrToList?.forEach { destinationResourcId ->

                val lambdaUrl = "https://xxxxxxx.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

                val httpPost = HttpPost(lambdaUrl)
                httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

                val requestData = JsonObject()

                requestData.addProperty("destinationResourceId", destinationResourcId)
                requestData.addProperty("syncConfigId", syncConfigId)
                requestData.addProperty("configName", "${configName!!}_SYNC_CONFIG")
                requestData.addProperty("url", url)
                requestData.addProperty("fileName", uploadedFileName)
                requestData.addProperty("action", "DOWNLOAD_FOR_SYNC")


                httpPost.entity = StringEntity(requestData.toString(), ContentType.APPLICATION_JSON)

                HttpClients.createDefault().use { httpClient ->
                    httpClient.execute(httpPost).use { response ->
                        val statusCode = response.statusLine.statusCode
                        val responseEntity = response.entity
                        val responseJsonStr = EntityUtils.toString(responseEntity)
                        println("LAMBDA RESPONSE CAME for  WITH BACKUP statusCOde : $statusCode , res String : $responseJsonStr")
                    }
                }
            }
            "File uploaded AND SENT TO SYNC to destination assets successfully: $fileName"
        } catch (e: IOException) {
            e.printStackTrace()
            "Failed to upload file"
        }
    }


    @PostMapping("/uploadv2")
    fun handleFileUploadv2(@RequestParam("file") file: MultipartFile,
                           @RequestHeader(value = "companyId") companyId: String,
                           @RequestHeader(value = "resourceId") resourceId: String): String {
        if (file.isEmpty) {
            return "File is empty"
        }

        return try {
            // Use the original filename or specify a custom name
            var fileName = file.originalFilename!!


            val path = "$resourceId/$fileName"

            val companyName = companyRepo.findById(companyId.toLong()).get().name!!

            val bucketName = "$companyName-$companyId"

            //Upload the file directly to S3
            s3Service.uploadFileV2(file.inputStream, path, bucketName)

            "File uploaded successfully: $fileName"
        } catch (e: IOException) {
            e.printStackTrace()
            "Failed to upload file"
        }
    }
}