package fr.aphp.fhir.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.codesystems.OrganizationType;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Dependent
public class OrganizationService implements Service<Organization> {

	private static Long counter = 1L;
	private static Long local = 532L;

	private static final ConcurrentHashMap<Long, List<Organization>> organizations = new ConcurrentHashMap<>();

	static {
		organizations.put(counter, _createOrganization("Assistance Publique des Hopitaix de Paris", OrganizationType.OTHER, 0L));
		organizations.put(counter, _createOrganization("Groupe Hospitalier Paris Nord", OrganizationType.OTHER, 1l));
		organizations.put(counter, _createOrganization("Hospital Lariboisière", OrganizationType.PROV, 2L));
		organizations.put(counter, _createOrganization("Urgence Lrb", OrganizationType.DEPT, 2L));
		organizations.put(counter, _createOrganization("Hospital Henry Mondore", OrganizationType.PROV, 2L));
		organizations.put(counter, _createOrganization("Maternité HMN", OrganizationType.DEPT, 2L));
		organizations.values().forEach(l -> {
			l.forEach(p -> {
				Identifier i = new Identifier();
				i.setSystem("urn:sirus.aphp.fr");
				i.setValue("" + local++);
				p.addIdentifier(i );
			});
		});
		System.err.println(organizations);
	}


	private static Organization _getLast(final List<Organization> list) {
		return list.get(list.size() - 1);
	}

	private static IdType _createId(final Long id, final Long theVersionId) {
		return new IdType("Organization", "" + id, "" + theVersionId);
	}

	private static List<Organization> _createOrganization(final Organization Organization) {
		Organization.setId(_createId(counter, 1L));
		final LinkedList<Organization> list = new LinkedList<Organization>();
		list.add(Organization);
		counter++;
		return list;
	}

	private static List<Organization> _createOrganization(final String name, OrganizationType type, long ref) {
		final Organization organization = new Organization();
		organization.setName(name);
		CodeableConcept code = new CodeableConcept(new Coding(type.getSystem(), type.toCode(), type.getDisplay()));
		organization.addType(code);
		if (0l != ref) {
			Organization parent = organizations.get(ref).get(0);
			
			organization.setPartOf(new Reference(parent));
		}

		return _createOrganization(organization);
	}

	@Override
	public Organization find(IdType theId) {
		var id = Long.parseLong(theId.getIdPart());
		if (organizations.containsKey(id)) {
			return _getLast(organizations.get(id));
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public Organization findVersion(IdType theId) {
		var id = Long.parseLong(theId.getIdPart());
		if (organizations.containsKey(id)) {
			final List<Organization> list = organizations.get(id);
			for (final Organization Organization : list) {
				if (Organization.getIdElement().getVersionIdPartAsLong().equals(theId.getVersionIdPartAsLong())) {
					return Organization;
				}
			}
		}
		throw new ResourceNotFoundException(theId);
	}

	@Override
	public Organization remove(final IdType theId) {
		final Organization deletedOrganization = find(theId);
		organizations.remove(Long.parseLong(deletedOrganization.getIdElement().getIdPart()));
		return deletedOrganization;
	}

	@Override
	public List<Organization> history(IdType theId) {
		List<Organization> list;
		var id = Long.parseLong(theId.getIdPart());
		if (organizations.containsKey(id)) {
			list = organizations.get(id);
		} else {
			list = Collections.emptyList();
		}
		return list;
	}

	@Override
	public Parameters firstVersion(final IdType theId, StringType dummyInput) {
		Parameters parameters = new Parameters();
		Organization Organization = find(new IdType(theId.getResourceType(), theId.getIdPart(), "0"));
		parameters.addParameter().setName("return").setResource(Organization).setValue(new StringType((counter - 1) + "" + "inputVariable [ " + dummyInput.getValue() + "]"));
		return parameters;
	}

	@Override
	public List<Organization> search(StringParam name) {
		return organizations.values().stream().flatMap(List::stream)
				.filter(Organization -> name == null || Organization.getName().equals(name.getValueNotNull()))
				.collect(Collectors.toList());

	}

	@Override
	public Organization update(IdType theId, Organization Organization) {
		var id = Long.parseLong(theId.getIdPart());
		if (organizations.containsKey(id)) {
			final List<Organization> OrganizationList = organizations.get(id);
			final Organization lastOrganization = _getLast(OrganizationList);
			Organization.setId(_createId(theId.getIdPartAsLong(), lastOrganization.getIdElement().getVersionIdPartAsLong() + 1));
			OrganizationList.add(Organization);
			return Organization;
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public void create(Organization Organization) {
		organizations.put(counter, _createOrganization(Organization));
	}

}
