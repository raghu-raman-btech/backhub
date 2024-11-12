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

class SyncScheduleJobProcess : Job {
    override fun execute(context: JobExecutionContext) {
        println()
        println("Schedule with -> jobGroup: ${context?.jobDetail?.key?.group} ,jobName :  ${context?.jobDetail?.key?.name}")

        val resourceId = context.mergedJobDataMap["resourceId"]
        val configId = context.mergedJobDataMap["configId"]

        //  val destinationResources =  context.mergedJobDataMap["destinationResources"].toString().split(",").map { it.toLong() }


        val lambdaUrl = "https://xxxxxx.execute-api.ap-southeast-2.amazonaws.com/default/MyOwn"

        val httpPost = HttpPost(lambdaUrl)
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)

        val requestData = JsonObject()



        requestData.addProperty("jobName", context.jobDetail?.key?.name!!)
        requestData.addProperty("jobGroup", context.jobDetail?.key?.group!!)
        requestData.addProperty("resourceId", resourceId.toString())
        requestData.addProperty("configId", configId.toString())
        requestData.addProperty("paths", context.mergedJobDataMap["pathDetails"].toString())
        requestData.addProperty("action", "SYNC")

//        requestData.addProperty("connId", webSocConnId)
//
//
        httpPost.entity = StringEntity(requestData.toString(), ContentType.APPLICATION_JSON)

        HttpClients.createDefault().use { httpClient ->
            httpClient.execute(httpPost).use { response ->
                val statusCode = response.statusLine.statusCode
                val responseEntity = response.entity
                val responseJsonStr = EntityUtils.toString(responseEntity)
                println("LAMBDA RESPONSE CAME for  WITH SYNC statusCOde : $statusCode , res String : $responseJsonStr")
            }
        }
    }
}