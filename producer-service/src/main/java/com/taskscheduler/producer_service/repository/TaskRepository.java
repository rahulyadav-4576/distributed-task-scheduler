package com.taskscheduler.producer_service.repository;

import com.taskscheduler.producer_service.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Long> {
}
