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

<svg viewBox="0 0 950 480" xmlns="http://www.w3.org/2000/svg" font-family="'Segoe UI', Arial, sans-serif">
  <defs>
    <marker id="arrowY" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L0,6 L9,3 z" fill="#5b6472"/>
    </marker>
    <marker id="arrowR" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L0,6 L9,3 z" fill="#f87171"/>
    </marker>
    <marker id="arrowG" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
      <path d="M0,0 L0,6 L9,3 z" fill="#4ade80"/>
    </marker>
  </defs>

  <rect width="950" height="480" fill="#0f1420"/>

  <text x="475" y="42" text-anchor="middle" fill="#f5f7fa" font-size="22" font-weight="700">Task Lifecycle — Distributed Task Queue</text>

  <!-- PENDING -->
  <rect x="40" y="180" width="150" height="80" rx="14" fill="#16213a" stroke="#94a3b8" stroke-width="2"/>
  <text x="115" y="215" text-anchor="middle" fill="#e6ebf5" font-size="16" font-weight="700">PENDING</text>
  <text x="115" y="235" text-anchor="middle" fill="#9aa5b8" font-size="11">queued in Redis</text>

  <!-- PROCESSING -->
  <rect x="280" y="180" width="160" height="80" rx="14" fill="#16213a" stroke="#3b82f6" stroke-width="2"/>
  <text x="360" y="215" text-anchor="middle" fill="#e6ebf5" font-size="16" font-weight="700">PROCESSING</text>
  <text x="360" y="235" text-anchor="middle" fill="#6ea8fe" font-size="11">worker holds task</text>
  <text x="360" y="250" text-anchor="middle" fill="#6ea8fe" font-size="11">sends heartbeats</text>

  <!-- COMPLETED -->
  <rect x="540" y="70" width="170" height="80" rx="14" fill="#16213a" stroke="#22c55e" stroke-width="2"/>
  <text x="625" y="105" text-anchor="middle" fill="#e6ebf5" font-size="16" font-weight="700">COMPLETED</text>
  <text x="625" y="125" text-anchor="middle" fill="#4ade80" font-size="11">task finished OK</text>

  <!-- FAILED -->
  <rect x="540" y="290" width="170" height="80" rx="14" fill="#16213a" stroke="#f59e0b" stroke-width="2"/>
  <text x="625" y="325" text-anchor="middle" fill="#e6ebf5" font-size="16" font-weight="700">FAILED</text>
  <text x="625" y="345" text-anchor="middle" fill="#fbbf24" font-size="11">error / crash / TTL expiry</text>

  <!-- DEAD -->
  <rect x="770" y="290" width="150" height="80" rx="14" fill="#2a1616" stroke="#ef4444" stroke-width="2"/>
  <text x="845" y="325" text-anchor="middle" fill="#e6ebf5" font-size="16" font-weight="700">DEAD</text>
  <text x="845" y="345" text-anchor="middle" fill="#f87171" font-size="11">moved to DLQ</text>

  <!-- PENDING -> PROCESSING -->
  <line x1="190" y1="220" x2="275" y2="220" stroke="#5b6472" stroke-width="2" marker-end="url(#arrowY)"/>
  <text x="232" y="210" text-anchor="middle" fill="#9aa5b8" font-size="11">worker BRPOP</text>

  <!-- PROCESSING -> COMPLETED -->
  <path d="M400,180 C420,150 480,120 535,105" fill="none" stroke="#4ade80" stroke-width="2" marker-end="url(#arrowG)"/>
  <text x="470" y="130" text-anchor="middle" fill="#4ade80" font-size="11">success</text>

  <!-- PROCESSING -> FAILED -->
  <path d="M400,260 C420,290 480,315 535,330" fill="none" stroke="#f87171" stroke-width="2" marker-end="url(#arrowR)"/>
  <text x="470" y="300" text-anchor="middle" fill="#fbbf24" font-size="11">error / no heartbeat</text>

  <!-- FAILED -> PROCESSING (retry) -->
  <path d="M560,290 C480,240 420,240 400,225" fill="none" stroke="#5b6472" stroke-width="2" stroke-dasharray="6,4" marker-end="url(#arrowY)"/>
  <text x="480" y="205" text-anchor="middle" fill="#9aa5b8" font-size="11">retry (attempts &lt; max)</text>

  <!-- FAILED -> DEAD -->
  <line x1="710" y1="330" x2="765" y2="330" stroke="#f87171" stroke-width="2" marker-end="url(#arrowR)"/>
  <text x="737" y="315" text-anchor="middle" fill="#f87171" font-size="11">max retries</text>
  <text x="737" y="350" text-anchor="middle" fill="#f87171" font-size="11">exceeded</text>

  <!-- Legend -->
  <text x="475" y="430" text-anchor="middle" fill="#7c8699" font-size="13">Solid = state transition · Dashed = automatic retry loop</text>
  <text x="475" y="453" text-anchor="middle" fill="#7c8699" font-size="13">Heartbeat + TTL checks detect stalled workers and requeue their tasks as FAILED</text>
</svg>

# 💡 Design Highlights

- Why BRPOP over polling: blocking pops avoid busy-wait polling, cutting latency and Redis load compared to a naive while(true) { poll() } loop.
- Why heartbeats + TTL: if a worker crashes mid-task, its heartbeat expires and the task is automatically requeued instead of being stuck in PROCESSING forever.
- Why a DLQ: repeatedly failing tasks are isolated for manual inspection instead of endlessly retrying and starving the queue.
