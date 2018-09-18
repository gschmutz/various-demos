package com.hortonworks.simulator.impl.domain;

public abstract class AbstractEventEmitter extends AbstractDomainObject {
	private static final long serialVersionUID = 3553392748138862662L;

	public AbstractEventEmitter() {

	}

	public abstract Event generateEvent();
}