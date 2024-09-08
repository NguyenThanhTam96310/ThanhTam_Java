package com.nguyentam.example05.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.nguyentam.example05.entity.Cart;
import com.nguyentam.example05.entity.CartItem;
import com.nguyentam.example05.entity.Order;
import com.nguyentam.example05.entity.OrderItem;
import com.nguyentam.example05.entity.Payment;
import com.nguyentam.example05.entity.Product;
import com.nguyentam.example05.exceptions.APIException;
import com.nguyentam.example05.exceptions.ResourceNotFoundException;
import com.nguyentam.example05.payloads.OrderDTO;
import com.nguyentam.example05.payloads.OrderItemDTO;
import com.nguyentam.example05.payloads.OrderResponse;
import com.nguyentam.example05.repository.CartItemRepo;
import com.nguyentam.example05.repository.CartRepo;
import com.nguyentam.example05.repository.OrderItemRepo;
import com.nguyentam.example05.repository.OrderRepo;
import com.nguyentam.example05.repository.PaymentRepo;
import com.nguyentam.example05.repository.UserRepo;
import com.nguyentam.example05.service.CartService;
import com.nguyentam.example05.service.OrderService;
import com.nguyentam.example05.service.UserService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {
  @Autowired
  public UserRepo userRepo;
  @Autowired
  public CartRepo cartRepo;
  @Autowired
  public OrderRepo orderRepo;
  @Autowired
  private PaymentRepo paymentRepo;
  @Autowired
  public OrderItemRepo orderItemRepo;
  @Autowired
  public CartItemRepo cartItemRepo;
  @Autowired
  public UserService userService;
  @Autowired
  public CartService cartService;
  @Autowired
  public ModelMapper modelMapper;

  @Override
  public OrderDTO placeOrder(String emailld, Long cartId, String paymentMethod) {
    Cart cart = cartRepo.findCartByEmailAndCartId(emailld, cartId);
    if (cart == null) {
      throw new ResourceNotFoundException("Cart", "cartId", cartId);
    }
    Order order = new Order();
    order.setEmail(emailld);
    order.setOrderDate(LocalDate.now());
    order.setTotalAmount(cart.getTotalPrice());
    order.setOrderStatus("Order Accepted!");
    Payment payment = new Payment();
    payment.setOrder(order);
    payment.setPaymentMethod(paymentMethod);
    payment = paymentRepo.save(payment);
    order.setPayment(payment);

    Order savedOrder = orderRepo.save(order);
    List<CartItem> cartItems = cart.getCartItems();
    if (cartItems.size() == 0) {
      throw new APIException("Cart is empty");
    }
    List<OrderItem> orderItems = new ArrayList<>();
    for (CartItem cartItem : cartItems) {
      OrderItem orderItem = new OrderItem();
      orderItem.setProduct(cartItem.getProduct());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setDiscount(cartItem.getDiscount());

      orderItem.setOrderedProductPrice(cartItem.getProductPrice());
      orderItem.setOrder(savedOrder);
      orderItems.add(orderItem);
    }

    orderItems = orderItemRepo.saveAll(orderItems);
    cart.getCartItems().forEach(item -> {
      int quantity = item.getQuantity();
      Product product = item.getProduct();
      cartService.deleteProductFromCart(cartId, item.getProduct().getProductId());
      product.setQuantity(product.getQuantity() - quantity);
    });
    OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
    orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));
    return orderDTO;
  }

  @Override
  public List<OrderDTO> getOrdersByUser(String emailId) {
    List<Order> orders = orderRepo.findAllByEmail(emailId);
    List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
        .collect(Collectors.toList());
    if (orderDTOs.size() == 0) {
      throw new APIException("No orders placed yet by the user with email: " + emailId);
    }
    return orderDTOs;
  }

  @Override
  public OrderDTO getOrder(String emailld, Long orderId) {
    Order order = orderRepo.findOrderByEmailAndOrderId(emailld, orderId);
    if (order == null) {
      throw new ResourceNotFoundException("Order", "orderId", orderId);
    }
    return modelMapper.map(order, OrderDTO.class);
  }

  @Override
  public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    Page<Order> pageOrders = orderRepo.findAll(pageDetails);
    List<Order> orders = pageOrders.getContent();
    List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
        .collect(Collectors.toList());
    if (orderDTOs.size() == 0) {
      throw new APIException("No orders placed yet by the users");
    }
    OrderResponse orderResponse = new OrderResponse();
    orderResponse.setContent(orderDTOs);
    orderResponse.setPageNumber(pageOrders.getNumber());
    orderResponse.setPageSize(pageOrders.getSize());
    orderResponse.setTotalElements(pageOrders.getTotalElements());
    orderResponse.setTotalPages(pageOrders.getTotalPages());
    orderResponse.setLastPage(pageOrders.isLast());
    return orderResponse;
  }

  @Override
  public OrderDTO updateOrder(String emailId, Long orderId, String orderStatus) {
    Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);
    if (order == null) {
      throw new ResourceNotFoundException("Order", "orderId", orderId);
    }
    order.setOrderStatus(orderStatus);
    return modelMapper.map(order, OrderDTO.class);
  }
}