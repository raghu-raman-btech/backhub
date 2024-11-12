package com.msp.backHub.service

import com.msp.backHub.Repo.PolicyRepository
import com.msp.backHub.Repo.UserRepository
import com.msp.backHub.entity.Company
import com.msp.backHub.entity.Policy
import com.msp.backHub.entity.User
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Service
class MyUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
                ?: throw UsernameNotFoundException("User not found with email: $email")

        println("user at MyUserDetailsService : ${user.email}")

//        return org.springframework.security.core.userdetails.User(
//                user.email, user.password, emptyList()
//        )


        return MyUserDetails(user)
    }
}



// TODO : MOVE IN SEPERATE CLASS
class MyUserDetails(private val user: User) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        // You can map roles or authorities here. Assuming every user has a "USER" role for now.
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getPassword(): String {
        return user.password ?: ""
    }

    override fun getUsername(): String {
        return user.email ?: ""
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    // Expose company directly through a method or property
    fun getCompany(): Long? {
        return user.company!!.id
    }
}


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@PreAuthorize("@companySecurityService.hasAccessToCompany(#companyId)")
annotation class CheckCompanyAccess

@Service
class CompanySecurityService(
        private val userRepository: UserRepository
) {

    fun hasAccessToCompany(companyId: Long): Boolean {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val currentUserEmail = authentication.name


        val user = userRepository.findByEmail(currentUserEmail)
                ?: throw RuntimeException("User not found")

        val userCompanyId = user.company?.id

        if(userCompanyId != companyId){
            println("$currentUserEmail dont have access to companyId : $companyId")
        }

        return userCompanyId == companyId
    }
}




@Service
class PolicyService(
        private val policyRepository: PolicyRepository
) {

    fun getPoliciesForCompany(companyId: Long): List<Policy>? {
        return policyRepository.findAllByCompanyId(companyId)
    }
}


@Component
class CustomAuthenticationSuccessHandler : SavedRequestAwareAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication
    ) {
        println("inside CustomAuthenticationSuccessHandler")
        // Retrieve the companyId from the authenticated user's details
        val user = authentication.principal as MyUserDetails // Replace with your user details class
        val companyId = user.getCompany()

        println("inSide Auth : cID :$companyId")

        // Create and add the companyId cookie
        val companyIdCookie = Cookie("companyId", companyId.toString())
        companyIdCookie.path = "/"
        companyIdCookie.isHttpOnly = false
        companyIdCookie.maxAge = 7 * 24 * 60 * 60 // 1 week


        println("ckie with cusId insiode")
        response.addCookie(companyIdCookie)


        // Redirect to the default success URL or any custom URL
        val targetUrl = "/home"
        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $targetUrl")
        } else {
            getRedirectStrategy().sendRedirect(request, response, targetUrl)
        }

        // Proceed with the default behavior
        //super.onAuthenticationSuccess(request, response, authentication)
    }
}
























