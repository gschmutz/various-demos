package com.trivadis.sample.axon.account.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

/**
 * @author saikatkar1
 *
 */
public class Account implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -683252464592318120L;

	@Id
	private String accountNo;
	
	private BigDecimal balance;
	private String forCustomerId;
	private String accountType;
	private String lastUpdated;
	
	private List<Transaction> transactions;
	
	public Account(String accountNo, BigDecimal balance, String forCustomerId, String accountType,String lastUpdated) {
		super();
		this.accountNo = accountNo;
		this.balance = balance;
		this.forCustomerId = forCustomerId;
		this.lastUpdated = lastUpdated;
		this.accountType = accountType;
		this.transactions = new ArrayList<Transaction>();
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

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

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public void appendTransaction(Transaction transaction) {
		transactions.add(transaction);
	}

}

