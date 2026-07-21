# Distributed Task Scheduler & Notification System

A microservices-based backend system that processes asynchronous tasks reliably at scale, using message queues, distributed locking, and automatic retry with dead-letter handling.

## Overview

This project simulates a real-world production task-processing pipeline (similar to background job systems used for sending emails, processing payments, or generating reports). It demonstrates asynchronous processing, fault tolerance, and secure API design using a microservices architecture.

## Architecture

```
Client → [Producer Service] → RabbitMQ Queue → [Worker Service] → PostgreSQL
              (REST API,                            (Consumer,
              JWT Auth)                              Redis Lock,
                                                       Retry Logic)
                                                            ↓
                                                    Dead Letter Queue
                                                    (after max retries)
```

- **Producer Service** — exposes REST APIs to create and query tasks, secured with JWT authentication. Publishes task IDs to RabbitMQ.
- **Worker Service** — consumes tasks from the queue, processes them, and updates their status in PostgreSQL.
- **Redis** — used for distributed locking to prevent the same task from being processed twice if multiple worker instances are running.
- **RabbitMQ** — message broker for asynchronous communication between services; also handles dead-letter routing for permanently failed tasks.
- **PostgreSQL** — stores task metadata and user credentials.

## Key Features

- **Asynchronous task processing** via RabbitMQ, decoupling task creation from execution
- **JWT-based authentication** with secure password hashing (BCrypt)
- **Distributed locking with Redis** to prevent duplicate task execution across multiple worker instances
- **Automatic retry with exponential backoff** (up to 3 attempts) for transient failures
- **Dead Letter Queue (DLQ)** for tasks that fail permanently, enabling manual inspection without blocking the main queue
- **Layered architecture** (Controller → Service → Repository) for maintainability

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot, Spring Security, Spring Data JPA |
| Messaging | RabbitMQ |
| Caching / Locking | Redis |
| Database | PostgreSQL |
| Auth | JWT (JJWT library) |
| Containerization | Docker, Docker Compose |

## Getting Started

### Prerequisites
- Java 17+
- Maven
- Docker Desktop

### Setup

1. Clone the repository
2. Start infrastructure (PostgreSQL, Redis, RabbitMQ):
   ```bash
   docker compose up -d
   ```
3. Run the Producer Service:
   ```bash
   cd producer-service
   ./mvnw spring-boot:run
   ```
4. Run the Worker Service:
   ```bash
   cd worker-service
   ./mvnw spring-boot:run
   ```

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and receive JWT token | No |
| POST | `/api/tasks` | Create a new task | Yes |
| GET | `/api/tasks/{id}` | Get task status by ID | Yes |

### Example: Create a task
```http
POST /api/tasks
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "taskType": "EMAIL",
  "payload": "Send welcome email to user123"
}
```

## What This Project Demonstrates

- Designing and coordinating **multiple independent microservices**
- Handling **asynchronous, event-driven communication** between services
- Implementing **fault-tolerant systems** with retries and dead-letter handling
- Preventing **race conditions** in distributed systems using Redis locks
- Securing REST APIs with **JWT authentication**

## Future Improvements

- Add a monitoring dashboard for task status (e.g., using Spring Boot Actuator + Prometheus)
- Support for scheduled/delayed task execution
- Horizontal scaling test with multiple Worker Service instances
