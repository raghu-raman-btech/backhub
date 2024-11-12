package com.msp.backHub.Repo

import com.msp.backHub.entity.BackupConfigEntity
import com.msp.backHub.entity.ResourceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface BackupConfigRepo : JpaRepository<BackupConfigEntity, Long> {


   // todo correct this
    // fun findAllByClientIdAndResourceIdIsNull(clientId:Long):List<BackupConfigEntity>?


    fun findByResourceId(resourceId:Long):List<BackupConfigEntity>?


    fun findByCompanyIdAndResourceId(companyId:Long,resourceId: Long?=null):List<BackupConfigEntity>?

    fun findByCompanyIdOrResourceId(companyId: Long, resourceId: Long): List<BackupConfigEntity>?



}