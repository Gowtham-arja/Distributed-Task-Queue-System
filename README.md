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
