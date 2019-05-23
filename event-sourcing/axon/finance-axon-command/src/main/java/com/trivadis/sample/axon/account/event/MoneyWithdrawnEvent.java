package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;

public class MoneyWithdrawnEvent extends BaseEvent<String> {
	private  BigDecimal amount;
	private long when;

	public MoneyWithdrawnEvent(String id, BigDecimal amount, long when) {
		super(id);
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

	@Override
	public String toString() {
		return "MoneyWithdrawnEvent [amount=" + amount + ", when=" + when + ", get__eventType()=" + get__eventType()
				+ ", getId()=" + getId() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	
}