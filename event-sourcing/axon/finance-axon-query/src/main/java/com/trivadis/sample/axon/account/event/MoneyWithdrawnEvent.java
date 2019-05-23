package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;

public class MoneyWithdrawnEvent extends BaseEvent<String> {
	private  BigDecimal amount;
	private long when;
	
	public MoneyWithdrawnEvent(String __eventId, String id, BigDecimal amount, long when) {
		super(__eventId, id);
		this.amount = amount;
		this.when = when;
	}
	
	public MoneyWithdrawnEvent() {
		
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public long getWhen() {
		return when;
	}
	
	
}