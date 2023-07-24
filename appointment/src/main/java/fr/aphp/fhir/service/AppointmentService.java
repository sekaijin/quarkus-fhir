package fr.aphp.fhir.service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

@Dependent
public class AppointmentService implements Service<Appointment> {

	private static Long counter = 1L;

	private static final ConcurrentHashMap<String, List<Appointment>> appointments = new ConcurrentHashMap<>();

	static {
		try {
		appointments.put(String.valueOf(counter), _createappointment("Van Houte"));
		appointments.put(String.valueOf(counter), _createappointment("Agnew"));
		for (int i = 0; i < 20; i++) {
			appointments.put(String.valueOf(counter), _createappointment("Random patient " + counter));
		}
		appointments.values().forEach(l -> {
			l.forEach(p -> {
				Identifier i = new Identifier();
				i.setSystem("urn:apmt.aphp.fr");
				i.setValue(UUID.randomUUID().toString());
				p.addIdentifier(i );
			});
		});
		} catch (Exception e) {}
	}


	private static Appointment _getLast(final List<Appointment> list) {
		return list.get(list.size() - 1);
	}

	private static IdType _createId(final Long id, final Long theVersionId) {
		return new IdType("appointment", "" + id, "" + theVersionId);
	}

	private static List<Appointment> _createappointment(final Appointment appointment) {
		appointment.setId(_createId(counter, 1L));
		final LinkedList<Appointment> list = new LinkedList<>();
		list.add(appointment);
		counter++;
		return list;
	}

	private static List<Appointment> _createappointment(final String name) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		final Appointment appointment = new Appointment();
		appointment.setStart(formatter.parse("01-02-2023"));
		appointment.setEnd(formatter.parse("01-02-2023"));
		Reference r = new Reference();
		Identifier idt = new Identifier();
		idt.setSystem("urn:ipp.aphp.fr");
		idt.setValue("4588877c-f639-440f-aa89-ea7d2b044168");
		r.setIdentifier(idt);
		r.setDisplay(name);
		appointment.addParticipant().setActor(r);
		Reference dr = new Reference();
		dr.setReference("/Practitioner/1");
		dr.setDisplay("DR Van Houte");
		appointment.addParticipant().setActor(dr);
		
		//appointment.getName().add(new HumanName().setFamily(name));
		return _createappointment(appointment);
	}

	@Override
	public Appointment find(IdType theId) {
		if (appointments.containsKey(theId.getIdPart())) {
			return _getLast(appointments.get(theId.getIdPart()));
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public Appointment findVersion(IdType theId) {
		if (appointments.containsKey(theId.getIdPart())) {
			final List<Appointment> list = appointments.get(theId.getIdPart());
			for (final Appointment appointment : list) {
				if (appointment.getIdElement().getVersionIdPartAsLong().equals(theId.getVersionIdPartAsLong())) {
					return appointment;
				}
			}
		}
		throw new ResourceNotFoundException(theId);
	}

	@Override
	public Appointment remove(final IdType theId) {
		final Appointment deletedappointment = find(theId);
		appointments.remove(deletedappointment.getIdElement().getIdPart());
		return deletedappointment;
	}

	@Override
	public List<Appointment> history(IdType theId) {
		List<Appointment> list;
		if (appointments.containsKey(theId.getIdPart())) {
			list = appointments.get(theId.getIdPart());
		} else {
			list = Collections.emptyList();
		}
		return list;
	}

	@Override
	public Parameters firstVersion(final IdType theId, StringType dummyInput) {
		Parameters parameters = new Parameters();
		Appointment appointment = find(new IdType(theId.getResourceType(), theId.getIdPart(), "0"));
		parameters.addParameter().setName("return").setResource(appointment).setValue(new StringType((counter - 1) + "" + "inputVariable [ " + dummyInput.getValue() + "]"));
		return parameters;
	}

	@Override
	public List<Appointment> search(StringParam name) {
		return appointments.values().stream().flatMap(List::stream)
		//.filter(appointment -> name == null || appointment.getName().get(0).getFamilyElement().getValueNotNull().equals(name.getValueNotNull()))
		.collect(Collectors.toList());
		
	}

	@Override
	public Appointment update(IdType theId, Appointment appointment) {
		final String idPart = theId.getIdPart();
		if (appointments.containsKey(idPart)) {
			final List<Appointment> appointmentList = appointments.get(idPart);
			final Appointment lastappointment = _getLast(appointmentList);
			appointment.setId(_createId(theId.getIdPartAsLong(), lastappointment.getIdElement().getVersionIdPartAsLong() + 1));
			appointmentList.add(appointment);
			return appointment;
		} else {
			throw new ResourceNotFoundException(theId);
		}
	}

	@Override
	public void create(Appointment appointment) {
		appointments.put("" + counter, _createappointment(appointment));
	}

}
