package com.msp.backHub.jobExecution

import com.google.gson.JsonObject
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.util.*

class ScheduleJobProcess : Job {
    override fun execute(context: JobExecutionContext) {

        println("Schedule with -> jobGroup: ${context?.jobDetail?.key?.group} ,jobName :  ${context?.jobDetail?.key?.name}")
        val resourceId =  context.mergedJobDataMap["resourceId"]
        val configId = context.mergedJobDataMap["configId"]

        if(resourceId == null || configId == null){
            return
        }

/*
    If http , if action is "Backup" , then look for watcher data in cache . if cache is not there ,
    use jobKey to fetch pathDetails and send those data to agent for backup

*/

        val lambdaUrl = "https://xxxxxx.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

        val httpPost = HttpPost(lambdaUrl)
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

        val requestData = JsonObject()

        requestData.addProperty("jobName", context.jobDetail?.key?.name!!)
        requestData.addProperty("jobGroup", context.jobDetail?.key?.group!!)
        requestData.addProperty("resourceId", resourceId.toString())
        requestData.addProperty("configId", configId.toString())
        requestData.addProperty("paths",context.mergedJobDataMap["pathDetails"].toString())
        requestData.addProperty("action", "BACKUP")

//        requestData.addProperty("connId", webSocConnId)
//
//
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
}



















