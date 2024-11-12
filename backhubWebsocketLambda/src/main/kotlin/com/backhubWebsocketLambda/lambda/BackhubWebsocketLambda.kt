package com.backhubWebsocketLambda.lambda


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.backhubWebsocketLambda.service.BackupWsLambdaService
import com.backhubWebsocketLambda.service.RedisService
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.nio.charset.StandardCharsets


class BackhubWebsocketLambda : RequestStreamHandler {
//    val redisHost = System.getenv("app_redis_host")
//    val redisPort = System.getenv("app_redis_port").toInt()
//    val a  = RedisURI.Builder.redis(redisHost,redisPort).build()

    lateinit var backupWsLambdaService: BackupWsLambdaService
    lateinit var redisService: RedisService

    init {
        backupWsLambdaService = BackupWsLambdaService()
        redisService = RedisService()
    }


    override fun handleRequest(input: InputStream?, output: OutputStream?, context: Context?) {
        val response = JsonObject()
        response.addProperty("statusCode", 200)

        val requestJson = JsonParser.parseString(IOUtils.toString(input, StandardCharsets.UTF_8.name())).asJsonObject
        val reqestContext = requestJson.getAsJsonObject("requestContext")
        val headerJson = requestJson.get("headers")?.asJsonObject
        val queryStringParameters = requestJson.get("queryStringParameters")?.asJsonObject


        var httpReq: Boolean = false
        if (requestJson.has("httpMethod") ||
                reqestContext.has("http")
        ) {
            httpReq = true
        }

        println("new requestJson : $requestJson , httpReq:$httpReq")

        val ctxDomain = System.getenv("domainName")
        if (!httpReq) {
            val connId = reqestContext.get("connectionId").asString
            when (reqestContext.get("routeKey").asString) {
                "\$connect" -> {
                    println("route inside connect ")
                    redisService.handleConnIdCache(headerJson, connId, queryStringParameters)
                    println("done inside connect, res : $response")
                }

                "pingPong" -> {
                    println("route inside pingPong ")

                    val pingPongJson = JsonObject()
                    pingPongJson.addProperty("action", "PingPongResponse")

                    val messageString = Gson().toJson(pingPongJson)

                    backupWsLambdaService.speakWithConnection(connId, messageString, ctxDomain)
                }

                "speakWithResource" -> {
                    try {
                        val body = requestJson.get("body").asString
                        val data = JsonParser.parseString(body).asJsonObject

                        val resourceId = data!!.get("Resourceid").asString
                        val resourceConnId = redisService.getResourceConnId(resourceId)
                        println("speakWithResource.resourceConnId : $resourceConnId")
                        backupWsLambdaService.speakWithConnection(resourceConnId, body, ctxDomain)
                    } catch (ex: Exception) {
                        println("speakWithResource.speakWithConnection went wrong : ${ex.printStackTrace()}")
                    }
                }

                "speakWithClient" -> {
                    try {
                        val body = requestJson.get("body").asString
                        val data = JsonParser.parseString(body).asJsonObject

                        val clientToken = data?.get("clientToken")?.asString
                        val clientConnId = redisService.getClientConnId(clientToken!!)
                        println("speakWithClient.clientConnId : $clientConnId")
                        backupWsLambdaService.speakWithConnection(clientConnId, body, ctxDomain)
                    } catch (ex: Exception) {
                        println("speakWithClient.speakWithConnection went wrong : ${ex.printStackTrace()}")
                    }
                }

                "backupPathsByWatcher" -> {
                    try {
                        val body = requestJson.get("body").asString
                        val data = JsonParser.parseString(body).asJsonObject

                        val resourceId = data.get("resourceId").asString
                        val configId = data.get("configId").asString
                        val path = data.get("path").asString

                        redisService.updateBackupPathsByWatcher(resourceId, configId, path)

                    } catch (ex: Exception) {
                        println("updateBackupPathsByWatcher went wrong : ${ex.printStackTrace()}")
                    }
                }

                "syncPathsByWatcher" -> {
                    try {
                        val body = requestJson.get("body").asString
                        val data = JsonParser.parseString(body).asJsonObject

                        val resourceId = data.get("resourceId").asString
                        val configId = data.get("configId").asString
                        val path = data.get("path").asString

                        redisService.updateSyncPathsByWatcher(resourceId, configId, path)

                    } catch (ex: Exception) {
                        println("updateBackupPathsByWatcher went wrong : ${ex.printStackTrace()}")
                    }
                }


            }
        } else {

            val body = requestJson.get("body").asString

            val bodyJson = JsonParser.parseString(body).asJsonObject

            val action = bodyJson.get("action").asString


            when (action) {
                // working as : postMan . path as list at 1-2 am - 16Sept
//                "ADD_CONFIG" -> {
//
//                    val jobName = bodyJson.get("jobName").asString
//                    val jobGroup = bodyJson.get("jobGroup").asString
//                    val resourceId = bodyJson.get("resourceId").asString
//                    val configId = bodyJson.get("configId").asString
//
//
//                    // TODO : ONCE REDIS CONNECTION IS READY , GET FROM CACHE with jobGroup , jobName
//                    val paths = bodyJson.get("paths").asJsonArray
//                   // val paths = redisService.pathsForJobKey(jobGroup,jobName)
//
//                    val resourceConnId = redisService.getResourceConnId(resourceId)
//
//                    bodyJson.add("pathsArr",paths)
//
//                    val messageString = Gson().toJson(bodyJson)
//
//
//                    backupWsLambdaService.speakWithConnection(resourceConnId, messageString,ctxDomain)
//
//                }


                "ADD_CONFIG" -> {

                    val jobName = bodyJson.get("jobName").asString
                    val jobGroup = bodyJson.get("jobGroup").asString
                    val resourceId = bodyJson.get("resourceId").asString
                    val configId = bodyJson.get("configId").asString

                    // TODO : ONCE REDIS CONNECTION IS READY , GET FROM CACHE with jobGroup , jobName
                    val paths = bodyJson.get("paths").asString
                    // val paths = redisService.pathsForJobKey(jobGroup,jobName)


//                 val jsonArray: JsonArray = Gson().fromJson(paths, JsonArray::class.java)
//
                    bodyJson.addProperty("pathsArr", paths)

                    val resourceConnId = redisService.getResourceConnId(resourceId)

                    val messageString = Gson().toJson(bodyJson)

                    backupWsLambdaService.speakWithConnection(resourceConnId, messageString, ctxDomain)

                    response.addProperty("body", "$action - sent to agent")

                }


                "ADD_SYNC_CONFIG" -> {

                    val jobName = bodyJson.get("jobName").asString
                    val jobGroup = bodyJson.get("jobGroup").asString
                    val resourceId = bodyJson.get("resourceId").asString
                    val configId = bodyJson.get("configId").asString

                    // TODO : ONCE REDIS CONNECTION IS READY , GET FROM CACHE with jobGroup , jobName
                    val paths = bodyJson.get("paths").asString
                    // val paths = redisService.pathsForJobKey(jobGroup,jobName)


//                 val jsonArray: JsonArray = Gson().fromJson(paths, JsonArray::class.java)
//
                    bodyJson.addProperty("pathsArr", paths)

                    val resourceConnId = redisService.getResourceConnId(resourceId)

                    val messageString = Gson().toJson(bodyJson)

                    backupWsLambdaService.speakWithConnection(resourceConnId, messageString, ctxDomain)

                    response.addProperty("body", "$action - sent to agent")

                }


                "REMOVE_CONFIG" -> {
                    // need to update zip after lunch
                    val resourceId = bodyJson.get("resourceId").asString
                    val resourceConnId = redisService.getResourceConnId(resourceId)
                    backupWsLambdaService.speakWithConnection(resourceConnId, body, ctxDomain)

                    response.addProperty("body", "$action - sent to agent")
                }

                "REMOVE_SYNC_CONFIG" -> {
                    // need to update zip after lunch
                    val resourceId = bodyJson.get("resourceId").asString
                    val resourceConnId = redisService.getResourceConnId(resourceId)
                    backupWsLambdaService.speakWithConnection(resourceConnId, body, ctxDomain)

                    response.addProperty("body", "$action - sent to agent")
                }


//                "PATHS_REMOVE" -> {
//
//                }
//
//                "PATHS_UPDATE" -> {
//
//                }

                "BACKUP" -> {
                    val resourceId = bodyJson.get("resourceId").asString
                    val configId = bodyJson.get("configId").asString

                    var pathsJsonArray: JsonArray? = null
                    var configPathArray: JsonArray? = null


                    //  val lastBackupTime = redisService.getLastBackupTime(resourceId, configId)

                    //SINCE , no cache connection yet from webApp ,
                    // (commented as i used updateLastBackupTime)
                    // lastBackupTime = ""

                    //commenting if part because , on agent init ,  pushing all paths if no lastBackupTime !
//                    if (lastBackupTime == null) {
//                        val jobName = bodyJson.get("jobName").asString
//                        val jobGroup = bodyJson.get("jobGroup").asString
//
//                          //  val configPathStrings = redisService.pathsForJobKey(jobGroup, jobName)
//                         //  configPathArray = Gson().toJsonTree(configPathStrings).asJsonArray
//                        // TODO : ONCE REDIS CONNECTION IS READY , GET FROM CACHE with jobGroup , jobName
//                        val paths = bodyJson.get("paths").asString
//                        println("all paths to backup : $paths")
//
//
//                        val jsonArray: JsonArray = JsonParser.parseString(paths).asJsonArray
//                        println("jsonArray to backup: $jsonArray")
//
//                        val pathsList: List<String>? = Gson().fromJson(jsonArray, List::class.java) as List<String>?
//
//                        configPathArray= JsonArray()
//                        if (pathsList != null) {
//                            for (path in pathsList) {
//                                configPathArray.add(path)
//                            }
//                        }else {
//                            return
//                        }
//
//                    //    configPathArray = Gson().toJsonTree(paths).asJsonArray
//
//
//                    } else {
                    val paths = redisService.getBackupPathsByWatcher(resourceId, configId)

                    if (paths != null) {
                        pathsJsonArray = JsonArray()
                        for (path in paths) {
                            pathsJsonArray.add(path)
                        }
                    } else {
                        return
                    }
//                    }

                    bodyJson.add("paths", pathsJsonArray)
                    val resourceConnId = redisService.getResourceConnId(resourceId)
                    val messageString = Gson().toJson(bodyJson)
                    backupWsLambdaService.speakWithConnection(resourceConnId, messageString, ctxDomain)


                    // since there is no redis connection from webapp , for lastBackupTime update after successful upload ,
                    // updating lastBackupTime from here irrespective of successful backup or failure backup .

                    redisService.updateLastBackupTime(resourceId, configId)
                    redisService.deleteBackupPathsByWatcher(resourceId, configId)


                    response.addProperty("body", "$action - sent to agent")
                }


                "SYNC" -> {
                    val resourceId = bodyJson.get("resourceId").asString
                    val configId = bodyJson.get("configId").asString

                    var pathsJsonArray: JsonArray? = null
                    var configPathArray: JsonArray? = null


                    // val lastBackupTime = redisService.getLastBackupTime(resourceId, configId)

                    //SINCE , no cache connection yet from webApp ,
                    // (commented as i used updateLastBackupTime)
                    // lastBackupTime = ""

                    //commenting if part because , on agent init ,  pushing all paths if no lastBackupTime !
//                    if (lastBackupTime == null) {
//                        val jobName = bodyJson.get("jobName").asString
//                        val jobGroup = bodyJson.get("jobGroup").asString
//
//                          //  val configPathStrings = redisService.pathsForJobKey(jobGroup, jobName)
//                         //  configPathArray = Gson().toJsonTree(configPathStrings).asJsonArray
//                        // TODO : ONCE REDIS CONNECTION IS READY , GET FROM CACHE with jobGroup , jobName
//                        val paths = bodyJson.get("paths").asString
//                        println("all paths to backup : $paths")
//
//
//                        val jsonArray: JsonArray = JsonParser.parseString(paths).asJsonArray
//                        println("jsonArray to backup: $jsonArray")
//
//                        val pathsList: List<String>? = Gson().fromJson(jsonArray, List::class.java) as List<String>?
//
//                        configPathArray= JsonArray()
//                        if (pathsList != null) {
//                            for (path in pathsList) {
//                                configPathArray.add(path)
//                            }
//                        }else {
//                            return
//                        }
//
//                    //    configPathArray = Gson().toJsonTree(paths).asJsonArray
//
//
//                    } else {
                    val paths = redisService.getSyncPathsByWatcher(resourceId, configId)

                    if (paths != null) {
                        pathsJsonArray = JsonArray()
                        for (path in paths) {
                            pathsJsonArray.add(path)
                        }
                    } else {
                        return
                    }
//                    }

                    bodyJson.add("paths", pathsJsonArray)

                    val resourceConnId = redisService.getResourceConnId(resourceId)
                    val messageString = Gson().toJson(bodyJson)
                    backupWsLambdaService.speakWithConnection(resourceConnId, messageString, ctxDomain)


                    // since there is no redis connection from webapp , for lastBackupTime update after successful upload ,
                    // updating lastBackupTime from here irrespective of successful backup or failure backup .

                    redisService.updateLastSyncTime(resourceId, configId)

                    redisService.deleteSyncPathsByWatcher(resourceId, configId)


                    response.addProperty("body", "$action - sent to agent")
                }


                "GET_LAST_BACKUP_TIME" -> {


                    val resourceId = bodyJson.get("resourceId").asString
                    val configId = bodyJson.get("configId").asString

                    val lastBackupTime = redisService.getLastBackupTime(resourceId, configId)

                    response.addProperty("body", lastBackupTime)
                }


                "GET_LAST_SYNC_TIME" -> {
                    val resourceId = bodyJson.get("resourceId").asString
                    val configId = bodyJson.get("configId").asString

                    val lastSyncTime = redisService.getLastSyncTime(resourceId, configId)

                    println("lastSyncTime for res : $resourceId , con:$configId : $lastSyncTime")

                    response.addProperty("body", lastSyncTime)
                }

                "DOWNLOAD_FOR_SYNC" -> {

                    // need to update zip after lunch
                    val resourceId = bodyJson.get("destinationResourceId").asString
                    val resourceConnId = redisService.getResourceConnId(resourceId)
                    backupWsLambdaService.speakWithConnection(resourceConnId, body, ctxDomain)

                    response.addProperty("body", "$action - sent to agent")
                }
            }
        }


        val writer = PrintWriter(output)
        writer.println(response.toString())
        writer.flush()

    }

}

