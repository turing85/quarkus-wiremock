package de.turing85.resource;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.netty.util.NetUtil;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class WireMockTestResource
    implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
  // @formatter:off
  private static final String CONTAINER_IMAGE = System.getProperty(
      "wiremock.container.image",
      "docker.io/wiremock/wiremock:3.5.4-alpine");
  private static final String CONTAINER_NAME = System.getProperty(
      "wiremock.container.name",
      "wiremock");
  private static final int CONTAINER_PORT = Integer.parseInt(System.getProperty(
      "wiremock.container.port",
      "%d".formatted(new Random().nextInt(1_024, 65_536))));
  private static final boolean VERBOSE = Boolean.parseBoolean(System.getProperty(
      "wiremock.verbose",
      "false"));
  private static final Slf4jLogConsumer CONTAINER_LOG_CONSUMER =
      new Slf4jLogConsumer(LoggerFactory.getLogger("WireMock Container"))
          .withMdc(Map.of(
              "container.image", CONTAINER_IMAGE,
              "container.name", CONTAINER_NAME,
              "container.port", "%d".formatted(CONTAINER_PORT)))
          .withSeparateOutputStreams();
  // @formatter:on
  private static GenericContainer<?> wireMockContainer;
  private static WireMock wireMock;
  private static String containerNetworkId;

  @Override
  public void setIntegrationTestContext(DevServicesContext context) {
    containerNetworkId = context.containerNetworkId().orElse(null);
  }

  @Override
  public Map<String, String> start() {
    if (Objects.isNull(wireMockContainer)) {
      wireMockContainer = constructContainer();
    }
    final String localhost = NetUtil.LOCALHOST4.getHostName();
    if (!wireMockContainer.isRunning()) {
      wireMockContainer.start();
      wireMock = constructWireMock(localhost);
    }
    wireMock.resetMappings();

    final String host = Objects.isNull(containerNetworkId) ? localhost : CONTAINER_NAME;
    final int port =
        Objects.isNull(containerNetworkId) ? wireMockContainer.getMappedPort(CONTAINER_PORT)
            : CONTAINER_PORT;
    return Map.of("quarkus.rest-client.external-client.url", "http://%s:%d".formatted(host, port));
  }

  private static GenericContainer<?> constructContainer() {
    GenericContainer<?> container = new GenericContainer<>(CONTAINER_IMAGE)
        .withCreateContainerCmdModifier(command -> command.withName(CONTAINER_NAME));
    Optional.ofNullable(containerNetworkId).ifPresent(container::withNetworkMode);
    // @formatter:off
    StringBuilder command = new StringBuilder("--disable-banner --port %d".formatted(CONTAINER_PORT));
    if (VERBOSE) {
      command.append(" --verbose");
    }
    return container
        .withCommand(command.toString())
        .withExposedPorts(CONTAINER_PORT)
        .withLogConsumer(CONTAINER_LOG_CONSUMER)
        .waitingFor(Wait.forHttp("/__admin").forPort(CONTAINER_PORT));
    // @formatter:on
  }

  private static WireMock constructWireMock(String localhost) {
    return new WireMock(localhost, wireMockContainer.getMappedPort(CONTAINER_PORT));
  }

  @Override
  public void inject(TestInjector testInjector) {
    testInjector.injectIntoFields(
        Optional.ofNullable(wireMock)
            .orElseThrow(() -> new IllegalStateException("WireMock has not yet been initialized.")),
        new TestInjector.AnnotatedAndMatchesType(InjectWireMock.class, WireMock.class));
  }

  @Override
  public void stop() {
    if (Optional.ofNullable(wireMockContainer).map(GenericContainer::isRunning).orElse(false)) {
      wireMockContainer.stop();
    }
  }
}
