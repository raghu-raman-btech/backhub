
As a first step , A account (client) is created for the User through Admin api : /admin/registerCompany

User and company details are stored in tables ,and a separate s3 bucket is created for the account .
Agent installs are renamed and copied to the company's bucket at path /installer.

* BACKUP

         User need to create a config (either at client level or asset level) , with schedule details and list of paths to backup .

    POST :localhost:8080/backup/updateConfig

    {
        "addConfig": {
            "name": "client_level_backup_plan_toBackup_Folder",
            "enable": true,
            "companyId": 19,
            "resourceId": null, // if this config should apply only for a particular resource .
            "scheduleDetails": {
                "scheduleType": "DAYS",
                "scheduleInterval": 1
            },
            "pathDetails": {
                // user will be choosing with FileExplorer view from web application .
                "includePaths": [
                    "/Users/raghuraman/Documents/toBackup2"
                ]
                // exclude path is yet to handle
            }
        }
    }


on creating a config , a schedule job is created with ,

    jobName = "JobName_backupConfig_${configId}"
    triggerName = "TriggerName_backupConfig_${configId}"

    jobGroup = "JobGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"
    triggerGroup = "TriggerGroup_client:${configInput.companyId}_resource:${configInput.resourceId}"


where job group is at resource level ( where every resource has seperate job group ). Weather a schedule of a config is applied to the asset or not is
determined by whether the jobName (with configID) is there in job group or not .

Weather a config is applied to the resource or not is determined by a mapping in table "ResourceIdConfigIdMapping".

The config details are sent to Resource through lambda and webSocket . Once resource receives the add_config action,it'll first push all files for backup and creates a watcher for mentioned paths and its supDirs .
Later , whenever a file modified or added , Watcher sends a message to lambda and updates in cache  with config detals and modified file's path detals .
so that on next schedule time , not all files is pushed for backup again . Only files in cache are pushed .


            On updating a backup config , if schedule details are updated , old schedules are deleted and new ones are created .
if paths to backup are updated , it is notified to resource agent ,the old watcher is deleted  , a new watcher is created right after pushing all the new paths to backup.

            On Deleting a backup config , marking the config as disabled , deleting schedules and cache ,and notifying the resource agent to delete the watcher





* SYNC

    Sync config doesn't have a schedule , But the path details  , resource watcher concepts are the same .

    POST : localhost:8080/sync/updateConfig

    {
    "addConfig":{
        "name":"sync-toSync2",
        "enable":true,
        "companyId":8,
         "resourceId": 1727907178642796000,
        "pathDetails":{
            "includePaths":["/Users/raghuraman/Documents/toSync2"]
        },
        "destinationResources":[1727907178642796000]
    }
    }

    Once a file under the config path is added or modified , the file path is sent to lambda to update in cache ,so that the paths in cache
    are pushed and downloaded in destination resources .


       On updating a Sync config ,  if paths to backup are updated , it is notified to resource agent ,the old watcher is deleted  , a new watcher is created right after pushing all the new paths to backup.

       On Deleting a backup config , marking the config as disabled , deleting schedules and cache ,and notifying the resource agent to delete the watcher



 The endpoint : /uploadv2ForSync will push the file to s3 and get the presigned url , and fetch the destination resources configured in config and Download action is sent to all the destination resources.


@CheckCompanyAccess is used to autherize the request whether the cookie (user) has access to the company for which conifg is updated .



RESOURCE AGENT - GO LANG :


            Company id is taken from installer name .

            Resource id is stored in Plist . If there is no resourceId in Plist , resource is registered
              POST : http://localhost:8080/resource/register

            websocket connection is established and resource keeps on listening for actions.

            When agent starts , we'll fetch all the configs and create watchers for the configs .
                if lastBackupTime is null in cache , we'll push all files from config paths.

            All the incoming actions are received from websocket .

            when add config is performed , files are uploaded and watcher object is created and stored in the map .
                configWatchers     = make(map[string]*fsnotify.Watcher) // map to store watchers by configId
                The key is config id ,and the value is the watcher object .

            on delete config , the watcher object from the map is taken and deleted .

            Download action is called for normal file share . if the resource is marked as destination resource in any sync config ,
            the synced file's presigned url is sent in download action .


            FILE EXPLORER :

            For configuring paths for confis , a file explorer view , which lists all the paths for the respective resource is shown .(as shown in medium article)

            By using this file explorer , user can add paths in config.


              "pathDetails": {
                            // user will be choosing with FileExplorer view from web application .
                            "includePaths": [
                                "/Users/raghuraman/Documents/toBackup2"
                            ]
                            // exclude path is yet to handle
                        }
