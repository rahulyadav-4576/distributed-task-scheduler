package com.taskscheduler.worker_service.repository;

import com.taskscheduler.worker_service.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Long> {
}
