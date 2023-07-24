package fr.aphp.fhir.service;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.HardcodedServerAddressStrategy;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.ResourceBinding;
import ca.uhn.fhir.rest.server.RestfulServerConfiguration;
import ca.uhn.fhir.rest.server.method.BaseMethodBinding;
import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;
import io.quarkus.restclient.runtime.QuarkusRestClientBuilder;
import io.vertx.core.json.JsonObject;

@Dependent
public class MetadataService {
	
	@ConfigProperty(name="fhir.urls")
	List<String> urls;

	@ConfigProperty(name="server.version")
	String serverVersion ;
	@ConfigProperty(name="implementation.description")
	String implementationDescription;
	@ConfigProperty(name="server.name")
	String serverName;
	RestfulServerConfiguration myServerConfiguration = new RestfulServerConfiguration();

	public MetadataService() {
		super();

		myServerConfiguration.setServerBindings(Collections.<BaseMethodBinding>emptyList());
		myServerConfiguration.setResourceBindings(Collections.<ResourceBinding>emptyList());
		myServerConfiguration.computeSharedSupertypeForResourcePerName(Collections.<IResourceProvider>emptyList());
	}


	public CapabilityStatement getCapability(String adress) {
		HardcodedServerAddressStrategy hardcodedServerAddressStrategy = new HardcodedServerAddressStrategy();
		hardcodedServerAddressStrategy.setValue(adress);
		myServerConfiguration.setImplementationDescription(StringUtils.defaultIfEmpty(implementationDescription, ""));
		myServerConfiguration.setServerName(StringUtils.defaultIfEmpty(serverName, ""));
		myServerConfiguration.setServerVersion(StringUtils.defaultIfEmpty(serverVersion, ""));
		myServerConfiguration.setServerAddressStrategy(hardcodedServerAddressStrategy);

		ServerCapabilityStatementProvider r4apabilityProvider = new ServerCapabilityStatementProvider(FhirContext.forR4(), myServerConfiguration);
		CapabilityStatement capability = (CapabilityStatement) r4apabilityProvider.getServerConformance(null, null);
		collectCapabilities(capability);
		capability.setName("QuarkusServer");
		capability.setPublisher("Quarkus.io");
		return capability;
	}


	private void collectCapabilities(CapabilityStatement capability) {

		
		List<CapabilityStatementRestResourceComponent> resources = capability.getRest().get(0).getResource();
		List<CapabilityStatementRestResourceOperationComponent> operations = capability.getRest().get(0).getOperation();
		resources.clear();
		operations.clear();
		
		urls.forEach(url -> {
			CapabilityStatement c = getCapabilityFor(url);
			resources.addAll(c.getRest().get(0).getResource());
			operations.addAll(c.getRest().get(0).getOperation());
			
		});
		
		
		}

	private CapabilityStatement getCapabilityFor(String url) {
		ExtensionsService extensionsService = new QuarkusRestClientBuilder()
				.baseUri(URI.create(url))
				.build(ExtensionsService.class);

		JsonObject p = extensionsService.metadata();
		IParser parser = FhirContext.forR4().newJsonParser();
		return parser.parseResource(CapabilityStatement.class, p.toString());		

	}
}
