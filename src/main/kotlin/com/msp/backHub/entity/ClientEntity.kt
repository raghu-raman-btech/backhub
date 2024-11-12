package com.msp.backHub.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
@Deprecated("use Company entity instead")
class ClientEntity {

    @Id
    var clientId:Long? = null

    var name:String? = null


}