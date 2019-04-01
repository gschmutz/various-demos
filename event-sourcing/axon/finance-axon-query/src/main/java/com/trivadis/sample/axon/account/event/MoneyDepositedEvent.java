package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;

public class MoneyDepositedEvent extends BaseEvent<String> {
	private  BigDecimal amount;

	public MoneyDepositedEvent(String __eventId, String id, BigDecimal amount) {
		super(__eventId, id);
		this.amount = amount;
	}
	
	public MoneyDepositedEvent() {
		
	}

	public BigDecimal getAmount() {
		return amount;
	}
}