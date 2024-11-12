package com.msp.backHub.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL

@Service
class S3ServiceTest @Autowired constructor(
        private val amazonS3: AmazonS3
) {

    private val BUCKET_NAME = "testbktrr" // Replace with your bucket name
    private val FOLDER_PREFIX = "subDir1/" // Replace with your folder path inside the bucket

    fun uploadFile(file: File, fileName: String) {
        val key = "$FOLDER_PREFIX$fileName" // Combine folder path and file name
        amazonS3.putObject(PutObjectRequest(BUCKET_NAME, key, file))
    }



    fun uploadFileV2(inputStream: InputStream, fileName: String, bucketName: String) {
        val putObjectRequest = PutObjectRequest(bucketName, fileName, inputStream, ObjectMetadata())
        amazonS3.putObject(putObjectRequest)
    }


    fun gets3UrlTest(bucketName: String, path: String): URL? {

        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(bucketName,path)

        val url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest)


        println(url)

        return url
    }


    fun createBucket(bucketName: String): Bucket {
        return amazonS3.createBucket(bucketName)
    }

    fun createFolder(resourceId:Long,bucketName:String){
        val folderKey = "$resourceId/"

        // Create an empty byte array input stream
        val emptyStream = ByteArrayInputStream(ByteArray(0))

        // Create a PutObjectRequest for the "folder"
        val putObjectRequest = PutObjectRequest(bucketName, folderKey, emptyStream, null)

        // Put the object (this simulates creating a folder)
        amazonS3.putObject(putObjectRequest)
    }



    fun copyInstallerForMac(macInstallerBucketName: String, macInstallerInstallerName: String, s3BucketName: String, macInstallerForClient: String) {
        amazonS3.copyObject(
                macInstallerBucketName,macInstallerInstallerName,s3BucketName,macInstallerForClient
        )
    }




}