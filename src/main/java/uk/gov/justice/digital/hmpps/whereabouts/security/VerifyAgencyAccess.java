package uk.gov.justice.digital.hmpps.whereabouts.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyAgencyAccess {
    String[] overrideRoles() default "ROLE_SYSTEM_USER";
}
