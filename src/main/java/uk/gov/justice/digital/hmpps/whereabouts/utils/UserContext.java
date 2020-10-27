package uk.gov.justice.digital.hmpps.whereabouts.utils;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Component
public class UserContext {
    private static final ThreadLocal<String> authToken = new ThreadLocal<>();
    private static final ThreadLocal<Authentication> authentication = new ThreadLocal<>();

    public static String getAuthToken() {
        return authToken.get();
    }

    public static void setAuthToken(final String aToken) {
        authToken.set(aToken);
    }

    public static void setAuthentication(Authentication auth) {
        authentication.set(auth);
    }

    public static Authentication getAuthentication() {
        return authentication.get();
    }
}
