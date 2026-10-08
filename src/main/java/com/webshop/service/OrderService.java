package com.webshop.service;

import com.webshop.dto.OrderItemRequest;
import com.webshop.dto.OrderRequest;
import com.webshop.dto.OrderResponse;
import com.webshop.entity.Customer;
import com.webshop.entity.CustomerAddress;
import com.webshop.entity.Order;
import com.webshop.entity.OrderItem;
import com.webshop.entity.OrderItemId;
import com.webshop.entity.OrderStatus;
import com.webshop.entity.Product;
import com.webshop.exception.BadRequestException;
import com.webshop.exception.ConflictException;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.OrderMapper;
import com.webshop.repository.CustomerAddressRepository;
import com.webshop.repository.CustomerRepository;
import com.webshop.repository.OrderRepository;
import com.webshop.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository,
            ProductRepository productRepository,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    public List<OrderResponse> search(Integer customerId) {
        return findOrders(customerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    public OrderResponse findById(Integer id) {
        return orderMapper.toResponse(getOrder(id));
    }

    // Placing an order is one transaction: the order, its items and every stock change are stored together.
    // If any item fails, the exception rolls everything back and no stock is taken
    @Transactional
    public OrderResponse create(OrderRequest request) {
        rejectRepeatedProducts(request.items());
        Customer customer = findCustomer(request.customerId());

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(findShippingAddress(request.shippingAddressId(), customer.getId()));
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        for (OrderItemRequest itemRequest : inProductIdOrder(request.items())) {
            order.getOrderItems().add(reserveItem(order, itemRequest));
        }
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Integer id) {
        Order order = getOrder(id);
        order.setStatus(order.getStatus().next());
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse changeStatus(Integer id, OrderStatus newStatus) {
        Order order = getOrder(id);
        if (!order.getStatus().canChangeTo(newStatus)) {
            throw new ConflictException(String.format(
                    "Order with id %d is %s and cannot be updated to %s", id, order.getStatus(), newStatus));
        }
        if (newStatus == OrderStatus.CANCELLED) {
            returnStock(order);
        }
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryDate(LocalDateTime.now());
        }
        order.setStatus(newStatus);
        return orderMapper.toResponse(order);
    }

    // Find orders of one customer, or all orders when no customer is given
    private List<Order> findOrders(Integer customerId) {
        if (customerId != null) {
            return orderRepository.findByCustomerId(customerId);
        }
        return orderRepository.findAll();
    }

    private Order getOrder(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    // Reserve product item
    private OrderItem reserveItem(Order order, OrderItemRequest itemRequest) {
        Product product = findProductForUpdate(itemRequest.productId());
        if (product.getStockQuantity() < itemRequest.quantity()) {
            throw new ConflictException(String.format(
                    "Not enough stock for product %s (id %d): requested %d, available %d",
                    product.getName(), product.getId(), itemRequest.quantity(), product.getStockQuantity()));
        }
        product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());

        OrderItem item = new OrderItem();
        item.setId(new OrderItemId());
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(itemRequest.quantity());
        item.setUnitPrice(product.getPrice());
        return item;
    }

    private void returnStock(Order order) {
        for (OrderItem item : inProductIdOrder(order)) {
            Product product = findProductForUpdate(item.getProduct().getId());
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private Product findProductForUpdate(Integer productId) {
        return productRepository.findWithLockById(productId)
                .orElseThrow(() -> new BadRequestException(String.format("Product with id %d does not exist", productId)));
    }

    private Customer findCustomer(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BadRequestException(String.format("Customer with id %d does not exist", customerId)));
    }

    // Shipping address is optional defined by nullable in the db schema, but when given it must belong to the customer
    private CustomerAddress findShippingAddress(Integer shippingAddressId, Integer customerId) {
        if (shippingAddressId == null) {
            return null;
        }
        return customerAddressRepository.findByIdAndCustomerId(shippingAddressId, customerId)
                .orElseThrow(() -> new BadRequestException(String.format(
                        "Shipping address with id %d is not an address of customer %d", shippingAddressId, customerId)));
    }

    // Reject multiple product in same order
    private void rejectRepeatedProducts(List<OrderItemRequest> items) {
        long distinctProducts = items.stream().map(OrderItemRequest::productId).distinct().count();
        if (distinctProducts != items.size()) {
            throw new BadRequestException("A product can only appear once in an order, use the quantity instead");
        }
    }

    // Sort lock requests for consistent lock,makes lock orders always reproducible and consistent
    private List<OrderItemRequest> inProductIdOrder(List<OrderItemRequest> items) {
        return items.stream().sorted(Comparator.comparing(OrderItemRequest::productId)).toList();
    }

    // Sort lock requests for product, makes lock orders always reproducible and consistent
    private List<OrderItem> inProductIdOrder(Order order) {
        return order.getOrderItems().stream()
                .sorted(Comparator.comparing((OrderItem item) -> item.getProduct().getId()))
                .toList();
    }
}
