import React from 'react'
import { Server } from 'lucide-react'

function timeAgo(ts) {
  if (!ts) return 'unknown'
  const diff = Math.floor((Date.now() - ts) / 1000)
  if (diff < 5) return 'just now'
  if (diff < 60) return `${diff}s ago`
  return `${Math.floor(diff / 60)}m ago`
}

export default function WorkerList({ stats }) {
  const workers = stats?.workers ?? []

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-4 h-full">
      <div className="flex items-center gap-2 mb-4">
        <Server size={13} className="text-slate-500" />
        <span className="text-xs font-mono text-slate-400 tracking-widest uppercase">Worker Nodes</span>
        <span className="ml-auto font-mono text-xs text-slate-600">{workers.length} registered</span>
      </div>

      {workers.length === 0 ? (
        <div className="flex items-center justify-center h-24 text-slate-600 text-xs font-mono">
          No workers detected yet
        </div>
      ) : (
        <div className="space-y-2">
          {workers.map((w) => {
            const alive = w.status === 'ALIVE'
            return (
              <div
                key={w.id}
                className={`flex items-center gap-3 rounded-lg px-3 py-2.5 border transition-colors
                  ${alive
                    ? 'border-emerald-400/10 bg-emerald-400/5'
                    : 'border-red-400/10 bg-red-400/5'
                  }`}
              >
                {/* status dot */}
                <div className="relative flex-shrink-0">
                  <div className={`w-2 h-2 rounded-full ${alive ? 'bg-emerald-400' : 'bg-red-400'}`} />
                  {alive && (
                    <div className="absolute inset-0 w-2 h-2 rounded-full bg-emerald-400 animate-ping opacity-60" />
                  )}
                </div>

                {/* worker id */}
                <span className="font-mono text-xs text-slate-300 flex-1">{w.id}</span>

                {/* tasks processed */}
                <span className="font-mono text-xs text-slate-500">
                  {w.tasksProcessed ?? 0} tasks
                </span>

                {/* last seen */}
                <span className={`font-mono text-xs ${alive ? 'text-emerald-400' : 'text-red-400'}`}>
                  {alive ? timeAgo(w.lastHeartbeat) : 'DEAD'}
                </span>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
