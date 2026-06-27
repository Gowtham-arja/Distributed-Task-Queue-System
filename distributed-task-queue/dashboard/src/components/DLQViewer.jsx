import React, { useState, useEffect, useCallback } from 'react'
import { RefreshCw, RotateCcw, AlertTriangle } from 'lucide-react'

function formatTime(ts) {
  if (!ts) return '—'
  return new Date(ts).toLocaleTimeString()
}

export default function DLQViewer() {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(false)
  const [retrying, setRetrying] = useState(null)

  const fetchDLQ = useCallback(async () => {
    setLoading(true)
    try {
      const res = await fetch('/tasks/failed')
      if (res.ok) {
        const data = await res.json()
        setTasks(Array.isArray(data) ? data : [])
      }
    } catch (_) {}
    finally { setLoading(false) }
  }, [])

  useEffect(() => {
    fetchDLQ()
    const t = setInterval(fetchDLQ, 10000)
    return () => clearInterval(t)
  }, [fetchDLQ])

  async function retry(id) {
    setRetrying(id)
    try {
      await fetch(`/tasks/retry/${id}`, { method: 'POST' })
      await fetchDLQ()
    } catch (_) {}
    finally { setRetrying(null) }
  }

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-4 h-full flex flex-col">
      <div className="flex items-center gap-2 mb-4">
        <AlertTriangle size={13} className="text-red-400" />
        <span className="text-xs font-mono text-slate-400 tracking-widest uppercase">Dead Letter Queue</span>
        <span className="ml-1 px-1.5 py-0.5 rounded bg-red-400/10 text-red-400 text-xs font-mono">
          {tasks.length}
        </span>
        <button
          onClick={fetchDLQ}
          className="ml-auto text-slate-600 hover:text-slate-400 transition-colors"
          title="Refresh"
        >
          <RefreshCw size={13} className={loading ? 'animate-spin' : ''} />
        </button>
      </div>

      {tasks.length === 0 ? (
        <div className="flex flex-col items-center justify-center flex-1 gap-2 text-slate-600">
          <AlertTriangle size={24} className="opacity-30" />
          <span className="text-xs font-mono">No dead tasks — system healthy</span>
        </div>
      ) : (
        <div className="space-y-2 overflow-y-auto flex-1">
          {tasks.map((t) => (
            <div
              key={t.id}
              className="rounded-lg border border-red-400/10 bg-red-400/5 px-3 py-2.5 text-xs font-mono"
            >
              <div className="flex items-center justify-between mb-1.5">
                <span className="text-red-400 font-medium">{t.type}</span>
                <button
                  onClick={() => retry(t.id)}
                  disabled={retrying === t.id}
                  className="flex items-center gap-1 text-slate-400 hover:text-blue-400 transition-colors disabled:opacity-40"
                >
                  <RotateCcw size={11} className={retrying === t.id ? 'animate-spin' : ''} />
                  Retry
                </button>
              </div>
              <div className="text-slate-500 truncate mb-1">{t.id}</div>
              <div className="text-red-400/70 truncate mb-1">{t.errorMessage || 'Unknown error'}</div>
              <div className="flex items-center justify-between text-slate-600">
                <span>retried {t.retryCount}×</span>
                <span>{formatTime(t.completedAt)}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
