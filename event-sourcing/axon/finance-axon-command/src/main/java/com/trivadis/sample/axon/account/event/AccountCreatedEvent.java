package com.trivadis.sample.axon.account.event;

import java.math.BigDecimal;


/**
 * @author gschmutz
 *
 */
public class AccountCreatedEvent extends BaseEvent<String> {

	private String forCustomerId;
	private String accountType;
	private BigDecimal balance;

	public AccountCreatedEvent(String id, String forCustoemrId, String accountType, BigDecimal balance) {
		super(id);
		this.forCustomerId = forCustoemrId;
		this.accountType = accountType;
		this.balance = balance;
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

	@Override
	public String toString() {
		return "AccountCreatedEvent [forCustomerId=" + forCustomerId + ", accountType=" + accountType + ", balance="
				+ balance + ", get__eventType()=" + get__eventType() + ", getId()=" + getId() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}
	
}
