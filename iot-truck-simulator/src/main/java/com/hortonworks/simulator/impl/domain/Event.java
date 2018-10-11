package com.hortonworks.simulator.impl.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Event {
	private String delimiter = ",";
	
	protected String getDelimiter() {
		return delimiter;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).toString();
	}
}
