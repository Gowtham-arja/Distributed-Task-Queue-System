package com.taskqueue.producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.producer.model.Task;
import com.taskqueue.producer.model.TaskStatus;
import com.taskqueue.producer.model.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {
    public static final String QUEUE = "task:queue";
    public static final String DLQ = "task:dlq";

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public TaskService(StringRedisTemplate redis) { this.redis = redis; }

    public Task submit(TaskType type, String payload, int maxRetries) throws Exception {
        Task t = Task.create(type, payload, maxRetries);
        String json = mapper.writeValueAsString(t);
        redis.opsForList().leftPush(QUEUE, json);
        storeHash(t);
        return t;
    }

    public void storeHash(Task t) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("json", mapper.writeValueAsString(t));
        map.put("status", t.getStatus().name());
        redis.opsForHash().putAll("task:" + t.getId(), map);
    }

    public Task get(String id) throws Exception {
        Object json = redis.opsForHash().get("task:" + id, "json");
        if (json == null) return null;
        return mapper.readValue(json.toString(), Task.class);
    }

    public List<Task> dlqTasks() throws Exception {
        List<String> raw = redis.opsForList().range(DLQ, 0, -1);
        List<Task> out = new ArrayList<>();
        if (raw == null) return out;
        for (String s : raw) out.add(mapper.readValue(s, Task.class));
        return out;
    }

    public boolean retry(String id) throws Exception {
        List<String> raw = redis.opsForList().range(DLQ, 0, -1);
        if (raw == null) return false;
        for (String s : raw) {
            Task t = mapper.readValue(s, Task.class);
            if (t.getId().equals(id)) {
                redis.opsForList().remove(DLQ, 1, s);
                t.setRetryCount(0);
                t.setStatus(TaskStatus.PENDING);
                t.setErrorMessage(null);
                String json = mapper.writeValueAsString(t);
                redis.opsForList().leftPush(QUEUE, json);
                storeHash(t);
                return true;
            }
        }
        return false;
    }
}
