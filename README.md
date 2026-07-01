# ✨ Features

- 📥 Redis BRPOP-based queuing — workers block-pop tasks for low-latency dispatch instead of polling.
- 🔄 5-state task lifecycle — PENDING → PROCESSING → COMPLETED / FAILED → DEAD for full task traceability.
- 💓 Heartbeat & TTL-based liveness detection — automatically detects and recovers tasks from crashed/stalled workers.
- ☠️ Dead Letter Queue (DLQ) — tasks that repeatedly fail are moved to a DLQ for inspection instead of being lost or retried forever.
- 📡 Real-time WebSocket dashboard — live view of queue depth, task states, and worker health.
- 🐳 Docker Compose setup — spin up Redis, backend, and workers with a single command.

# 🏗️ Tech Stack

- Java
- Spring Boot
- Redis (BRPOP-based queuing)
- WebSockets
- Docker, Docker Compose

# 📐 Architecture

<img width="575" height="389" alt="image" src="https://github.com/user-attachments/assets/14e44132-0a80-4ed6-bea4-5f00a29f05de" />


# 🚀 Task Lifecycle
<img width="575" height="290" alt="image" src="https://github.com/user-attachments/assets/c13fee00-ecdc-4d33-bb39-49146b3464dc" />


# 💡 Design Highlights

- Why BRPOP over polling: blocking pops avoid busy-wait polling, cutting latency and Redis load compared to a naive while(true) { poll() } loop.
- Why heartbeats + TTL: if a worker crashes mid-task, its heartbeat expires and the task is automatically requeued instead of being stuck in PROCESSING forever.
- Why a DLQ: repeatedly failing tasks are isolated for manual inspection instead of endlessly retrying and starving the queue.
