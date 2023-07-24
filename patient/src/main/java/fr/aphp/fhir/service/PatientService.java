package fr.aphp.fhir.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Dependent
public class PatientService implements Service<Patient> {

	private static Long counter = 1L;
	private static Long local = 532L;

	private static final ConcurrentHashMap<String, List<Patient>> patients = new ConcurrentHashMap<String, List<Patient>>();

	static {
		patients.put(String.valueOf(counter), _createPatient("Van Houte"));
		patients.put(String.valueOf(counter), _createPatient("Agnew"));
		for (int i = 0; i < 20; i++) {
			patients.put(String.valueOf(counter), _createPatient("Random Patient " + counter));
		}
		patients.values().forEach(l -> {
			l.forEach(p -> {
				Identifier i = new Identifier();
				i.setSystem("urn:ipp.aphp.fr");
				i.setValue(UUID.randomUUID().toString());
				p.addIdentifier(i );
				i = new Identifier();
				i.setSystem("urn:lrb.aphp.fr");
				i.setValue("" + local++);
				p.addIdentifier(i );
			});
		});
	}


	private static Patient _getLast(final List<Patient> list) {
		return list.get(list.size() - 1);
	}

	private static IdType _createId(final Long id, final Long theVersionId) {
		return new IdType("Patient", "" + id, "" + theVersionId);
	}

	private static List<Patient> _createPatient(final Patient patient) {
		patient.setId(_createId(counter, 1L));
		final LinkedList<Patient> list = new LinkedList<Patient>();
		list.add(patient);
		counter++;
		return list;
	}

	private static List<Patient> _createPatient(final String name) {
		final Patient patient = new Patient();
		patient.getName().add(new HumanName().setFamily(name));
		return _createPatient(patient);
	}

	@Override
	public Patient find(IdType theId) {
		if (patients.containsKey(theId.getIdPart())) {
			return _getLast(patients.get(theId.getIdPart()));
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public Patient findVersion(IdType theId) {
		if (patients.containsKey(theId.getIdPart())) {
			final List<Patient> list = patients.get(theId.getIdPart());
			for (final Patient patient : list) {
				if (patient.getIdElement().getVersionIdPartAsLong().equals(theId.getVersionIdPartAsLong())) {
					return patient;
				}
			}
		}
		throw new ResourceNotFoundException(theId);
	}

	@Override
	public Patient remove(final IdType theId) {
		final Patient deletedPatient = find(theId);
		patients.remove(deletedPatient.getIdElement().getIdPart());
		return deletedPatient;
	}

	@Override
	public List<Patient> history(IdType theId) {
		List<Patient> list;
		if (patients.containsKey(theId.getIdPart())) {
			list = patients.get(theId.getIdPart());
		} else {
			list = Collections.emptyList();
		}
		return list;
	}

	@Override
	public Parameters firstVersion(final IdType theId, StringType dummyInput) {
		Parameters parameters = new Parameters();
		Patient patient = find(new IdType(theId.getResourceType(), theId.getIdPart(), "0"));
		parameters.addParameter().setName("return").setResource(patient).setValue(new StringType((counter - 1) + "" + "inputVariable [ " + dummyInput.getValue() + "]"));
		return parameters;
	}

	@Override
	public List<Patient> search(StringParam name) {
		return patients.values().stream().flatMap(List::stream)
		.filter(patient -> name == null || patient.getName().get(0).getFamilyElement().getValueNotNull().equals(name.getValueNotNull()))
		.collect(Collectors.toList());
		
	}

	@Override
	public Patient update(IdType theId, Patient patient) {
		final String idPart = theId.getIdPart();
		if (patients.containsKey(idPart)) {
			final List<Patient> patientList = patients.get(idPart);
			final Patient lastPatient = _getLast(patientList);
			patient.setId(_createId(theId.getIdPartAsLong(), lastPatient.getIdElement().getVersionIdPartAsLong() + 1));
			patientList.add(patient);
			return patient;
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public void create(Patient patient) {
		patients.put("" + counter, _createPatient(patient));
	}

}
