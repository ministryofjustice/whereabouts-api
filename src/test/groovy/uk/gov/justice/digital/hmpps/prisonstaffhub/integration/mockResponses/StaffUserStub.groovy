package uk.gov.justice.digital.hmpps.prisonstaffhub.integration.mockResponses

class StaffUserStub {

    static responseItag(username) {
        def response = """
{
    "staffId": -2,
    "firstName": "Elite2",
    "lastName": "User",
    "username": "${username}"
}
        """
        return response
    }
}