package com.webshop.service;

import com.webshop.entity.OrderStatusHistory;
import com.webshop.repository.OrderStatusHistoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// uses views triggers e.t.c so this is read only service, no write operations are allowed
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public ReportService(OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    public List<OrderStatusHistory> findOrderStatusHistory(Integer orderId) {
        return orderStatusHistoryRepository.findByOrderIdOrderById(orderId);
    }

}
