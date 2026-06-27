package com.taskqueue.worker.executor;

import com.taskqueue.worker.model.Task;
import java.util.Random;

public class TaskExecutorEngine {
    private final Random rng = new Random();

    public void execute(Task t) throws Exception {
        switch (t.type) {
            case "SEND_EMAIL":       run("Sending email...", 2000, 0.10, "SMTP server unavailable"); break;
            case "RESIZE_IMAGE":     run("Resizing image...", 3000, 0.15, "Image corrupt"); break;
            case "GENERATE_REPORT":  run("Generating report...", 4000, 0.05, "Report generation failed"); break;
            case "SEND_NOTIFICATION":run("Sending notification...", 1000, 0.05, "Push gateway error"); break;
            case "PROCESS_PAYMENT":  run("Processing payment...", 3000, 0.20, "Payment gateway timeout"); break;
            default: throw new RuntimeException("Unknown type: " + t.type);
        }
    }

    private void run(String log, long ms, double failProb, String err) throws Exception {
        System.out.println("  -> " + log);
        Thread.sleep(ms);
        if (rng.nextDouble() < failProb) throw new RuntimeException(err);
    }
}
