package com.trivadis.sample.kafkastreams.ms.account.event;

import org.springframework.util.Assert;

public class BaseEvent<T> {

	private final String __eventType = this.getClass().getSimpleName();
	private  T id;
	
	public BaseEvent(T id) {
		Assert.notNull(id, "Id must be not null");
		this.id = id;
	}
	
	public BaseEvent() {
		
	}

	public String get__eventType() {
		return __eventType;
	}

	public T getId() {
		return id;
	}

}
