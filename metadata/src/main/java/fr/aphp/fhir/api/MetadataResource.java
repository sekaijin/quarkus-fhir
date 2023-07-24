package fr.aphp.fhir.api;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.r4.model.CapabilityStatement;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsProvider;
import ca.uhn.fhir.jaxrs.server.util.JaxRsRequest;
import ca.uhn.fhir.jaxrs.server.util.JaxRsRequest.Builder;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.api.server.IRestfulResponse;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import fr.aphp.fhir.service.MetadataService;


@Path("/metadata")
@Produces({ MediaType.APPLICATION_JSON, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
public class MetadataResource extends AbstractJaxRsProvider {

	@Inject
	MetadataService metadataService;

	/**
	 * Standard Constructor
	 */
	public MetadataResource() {
		super(FhirContext.forR4());
	}

	@GET
	public Response conformance() throws IOException {
		
		
		CapabilityStatement u = metadataService.getCapability(getBaseForServer());
		
		

		Builder request = getRequest(RequestTypeEnum.OPTIONS, RestOperationTypeEnum.METADATA);
		JaxRsRequest requestDetails = request.build();
		IRestfulResponse response = requestDetails.getResponse();
		response.addHeader(Constants.HEADER_CORS_ALLOW_ORIGIN, "*");

		Set<SummaryEnum> summaryMode = Collections.emptySet();

		return (Response) RestfulServerUtils.streamResponseAsResource(this, u, summaryMode, Constants.STATUS_HTTP_200_OK, false, true, requestDetails, null, null);
	}
}
