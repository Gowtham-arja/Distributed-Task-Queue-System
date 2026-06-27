# Distributed Task Queue

Production-inspired background job processing system: Java Spring Boot producer + plain-Java workers + Redis 7 + React dashboard.

## Architecture

```
   +----------+    LPUSH task:queue    +-------+   BRPOP   +----------+
   | Producer | ---------------------> | Redis | <-------- | Worker N |
   |  (HTTP)  |     task:dlq           |  7    |  SETEX    | (3 thr)  |
   +----------+ <--------------------- +-------+ <-------- +----------+
        |  WebSocket /ws/stats (2s)        ^                    |
        v                                  |                    |
   +-----------+        stats              |        heartbeats  |
   | Dashboard | <-----------------------------------------------
   |  (React)  |
   +-----------+
```

Each worker runs 3 daemon threads sharing a `LinkedBlockingQueue<Task>`:
- **TaskPullerThread** — `BRPOP task:queue`, marks PROCESSING, hands off.
- **TaskExecutorThread** — `take()`, runs `TaskExecutorEngine`, retries or DLQs on failure.
- **HeartbeatThread** — `SETEX worker:{id}:heartbeat 15 <ts>` every 5s.

## Run

```bash
docker compose up --build
```

- Producer: http://localhost:8080
- Redis: localhost:6379
- (Dashboard runs separately in the Lovable preview — point it at `http://localhost:8080`)

## Submit a task

```bash
curl -X POST http://localhost:8080/tasks \
  -H 'Content-Type: application/json' \
  -d '{"type":"SEND_EMAIL","payload":"{\"to\":\"a@b.com\"}","maxRetries":3}'
```

## Endpoints

| Method | Path                | Purpose                       |
|--------|---------------------|-------------------------------|
| POST   | /tasks              | Enqueue a task                |
| GET    | /tasks/{id}         | Inspect single task           |
| GET    | /tasks/stats        | Aggregate stats               |
| GET    | /tasks/failed       | DLQ contents                  |
| POST   | /tasks/retry/{id}   | Re-queue a DLQ task           |
| WS     | /ws/stats           | 2s broadcast of stats         |

## Crash test

Kill a worker mid-flight:
```bash
docker kill distributed-task-queue-worker-2-1
```
Its heartbeat key expires (15s TTL) → dashboard flips it to DEAD. In-flight tasks that were already pulled and not yet completed are lost (at-most-once for in-flight); pending tasks in `task:queue` are picked up by the remaining workers. Production hardening would use a per-worker processing list with `RPOPLPUSH` for exactly-once-style recovery.

## Tech decisions

- **Redis List vs Pub/Sub** — Lists are persistent and back-pressured; `BRPOP` gives blocking semantics and at-least-once semantics across workers. Pub/Sub drops messages if no subscriber is connected.
- **LinkedBlockingQueue handoff** — separates network I/O (puller) from CPU/IO work (executor) so a slow task can't stall the next `BRPOP`. Bounded queue would add back-pressure if needed.
- **3 separate threads per worker** — single responsibility, easier to reason about, each can fail/restart independently. Heartbeats stay alive even if the executor blocks.
- **At-most-once retries with cap → DLQ** — retries push back onto `task:queue` with incremented `retryCount`; after `maxRetries` the task lands in `task:dlq` for manual inspection / replay.

## Files

```
producer/  Spring Boot REST + WebSocket
worker/    Plain-Java 3-thread worker (Jedis + Jackson)
dashboard/ React dashboard (use the Lovable preview)
docker-compose.yml
```
