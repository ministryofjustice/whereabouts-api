package uk.gov.justice.digital.hmpps.whereabouts.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.time.Duration
import java.util.*

@Component
class JwtAuthenticationHelper(@Value("\${jwt.signing.key.pair}") privateKeyPair: String,
                              @Value("\${jwt.keystore.password}") keystorePassword: String,
                              @Value("\${jwt.keystore.alias:elite2api}") keystoreAlias: String) {
    private val keyPair: KeyPair

    init {

        val keyStoreKeyFactory = KeyStoreKeyFactory(ByteArrayResource(Base64.decodeBase64(privateKeyPair)),
                keystorePassword.toCharArray())
        keyPair = keyStoreKeyFactory.getKeyPair(keystoreAlias)
    }

    fun createJwt(parameters: JwtParameters): String {

        val claims: HashMap<String, Any?> = hashMapOf(
                "user_name" to parameters.username,
                "user_id" to parameters.userId,
                "client_id" to "elite2apiclient"
        )

        if (parameters.roles.isNotEmpty()) claims["authorities"] = parameters.roles

        if (parameters.scope.isNotEmpty()) claims["scope"] = parameters.scope

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(parameters.username)
                .addClaims(claims)
                .setExpiration(Date(System.currentTimeMillis() + parameters.expiryTime.toMillis()))
                .signWith(SignatureAlgorithm.RS256, keyPair.private)
                .compact()
    }
}

data class JwtParameters(val username: String?, val userId: String = "12345", val scope: List<String>, val roles: List<String>, val expiryTime: Duration)
