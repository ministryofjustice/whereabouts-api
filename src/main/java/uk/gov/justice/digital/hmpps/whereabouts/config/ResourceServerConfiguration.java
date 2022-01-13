package uk.gov.justice.digital.hmpps.whereabouts.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthAwareTokenConverter;


@Configuration
@EnableSwagger2
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // Can't have CSRF protection as requires session
                .and().csrf().disable()
                .authorizeRequests(auth ->
                        auth.antMatchers("/webjars/**", "/favicon.ico", "/csrf",
                                "/health/**", "/info", "/ping", "/h2-console/**",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/swagger-ui/**", "/swagger-resources", "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security")
                                .permitAll().anyRequest().authenticated()
                )
                .oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
    }
}
