package com.msp.backHub.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class ResourceEntity {

    @Id
    var resourceId:Long? = null

    var name:String? = null

    var clientId:Long? = null

    var deleted:Boolean?=null
}