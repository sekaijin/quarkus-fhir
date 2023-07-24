package fr.aphp.fhir.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.api.AddProfileTagEnum;
import ca.uhn.fhir.context.api.BundleInclusionRule;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsResourceProvider;
import ca.uhn.fhir.jaxrs.server.util.JaxRsRequest.Builder;
import ca.uhn.fhir.rest.annotation.At;
import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.History;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Since;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import fr.aphp.fhir.patch.JaxRsRequestBuilder;
import fr.aphp.fhir.service.Service;

/**
 * A demo JaxRs Patient Rest Provider
 */
@Path(JaxRsPatientRestProviderR4.PATH)
@Produces({ MediaType.APPLICATION_JSON, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
public class JaxRsPatientRestProviderR4 extends AbstractJaxRsResourceProvider<Patient> {

	/**
	 * The HAPI paging provider for this server
	 */
//	public static final IPagingProvider PAGE_PROVIDER;

	static final String PATH = "/Patient";

//	static {
//		PAGE_PROVIDER = new FifoMemoryPagingProvider(10);
//	}


	@Inject
	Service<Patient> patientService;
	
	public JaxRsPatientRestProviderR4() {
		super(FhirContext.forR4(), JaxRsPatientRestProviderR4.class);
	}

	@Create
	public MethodOutcome create(@ResourceParam final Patient patient, @ConditionalUrlParam String theConditional) throws Exception {
		patientService.create(patient);
		final MethodOutcome result = new MethodOutcome().setCreated(true);
		result.setResource(patient);
		result.setId(new IdType(patient.getId()));
		return result;
	}

	@Delete
	public MethodOutcome delete(@IdParam final IdType theId) {
		final Patient deletedPatient = patientService.remove(theId);
		final MethodOutcome result = new MethodOutcome().setCreated(true);
		result.setResource(deletedPatient);
		return result;
	}

	@Read
	public Patient find(@IdParam final IdType theId) {
		return patientService.find(theId);
	}

	@Read(version = true)
	public Patient findVersion(@IdParam final IdType theId) {
		return patientService.findVersion(theId);
	}

	@History
	public IBundleProvider getHistoryForInstance(@IdParam IdType theId, @Since Date theSince, @At DateRangeParam theAt, RequestDetails theRequestDetails) {
		return new SimpleBundleProvider(patientService.history(theId), "myTestId");
	}

	@History
	public IBundleProvider getHistoryForType(@Since Date theSince, @At DateRangeParam theAt, RequestDetails theRequestDetails) {
		return new SimpleBundleProvider(Collections.emptyList(), "myTestId");
	}

	@Operation(name = "firstVersion", idempotent = true, returnParameters = { @OperationParam(name = "return", type = StringType.class) })
	public Parameters firstVersion(@IdParam final IdType theId, @OperationParam(name = "dummy") StringType dummyInput) {
		return patientService.firstVersion(theId, dummyInput);
	}

	@Override
	public AddProfileTagEnum getAddProfileTag() {
		return AddProfileTagEnum.NEVER;
	}

	@Override
	public BundleInclusionRule getBundleInclusionRule() {
		return BundleInclusionRule.BASED_ON_INCLUDES;
	}

	@Override
	public ETagSupportEnum getETagSupport() {
		return ETagSupportEnum.DISABLED;
	}

	/** THE DEFAULTS */

	@Override
	public List<IServerInterceptor> getInterceptors_() {
		return Collections.emptyList();
	}

//	@Override
//	public IPagingProvider getPagingProvider() {
//		return PAGE_PROVIDER;
//	}

	@Override
	public Class<Patient> getResourceType() {
		return Patient.class;
	}

	@Override
	public boolean isDefaultPrettyPrint() {
		return true;
	}

	@GET
	@Path("/{id}/$firstVersion")
	public Response operationFirstVersionUsingGet(@PathParam("id") String id) throws IOException {
		return customOperation(null, RequestTypeEnum.GET, id, "$firstVersion", RestOperationTypeEnum.EXTENDED_OPERATION_INSTANCE);
	}

	@POST
	@Path("/{id}/$firstVersion")
	public Response operationFirstVersionUsingGet(@PathParam("id") String id, final String resource) throws Exception {
		return customOperation(resource, RequestTypeEnum.POST, id, "$firstVersion", RestOperationTypeEnum.EXTENDED_OPERATION_INSTANCE);
	}

	@Search
	public List<Patient> search(@RequiredParam(name = Patient.SP_NAME) final StringParam name) {
		return patientService.search(name);
	}

	@Search(compartmentName = "Condition")
	public List<IBaseResource> searchCompartment(@IdParam IdType thePatientId) {
		List<IBaseResource> retVal = new ArrayList<IBaseResource>();
		Condition condition = new Condition();
		condition.setId(new IdType("665577"));
		retVal.add(condition);
		return retVal;
	}

	@Update
	public MethodOutcome update(@IdParam final IdType theId, @ResourceParam final Patient patient) {
		
		patientService.update(theId, patient);
		final MethodOutcome result = new MethodOutcome().setCreated(false);
		result.setResource(patient);
		result.setId(new IdType(patient.getId()));
		return result;
	}


	@Override
	public Builder getRequest(final RequestTypeEnum requestType, final RestOperationTypeEnum restOperation, final String theResourceName) {
		return new JaxRsRequestBuilder(this, requestType, restOperation, getUriInfo().getRequestUri().toString(), theResourceName);
	}
}
