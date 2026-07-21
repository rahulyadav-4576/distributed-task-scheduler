package com.taskscheduler.producer_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskRequest {
    @NotBlank(message = "task type is required")
    private String taskType;
    @NotBlank(message = "Payload is required")
    private String payload;
}
