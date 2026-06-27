package com.taskqueue.worker.threads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.worker.Worker;
import com.taskqueue.worker.executor.TaskExecutorEngine;
import com.taskqueue.worker.model.Task;
import redis.clients.jedis.Jedis;

public class TaskExecutorThread implements Runnable {
    private final Worker w;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TaskExecutorEngine engine = new TaskExecutorEngine();

    public TaskExecutorThread(Worker w) { this.w = w; }

    @Override
    public void run() {
        while (w.isRunning()) {
            Task t;
            try { t = w.executionQueue.take(); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }

            long start = System.currentTimeMillis();
            try {
                engine.execute(t);
                t.status = "COMPLETED";
                t.completedAt = System.currentTimeMillis();
                try (Jedis j = w.pool.getResource()) {
                    j.hset("task:" + t.id, "json", mapper.writeValueAsString(t));
                    j.hset("task:" + t.id, "status", t.status);
                    j.incr("task:stats:completed");
                }
                w.log("Completed task " + t.id + " in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception e) {
                t.retryCount++;
                t.errorMessage = e.getMessage();
                try (Jedis j = w.pool.getResource()) {
                    if (t.retryCount < t.maxRetries) {
                        t.status = "PENDING";
                        j.lpush("task:queue", mapper.writeValueAsString(t));
                        j.hset("task:" + t.id, "json", mapper.writeValueAsString(t));
                        j.hset("task:" + t.id, "status", t.status);
                        w.log("Task " + t.id + " failed, retry " + t.retryCount + "/" + t.maxRetries);
                    } else {
                        t.status = "DEAD";
                        j.lpush("task:dlq", mapper.writeValueAsString(t));
                        j.hset("task:" + t.id, "json", mapper.writeValueAsString(t));
                        j.hset("task:" + t.id, "status", t.status);
                        j.incr("task:stats:failed");
                        w.log("Task " + t.id + " sent to DLQ after " + t.retryCount + " retries");
                    }
                } catch (Exception re) {
                    w.log("Redis error handling failure: " + re.getMessage());
                }
            }
        }
    }
}
