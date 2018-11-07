package com.trivadis.avro.demo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.trivadis.avro.person.v1.Address;
import com.trivadis.avro.person.v1.Person;
import com.trivadis.avro.person.v1.TitleEnum;

public class TestAvroV1_1 {
	private final static String CONTAINER_FILE_NAME_V1 = "../../data/person_v1.0.avro";
	private final static String CONTAINER_FILE_NAME_V1_1 = "../../data/person_v1.1.avro";
	private final static String BIN_FILE_NAME_V1_1 = "../../data/person_v1.1.bin";
	@Test
	public void testWriteToBinaryFileV2() throws IOException {
		List<Address> addresses = new ArrayList<Address>();
		
		addresses.add(Address.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Person person = Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setMiddleName("Paul")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setTitle(TitleEnum.Mr)
					.setBirthDate(new LocalDate("1995-11-10"))
					.setAddresses(addresses).build();

		System.out.println(person);
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
		DatumWriter<Person> writer = new SpecificDatumWriter<Person>(Person.getClassSchema());

		writer.write(person, encoder);
		encoder.flush();
		out.close();
		byte[] serializedBytes = out.toByteArray();
		System.out.println(serializedBytes.length);
		
		try (FileOutputStream fos = new FileOutputStream(BIN_FILE_NAME_V1_1)) {
			fos.write(serializedBytes);
		}
		
	}
	
	@Test
	public void testGenericRecord() throws IOException {
		final String schemaLoc = "src/main/avro/Person-v1.1.avsc";
		final File schemaFile = new File(schemaLoc);
		final Schema schema = new Schema.Parser().parse(schemaFile);

		GenericRecord person1 = new GenericData.Record(schema);
		person1.put("id", 2);
		person1.put("firstName", "Peter");
		person1.put("middleName", "Paul");
		person1.put("lastName", "Sample");
		person1.put("title", "Mr");
		person1.put("emailAddress", "peter.sample@somecorp.com");
		person1.put("phoneNumber", "+41 79 345 34 44");
		person1.put("birthDate", new LocalDate("1995-11-10"));

		assertEquals("Peter", person1.get("firstName"));
	}

	@Test
	public void testWriteToContainerFileV1_1() throws IOException {
		List<Person> persons = new ArrayList<Person>();
		List<Address> addresses = new ArrayList<Address>();
		
		addresses.add(Address.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Person person1 = Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setTitle(TitleEnum.Mr)
					.setBirthDate(new LocalDate("1995-11-10"))
					.setAddresses(addresses).build();
		persons.add(person1);

		final DatumWriter<Person> datumWriter = new SpecificDatumWriter<>(Person.class);
		final DataFileWriter<Person> dataFileWriter = new DataFileWriter<>(datumWriter);
		
		try {
		    dataFileWriter.create(persons.get(0).getSchema(), new File(CONTAINER_FILE_NAME_V1_1));
		    persons.forEach(employee -> {
		        try {
		            dataFileWriter.append(employee);
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }

		    });
		} catch (IOException e) {
			throw new RuntimeException();
		}
		finally {
		    dataFileWriter.close();
		}		

	}
	
	@Test
	public void testReadFromContainerFileV1() throws IOException {
		final File file = new File(CONTAINER_FILE_NAME_V1);
		final List<Person> persons = new ArrayList<>();
		final DatumReader<Person> personReader = new SpecificDatumReader<>(Person.SCHEMA$);
		final DataFileReader<Person> dataFileReader = new DataFileReader<>(file, personReader);

		while (dataFileReader.hasNext()) {
		    persons.add(dataFileReader.next(new Person()));
		}
		
		for (Person person : persons) {
			System.out.println(person);
		}
	}

	
}
