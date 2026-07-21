package com.taskscheduler.producer_service.controller;

import com.taskscheduler.producer_service.config.RabbitMQConfig;
import com.taskscheduler.producer_service.dto.TaskRequest;
import com.taskscheduler.producer_service.entity.Task;
import com.taskscheduler.producer_service.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        task.setTaskType(request.getTaskType());
        task.setPayload(request.getPayload());
        task.setStatus("PENDING");
        Task savedTask = taskRepository.save(task);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TASK_QUEUE, savedTask.getId().toString());

        return ResponseEntity.ok(savedTask);
    }
        @GetMapping("/{id}")
        public ResponseEntity<Task> getTask(@PathVariable Long id) {
            return taskRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

    }

}
