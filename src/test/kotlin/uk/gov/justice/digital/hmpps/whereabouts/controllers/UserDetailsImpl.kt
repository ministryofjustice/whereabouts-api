package uk.gov.justice.digital.hmpps.whereabouts.controllers

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.HashMap
import java.util.HashSet

class UserDetailsImpl(
  private val username: String,
  private val password: String,
  authorities: Collection<GrantedAuthority>?,
  additionalProperties: Map<String, Any>?
) : UserDetails {
  private val authorities: MutableSet<GrantedAuthority> = HashSet()
  private val additionalProperties: MutableMap<String, Any> = HashMap()
  override fun isEnabled(): Boolean {
    return true
  }

  override fun isCredentialsNonExpired(): Boolean {
    return true
  }

  override fun isAccountNonLocked(): Boolean {
    return true
  }

  override fun isAccountNonExpired(): Boolean {
    return true
  }

  override fun getUsername(): String {
    return username
  }

  override fun getPassword(): String {
    return password
  }

  override fun getAuthorities(): Set<GrantedAuthority> {
    return authorities
  }

  fun getAdditionalProperties(): Map<String, Any> {
    return additionalProperties
  }

  init {
    this.authorities.addAll(authorities!!)
    if (additionalProperties != null) {
      this.additionalProperties.putAll(additionalProperties)
    }
  }
}
