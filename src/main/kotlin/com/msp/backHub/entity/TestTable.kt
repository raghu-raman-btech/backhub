package com.msp.backHub.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
class TestTable {
    @Id
    var id:Long?=null

    var name:String?=null

    var data:String?=null
}


