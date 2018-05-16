package com.trivadis.semantic.rml;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;

public class TestRML {

	public void test() {
		InputStream inputStream = TestRML.class
				.getClassLoader().getResourceAsStream("input.json");
		
		Set<TriplesMap> mapping = RmlMappingLoader.build().load(Paths.get("/Users/gus/workspace/git/trivadis/boehringer/rml-test/src/main/resources/mapping.ttl"), RDFFormat.TURTLE);

		RmlMapper mapper = RmlMapper.newBuilder().build();
		mapper.bindInputStream("input", inputStream);

		Model result = mapper.map(mapping);
		Iterator<Statement> iter = result.iterator();
		while (iter.hasNext()) {
			Statement s = iter.next();
			System.out.println(s);
		}
	}
	
	public static void main(String[] args) {
		TestRML rml = new TestRML();
		rml.test();
	}
	
/*
	public byte[] convertToJson(GenericRecord record) {
		Decoder binaryDecoder = DecoderFactory.get().get().binaryDecoder(value.toString().getBytes(), null);

		GenericDatumReader<GenericRecord> payloadReader = new SpecificDatumReader<GenericRecord>(schema);
		System.out.println("test1" + binaryDecoder.toString().length() + "test");
		GenericRecord recode = null;
		recode = payloadReader.read(recode, binaryDecoder);
		System.out.println("test2");
	}
*/
}
