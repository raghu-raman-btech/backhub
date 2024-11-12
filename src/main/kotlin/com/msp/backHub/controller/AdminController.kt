package com.msp.backHub.controller

import com.msp.backHub.pojo.Pojo
import com.msp.backHub.service.CompanyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/admin")
class AdminController {
    @Autowired
    private lateinit var companyService: CompanyService


    @PostMapping("/registerCompany")
    fun registerCompany(@RequestBody reqBody: Pojo.RegisterCompanyRequestBody):Boolean{
        return companyService.registerCompany(reqBody)
    }
}