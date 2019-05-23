package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;

public class MoneyDepositedEvent extends BaseEvent<String> {
	private BigDecimal amount;
	private long when;

	public MoneyDepositedEvent(String id, BigDecimal amount, long when) {
		super(id);
		this.amount = amount;
		this.when = when;
	}

	
	public MoneyDepositedEvent() {
		
	}

	public BigDecimal getAmount() {
		return amount;
	}
	
	public long getWhen() {
		return when;
	}


	@Override
	public String toString() {
		return "MoneyDepositedEvent [amount=" + amount + ", when=" + when + ", get__eventType()=" + get__eventType()
				+ ", getId()=" + getId() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}	
	
	
}