package fr.aphp.fhir.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.vertx.core.json.JsonObject;

@Path("/metadata")
@RegisterRestClient
public interface ExtensionsService {

    @GET
    JsonObject metadata();
}
