package uk.gov.justice.digital.hmpps.whereabouts.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.gov.justice.digital.hmpps.whereabouts.controllers.AttendanceController;
import uk.gov.justice.digital.hmpps.whereabouts.security.AuthAwareTokenConverter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

@Configuration
@EnableSwagger2
//@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableWebSecurity
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {


    private BuildProperties buildProperties;

    public ResourceServerConfiguration(@Autowired(required = false) final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // Can't have CSRF protection as requires session
                .and().csrf().disable()
                .authorizeRequests(auth ->
                        auth.antMatchers("/webjars/**", "/favicon.ico", "/csrf",
                                "/health","/health/ping", "/info", "/ping",
                                "/v2/api-docs",
                                "/swagger-ui.html", "/swagger-resources", "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security")
                                .permitAll().anyRequest().authenticated())
                .oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
    }

//    @Override
//    public void configure(final ResourceServerSecurityConfigurer config) {
//        config.tokenServices(tokenServices());
//    }
//
//    @Bean
//    public TokenStore tokenStore() {
//        return new JwtTokenStore(accessTokenConverter());
//    }
//
//    @Bean
//    public JwtAccessTokenConverter accessTokenConverter() {
//        final var converter = new JwtAccessTokenConverter();
//        converter.setVerifierKey(new String(Base64.decodeBase64(jwtPublicKey)));
//        return converter;
//    }
//
//    @Bean
//    @Primary
//    public DefaultTokenServices tokenServices() {
//        final var defaultTokenServices = new DefaultTokenServices();
//        defaultTokenServices.setTokenStore(tokenStore());
//        return defaultTokenServices;
//    }

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

//    @Bean
//    @ConfigurationProperties("elite2api.client")
//    public ClientCredentialsResourceDetails elite2apiClientCredentials() {
//        return new ClientCredentialsResourceDetails();
//    }
//
//    @Bean
//    @Primary
//    @RequestScope
//    public OAuth2ClientContext oAuth2ClientContext() {
//        return new DefaultOAuth2ClientContext();
//    }
//
//    @Bean(name = "oauth2ClientContextAppScope")
//    public OAuth2ClientContext oauth2ClientContextSingleton() {
//        return new DefaultOAuth2ClientContext();
//    }
//
//    @Bean
//    public OAuth2ClientContext oAuth2ClientContextNoneRequestScope() {
//        return new DefaultOAuth2ClientContext();
//    }
}
