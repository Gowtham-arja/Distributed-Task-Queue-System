package com.taskqueue.worker.threads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.worker.Worker;
import com.taskqueue.worker.model.Task;
import redis.clients.jedis.Jedis;

import java.util.List;

public class TaskPullerThread implements Runnable {
    private final Worker w;
    private final ObjectMapper mapper = new ObjectMapper();

    public TaskPullerThread(Worker w) { this.w = w; }

    @Override
    public void run() {
        while (w.isRunning()) {
            try (Jedis j = w.pool.getResource()) {
                List<String> res = j.brpop(0, "task:queue");
                if (res == null || res.size() < 2) continue;
                String json = res.get(1);
                Task t = mapper.readValue(json, Task.class);
                t.status = "PROCESSING";
                t.workerId = w.workerId;
                t.startedAt = System.currentTimeMillis();
                j.hset("task:" + t.id, "json", mapper.writeValueAsString(t));
                j.hset("task:" + t.id, "status", t.status);
                w.log("Pulled task " + t.id + " (" + t.type + ")");
                w.executionQueue.put(t);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                w.log("Puller error: " + e.getMessage() + ". Reconnecting in 5s.");
                try { Thread.sleep(5000); } catch (InterruptedException ie) { return; }
            }
        }
    }
}
