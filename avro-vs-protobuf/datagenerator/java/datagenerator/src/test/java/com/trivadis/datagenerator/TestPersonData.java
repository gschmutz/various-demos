package com.trivadis.datagenerator;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.trivadis.datagenerator.domain.PersonDO;

public class TestPersonData {

	@Test
	public void testGetPersons() throws IOException {
		List<PersonDO> persons = PersonData.getPersons();
	}
	
}
