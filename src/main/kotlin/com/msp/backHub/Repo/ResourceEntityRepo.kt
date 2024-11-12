package com.msp.backHub.Repo

import com.msp.backHub.entity.ResourceEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResourceEntityRepo: JpaRepository<ResourceEntity, Long>  {

    fun findAllByClientId(clientId:Long):List<ResourceEntity>?
}