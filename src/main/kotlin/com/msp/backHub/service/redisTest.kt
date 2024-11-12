package com.msp.backHub.service


import com.msp.backHub.config.RedisConfig
import io.lettuce.core.api.sync.RedisCommands
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class redisTest(@Autowired private val redisTemplate: RedisTemplate<String, Any?>) {

     private val aaa: HashOperations<String, String, String> = redisTemplate.opsForHash()

     private  var abc: RedisCommands<String, String>  = RedisConfig().redisCommands()


    // hget - k ,v , v in redisCommands

    // val a = abc.hget()



    fun setabc(key:String,value:String):String{

        return abc.set(key,value)
    }

    fun getabc(key:String): String {
        return abc.get(key).toString()
    }


    fun setaaa():Boolean{
        try{

            aaa.put("key4","hashKey4","hashValue4")

            val setted = aaa.get("key4","hashKey4")


            println("setted $setted")
        }catch (ex:Exception){
            println("setaaa fucked ${ex.stackTrace}")
        }
        return true

    }

    fun getaaa():String?{
       return aaa.get("key4","hashKey4")
    }


}