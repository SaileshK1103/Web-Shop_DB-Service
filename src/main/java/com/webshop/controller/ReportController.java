package com.webshop.controller;

import com.webshop.entity.OrderStatusHistory;
import com.webshop.service.ReportService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/orders/{orderId}/status-history")
    public List<OrderStatusHistory> getOrderStatusHistory(@PathVariable Integer orderId) {
        return reportService.findOrderStatusHistory(orderId);
    }

}
