package com.msp.backHub.entity

import jakarta.persistence.*


@Entity
@Table(name = "SyncConfig")
class SyncConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long? = null

    var name:String? = "Untitled Sync Config"

    @Lob
    var pathDetails:String?=null

    //var scheduleDetails:String?=null

    var enable:Boolean = false

    @Deprecated("NON SENCE FOR SYNC CONFIG")
    var companyId:Long?=null

    var resourceId:Long? =null

    var destinationResources:String?=null

}