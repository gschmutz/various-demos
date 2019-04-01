package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;

/**
 * @author saikatkar1
 *
 */
public class AccountCreatedEvent extends BaseEvent<String> {

	private String forCustomerId;
	private String accountType;
	private BigDecimal balance;
	public AccountCreatedEvent(String __eventId, String id,String forCustomerId,String accountType,BigDecimal balance) {
		super(__eventId, id);
		this.setForCustomerId(forCustomerId);
		this.setBalance(balance);
		this.setAccountType(accountType);
	}
	
	public AccountCreatedEvent() {}

	public String getForCustomerId() {
		return forCustomerId;
	}

	public void setForCustomerId(String forCustomerId) {
		this.forCustomerId = forCustomerId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
