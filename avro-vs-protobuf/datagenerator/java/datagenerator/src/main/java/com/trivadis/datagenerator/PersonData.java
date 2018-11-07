package com.trivadis.datagenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.trivadis.datagenerator.domain.AddressDO;
import com.trivadis.datagenerator.domain.PersonDO;


public class PersonData {
	
	public static List<PersonDO> getPersons() {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		JSONArray jsonArray = null;
		
		List<PersonDO> personsDO = new ArrayList<>();
		
        try {
            Object obj = parser.parse(new FileReader("/mnt/hgfs/git/gschmutz/various-demos/avro-vs-protobuf/datagenerator/java/datagenerator/src/main/resources/person.json"));

            jsonArray = (JSONArray) obj;
            
            Iterator<JSONObject> personIterator = jsonArray.iterator();
            
            while (personIterator.hasNext()) {
            	JSONObject person = personIterator.next();
            	
            	PersonDO personDO = new PersonDO();
            	personDO.setId(((Long)person.get("id")).intValue());
            	personDO.setFirstName((String)person.get("firstName"));
            	personDO.setLastName((String)person.get("lastName"));
            	personDO.setEmailAddress((String)person.get("emailAddress"));
            	personDO.setPhoneNumber((String)person.get("phoneNumber"));
            	
                AddressDO addressDO = new AddressDO(1, (String)person.get("streetAndNr"), (String)person.get("zipAndCity"));
                personDO.addAddress(addressDO);
                
            	personsDO.add(personDO);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
		}
        return personsDO;

	}

}
