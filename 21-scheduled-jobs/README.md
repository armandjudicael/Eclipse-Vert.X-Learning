# Module 21: Scheduled Jobs and Cron

## Overview
This module demonstrates scheduling periodic tasks and cron jobs with Eclipse Vert.x using Quartz scheduler.

## Key Concepts Covered

### 1. **Job Scheduling**
- Cron expressions
- Periodic tasks
- One-time jobs
- Job triggers

### 2. **Quartz Scheduler**
- Job definition
- Trigger configuration
- Listener implementation
- Persistence

### 3. **Advanced Features**
- Distributed job scheduling
- Job clustering
- Misfire handling
- Job monitoring

### 4. **Best Practices**
- Error handling
- Logging and monitoring
- Resource management
- Graceful shutdown

## Project Structure
```
21-scheduled-jobs/
├── src/main/java/com/vertx/scheduler/
│   └── MainVerticle.java
├── src/main/resources/
│   └── logback.xml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Running the Application

### Using Docker Compose
```bash
cd 21-scheduled-jobs
docker-compose up --build

# View scheduled jobs
curl http://localhost:8080/api/jobs
```

## Cron Expression Examples

```
0 0 * * * ?           # Every day at midnight
0 */15 * * * ?        # Every 15 minutes
0 0 0 1 * ?           # First day of month
0 0 12 * * MON-FRI    # Weekdays at noon
```

## Learning Objectives
- ✅ Cron expression syntax
- ✅ Quartz scheduler configuration
- ✅ Job implementation
- ✅ Distributed scheduling
- ✅ Monitoring and alerting

## References
- [Quartz Scheduler Documentation](https://www.quartz-scheduler.org/)
- [Cron Expression Format](https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html)
- [Vert.x Scheduler](https://vertx.io/docs/vertx-core/java/#_periodic_and_delayed_actions)