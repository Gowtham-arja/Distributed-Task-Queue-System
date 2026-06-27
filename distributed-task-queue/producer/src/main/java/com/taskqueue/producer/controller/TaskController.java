package com.taskqueue.producer.controller;

import com.taskqueue.producer.model.Task;
import com.taskqueue.producer.model.TaskType;
import com.taskqueue.producer.service.StatsService;
import com.taskqueue.producer.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService tasks;
    private final StatsService stats;

    @Autowired
    public TaskController(TaskService tasks, StatsService stats) {
        this.tasks = tasks; 
        this.stats = stats;
    }

    @PostMapping
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) throws Exception {
        TaskType type = TaskType.valueOf((String) body.get("type"));
        String payload = String.valueOf(body.getOrDefault("payload", "{}"));
        int max = body.get("maxRetries") == null ? 3 : ((Number) body.get("maxRetries")).intValue();
        Task t = tasks.submit(type, payload, max);
        Map<String, Object> r = new HashMap<>();
        r.put("taskId", t.getId());
        r.put("status", t.getStatus().name());
        r.put("message", "Task enqueued");
        return r;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> get(@PathVariable String id) throws Exception {
        Task t = tasks.get(id);
        return t == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(t);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return stats.getStats(); }

    @GetMapping("/failed")
    public List<Task> failed() throws Exception { return tasks.dlqTasks(); }

    @PostMapping("/retry/{id}")
    public Map<String, Object> retry(@PathVariable String id) throws Exception {
        boolean ok = tasks.retry(id);
        Map<String, Object> r = new HashMap<>();
        r.put("retried", ok);
        return r;
    }
}
