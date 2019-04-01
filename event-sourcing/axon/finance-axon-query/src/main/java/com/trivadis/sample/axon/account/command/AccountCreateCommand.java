package com.trivadis.sample.axon.account.command;

/**
 * @author saikatkar1
 *
 */
public class AccountCreateCommand extends BaseCommand<String>{

	private String forCustomerId;
	private String accountType;
	
	public AccountCreateCommand(String id,String forCustomerId,String accountType) {
		super(id);
		this.forCustomerId = forCustomerId;
		this.accountType = accountType;
	
		// TODO Auto-generated constructor stub
	}
	public AccountCreateCommand() {}
	
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
	

}
