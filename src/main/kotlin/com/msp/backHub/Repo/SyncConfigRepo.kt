package com.msp.backHub.Repo

import com.msp.backHub.entity.BackupConfigEntity
import com.msp.backHub.entity.SyncConfigEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SyncConfigRepo : JpaRepository<SyncConfigEntity, Long> {
    fun findByResourceId(resourceId:Long):List<SyncConfigEntity>?


    fun findByCompanyIdAndResourceId(companyId:Long,resourceId: Long?=null):List<SyncConfigEntity>?

    fun findByCompanyIdOrResourceId(companyId: Long, resourceId: Long): List<SyncConfigEntity>?
}