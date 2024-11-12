package com.msp.backHub.controller

import com.msp.backHub.service.ResourceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/resourceTest")
class ResourceControllerTest {

    @Autowired
    private lateinit var  resourceService: ResourceService


//
//    @PostMapping("/register")
//    fun registerResource(@RequestBody registerResourceInput: Pojo.RegisterResourceInput): Boolean {
//        resourceService.registerResource(registerResourceInput)
//        return true
//    }


//    @PostMapping("/update")
//    fun updateResource(@RequestBody updateRegisterResourceInput: Pojo.UpdateResourceInput): Boolean {
//        resourceService.updateResource(updateRegisterResourceInput)
//        return true
//    }
//
//
//    @PostMapping("/removeConfig")
//    fun removeConfigForResource(@RequestBody removeConfigForResourceInput: Pojo.RemoveConfigForResource): Boolean {
//        resourceService.removeConfigForResource(removeConfigForResourceInput)
//        return true
//    }
//
//
//    @PostMapping("/deRegister")
//    fun deRegisterResource(@RequestBody updateConfigBody: Pojo.DeRegisterResourceInput): Boolean {
//        resourceService.deRegisterResource(updateConfigBody)
//        return true
//    }






}