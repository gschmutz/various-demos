package com.trivadis.avro.demo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.trivadis.avro.person.v1.Address;
import com.trivadis.avro.person.v1.Person;
import com.trivadis.avro.person.v1.TitleEnum;
import com.trivadis.datagenerator.PersonData;
import com.trivadis.datagenerator.domain.PersonDO;


public class TestAvroV1 {

	private final static String CONTAINER_FILE_NAME_V1 = "../../data/person_v1.0.avro";
	private final static String SNAPPY_COMPRESSESD_CONTAINER_FILE_NAME_V1 = "../../data/snappy-person_v1.0.avro";
	private final static String BIN_FILE_NAME_V1 = "../../data/person_v1.0.bin";
	private final static String JSON_FILE_NAME_V1 = "../../data/person_v1.0.json";
	
	@Test
	public void testToByteArray() throws IOException {
		List<Address> addresses = new ArrayList<Address>();
		
		addresses.add(Address.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Person person = Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setFaxNumber("+41 31 322 33 22")
					.setTitle(TitleEnum.Mr)
					.setDateOfBirth(LocalDate.parse("1995-11-10"))
					.setAddresses(addresses).build();
		
		byte[] serialized = person.toByteBuffer().array();
		System.out.println("Length of byte array:" + serialized.length);
	}	
	
	@Test
	public void testJsonEncoder() throws IOException {
		List<Address> addresses = new ArrayList<Address>();
		
		addresses.add(Address.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Person person = Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setFaxNumber("+41 31 322 33 22")
					.setTitle(TitleEnum.Mr)
					.setDateOfBirth(LocalDate.parse("1995-11-10"))
					.setAddresses(addresses).build();
		
		FileOutputStream fos = new FileOutputStream(JSON_FILE_NAME_V1);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonEncoder encoder = EncoderFactory.get().jsonEncoder(Person.SCHEMA$, out);
		DatumWriter<Person> writer = new SpecificDatumWriter<Person>(Person.getClassSchema());

		writer.write(person, encoder);
		encoder.flush();
		out.close();
		byte[] serializedBytes = out.toByteArray();

		fos.write(serializedBytes);
	}	
	
	@Test
	public void testWriteToBinaryFileV1() throws IOException {
		List<Address> addresses = new ArrayList<Address>();
		
		addresses.add(Address.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Person person = Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setFaxNumber("+41 31 322 33 22")
					.setTitle(TitleEnum.Mr)
					.setDateOfBirth(LocalDate.parse("1995-11-10"))
					.setAddresses(addresses).build();

		System.out.println(person);
		
		FileOutputStream fos = new FileOutputStream(BIN_FILE_NAME_V1);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
		DatumWriter<Person> writer = new SpecificDatumWriter<Person>(Person.getClassSchema());

		writer.write(person, encoder);
		encoder.flush();
		out.close();
		byte[] serializedBytes = out.toByteArray();

		fos.write(serializedBytes);
		
	}
	
	@Test
	public void testSpecificReadFromBinaryFileV1() throws IOException {
		DatumReader<Person> datumReader = new SpecificDatumReader<Person>(Person.class);
		byte[] bytes = Files.readAllBytes(new File(BIN_FILE_NAME_V1).toPath());

		BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
		Person person = datumReader.read(null, decoder);
		
		System.out.println(person);
	}

	@Test
	public void testGenericReadFromBinaryFileV1() throws IOException {
		final String schemaLoc = "src/main/avro/Person-v1.avsc";
		final File schemaFile = new File(schemaLoc);
		final Schema schema = new Schema.Parser().parse(schemaFile);

		DatumReader<Object> datumReader = new GenericDatumReader<>();
		datumReader.setSchema(schema);
		byte[] bytes = Files.readAllBytes(new File(BIN_FILE_NAME_V1).toPath());

		BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
		Object datum = null;
		datum = datumReader.read(datum, decoder);
		
		System.out.println(datum);
	}
	
	
	@Test
	public void testCreateASpecificRecord() throws IOException {
		List<Address> addresses = new ArrayList<Address>();
		
		addresses.add(Address.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Person person = Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setFaxNumber("+41 31 322 33 22")
					.setTitle(TitleEnum.Mr)
					.setDateOfBirth(LocalDate.parse("1995-11-10"))
					.setAddresses(addresses).build();
	}
	
	@Test
	public void testCreateAGenericRecord() throws IOException {
		final String schemaLoc = "src/main/avro/Person-v1.avsc";
		final File schemaFile = new File(schemaLoc);
		final Schema schema = new Schema.Parser().parse(schemaFile);

		GenericRecord person1 = new GenericData.Record(schema);
		person1.put("id", 2);
		person1.put("firstName", "Peter");
		person1.put("lastName", "Sample");
		person1.put("title", "Mr");
		person1.put("emailAddress", "peter.sample@somecorp.com");
		person1.put("phoneNumber", "+ 41 79 412 05 39");
		person1.put("faxNumber", "+41 31 322 33 22");
		person1.put("dateOfBirth", new LocalDate("1995-11-10"));

		assertEquals("Peter", person1.get("firstName"));
	}

	@Test
	public void testWriteToContainerFileV1() throws IOException {
		List<PersonDO> personsDO = PersonData.getPersons();
		List<Person> persons = new ArrayList<>();
		List<Address> addresses = new ArrayList<>();

		for (PersonDO personDO: personsDO) {
			addresses.add(Address.newBuilder()
					.setId(1)
					.setStreetAndNr(personDO.getAddresses().get(0).getSteetAndNr())
					.setZipAndCity(personDO.getAddresses().get(0).getZipAndCity()).build());
			
			Person person = Person.newBuilder().setId(1)
						.setFirstName(personDO.getFirstName())
						.setLastName(personDO.getLastName())
						.setEmailAddress(personDO.getEmailAddress())
						.setPhoneNumber(personDO.getPhoneNumber())
						.setFaxNumber(personDO.getFaxNumber())
						.setTitle(TitleEnum.valueOf("Mr"))
						.setDateOfBirth(LocalDate.parse("1995-11-10"))
						.setAddresses(addresses).build();
			persons.add(person);
		}
		
		final DatumWriter<Person> datumWriter = new SpecificDatumWriter<>(Person.class);
		final DataFileWriter<Person> dataFileWriter = new DataFileWriter<>(datumWriter);

		try {
		    dataFileWriter.create(persons.get(0).getSchema(), new File(CONTAINER_FILE_NAME_V1));
		    
		    // specify block size
		    dataFileWriter.setSyncInterval(1000);
		    persons.forEach(employee -> {
		        try {
		            dataFileWriter.append(employee);
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }

		    });
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
		    dataFileWriter.close();
		}		

	}
	
	@Test
	public void testWriteToSnappyCompresssedContainerFileV1() throws IOException {
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
					.setFaxNumber("+41 31 322 33 22")
					.setTitle(TitleEnum.Mr)
					.setDateOfBirth(LocalDate.parse("1995-11-10"))
					.setAddresses(addresses).build();
		for (int i = 1; i<100; i++) {
			persons.add(person1);
		}

		final DatumWriter<Person> datumWriter = new SpecificDatumWriter<>(Person.class);
		final DataFileWriter<Person> dataFileWriter = new DataFileWriter<>(datumWriter);
		dataFileWriter.setCodec(CodecFactory.snappyCodec());
		
		try {
		    dataFileWriter.create(persons.get(0).getSchema(), new File(SNAPPY_COMPRESSESD_CONTAINER_FILE_NAME_V1));
		    
		    // specify block size
		    dataFileWriter.setSyncInterval(1000);
		    persons.forEach(employee -> {
		        try {
		            dataFileWriter.append(employee);
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }

		    });
		} catch (IOException e) {
			throw new RuntimeException(e);
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
