package uk.gov.justice.digital.hmpps.whereabouts.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;
import org.springframework.web.context.annotation.RequestScope;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.justice.digital.hmpps.whereabouts.controllers.AttendanceController;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Configuration
@EnableSwagger2
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private String keySetUri;
    private BuildProperties buildProperties;

    public ResourceServerConfiguration(
            @Value("${security.oauth2.resource.jwk.key-set-uri}") final String keySetUri,
            @Autowired final BuildProperties buildProperties) {
        this.keySetUri = keySetUri;
        this.buildProperties = buildProperties;
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {

        http.headers().frameOptions().sameOrigin().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // Can't have CSRF protection as requires session
                .and().csrf().disable()
                .authorizeRequests()
                .antMatchers("/webjars/**", "/favicon.ico", "/csrf",
                        "/health", "/health/ping", "/info", "/ping", "/h2-console/**",
                        "/v2/api-docs",
                        "/swagger-ui.html", "/swagger-resources", "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security").permitAll()
                .anyRequest()
                .authenticated();
    }

    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenServices());
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwkTokenStore(keySetUri);
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        final var defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

    @Bean
    public Docket api() {

        final var apiInfo = new ApiInfo(
                "Whereabouts API Documentation",
                "API for accessing the Whereabouts services.",
                getVersion(), "", contactInfo(), "", "",
                Collections.emptyList());

        final var docket = new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage(AttendanceController.class.getPackage().getName()))
                .paths(PathSelectors.any())
                .build();

        docket.genericModelSubstitutes(Optional.class);
        docket.directModelSubstitute(ZonedDateTime.class, java.util.Date.class);
        docket.directModelSubstitute(LocalDateTime.class, java.util.Date.class);

        return docket;
    }

    /**
     * @return health data. Note this is unsecured so no sensitive data allowed!
     */
    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }

    private Contact contactInfo() {
        return new Contact(
                "HMPPS Digital Studio",
                "",
                "feedback@digital.justice.gov.uk");
    }

    @Bean
    @ConfigurationProperties("elite2api.client")
    public ClientCredentialsResourceDetails elite2apiClientCredentials() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean
    @Primary
    @RequestScope
    public OAuth2ClientContext oAuth2ClientContext() {
        return new DefaultOAuth2ClientContext();
    }

    @Bean(name = "oauth2ClientContextAppScope")
    public OAuth2ClientContext oauth2ClientContextSingleton() {
        return new DefaultOAuth2ClientContext();
    }

    @Bean
    public OAuth2ClientContext oAuth2ClientContextNoneRequestScope() {
        return new DefaultOAuth2ClientContext();
    }
}
