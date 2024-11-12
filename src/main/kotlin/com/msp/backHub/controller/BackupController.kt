package com.msp.backHub.controller

import com.msp.backHub.pojo.Pojo
import com.msp.backHub.service.BackupConfigService
import com.msp.backHub.service.CheckCompanyAccess
import com.msp.backHub.service.SyncConfigService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/backup")
class BackupController {

    @Autowired private lateinit var  backupConfigService: BackupConfigService

    @PostMapping("/updateConfig")
    @CheckCompanyAccess
    fun updateConfig(@RequestBody updateConfigBody: Pojo.UpdateBackupConfigInput): Boolean {
        if(updateConfigBody.addConfig != null){
            backupConfigService.addBackupConfig(updateConfigBody.addConfig)
        }
        if(!updateConfigBody.updateConfig.isNullOrEmpty()){
            backupConfigService.updateBackupConfig(updateConfigBody.updateConfig)
        }
        if(updateConfigBody.deleteConfig != null){
            backupConfigService.deleteBackupConfig(updateConfigBody.deleteConfig)
        }
        return true
    }
}