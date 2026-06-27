package com.taskqueue.producer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {
    private final StringRedisTemplate redis;

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
                Map<String, Object> w = new HashMap<>();
                w.put("workerId", id);
                w.put("lastHeartbeat", ts == null ? 0 : Long.parseLong(ts));
                w.put("alive", ts != null);
                workers.add(w);
            }
        }
        out.put("workers", workers);
        out.put("processing", processing);
        out.put("timestamp", System.currentTimeMillis());
        return out;
    }
}
