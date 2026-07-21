package com.taskscheduler.worker_service.listener;

import com.taskscheduler.worker_service.config.RabbitMQConfig;
import com.taskscheduler.worker_service.entity.Task;
import com.taskscheduler.worker_service.repository.TaskRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TaskListener {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final int MAX_RETRIES = 3;

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void processTask(String taskId) {
        String lockKey = "lock:task:" + taskId;

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            System.out.println("Task " + taskId + " is already being processed. Skipping.");
            return;
        }

        Long id = Long.parseLong(taskId);
        Task task = taskRepository.findById(id).orElse(null);

        try {
            if (task == null) {
                System.out.println("Task not found: " + id);
                return;
            }

            task.setStatus("PROCESSING");
            taskRepository.save(task);

            simulateTaskWork(task);

            task.setStatus("COMPLETED");
            taskRepository.save(task);
            System.out.println("Task completed: " + id);

        } catch (Exception e) {
            handleFailure(task, id, e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private void simulateTaskWork(Task task) throws Exception {
        Thread.sleep(1000);
        // Testing ke liye: agar payload mein "FAIL" word hai, to jaanbujh ke fail karo
        if (task.getPayload().contains("FAIL")) {
            throw new RuntimeException("Simulated task failure");
        }
        System.out.println("Processing task: " + task.getTaskType() + " -> " + task.getPayload());
    }

    private void handleFailure(Task task, Long id, Exception e) {
        if (task == null) return;

        int currentRetry = task.getRetryCount() + 1;
        task.setRetryCount(currentRetry);

        if (currentRetry >= MAX_RETRIES) {
            // Max retries khatam — permanently fail, DLQ mein bhejo
            task.setStatus("FAILED");
            taskRepository.save(task);
            System.out.println("Task " + id + " permanently failed after " + currentRetry + " attempts. Sending to DLQ.");

            // Ye exception RabbitMQ ko batata hai: "requeue mat karo, seedha DLQ mein bhej do"
            throw new AmqpRejectAndDontRequeueException("Max retries exceeded for task " + id);
        } else {
            task.setStatus("PENDING");
            taskRepository.save(task);
            System.out.println("Task " + id + " failed. Retry attempt " + currentRetry + "/" + MAX_RETRIES);

            // Exponential backoff: retry se pehle wait karo (2s, 4s, 8s...)
            try {
                long backoffMillis = (long) Math.pow(2, currentRetry) * 1000;
                Thread.sleep(backoffMillis);
            } catch (InterruptedException ignored) {}

            // Exception throw karo taaki RabbitMQ dobara requeue kare
            throw new RuntimeException("Retrying task " + id, e);
        }
    }

}
