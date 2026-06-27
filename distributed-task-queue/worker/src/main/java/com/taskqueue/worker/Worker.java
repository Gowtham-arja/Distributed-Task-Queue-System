package com.taskqueue.worker;

import com.taskqueue.worker.model.Task;
import com.taskqueue.worker.threads.*;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.LinkedBlockingQueue;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Worker {
    public final String workerId;
    public final JedisPool pool;
    public final LinkedBlockingQueue<Task> executionQueue = new LinkedBlockingQueue<>();
    private final Thread puller, executor, heartbeat;
    private volatile boolean running = true;

    public Worker(String workerId, String host, int port) {
        this.workerId = workerId;
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(8);
        this.pool = new JedisPool(cfg, host, port);
        this.puller = new Thread(new TaskPullerThread(this), "puller");
        this.executor = new Thread(new TaskExecutorThread(this), "executor");
        this.heartbeat = new Thread(new HeartbeatThread(this), "heartbeat");
        puller.setDaemon(true); executor.setDaemon(true); heartbeat.setDaemon(true);
    }

    public void start() {
        log("Worker starting");
        puller.start(); executor.start(); heartbeat.start();
    }

    public boolean isRunning() { return running; }

    public void shutdown() {
        log("Shutdown requested");
        running = false;
        puller.interrupt(); executor.interrupt(); heartbeat.interrupt();
        pool.close();
    }

    public void log(String msg) {
        String ts = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        System.out.println("[" + ts + "] [" + workerId + "] " + msg);
    }
}
