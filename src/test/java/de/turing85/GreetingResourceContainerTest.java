package de.turing85;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import de.turing85.resource.QuarkusWireMockTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.StubImport.stubImport;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@DisplayName("GreetingResource Test with WireMock Container")
class GreetingResourceContainerTest extends QuarkusWireMockTest {

  @Test
  @TestHTTPEndpoint(GreetingResource.class)
  @DisplayName("get -> ok ✔")
  void okTest() {
    // GIVEN
    String expected = "expected ✔";
    // @formatter:off
    wireMock().importStubMappings(
        stubImport()
            .stub(get(ExternalClient.GREETING_URL)
                .willReturn(ok()
                      .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                      .withBody(expected))));

    // WHEN
    RestAssured
        .when().get()

    // THEN
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .contentType(MediaType.TEXT_PLAIN)
            .body(is(expected));
    // @formatter:on

    wireMock().verifyThat(exactly(1), getRequestedFor(urlEqualTo(ExternalClient.GREETING_URL)));
  }

  @Test
  @TestHTTPEndpoint(GreetingResource.class)
  @DisplayName("get -> not found ❌")
  void notFoundTest() {
    // GIVEN
    final String expected = "not found ❌";
    // @formatter:off
    wireMock().importStubMappings(
        stubImport()
            .deleteAllExistingStubsNotInImport()
            .stub(get(ExternalClient.GREETING_URL)
                .willReturn(notFound()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                    .withBody(expected))));

    // WHEN
    RestAssured
        .when().get()

    // THEN
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode())
            .contentType(MediaType.TEXT_PLAIN)
            .body(is(expected));
    // @formatter:on

    wireMock().verifyThat(exactly(1), getRequestedFor(urlEqualTo(ExternalClient.GREETING_URL)));
  }

  @Test
  @TestHTTPEndpoint(GreetingResource.class)
  @DisplayName("get -> internal server error \uD83D\uDCA3")
  void iseTest() {
    // GIVEN
    final String expected = "boom \uD83D\uDCA3";
    // @formatter:off
    wireMock().importStubMappings(
        stubImport()
            .stub(get(ExternalClient.GREETING_URL)
                .willReturn(serverError()
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                    .withBody(expected))));

    // WHEN
    RestAssured
        .when().get()

    // THEN
        .then()
        .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
        .contentType(MediaType.TEXT_PLAIN)
        .body(is(expected));
    // @formatter:on

    wireMock().verifyThat(exactly(1), getRequestedFor(urlEqualTo(ExternalClient.GREETING_URL)));
  }
}
