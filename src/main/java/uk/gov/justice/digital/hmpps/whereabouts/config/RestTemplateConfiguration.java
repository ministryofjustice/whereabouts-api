package uk.gov.justice.digital.hmpps.whereabouts.config;

//@Configuration
//public class RestTemplateConfiguration {
//
//    @Value("${elite2api.endpoint.url}")
//    private String elite2UriRoot;
//
//    @Value("${oauth.endpoint.url}")
//    private String oauthRootUri;
//
//    @Value("${casenotes.endpoint.url}")
//    private String caseNotesRootUri;
//
//    private final Duration healthTimeout;
//
//    private final OAuth2ClientContext oauth2ClientContext;
//    private final OAuth2ClientContext oauth2ClientContextSingleton;
//    private final ClientCredentialsResourceDetails elite2apiDetails;
//
//    public RestTemplateConfiguration(final OAuth2ClientContext oauth2ClientContext,
//                                     final ClientCredentialsResourceDetails elite2apiDetails,
//                                     @Value("${api.health-timeout:1s}") final Duration healthTimeout,
//                                     @Qualifier("oauth2ClientContextAppScope") final OAuth2ClientContext oauth2ClientContextSingleton) {
//        this.oauth2ClientContext = oauth2ClientContext;
//        this.elite2apiDetails = elite2apiDetails;
//        this.healthTimeout = healthTimeout;
//        this.oauth2ClientContextSingleton = oauth2ClientContextSingleton;
//    }
//
//    @Bean(name = "elite2ApiHealthRestTemplate")
//    public RestTemplate elite2ApiHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
//        return getHealthRestTemplate(restTemplateBuilder, elite2UriRoot);
//    }
//
//    @Bean(name = "oauthApiHealthRestTemplate")
//    public RestTemplate oauthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
//        return getHealthRestTemplate(restTemplateBuilder, oauthRootUri);
//    }
//
//    @Bean(name = "caseNotesApiHealthRestTemplate")
//    public RestTemplate caseNotesHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
//        return getHealthRestTemplate(restTemplateBuilder, caseNotesRootUri);
//    }
//
//    private RestTemplate getHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder, final String uri) {
//        return restTemplateBuilder
//                .rootUri(uri)
//                .additionalInterceptors(new JwtAuthInterceptor())
//                .setConnectTimeout(healthTimeout)
//                .setReadTimeout(healthTimeout)
//                .build();
//    }
//
//    @Bean(name = "elite2ApiRestTemplate")
//    public OAuth2RestTemplate elite2ApiRestTemplate(final AuthenticationFacade authenticationFacade) {
//
//        final var elite2SystemRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);
//
//        elite2SystemRestTemplate.setAccessTokenProvider(new GatewayAwareAccessTokenProvider(authenticationFacade));
//
//        RootUriTemplateHandler.addTo(elite2SystemRestTemplate, elite2UriRoot + "/api");
//
//        return elite2SystemRestTemplate;
//    }
//
//    @Bean(name = "elite2ApiRestTemplateAppScope")
//    public OAuth2RestTemplate elite2ApiRestTemplateForEventQueue() {
//
//        final var elite2SystemRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContextSingleton);
//
//        RootUriTemplateHandler.addTo(elite2SystemRestTemplate, elite2UriRoot + "/api");
//
//        return elite2SystemRestTemplate;
//    }
//
//    @Bean(name = "caseNotesApiRestTemplate")
//    public OAuth2RestTemplate caseNotesApiRestTemplate(final AuthenticationFacade authenticationFacade) {
//
//        final var caseNotesApiRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);
//
//        caseNotesApiRestTemplate.setAccessTokenProvider(new GatewayAwareAccessTokenProvider(authenticationFacade));
//
//        RootUriTemplateHandler.addTo(caseNotesApiRestTemplate, caseNotesRootUri);
//
//        return caseNotesApiRestTemplate;
//    }
//
//    public static class GatewayAwareAccessTokenProvider extends ClientCredentialsAccessTokenProvider {
//
//        GatewayAwareAccessTokenProvider(final AuthenticationFacade authenticationFacade) {
//            this.setTokenRequestEnhancer(new TokenRequestEnhancer(authenticationFacade));
//        }
//
//        @Override
//        public RestOperations getRestTemplate() {
//            return super.getRestTemplate();
//        }
//    }
//
//    public static class TokenRequestEnhancer implements RequestEnhancer {
//
//        private final AuthenticationFacade authenticationFacade;
//
//        TokenRequestEnhancer(final AuthenticationFacade authenticationFacade) {
//            this.authenticationFacade = authenticationFacade;
//        }
//
//        @Override
//        public void enhance(final AccessTokenRequest request, final OAuth2ProtectedResourceDetails resource, final MultiValueMap<String, String> form, final HttpHeaders headers) {
//            form.set("username", authenticationFacade.getCurrentUsername());
//        }
//    }
//}
