package com.taskqueue.worker;

public class WorkerApplication {
    public static void main(String[] args) throws Exception {
        String workerId = System.getenv().getOrDefault("WORKER_ID", "worker-local");
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        Worker w = new Worker(workerId, redisHost, 6379);
        Runtime.getRuntime().addShutdownHook(new Thread(w::shutdown));
        w.start();
        Thread.currentThread().join();
    }
}
