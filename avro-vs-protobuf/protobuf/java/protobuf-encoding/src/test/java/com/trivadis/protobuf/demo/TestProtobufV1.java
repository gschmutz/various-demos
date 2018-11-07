package com.trivadis.protobuf.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.trivadis.protobuf.person.v1.PersonWrapper;

public class TestProtobufV1 {

	private final static String BIN_FILE_NAME_V1 = "../../data/encoding_v1.0.bin";
	
	@Test
	public void testWriteToBinaryFileV1() throws IOException {
		List<String> skills = new ArrayList<>();
		
		skills.add("Avro");
		skills.add("Protobuf");
		skills.add("Kafka");
		
		PersonWrapper.Person person = PersonWrapper.Person.newBuilder().setId(1842)
					.setName("Guido Schmutz")
					.addAllSkills(skills).build();

		System.out.println(person);
		
		FileOutputStream output = new FileOutputStream(BIN_FILE_NAME_V1);
		person.writeTo(output);
	}
	
	@Test
	public void testReadFromBinaryFileV1() throws IOException {
		
		PersonWrapper.Person person =
			      PersonWrapper.Person.parseFrom(new FileInputStream(BIN_FILE_NAME_V1));
		System.out.println("Person:" + person);
		System.out.println("FirstName: " + person.getName());
		System.out.println("Unknown fields:" + person.getUnknownFields());
	}	
}
