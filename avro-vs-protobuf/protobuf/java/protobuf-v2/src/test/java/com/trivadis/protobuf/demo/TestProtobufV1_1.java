package com.trivadis.protobuf.demo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.protobuf.Timestamp;
import com.trivadis.protobuf.address.v1.AddressWrapper;
import com.trivadis.protobuf.person.v1.PersonWrapper;

import example.enumerations.TitleEnumWrapper;

public class TestProtobufV1_1 {

	private final static String BIN_FILE_NAME_V1 = "../../data/person_v1.0.bin";
	private final static String BIN_FILE_NAME_V2 = "../../data/person_v1.1.bin";
	
	@Test
	public void testSpecificRecord() throws IOException {
		List<AddressWrapper.Addresss> addresses = new ArrayList<>();
		
		addresses.add(AddressWrapper.Addresss.newBuilder()
				.setId(1)
				.setStreetAndNr("Somestreet 10")
				.setZipAndCity("9332 Somecity").build());
		
		Instant time = Instant.parse("1995-11-10T00:00:00.00Z");
		Timestamp timestamp = Timestamp.newBuilder().setSeconds(time.getEpochSecond())
			    .setNanos(time.getNano()).build();
		
		PersonWrapper.Person person = PersonWrapper.Person.newBuilder().setId(1)
					.setFirstName("Peter")
					.setMiddleName("Paul")
					.setLastName("Sample")
					.setEmailAddress("peter.sample@somecorp.com")
					.setPhoneNumber("+41 79 345 34 44")
					.setTitle(TitleEnumWrapper.Title.MR)
					.setDateOfBirth(timestamp)
					.addAllAddresses(addresses).build();

		System.out.println(person);
		
		FileOutputStream output = new FileOutputStream(BIN_FILE_NAME_V2);
		person.writeTo(output);
	}
	
	@Test
	public void testRead() throws IOException {
		
		PersonWrapper.Person person =
			      PersonWrapper.Person.parseFrom(new FileInputStream(BIN_FILE_NAME_V1));
		System.out.println(person.getMiddleName());
		System.out.println("Unknown fields:" + person.getUnknownFields());
	}	
}
