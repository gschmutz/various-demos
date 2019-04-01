package com.trivadis.sample.kafkastreams.ms.account.event;

import java.math.BigDecimal;


/**
 * @author gschmutz
 *
 */
public class AccountCreatedEvent extends BaseEvent<String> {

	private String forCustomerId;
	private String accountType;
	private BigDecimal balance;

	public AccountCreatedEvent(String id, String forCustomerId, String accountType, BigDecimal balance) {
		super(id);
		this.forCustomerId = forCustomerId;
		this.accountType = accountType;
		this.balance = balance;
	}
	
	public AccountCreatedEvent() {
		
	}

	public String getForCustomerId() {
		return forCustomerId;
	}

	public String getAccountType() {
		return accountType;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setForCustomerId(String forCustomerId) {
		this.forCustomerId = forCustomerId;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
}
