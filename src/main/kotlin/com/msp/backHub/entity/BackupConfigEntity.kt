package com.msp.backHub.entity

import jakarta.persistence.*


@Entity
@Table(name = "BackupConfig")
class BackupConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long? = null

    var name:String? = "Untitled Backup Config"

    var backupAll:Boolean?= null

    @Lob
    var pathDetails:String?=null

    var scheduleDetails:String?=null

    var enable:Boolean = false

    var companyId:Long?=null

    var resourceId:Long? =null

}