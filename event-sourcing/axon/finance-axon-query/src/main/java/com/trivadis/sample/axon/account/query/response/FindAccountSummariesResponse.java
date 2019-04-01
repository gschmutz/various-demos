package com.trivadis.sample.axon.account.query.response;

import java.util.List;

import com.trivadis.sample.axon.account.model.Account;

public class FindAccountSummariesResponse {

	private List<Account> accounts;

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}
}
