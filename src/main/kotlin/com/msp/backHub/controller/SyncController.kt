package com.msp.backHub.controller

import com.msp.backHub.pojo.Pojo
import com.msp.backHub.service.CheckCompanyAccess
import com.msp.backHub.service.SyncConfigService
import org.hibernate.annotations.Check
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/sync")
class SyncController {

    @Autowired
    private lateinit var  syncConfigService: SyncConfigService

    @PostMapping("/updateConfig")
    @CheckCompanyAccess
    fun updateConfig(@RequestBody updateConfigBody: Pojo.UpdateSyncConfigInput): Boolean {

        if(updateConfigBody.addConfig != null){
            syncConfigService.addSyncConfig(updateConfigBody.addConfig)
        }
        if(!updateConfigBody.updateConfig.isNullOrEmpty()){
            syncConfigService.updateSyncConfig(updateConfigBody.updateConfig)
        }
        if(updateConfigBody.deleteConfig != null){
            syncConfigService.deleteSyncConfig(updateConfigBody.deleteConfig)
        }

        return true
    }

}