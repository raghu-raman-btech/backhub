package com.msp.backHub.controller

import com.msp.backHub.pojo.Pojo
import com.msp.backHub.service.CompanyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class AuthController {

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }

    @GetMapping("/403")
    fun accessDenied(): String {
        return "403"
    }

    @GetMapping("/home")
    fun getHome(): String {
        println("getHome called")
        return "home"
    }




}