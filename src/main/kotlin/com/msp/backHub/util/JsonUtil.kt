package com.msp.backHub.util

import com.google.gson.Gson

class JsonUtil {
    private val gson = Gson()

    fun <T> toJson(value: T): String {
        return gson.toJson(value)
    }

    fun <T> fromJson(json: String, valueType: Class<T>): T {
        return gson.fromJson(json, valueType)
    }
}

//
//package com.msp.lambda
//
//import com.amazonaws.services.lambda.runtime.Context
//import com.amazonaws.services.lambda.runtime.RequestStreamHandler
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import java.io.InputStream
//import java.io.OutputStream
//
//
//class WebsocketLambda :RequestStreamHandler {
//    override fun handleRequest(input: InputStream?, output: OutputStream?, context: Context?) {
//
//        val jsonString = input?.bufferedReader()?.use { it.readText() } ?: "{}"
//
//        // Convert String to JsonObject
//        val inputJson = Gson().fromJson(jsonString,JsonObject::class.java).asJsonObject
//
//
//        println("inputStr : $jsonString")
//
//        println("inputJson : $inputJson")
//
//    }
//
//
//}