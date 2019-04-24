package uk.gov.justice.digital.hmpps.prisonstaffhub.services;

//@Component
//@Slf4j
//public class Elite2ApiHealth implements HealthIndicator {
//
//    private final RestTemplate restTemplate;
//
//    @Autowired
//    public Elite2ApiHealth(@Qualifier("elite2ApiHealthRestTemplate") RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    @Override
//    public Health health() {
//        try {
//            final ResponseEntity<String> responseEntity = this.restTemplate.getForEntity("/health", String.class);
//            return health(responseEntity.getStatusCode());
//        } catch (RestClientException e) {
//            log.error(String.format("Elite2ApiHealth failed: %s", e.getMessage()));
//            return health(HttpStatus.SERVICE_UNAVAILABLE);
//        }
//    }
//
//    private Health health(HttpStatus code) {
//        return health (
//                code.is2xxSuccessful() ? Health.up(): Health.down(),
//                code);
//    }
//
//    private Health health(Health.Builder builder, HttpStatus code) {
//        return builder
//                .withDetail("HttpStatus", code.value())
//                .build();
//    }
//}
