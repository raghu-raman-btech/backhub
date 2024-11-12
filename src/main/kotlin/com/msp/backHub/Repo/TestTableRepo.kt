package com.msp.backHub.Repo

import com.msp.backHub.entity.*
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface TestTableRepo: JpaRepository<TestTable, Long> {
}


@Repository
interface CompanyRepository : JpaRepository<Company, Long> {

}


@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}


@Repository
interface PolicyRepository : JpaRepository<Policy, Long> {
    fun findAllByCompanyId(company: Long): List<Policy>?
}

@Repository
interface ResourceIdConfigIdMappingRepo : JpaRepository<ResourceIdConfigIdMapping, Long> {

    @Transactional
    fun deleteByResourceIdAndConfigId(resourceId: Long, configId: Long)

    fun findAllByResourceId(resourceId: Long): List<ResourceIdConfigIdMapping>?
}


@Repository
interface ResourceIdSyncConfigIdMappingRepo : JpaRepository<ResourceIdSyncConfigIdMapping, Long> {

    @Transactional
    fun deleteByResourceIdAndConfigId(resourceId: Long, configId: Long)

    fun findAllByResourceId(resourceId: Long): List<ResourceIdConfigIdMapping>?
}
