package de.turing85;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(ExternalClient.GREETING_URL)
@RegisterRestClient(configKey = "external-client")
@Produces(MediaType.TEXT_PLAIN)
public interface ExternalClient {
  String GREETING_URL = "/greeting";

  @GET
  Uni<Response> getGreeting();
}
