package com.msp.backHub.entity

import jakarta.persistence.*

@Entity
data class ResourceIdConfigIdMapping(

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var resourceId: Long? = null,
        var configId: Long? = null,

        )




@Entity
data class ResourceIdSyncConfigIdMapping(

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var resourceId: Long? = null,
        var configId: Long? = null,

        )
