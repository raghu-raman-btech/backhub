package com.msp.backHub.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean
    @Qualifier("appDataSource")
    fun appDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.url = "jdbc:mysql://127.0.0.1:3306/myDb"
        dataSource.username = "root"
        dataSource.password = "root"
        return dataSource
    }

    @Bean
    @Qualifier("quartzDataSource")
    fun quartzDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.url = "jdbc:mysql://127.0.0.1:3306/bkpSchTest"
        dataSource.username = "root"
        dataSource.password = "root"
        return dataSource
    }
}
