package com.msp.backHub.entity

import com.msp.backHub.pojo.Pojo
import jakarta.persistence.*

@Entity
data class User(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        //var userName:String?= null,
        var email: String? = null,
        var password: String? = null,
        // var role: Pojo.CompanyUserRole? = null,

        var company: Company? = null
)