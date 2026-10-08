package com.webshop.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Only the status history can be tested here. The views and the trigger are MariaDB only (sql/views.sql uses
// FOR SYSTEM_TIME), so the two view endpoints have no automated test and the history rows are inserted by the test
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void orderStatusHistoryReturnsTheLoggedChangesOfThatOrderOldestFirst() throws Exception {
        saveStatusChange(1, "PENDING", "CONFIRMED");
        saveStatusChange(2, "PENDING", "CANCELLED");
        saveStatusChange(1, "CONFIRMED", "SHIPPED");

        mockMvc.perform(get("/api/reports/orders/{orderId}/status-history", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].previousStatus").value(contains("PENDING", "CONFIRMED")))
                .andExpect(jsonPath("$[*].updatedStatus").value(contains("CONFIRMED", "SHIPPED")))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    // Stands in for the MariaDB trigger, which does not exist on H2
    private void saveStatusChange(int orderId, String previousStatus, String updatedStatus) {
        entityManager.createNativeQuery(
                        "insert into OrderStatusHistory (order_id, previous_status, updated_status, created_at)"
                                + " values (?, ?, ?, current_timestamp)")
                .setParameter(1, orderId)
                .setParameter(2, previousStatus)
                .setParameter(3, updatedStatus)
                .executeUpdate();
    }
}
