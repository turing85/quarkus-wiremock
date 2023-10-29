package de.turing85;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.DisplayName;

@QuarkusIntegrationTest
@DisplayName("Native GreetingResource Test with WireMock Container")
public class GreetingResourceContainerIT extends GreetingResourceContainerTest {
}
