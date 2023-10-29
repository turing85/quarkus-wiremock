package de.turing85;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("greeting")
@Produces(MediaType.TEXT_PLAIN)
public class GreetingResource {
  private final ExternalClient externalClient;

  public GreetingResource(@RestClient ExternalClient externalClient) {
    this.externalClient = externalClient;
  }

  @GET
  public Uni<Response> hello() {
    // @formatter:off
    return externalClient.getGreeting()
        .onFailure(WebApplicationException.class)
            .recoverWithItem(e -> ((WebApplicationException) e).getResponse());
    // @formatter:on
  }
}
