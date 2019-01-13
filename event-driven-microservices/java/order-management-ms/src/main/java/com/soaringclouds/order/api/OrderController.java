package com.soaringclouds.order.api;

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
import com.soaringclouds.order.model.OrderDO;
import com.soaringclouds.order.repository.OrderRepository;
import com.soaringclouds.order.service.OrderService;

@RestController()
public class OrderController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;
    
    private void createOrder(OrderApi orderApi) throws ParseException {
        OrderDO orderDO = OrderConverter.convert(orderApi);
        orderService.createOrder(orderDO);
        LOGGER.info("Prodcut created: " + orderDO);
    }
    
    private void modifyOrder(OrderApi orderApi) throws ParseException {
        OrderDO orderDO = OrderConverter.convert(orderApi);
        orderService.modifyOrder(orderDO);
        LOGGER.info("Prodcut created: " + orderDO);
    }

    @RequestMapping(value= "/api/orders",
            method = RequestMethod.POST,
            consumes = "application/json") 
    @Transactional
    public void postOrder(@RequestBody @Valid OrderApi orderApi) throws ParseException {
        Preconditions.checkNotNull(orderApi);
        
        createOrder(orderApi);
    }
    
    @RequestMapping(value= "/api/order",
            method = RequestMethod.PUT,
            consumes = "application/json") 
    @Transactional
    public void putOrder(@RequestBody @Valid OrderApi orderApi) throws ParseException {
        Preconditions.checkNotNull(orderApi);
        Preconditions.checkNotNull(orderApi.getOrderId());
        
        modifyOrder(orderApi);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value= "/api/order/{id}"
    )
    //@CrossOrigin(origins = "http://localhost:4200")
    public OrderApi getOrder(@PathVariable(value="id") String id)  {
        OrderApi order = new OrderApi();
        OrderDO orderDO = null;
        // trim leading and training double quote
        id = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(id, '"'),'"');
        
        //ordersDO = orderRepository.findAll(); 
        
        if (id != null && id.length() > 0) {
        		orderDO = orderRepository.findById(id);
        }
        
        if(orderDO != null) {
        		order = OrderConverter.convert(orderDO);
        }
        return order;
    }
    
    @RequestMapping(
            method = RequestMethod.GET,
            value= "/api/orders"
    )
    //@CrossOrigin(origins = "http://localhost:4200")
    public List<OrderApi> getOrders(@RequestParam(value="code", defaultValue="") String code,
    										@RequestParam(value="name", defaultValue="") String name,
    										@RequestParam(value="categoryName", defaultValue="") String categoryName)  {
        OrderApi order = new OrderApi();
        List<OrderDO> ordersDO = new ArrayList<OrderDO>();
        List<OrderApi> orders = new ArrayList<OrderApi>();
        Predicate<OrderDO> pred = null;
        
        // trim leading and training double quote
//        id = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(id, '"'),'"');
        code = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(code, '"'),'"');
        name = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(name, '"'),'"');
        categoryName = StringUtils.trimTrailingCharacter(StringUtils.trimLeadingCharacter(categoryName, '"'),'"');
        
        //ordersDO = orderRepository.findAll(); 
        
        if (name != null && name.length() > 0) {		
            	ordersDO = orderRepository.findOrdersByNameRegex(name);        	
        } else if (code != null && code.length() > 0) {	
        	System.out.println (code);        	
//    			OrderDO orderDO = orderRepository.findByOrderCode(code);  
//    			if (orderDO != null) {
//    				ordersDO.add(orderDO);
    			}
        		//ordersDO.removeIf(p-> !(p.getOrderCode().equals(code)));
//        } else {
//        		ordersDO = orderRepository.findAll();   
//        }
        
        if (pred != null)
        		ordersDO.removeIf(pred);
        
        for (OrderDO orderDO : ordersDO) {
        		order = OrderConverter.convert(orderDO);
        		orders.add(order);
        }
        return orders;
    }


    
}