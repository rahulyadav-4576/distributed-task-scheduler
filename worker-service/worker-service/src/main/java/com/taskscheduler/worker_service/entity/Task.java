package com.taskscheduler.worker_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskType;
    private String payload;
    private String status;
    private int retryCount;
    private LocalDateTime createdAt=LocalDateTime.now();
    private LocalDateTime scheduledAt;
}
