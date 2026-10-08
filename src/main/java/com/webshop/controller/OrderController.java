package com.webshop.controller;

import com.webshop.dto.OrderRequest;
import com.webshop.dto.OrderResponse;
import com.webshop.dto.OrderStatusRequest;
import com.webshop.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getAllOrders(@RequestParam(required = false) Integer customerId) {
        return orderService.search(customerId);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Integer id) {
        return orderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Integer id, @Valid @RequestBody OrderStatusRequest request) {
        return orderService.changeStatus(id, request.status());
    }
}
