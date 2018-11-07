package com.trivadis.avro.demo;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileConstants;
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
import org.apache.avro.specific.SpecificRecord;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.trivadis.avro.person.v1.Address;
import com.trivadis.avro.person.v1.Person;
import com.trivadis.avro.person.v1.TitleEnum;

public class TestAvroV1 {

	private final static String CONTAINER_FILE_NAME_V1 = "../../data/encoding_v1.0.avro";
	private final static String BIN_FILE_NAME_V1 = "../../data/encoding_v1.0.bin";
		
	@Test
	public void testWriteToBinaryFileV1() throws IOException {
		List<CharSequence> skills = new ArrayList<>();
		
		skills.add("Avro");
		skills.add("Protobuf");
		skills.add("Kafka");	
		
		Person person = Person.newBuilder().setId(1842)
					.setName("Guido Schmutz")
					.setSkills(skills).build();

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
	public void testWriteToContainerFileV1() throws IOException {
		List<Person> persons = new ArrayList<Person>();
		List<CharSequence> skills = new ArrayList<>();
		
		skills.add("Avro");
		skills.add("Protobuf");
		skills.add("Kafka");	
		
		Person person1 = Person.newBuilder().setId(1842)
					.setName("Guido Schmutz")
					.setSkills(skills).build();
		for (int i = 1; i<100; i++) {
			persons.add(person1);
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
