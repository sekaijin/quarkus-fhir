package fr.aphp.fhir.service;

import java.util.List;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.rest.param.StringParam;

public interface Service<T extends Resource>  {

	T find(IdType theId);

	T findVersion(IdType theId);

	T remove(IdType theId);

	List<T> history(IdType theId);

	Parameters firstVersion(IdType theId, StringType dummyInput);

	List<T> search(StringParam name);

	T update(IdType theId, T Organization);

	void create(T Organization);

}