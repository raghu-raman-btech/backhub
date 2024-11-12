package com.msp.backHub.controller



import com.google.gson.Gson
import com.google.gson.JsonObject
import com.msp.backHub.Repo.CompanyRepository
import com.msp.backHub.Repo.SyncConfigRepo
import com.msp.backHub.entity.Policy
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
import org.springframework.web.multipart.MultipartFile;
import java.io.File
import java.io.IOException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestingController() {

    @Autowired
    private lateinit var schedulerService: SchedulerService
    @Autowired
    private lateinit var redisTest: redisTest
    @Autowired
    private lateinit var jpaTest: JpaTest
    @Autowired
    private lateinit var s3Service: S3ServiceTest
    @Autowired
    private lateinit var companyRepo: CompanyRepository
    @Autowired
    private lateinit var syncConfigRedisService: SyncConfigRedisService

    @Autowired
    private lateinit var syncConfigRepo: SyncConfigRepo

    // GENERAL
    @PostMapping("/b")
    fun test(): String {
        println("contrioller called")
        return "controller called"
    }



    // REDIS


    @PostMapping("/redisCommandsTestset")
    fun rediscommandsTestset(): String {
        println("redisCommandsTest called")
        return redisTest.setabc("RR_WON4", "THE Race_4")
    }


    @PostMapping("/redisCommandsTestget")
    fun rediscommandsTestget(): String {
        println("redisCommandsTest called")
        return redisTest.getabc("RR_WON4")
    }

    @PostMapping("/redisTemplateTestset")
    fun redisTemplateTestset(): Boolean {
        println("redisTemplateTestset called")
        return redisTest.setaaa()
    }


    @PostMapping("/redisTemplateTestget")
    fun redisTemplateTestget(): String? {
        println("redisTemplateTestget called")
        return redisTest.getaaa()
    }


    // SCHEDULAR

    @PostMapping("/schedule-mins")
    fun scheduleJobEveryMinutes(@RequestParam mins: Long): String {
        println("schedule-mins called")
        schedulerService.scheduleJobInMins(mins)
        return "Job scheduled to run every $mins mins. - log from COntroller"
    }

    @PostMapping("/schedule-secs")
    fun scheduleJobEverySeconds(@RequestParam secs: Long): String {
        println("contrioller called")
        schedulerService.scheduleJobInSecs(secs)
        return "Job scheduled to run every $secs Secs. - log from COntroller"
    }


    @PostMapping("/delSch")
    fun scheduleJobEverySeconds(@RequestParam jobKey: String, @RequestParam jobGroup: String): Boolean {
        println("delSch called")
        schedulerService.delJob(jobKey, jobGroup)
        return true
    }


    @PostMapping("/delAllSch")
    fun delAllSch(@RequestParam jobKey: String, @RequestParam jobGroup: String): Boolean {
        println("delAll Sch called")


        schedulerService.delJob(jobKey, jobGroup)
        return true
    }


    // JPA DA

    @PostMapping("/jpaSave")
    fun jpaSave() {
        jpaTest.jpaSaveTest()
        println("jpa save success")
    }


    @PostMapping("/jpaGet")
    fun jpaGet() {
        val a = jpaTest.jpaGetTEst()
        a.forEach {
            println("jpa getall id :  ${it.id} ,name :  ${it.name}")
        }


    }


    // Gson Test
    @PostMapping("/gson")
    fun gsonSet(@RequestBody jsonBody: JsInput): String {
        val jsonString = Gson().toJson(jsonBody)

        println("JSONSTRING : $jsonString")

        jpaTest.jsonSave(jsonString)

        val jsO = Gson().fromJson(jsonString, JsInput::class.java)

        println("jso : ${jsO.k1}")

        jpaTest.jsonGet()

        return jsonString
    }

    data class JsInput(
            val k1: Map<String, Any>?,
            val k2: Map<String, Any>?
    )



    data class TestGetUrl(
            val path: String,
            val companyId:String,
            val resourceId:String
    )



    @PostMapping("/geturl")
    fun geturl(
            @RequestBody reqBody: TestGetUrl
    ): String {
        println("gets3url hit")

        val resourceId = reqBody.resourceId
        val companyId  = reqBody.companyId

        try {

            val path = "$resourceId/${reqBody.path}"


            val companyName = companyRepo.findById(companyId.toLong()).get().name!!

            val bucketName = "$companyName-$companyId"

            println("Path: ${reqBody.path}, CompanyId: ${reqBody.companyId}, ResourceId: ${reqBody.resourceId}")


            val generatePresignedUrlRequest = s3Service.gets3UrlTest(bucketName,path)

            return generatePresignedUrlRequest.toString()


        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Pre-signed URL not generated"
    }

    data class GetInstallerLink(
            val companyId:String
    )

//    @PostMapping("/getInstallerLink")
//    fun getInstallerLink(
//          @RequestParam companyId: String
//    ): String {
//
//
//
//        val companyName = companyRepo.findById(companyId.toLong()).get().name!!
//
//        val bucketName = "$companyName-$companyId"
//
//        val macInstallerInstallerName = "backHubForMac"
//        val macInstallerForClient = "installer/${macInstallerInstallerName}_${companyId}"
//
//
//        val generatePresignedUrlRequest = s3Service.gets3UrlTest(bucketName,macInstallerForClient)
//
//        return generatePresignedUrlRequest.toString()
//
//
//    }






}



@RestController
@RequestMapping("/getInstallerLink")
class Installer(
) {
    @Autowired
    private lateinit var s3Service: S3ServiceTest
    @Autowired
    private lateinit var companyRepo: CompanyRepository


    @PostMapping("/")
    @CheckCompanyAccess
    fun getInstallerLink(
            @RequestParam companyId: String
    ): String {

        val companyName = companyRepo.findById(companyId.toLong()).get().name!!

        val bucketName = "$companyName-$companyId"

        val macInstallerInstallerName = "backHubForMac"
        val macInstallerForClient = "installer/${macInstallerInstallerName}_${companyId}"


        val generatePresignedUrlRequest = s3Service.gets3UrlTest(bucketName,macInstallerForClient)

        return generatePresignedUrlRequest.toString()

    }
}






@RestController
@RequestMapping("/fileExp")
class FileExpController(
        private val bhWebappLambda: BackhubWebappLambdaService
) {
    @GetMapping("/{resourceId}/{dir}")
    @CheckCompanyAccess
    fun getFileExp(@PathVariable resourceId: String,@PathVariable dir: String): List<String> {
        return bhWebappLambda.getDirFiles(resourceId,dir)
    }
}



@RestController
@RequestMapping("/policies")
class CompanyController(
        private val policyService: PolicyService
) {
    @GetMapping("/{companyId}")
    @CheckCompanyAccess
    fun getPoliciesForCompany(@PathVariable companyId: Long): List<Policy>? {
        return policyService.getPoliciesForCompany(companyId)
    }
}
