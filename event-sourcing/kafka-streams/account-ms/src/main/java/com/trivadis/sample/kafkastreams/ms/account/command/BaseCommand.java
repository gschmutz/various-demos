package com.trivadis.sample.kafkastreams.ms.account.command;

import org.springframework.util.Assert;

public class BaseCommand<T> {

	private final String __command = this.getClass().getSimpleName();
	private  T id;

	public BaseCommand(T id) {
		Assert.notNull(id, "Id must be not null");
		this.id = id;
	}
	public BaseCommand() {}

	public String get__command() {
		return __command;
	}
	public T getId() {
		return id;
	}
}
