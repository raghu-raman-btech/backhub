package com.msp.backHub.config

import org.quartz.Scheduler
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import javax.sql.DataSource


    @Configuration
    class QuartzConfig {
        @Bean
        fun schedulerFactoryBean(@Qualifier("quartzDataSource") dataSource: DataSource): SchedulerFactoryBean {
            val factoryBean = SchedulerFactoryBean()
            factoryBean.setDataSource(dataSource)
            factoryBean.setConfigLocation(org.springframework.core.io.ClassPathResource("quartz.properties"))
            return factoryBean
        }

        @Bean
        fun scheduler(factoryBean: SchedulerFactoryBean): Scheduler {
            return factoryBean.scheduler
        }

}