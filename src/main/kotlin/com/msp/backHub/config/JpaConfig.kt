package com.msp.backHub.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.cache.annotation.CacheConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
@EnableJpaRepositories(basePackages = ["com.msp.backHub.Repo"])
class JpaConfig {

    @Bean
    fun entityManagerFactory(@Qualifier("appDataSource") dataSource: DataSource)
    : LocalContainerEntityManagerFactoryBean {
        val factoryBean = LocalContainerEntityManagerFactoryBean()
        factoryBean.dataSource = dataSource
        factoryBean.setPackagesToScan("com.msp.backHub.entity")

        val vendorAdapter = HibernateJpaVendorAdapter()
        factoryBean.jpaVendorAdapter = vendorAdapter

        return factoryBean
    }


    @Bean
    fun transactionManager(entityManagerFactory: LocalContainerEntityManagerFactoryBean): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory.`object`!!)
    }

}