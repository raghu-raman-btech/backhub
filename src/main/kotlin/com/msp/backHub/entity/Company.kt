package com.msp.backHub.entity


import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import lombok.Data
import lombok.NoArgsConstructor

@Entity
@Table(name = "company")
data class Company(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var name: String? = null
)








//INSERT INTO company (name) VALUES ('RR_INFOTECH');
//INSERT INTO company (name) VALUES ('sanjana_pvt.Ltd');
//
//INSERT INTO user (email, password, company_id) VALUES ('rr@rr.com', '$2a$12$AM2Px9dfIjTJateYjf7YreG8rlKOnOyejX4UbEITKq9fnkKs90itC', 1);
//INSERT INTO user (email, password, company_id) VALUES ('saga@saga.com', '$2a$12$AM2Px9dfIjTJateYjf7YreG8rlKOnOyejX4UbEITKq9fnkKs90itC', 2);
//
//INSERT INTO policy (description, company_id) VALUES ('RR _POLICY _ 1', 1);
//INSERT INTO policy (description, company_id) VALUES ('SAGA POLICY_1', 2);


