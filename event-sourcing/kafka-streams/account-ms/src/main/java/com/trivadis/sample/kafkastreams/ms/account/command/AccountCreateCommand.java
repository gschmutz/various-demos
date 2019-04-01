package com.trivadis.sample.kafkastreams.ms.account.command;

import org.springframework.http.converter.json.GsonBuilderUtils;

/**
 * @author gschmutz
 *
 */
public class AccountCreateCommand extends BaseCommand<String>{

	private String forCustomerId;
	private String accountType;
	
	public AccountCreateCommand(String id,String forCustomerId, String accountType) {
		super(id);
		this.forCustomerId = forCustomerId;
		this.accountType = accountType;
	}

	public AccountCreateCommand() {
		
	}
	
	public String getForCustomerId() {
		return forCustomerId;
	}

	public String getAccountType() {
		return accountType;
	}

	@Override
	public String toString() {
		return "AccountCreateCommand [forCustomerId=" + forCustomerId + ", accountType=" + accountType
				+ ", get__command()=" + get__command() + ", getId()=" + getId() + "]";
	}

}
