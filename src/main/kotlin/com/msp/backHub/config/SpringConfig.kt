package com.msp.backHub.config

import com.msp.backHub.service.CustomAuthenticationSuccessHandler
import com.msp.backHub.service.MyUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(private val userDetailsService: MyUserDetailsService,
                     @Qualifier("appDataSource")  private val dataSource: DataSource,
                     private val customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler
) {

   // @Autowired private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .authorizeHttpRequests { authorize ->
                    authorize
                            .requestMatchers("/login", "/resource/**", "/admin/**","/api/test/**").permitAll()
                            .anyRequest().authenticated()
                }
                //TODO HANDLE FILTER for "/resource" by having a secret key for every resource.

                .formLogin { form ->
                    form
                            .loginPage("/login")
                            .successHandler(customAuthenticationSuccessHandler)
//                            .successHandler { request, response, authentication ->
//                                println("Authentication successful from securityFilterChain")
//                                customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication)
//                            }
                            //.defaultSuccessUrl("/home",true)
                            .permitAll()
                }
                .logout { logout ->
                    logout
                            .logoutUrl("/logout")
                            .logoutSuccessUrl("/login")
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                }
                .rememberMe{ rm->
                    rm.tokenRepository(persistentTokenRepository())
                    rm.tokenValiditySeconds(86400*7)
                    rm.alwaysRemember(true)
                }
                .exceptionHandling { ex->
                    ex.accessDeniedPage("/403")
                }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

//    @Bean
//    fun passwordEncoder(): PasswordEncoder {
//        return NoOpPasswordEncoder.getInstance()
//    }


    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)

        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
        return authenticationManagerBuilder.build()
    }


    @Bean
    fun persistentTokenRepository(): PersistentTokenRepository {
        val tokenRepository = JdbcTokenRepositoryImpl()
        tokenRepository.dataSource = dataSource
        return tokenRepository
    }



}



























