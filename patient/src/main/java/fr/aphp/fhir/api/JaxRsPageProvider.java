//package fr.aphp.fhir.api;
//
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//
//import ca.uhn.fhir.jaxrs.server.AbstractJaxRsPageProvider;
//import ca.uhn.fhir.rest.api.Constants;
//import ca.uhn.fhir.rest.server.IPagingProvider;
//
//@Path("/")
//@Produces({ MediaType.APPLICATION_JSON, Constants.CT_FHIR_JSON, Constants.CT_FHIR_XML })
//public class JaxRsPageProvider extends AbstractJaxRsPageProvider {
//
//	@Override
//	public IPagingProvider getPagingProvider() {
//		return JaxRsPatientRestProviderR4.PAGE_PROVIDER;
//	}
//
//}
