package com.backhubWebsocketLambda.service

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest
import com.google.gson.JsonObject
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import kotlin.random.Random

class BackupWsLambdaService {

    lateinit var client: AmazonApiGatewayManagementApi
//    lateinit var redisService:RedisService


//    init {
//        redisService = RedisService()
//    }


    fun speakWithConnection(connId: String, data: String, domain: String) {

        // To send messages to a connection , 
        // https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/apigatewaymanagementapi/model/PostToConnectionRequest.html
        // https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-how-to-call-websocket-api-connections.html
        // https://docs.aws.amazon.com/code-library/latest/ug/apigatewaymanagementapi_example_apigatewaymanagementapi_PostToConnection_section.html
        

        val region = System.getenv("region")
        val config: AwsClientBuilder.EndpointConfiguration = AwsClientBuilder.EndpointConfiguration(domain, region)
        client = AmazonApiGatewayManagementApiClientBuilder.standard()
                .withEndpointConfiguration(config)
                .build()

        val request = PostToConnectionRequest()
        request.withConnectionId(connId)
        var buff: ByteBuffer? = null
        try {
            buff = Charset.forName("UTF-8").newEncoder().encode(CharBuffer.wrap(data))
        } catch (ex: CharacterCodingException) {
            println(ex.printStackTrace())
        }
        request.withData(buff)
        val res = client.postToConnection(request)

        println("speak res : $res")
    }
}
