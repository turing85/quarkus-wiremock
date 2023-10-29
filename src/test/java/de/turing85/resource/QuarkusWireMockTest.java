package de.turing85.resource;

import java.util.Objects;
import java.util.Optional;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTestResource(value = WireMockTestResource.class)
public abstract class QuarkusWireMockTest {
  @InjectWireMock
  WireMock wireMock;

  protected WireMock wireMock() {
    return Optional.ofNullable(wireMock)
        .orElseThrow(() -> new IllegalStateException("WireMock is not initialized"));
  }

  @BeforeEach
  void resetWireMock() {
    if (Objects.nonNull(wireMock)) {
      wireMock().resetMappings();
    }
  }
}
