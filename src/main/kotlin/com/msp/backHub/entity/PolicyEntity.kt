package com.msp.backHub.entity

import jakarta.persistence.*

@Entity
@Table(name = "policy")
data class Policy(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        val description: String? = null,
        @ManyToOne
        val company: Company? = null
)