package com.msp.backHub.service

import com.msp.backHub.Repo.CompanyRepository
import com.msp.backHub.Repo.UserRepository
import com.msp.backHub.entity.Company
import com.msp.backHub.entity.User
import com.msp.backHub.pojo.Pojo
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class CompanyService {

    @Autowired
    private lateinit var companyRepo:CompanyRepository
    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var s3Service: S3ServiceTest



    @Transactional
    fun registerCompany(reqBody: Pojo.RegisterCompanyRequestBody):Boolean{
        try {
            val company = Company()
            //company.id = Random.nextLong()
            company.name = reqBody.company.companyName!!
            companyRepo.save(company)

            val userList = mutableListOf<User>()
            reqBody.user.forEach {
                val user = User()
                user.email = it.email
                user.password = passwordEncoder.encode(it.password)
                user.company = company
                userList.add(user)
            }
            userRepo.saveAll(userList)


            val s3BucketName = "${company.name!!.toLowerCase()}-${company.id}"
            s3Service.createBucket(s3BucketName)
            println("bucket Crated  for company :  $s3BucketName")

            val macInstallerBucketName = "mac-installer"
            val macInstallerInstallerName = "backhub"

            val macInstallerForClient = "installer/${macInstallerInstallerName}_${company.id}"

            s3Service.copyInstallerForMac(macInstallerBucketName,macInstallerInstallerName,s3BucketName,macInstallerForClient)
            println("installer Generated")

        }catch (ex:Exception){
            ex.printStackTrace()
            return false
        }
        return true
    }




























}