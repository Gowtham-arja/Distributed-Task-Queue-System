package com.taskqueue.producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.producer.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public StatsService(StringRedisTemplate redis) { this.redis = redis; }

    public Map<String, Object> getStats() {
        Map<String, Object> out = new LinkedHashMap<>();
        Long pending = redis.opsForList().size(TaskService.QUEUE);
        Long dlq = redis.opsForList().size(TaskService.DLQ);
        String completed = redis.opsForValue().get("task:stats:completed");
        String failed = redis.opsForValue().get("task:stats:failed");

        out.put("pending", pending == null ? 0 : pending);
        out.put("dlqSize", dlq == null ? 0 : dlq);
        out.put("completed", completed == null ? 0 : Long.parseLong(completed));
        out.put("failed", failed == null ? 0 : Long.parseLong(failed));

        Set<String> hbKeys = redis.keys("worker:*:heartbeat");
        List<Map<String, Object>> workers = new ArrayList<>();
        int processing = 0;
        if (hbKeys != null) {
            for (String k : hbKeys) {
                String ts = redis.opsForValue().get(k);
                String id = k.replace("worker:", "").replace(":heartbeat", "");
                String state = redis.opsForValue().get("worker:" + id + ":state");
                String currentTask = redis.opsForValue().get("worker:" + id + ":currentTask");
                Map<String, Object> w = new HashMap<>();
                w.put("workerId", id);
                w.put("lastHeartbeat", ts == null ? 0 : Long.parseLong(ts));
                w.put("alive", ts != null);
                w.put("state", state == null ? "UNKNOWN" : state);
                w.put("currentTask", currentTask);
                workers.add(w);
                if ("BUSY".equals(state)) processing++;
            }
        }
        out.put("workers", workers);
        out.put("processing", processing);
        out.put("queue", listTasks(TaskService.QUEUE));
        out.put("dlq", listTasks(TaskService.DLQ));
        out.put("timestamp", System.currentTimeMillis());
        return out;
    }

    private List<Map<String, Object>> listTasks(String redisKey) {
        List<String> raw = redis.opsForList().range(redisKey, 0, -1);
        List<Map<String, Object>> out = new ArrayList<>();
        if (raw == null) return out;
        for (String s : raw) {
            try {
                Task t = mapper.readValue(s, Task.class);
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getId());
                m.put("type", t.getType());
                m.put("errorMessage", t.getErrorMessage());
                out.add(m);
            } catch (Exception ignored) { }
        }
        return out;
    }
}