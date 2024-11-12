package com.msp.backHub.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.sync.RedisCommands
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConfiguration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory (): LettuceConnectionFactory{
        val redisConfig: RedisConfiguration =  RedisStandaloneConfiguration("localhost", 6379)
        return LettuceConnectionFactory(redisConfig)
    }


//    @Bean
//    fun redisConnectionFactory(): LettuceConnectionFactory {
//        // Replace with your AWS Redis endpoint and port
//        val redisConfig = RedisStandaloneConfiguration("backhubcache.tyybtl.ng.0001.apse2.cache.amazonaws.com", 6379)
//        return LettuceConnectionFactory(redisConfig)
//    }

//
    @Bean
    fun redisTemplate(): RedisTemplate<String, Any?> {
        val template = RedisTemplate<String, Any?>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        return template
    }

//    @Bean
//    fun redisTemplate(): RedisTemplate<String, Any> {
//        val template = RedisTemplate<String, Any>()
//        template.setConnectionFactory(redisConnectionFactory())
//        template.keySerializer = StringRedisSerializer()
//        template.valueSerializer = GenericJackson2JsonRedisSerializer()
//        return template
//    }



    @Bean
    fun redisCommands(): RedisCommands<String, String> {
        val redisURI = RedisURI.Builder.redis("localhost", 6379).build()
        val redisClient = RedisClient.create(redisURI)
        return redisClient.connect()!!.sync()
    }


//    @Bean
//    fun redisCommands(): RedisCommands<String, String> {
//        val redisURI = RedisURI.Builder.redis("backhubcache.tyybtl.ng.0001.apse2.cache.amazonaws.com", 6379)
//
//                .build()
//        val redisClient = RedisClient.create(redisURI)
//        return redisClient.connect().sync()
//    }



// in lambda
//    val redisURI = RedisURI.Builder.redis(redisHost, redisPort).build()
//    redisClient = RedisClient.create(redisURI)
//    synchronized(this){
//        connection = redisClient.connect()
//        syncCommands = connection.sync()
//    }


   // redis-cli -h backhubcache.tyybtl.ng.0001.apse2.cache.amazonaws.com -p 6379


}