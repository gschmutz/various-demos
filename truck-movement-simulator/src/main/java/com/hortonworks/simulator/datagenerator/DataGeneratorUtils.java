package com.hortonworks.simulator.datagenerator;

import org.apache.commons.codec.binary.Hex;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class DataGeneratorUtils {

	public static int getRandomInt(int max, boolean excludeMax) {
		if(excludeMax) {
			return ((int) (Math.random() * max));
		} else {
			return (int) Math.round((Math.random() * max));
		}	
	}
	
	public static int getRandomIntBetween(int min, int max, List<Integer> excludeList) {
		int value =getRandomInt(max, false);
		while(value < min || excludeList.contains(value)) {
			value = getRandomInt(max, false);
		}
		return value;
	}	
	
	public static long getRandomLong(long max, boolean excludeMax) {
		if(excludeMax) {
			return ((long) (Math.random() * max));
		} else {
			return (long) Math.round((Math.random() * max));
		}
		
	}	
	
	public static double getRandomDouble(double max, String decimalFormat) {
		DecimalFormat formatter = new DecimalFormat(decimalFormat);
		return Double.parseDouble(formatter.format(Math.random() * max));
		
	}
	
	public static int getRandomInt(int max, List<Integer> excludeList, boolean excludeMax) {
		int value =getRandomInt(max, excludeMax);
		while(excludeList.contains(value)) {
			value = getRandomInt(max, excludeMax);
		}
		return value;
	}	
	
	public static String getRandomString(int stringLength) {
		Random random = new Random();
		byte[] bytes = new byte[30];
		random.nextBytes(bytes);
		String key = Hex.encodeHexString(bytes);
		return key.substring(0,stringLength);
	}		
}
