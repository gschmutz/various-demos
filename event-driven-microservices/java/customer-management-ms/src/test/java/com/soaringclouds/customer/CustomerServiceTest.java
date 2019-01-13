package com.soaringclouds.customer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.soaringclouds.customer.model.CustomerDO;
import com.soaringclouds.customer.service.CustomerService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CustomerServiceTest {

    @Autowired
    private CustomerService sut;

    @Test
    public void shouldSaveCustomer() throws InterruptedException {
        CustomerDO customer = new CustomerDO();
        customer.setFirstName("Peter");
        customer.setLastName("Sample");
        customer.setTitle("Mr");        
        customer.setTitle("guido.schmutz@trivadis.com");        
        sut.createCustomer(customer);
        
        Thread.sleep(10000);
    }
}
