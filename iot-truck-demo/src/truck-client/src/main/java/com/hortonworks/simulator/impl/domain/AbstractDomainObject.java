package com.hortonworks.simulator.impl.domain;

import akka.actor.UntypedActor;
import com.hortonworks.simulator.interfaces.DomainObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.logging.Logger;

public abstract class AbstractDomainObject extends UntypedActor implements DomainObject,
		Serializable {
	private static final long serialVersionUID = -2630503054916573455L;
	protected Logger logger = Logger.getLogger(this.getClass().toString());
	
	private String delimiter = ",";
	
	protected String getDelimiter() {
		return delimiter;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).toString();
	}
}
