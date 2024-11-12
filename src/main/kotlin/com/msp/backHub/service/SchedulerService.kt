package com.msp.backHub.service

import com.msp.backHub.jobExecution.ScheduleJobProcess
import com.msp.backHub.jobExecution.SyncScheduleJobProcess
import jakarta.annotation.PostConstruct
import org.quartz.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SchedulerService @Autowired constructor(private val scheduler: Scheduler) {


    @Autowired
    private lateinit var backupConfigRedisService: BackupConfigRedisService

    @PostConstruct
    fun init() {
        try {
            println("Scheduler context: ${scheduler.context}")
            scheduler.start()
        } catch (e: SchedulerException) {
            throw RuntimeException("Failed to initialize or start the scheduler", e)
        }
    }

    // TESTING

    fun scheduleJobInMins(min: Long) {
        val jobDetail: JobDetail = JobBuilder.newJob(ScheduleJobProcess::class.java)
                .withIdentity("myJobpaths2", "group1paths")
                 .usingJobData("paths","listOfPaths")
                .build()

        val trigger: Trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTriggerpaths2", "group1paths")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(min.toInt())
                        .repeatForever())
                .build()

        try {
            scheduler.scheduleJob(jobDetail, trigger)
            println("Job scheduled to run every $min minutes.")
        } catch (e: SchedulerException) {
            e.printStackTrace()
        }
    }

    fun scheduleJobInSecs(secs: Long) {
        val jobDetail: JobDetail = JobBuilder.newJob(ScheduleJobProcess::class.java)
                .withIdentity("myJobSecs", "group1secs")
                .build()

        val trigger: Trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTriggersecs", "group1secs")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(secs.toInt())
                        .repeatForever())
                .build()

        try {

            scheduler.scheduleJob(jobDetail, trigger)
            println("Job scheduled to run every $secs Secs.")
        } catch (e: SchedulerException) {
            e.printStackTrace()
        }

    }

    fun delJob(jobName:String,jobGroup:String){
        val jobKey = JobKey.jobKey(jobName, jobGroup)
        if(scheduler.checkExists(jobKey)) {
            println("came inside exists")
            scheduler.deleteJob(jobKey)
        }
    }




    fun scheduleBackupJob(jobName: String, jobGroup: String,
                          triggerName: String, triggerGroup: String,
                          intervalInHours: Int, pathDetails: String, resourceId: Long, configId: Long): Boolean {

        val jobDetail: JobDetail = JobBuilder.newJob(ScheduleJobProcess::class.java)
                .withIdentity(jobName, jobGroup)
                .usingJobData("pathDetails", pathDetails)
                .usingJobData("resourceId", resourceId)
                .usingJobData("configId", configId)
                .build()

        // since there is no aws cache from webApp in localhost , else , i will take it from redis ,
        // and update/remove paths from watcher instead of  REMOVE AND ADD a new one

        val trigger: Trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(intervalInHours)
                        .repeatForever())
                .build()

        return try {
            scheduler.scheduleJob(jobDetail, trigger)
            println("Backup Job $jobGroup scheduled to run every $intervalInHours hours.")
            true
        } catch (e: SchedulerException) {
            e.printStackTrace()
            false
        }
    }



    fun scheduleSyncJob(jobName: String, jobGroup: String,
                        triggerName: String, triggerGroup: String,
                        pathDetails: String, resourceId: Long, configId: Long, destinationResourcesString: String):Boolean{

        val jobDetail: JobDetail = JobBuilder.newJob(SyncScheduleJobProcess::class.java)
                .withIdentity(jobName, jobGroup)
                .usingJobData("resourceId",resourceId)
                .usingJobData("configId",configId)


                // since there is no aws cache from webApp in localhost , else , i will take it from redis ,
                // and update/remove paths from watcher instead of  REMOVE AND ADD a new one

                .usingJobData("pathDetails",pathDetails)
                .usingJobData("destinationResources",destinationResourcesString)

                .build()

        val trigger: Trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(30)
                        .repeatForever())
                .build()

        return try {
            scheduler.scheduleJob(jobDetail, trigger)
            println("Sync Job $jobGroup scheduled to run every 5 mins.")
            true
        } catch (e: SchedulerException) {
            e.printStackTrace()
            false
        }
    }




    fun deleteBackupJobSchedule(jobName: String, jobGroup: String) {
        val jobKey = JobKey.jobKey(jobName, jobGroup)
        println("deleting schedule  $jobName , $jobGroup")
        if (scheduler.checkExists(jobKey)) {
            //println("delete sehedule insude")
            scheduler.deleteJob(jobKey)
//            if(operation == Pojo.ConfigOperation.DELETE) {
//                backupConfigRedisService.deleteBackupConfigCache(jobGroup, jobName)
//            }
        }
    }


}