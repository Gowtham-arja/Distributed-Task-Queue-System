package com.taskqueue.producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskqueue.producer.model.Task;
import com.taskqueue.producer.model.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ReaperService {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ReaperService(StringRedisTemplate redis) { this.redis = redis; }

    // Runs every 5s — same cadence as a worker's heartbeat write, so a dead
    // worker is caught within roughly one heartbeat TTL (15s) of expiring.
    @Scheduled(fixedRate = 5000)
    public void reap() {
        Set<String> stateKeys = redis.keys("worker:*:state");
        if (stateKeys == null) return;

        for (String stateKey : stateKeys) {
            String workerId = stateKey.replace("worker:", "").replace(":state", "");
            String currentState = redis.opsForValue().get(stateKey);

            if ("DEAD".equals(currentState)) continue; // already handled

            Boolean alive = redis.hasKey("worker:" + workerId + ":heartbeat");
            if (Boolean.TRUE.equals(alive)) continue; // still alive, nothing to do

            handleDeadWorker(workerId, currentState);
        }
    }

    private void handleDeadWorker(String workerId, String lastKnownState) {
        String taskKey = "worker:" + workerId + ":currentTask";

        if ("BUSY".equals(lastKnownState)) {
            String taskId = redis.opsForValue().get(taskKey);
            if (taskId != null) {
                requeueOrphanedTask(taskId, workerId);
            }
        }

        redis.opsForValue().set("worker:" + workerId + ":state", "DEAD");
        redis.delete(taskKey);
    }

    private void requeueOrphanedTask(String taskId, String workerId) {
        try {
            Object json = redis.opsForHash().get("task:" + taskId, "json");
            if (json == null) return; // task record missing, nothing to recover

            Task t = mapper.readValue(json.toString(), Task.class);
            t.setStatus(TaskStatus.PENDING);
            String updatedJson = mapper.writeValueAsString(t);

            redis.opsForList().leftPush(TaskService.QUEUE, updatedJson);
            redis.opsForHash().put("task:" + taskId, "json", updatedJson);
            redis.opsForHash().put("task:" + taskId, "status", TaskStatus.PENDING.name());

            System.out.println("[Reaper] Worker " + workerId + " died mid-task — requeued " + taskId);
        } catch (Exception e) {
            System.out.println("[Reaper] Failed to requeue task " + taskId + " from dead worker " + workerId + ": " + e.getMessage());
        }
    }
}