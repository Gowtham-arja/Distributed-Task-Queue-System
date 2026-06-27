package com.taskqueue.producer.model;

import java.util.UUID;

public class Task {
    private String id;
    private TaskType type;
    private String payload;
    private TaskStatus status;
    private int retryCount;
    private int maxRetries = 3;
    private String workerId;
    private long createdAt;
    private long startedAt;
    private long completedAt;
    private String errorMessage;

    public Task() {}

    public static Task create(TaskType type, String payload, int maxRetries) {
        Task t = new Task();
        t.id = UUID.randomUUID().toString();
        t.type = type;
        t.payload = payload;
        t.status = TaskStatus.PENDING;
        t.retryCount = 0;
        t.maxRetries = maxRetries > 0 ? maxRetries : 3;
        t.createdAt = System.currentTimeMillis();
        return t;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
