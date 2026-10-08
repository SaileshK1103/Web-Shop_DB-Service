package com.webshop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Immutable;

// Read only table writes are done by triggers, so this entity is read only
@Entity
@Immutable
@Table(name = "OrderStatusHistory")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @Column(name = "updated_status", nullable = false, length = 50)
    private String updatedStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Integer getId() { return id; }

    public Integer getOrderId() { return orderId; }

    public String getPreviousStatus() { return previousStatus; }

    public String getUpdatedStatus() { return updatedStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
