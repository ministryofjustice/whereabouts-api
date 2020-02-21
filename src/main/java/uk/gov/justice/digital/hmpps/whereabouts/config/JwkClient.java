package uk.gov.justice.digital.hmpps.whereabouts.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

@Component
public class JwkClient {

    private RestTemplate restTemplate;
    private String keyId;

    public JwkClient(@Qualifier("oauthApiHealthRestTemplate") final RestTemplate oauthApiHealthRestTemplate,
                     @Value("${jwt.jwk.key.id}") final String keyId) {
        this.restTemplate = oauthApiHealthRestTemplate;
        this.keyId = keyId;
    }

    public String findJwkSet() throws ParseException {
        ResponseEntity<String> jwkSetString = restTemplate.getForEntity("/.well-known/jwks.json", String.class);
        JWKSet jwkSet = JWKSet.parse(jwkSetString.getBody());
        JWK jwk = jwkSet.getKeyByKeyId(keyId);
        List<Key> keys = KeyConverter.toJavaKeys(Collections.singletonList(jwk));
        return keys.get(0).toString();
    }
}

