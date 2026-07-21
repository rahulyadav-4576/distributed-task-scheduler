package com.taskscheduler.producer_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskType;          //"EMAIL", "PAYMENT_RETRY"
    private String payload;
    private String status;           // PENDING, PROCESSING, COMPLETED, FAILED
    private int retryCount=0;
    private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime scheduledAt;
}
