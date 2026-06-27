package com.taskqueue.worker.threads;

import com.taskqueue.worker.Worker;
import redis.clients.jedis.Jedis;

public class HeartbeatThread implements Runnable {
    private final Worker w;
    public HeartbeatThread(Worker w) { this.w = w; }

    @Override
    public void run() {
        while (w.isRunning()) {
            try (Jedis j = w.pool.getResource()) {
                j.setex("worker:" + w.workerId + ":heartbeat", 15, String.valueOf(System.currentTimeMillis()));
            } catch (Exception e) {
                w.log("Heartbeat error: " + e.getMessage());
            }
            try { Thread.sleep(5000); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
        }
    }
}
