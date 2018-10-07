package com.soaringclouds.customer.api;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Preconditions;
import com.soaringclouds.customer.model.CustomerDO;
import com.soaringclouds.customer.repository.CustomerRepository;
import com.soaringclouds.customer.service.CustomerService;

@RestController()
public class CustomerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;
    
    private void createCustomer(CustomerApi customerApi) throws ParseException {
        CustomerDO customerDO = CustomerConverter.convert(customerApi);
        customerService.createCustomer(customerDO);
        LOGGER.info("Prodcut created: " + customerDO);
    }
    
    private void modifyCustomer(CustomerApi customerApi) throws ParseException {
        CustomerDO customerDO = CustomerConverter.convert(customerApi);
        customerService.modifyCustomer(customerDO);
        LOGGER.info("Prodcut created: " + customerDO);
    }

    @RequestMapping(value= "/api/customers",
            method = RequestMethod.POST,
            consumes = "application/json") 
    @Transactional
    public void postCustomer(@RequestBody @Valid CustomerApi customerApi) throws ParseException {
        Preconditions.checkNotNull(customerApi);
        
        createCustomer(customerApi);
    }
    
    @RequestMapping(value= "/api/customer",
            method = RequestMethod.PUT,
            consumes = "application/json") 
    @Transactional
    public void putCustomer(@RequestBody @Valid CustomerApi customerApi) throws ParseException {
        Preconditions.checkNotNull(customerApi);
        Preconditions.checkNotNull(customerApi.getCustomerId());
        
        modifyCustomer(customerApi);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value= "/api/customer/{id}"
    )
    //@CrossOrigin(origins = "http://localhost:4200")
    public CustomerApi getCustomer(@PathVariable(value="id") String id)  {
        CustomerApi customer = new CustomerApi();
        CustomerDO customerDO = null;
        // trim leading and training double quote
        id = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(id, '"'),'"');
        
        //customersDO = customerRepository.findAll(); 
        
        if (id != null && id.length() > 0) {
        		customerDO = customerRepository.findById(id);
        }
        
        if(customerDO != null) {
        		customer = CustomerConverter.convert(customerDO);
        }
        return customer;
    }
    
    @RequestMapping(
            method = RequestMethod.GET,
            value= "/api/customers"
    )
    //@CrossOrigin(origins = "http://localhost:4200")
    public List<CustomerApi> getCustomers(@RequestParam(value="code", defaultValue="") String code,
    										@RequestParam(value="name", defaultValue="") String name,
    										@RequestParam(value="categoryName", defaultValue="") String categoryName)  {
        CustomerApi customer = new CustomerApi();
        List<CustomerDO> customersDO = new ArrayList<CustomerDO>();
        List<CustomerApi> customers = new ArrayList<CustomerApi>();
        Predicate<CustomerDO> pred = null;
        
        // trim leading and training double quote
//        id = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(id, '"'),'"');
        code = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(code, '"'),'"');
        name = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(name, '"'),'"');
        categoryName = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(categoryName, '"'),'"');
        
        //customersDO = customerRepository.findAll(); 
        
        if (name != null && name.length() > 0) {		
            	customersDO = customerRepository.findCustomersByNameRegex(name);        	
        } else if (code != null && code.length() > 0) {	
        	System.out.println (code);        	
//    			CustomerDO customerDO = customerRepository.findByCustomerCode(code);  
//    			if (customerDO != null) {
//    				customersDO.add(customerDO);
    			}
        		//customersDO.removeIf(p-> !(p.getCustomerCode().equals(code)));
//        } else {
//        		customersDO = customerRepository.findAll();   
//        }
        
        if (pred != null)
        		customersDO.removeIf(pred);
        
        for (CustomerDO customerDO : customersDO) {
        		customer = CustomerConverter.convert(customerDO);
        		customers.add(customer);
        }
        return customers;
    }


    
}