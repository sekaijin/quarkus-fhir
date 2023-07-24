package fr.aphp.fhir.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Dependent
public class PractitionerService implements Service<Practitioner> {

	private static Long counter = 1L;

	private static final ConcurrentHashMap<String, List<Practitioner>> practitioners = new ConcurrentHashMap<String, List<Practitioner>>();

	static {
		practitioners.put(String.valueOf(counter), _createPractitioner("Dr Van Houte"));
		practitioners.put(String.valueOf(counter), _createPractitioner("Dr Agnew"));
		for (int i = 0; i < 20; i++) {
			practitioners.put(String.valueOf(counter), _createPractitioner("Dr Random " + counter));
		}
		practitioners.values().forEach(l -> {
			l.forEach(p -> {
				Identifier i = new Identifier();
				i.setSystem("urn:rpps.sante.fr");
				i.setValue("10003750030");
				p.addIdentifier(i );
			});
		});
	}


	private static Practitioner _getLast(final List<Practitioner> list) {
		return list.get(list.size() - 1);
	}

	private static IdType _createId(final Long id, final Long theVersionId) {
		return new IdType("Practitioner", "" + id, "" + theVersionId);
	}

	private static List<Practitioner> _createPractitioner(final Practitioner practitioner) {
		practitioner.setId(_createId(counter, 1L));
		final LinkedList<Practitioner> list = new LinkedList<Practitioner>();
		list.add(practitioner);
		counter++;
		return list;
	}

	private static List<Practitioner> _createPractitioner(final String name) {
		final Practitioner practitioner = new Practitioner();
		practitioner.getName().add(new HumanName().setFamily(name));
		return _createPractitioner(practitioner);
	}

	@Override
	public Practitioner find(IdType theId) {
		if (practitioners.containsKey(theId.getIdPart())) {
			return _getLast(practitioners.get(theId.getIdPart()));
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public Practitioner findVersion(IdType theId) {
		if (practitioners.containsKey(theId.getIdPart())) {
			final List<Practitioner> list = practitioners.get(theId.getIdPart());
			for (final Practitioner practitioner : list) {
				if (practitioner.getIdElement().getVersionIdPartAsLong().equals(theId.getVersionIdPartAsLong())) {
					return practitioner;
				}
			}
		}
		throw new ResourceNotFoundException(theId);
	}

	@Override
	public Practitioner remove(final IdType theId) {
		final Practitioner deletedPractitioner = find(theId);
		practitioners.remove(deletedPractitioner.getIdElement().getIdPart());
		return deletedPractitioner;
	}

	@Override
	public List<Practitioner> history(IdType theId) {
		List<Practitioner> list;
		if (practitioners.containsKey(theId.getIdPart())) {
			list = practitioners.get(theId.getIdPart());
		} else {
			list = Collections.emptyList();
		}
		return list;
	}

	@Override
	public Parameters firstVersion(final IdType theId, StringType dummyInput) {
		Parameters parameters = new Parameters();
		Practitioner practitioner = find(new IdType(theId.getResourceType(), theId.getIdPart(), "0"));
		parameters.addParameter().setName("return").setResource(practitioner).setValue(new StringType((counter - 1) + "" + "inputVariable [ " + dummyInput.getValue() + "]"));
		return parameters;
	}

	@Override
	public List<Practitioner> search(StringParam name) {
		return practitioners.values().stream().flatMap(List::stream)
		.filter(practitioner -> name == null || practitioner.getName().get(0).getFamilyElement().getValueNotNull().equals(name.getValueNotNull()))
		.collect(Collectors.toList());
		
	}

	@Override
	public Practitioner update(IdType theId, Practitioner practitioner) {
		final String idPart = theId.getIdPart();
		if (practitioners.containsKey(idPart)) {
			final List<Practitioner> practitionerList = practitioners.get(idPart);
			final Practitioner lastPractitioner = _getLast(practitionerList);
			practitioner.setId(_createId(theId.getIdPartAsLong(), lastPractitioner.getIdElement().getVersionIdPartAsLong() + 1));
			practitionerList.add(practitioner);
			return practitioner;
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public void create(Practitioner practitioner) {
		practitioners.put("" + counter, _createPractitioner(practitioner));
	}

}
