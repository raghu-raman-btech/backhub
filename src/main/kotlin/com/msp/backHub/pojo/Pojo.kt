package com.msp.backHub.pojo

class Pojo {

    data class Config(
            val id:Long? = null,
            val name: String? = null,
            val enable:Boolean = false,
            val companyId:Long? = null,
            val resourceId:Long? = null,
            val backupAll:Boolean = false,
            val scheduleDetails:BackupConfigScheduleDetails = BackupConfigScheduleDetails(),
            val pathDetails:BackupConfigPathDetails = BackupConfigPathDetails()
    )

    data class SyncConfig(
            val id:Long? = null,
            val name: String? = null,
            val enable:Boolean = false,
            val companyId:Long? = null,
            val resourceId:Long? = null,

            //val backupAll:Boolean = false,
            //val scheduleDetails:BackupConfigScheduleDetails = BackupConfigScheduleDetails(),

            val pathDetails:BackupConfigPathDetails = BackupConfigPathDetails(),

            val destinationResources:List<Long>?=null,

    )

    data class UpdateBackupConfigInput(
            val addConfig:Config? = null,
            val updateConfig:List<Config>? = null,
            val deleteConfig:Config? = null
    )

    data class UpdateSyncConfigInput(
            val addConfig:SyncConfig? = null,
            val updateConfig:List<SyncConfig>? = null,
            val deleteConfig:SyncConfig? = null
    )


    // BackupConfig Schedule Details
    data class BackupConfigScheduleDetails(
            val scheduleType: BackupConfigScheduleType = BackupConfigScheduleType.DAYS,
            val scheduleInterval: Int = 15
    )
    enum class BackupConfigScheduleType {
        HOURS, DAYS
    }

    enum class ConfigOperation{
        ADD,UPDATE,DELETE
    }

    //BackupConfig PathDetails Json
    data class BackupConfigPathDetails(
            val includePaths: List<String>?=null,
            val excludePaths: List<String>?=null
    )

    // Register resource
    data class RegisterResourceInput(
            val resourceId:Long?=null,
            val clientId:Long?=null,
            val name:String?=null
    )

    data class UpdateResourceInput(
            val resourceId:Long?=null,
            val name:String?=null,
            val clientId:Long?=null,
    )

    data class RemoveConfigForResource(
            val resourceId:Long?=null,
           val configId:Long?=null

    )

    data class DeRegisterResourceInput(
            val resourceId:Long?=null,
            val clientId:Long?=null,
    )

    data class GetConfigForResourceInput(
            val resourceId:Long?=null,
            val clientId:Long?=null,
    )




    // REGISTER COMPANY :
    data class RegisterCompanyRequestBody(
            val company: RegisterCompanyDetails,
            val user: List<RegisterCompanyUserDetails>
    )
    data class RegisterCompanyUserDetails(
            val userName:String?=null,
            val email:String?=null,
            val password:String?=null,
            val role:CompanyUserRole?=null
    )
    data class RegisterCompanyDetails(
            val companyName:String?=null,
            val subscriptionType:CompanySubscriptionType?=null
    )
    enum class CompanyUserRole{
        ADMIN , TECHNISION
    }
    enum class CompanySubscriptionType{
         TRIAL , ACTIVE
    }


    // JUST TO COPY PASTE
    data class Dammy(
            val k1: Map<String, Any>?,
            val k2: Map<String, Any>?
    )
}