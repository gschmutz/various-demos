package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;

public class MoneyWithdrawnEvent extends BaseEvent<String> {
	private  BigDecimal amount;

	public MoneyWithdrawnEvent(String __eventId, String id, BigDecimal amount) {
		super(__eventId, id);
		this.amount = amount;
	}
	public MoneyWithdrawnEvent() {
		
	}

	public BigDecimal getAmount() {
		return amount;
	}
}