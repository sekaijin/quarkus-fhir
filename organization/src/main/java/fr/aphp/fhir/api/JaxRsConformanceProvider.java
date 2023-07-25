package fr.aphp.fhir.api;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jaxrs.server.AbstractJaxRsConformanceProvider;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * Conformance Rest Service
 * 
 * @author Peter Van Houte | peter.vanhoute@agfa.com | Agfa Healthcare
 */
@Path("")
@Produces({ MediaType.APPLICATION_JSON, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
public class JaxRsConformanceProvider extends AbstractJaxRsConformanceProvider {
	private static final String SERVER_VERSION = "1.0.0";
	private static final String SERVER_DESCRIPTION = "Jax-Rs Test Example Description";
	private static final String SERVER_NAME = "Jax-Rs Test Example";
	
    @Inject
    JaxRsOrganizationRestProviderR4 OrganizationProvider;

	/**
	 * Standard Constructor
	 */
	public JaxRsConformanceProvider() {
		super(FhirContext.forR4(), SERVER_DESCRIPTION, SERVER_NAME, SERVER_VERSION);
	}

	@Override
	protected ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> getProviders() {
		ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider> map = new ConcurrentHashMap<Class<? extends IResourceProvider>, IResourceProvider>();
		map.put(JaxRsConformanceProvider.class, this);
		map.put(JaxRsOrganizationRestProviderR4.class, OrganizationProvider);
		return map;
	}
}
