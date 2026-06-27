package com.taskqueue.worker.model;

public class Task {
    public String id;
    public String type;
    public String payload;
    public String status;
    public int retryCount;
    public int maxRetries = 3;
    public String workerId;
    public long createdAt;
    public long startedAt;
    public long completedAt;
    public String errorMessage;
}
