package com.msp.backHub.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SSSConfig {

    companion object {
        private const val ACCESS_KEY = "xxx"
        private const val SECRET_KEY = "yyy"
        private const val REGION = "ap-southeast-2"
    }

    @Bean
    fun amazonS3(): AmazonS3 {
        val awsCredentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
        return AmazonS3ClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
                .build()
    }
}
