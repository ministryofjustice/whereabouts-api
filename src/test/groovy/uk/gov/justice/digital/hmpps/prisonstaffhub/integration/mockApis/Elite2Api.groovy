package uk.gov.justice.digital.hmpps.prisonstaffhub.integration.mockApis

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.util.UriTemplate
import uk.gov.justice.digital.hmpps.prisonstaffhub.dto.Page
import uk.gov.justice.digital.hmpps.prisonstaffhub.integration.mockResponses.*

import static com.github.tomakehurst.wiremock.client.WireMock.*

class Elite2Api extends WireMockRule {

    public static final int WIREMOCK_PORT = 8999

    public static String NOMIS_API_PREFIX = "/api"


    Elite2Api() {
        super(WIREMOCK_PORT)
    }

    void stubAccessCodeListForKeyRole(String prisonId) {
        stubFor(get(urlEqualTo(new UriTemplate(NOMIS_API_PREFIX+ RemoteRoleService.STAFF_ACCESS_CODES_LIST_URL).expand(prisonId, "KEY_WORK").toString()))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody("[]")
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
    }

    void stubHealthOKResponse() {
        stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody("""{"status":"UP","HttpStatus":200}""")
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
    }

    void stubStaffUserDetails(String username) {
        if (username.equals("omicadmin")) {
            stubFor(get(urlEqualTo(new UriTemplate(NOMIS_API_PREFIX + GET_USER_DETAILS).expand(username).toString()))
                    .willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        } else {
            stubFor(get(urlEqualTo(new UriTemplate(NOMIS_API_PREFIX + GET_USER_DETAILS).expand(username).toString()))
                    .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                    .withBody(StaffUserStub.responseItag(username)) //empty list
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        }
    }

}
