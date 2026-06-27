import React, { useState } from 'react'
import { Send, CheckCircle, AlertCircle } from 'lucide-react'

const TASK_TYPES = [
  'SEND_EMAIL',
  'RESIZE_IMAGE',
  'GENERATE_REPORT',
  'SEND_NOTIFICATION',
  'PROCESS_PAYMENT',
]

const DEFAULT_PAYLOADS = {
  SEND_EMAIL: '{"to": "user@gmail.com", "subject": "Hello"}',
  RESIZE_IMAGE: '{"url": "https://example.com/img.png", "width": 800}',
  GENERATE_REPORT: '{"reportId": "R-001", "format": "pdf"}',
  SEND_NOTIFICATION: '{"userId": "u-123", "message": "You have a new message"}',
  PROCESS_PAYMENT: '{"amount": 500, "userId": "u-123", "currency": "INR"}',
}

export default function TaskSubmitForm() {
  const [type, setType] = useState('SEND_EMAIL')
  const [payload, setPayload] = useState(DEFAULT_PAYLOADS['SEND_EMAIL'])
  const [maxRetries, setMaxRetries] = useState(3)
  const [status, setStatus] = useState(null) // null | {ok, message}
  const [loading, setLoading] = useState(false)

  function handleTypeChange(e) {
    const t = e.target.value
    setType(t)
    setPayload(DEFAULT_PAYLOADS[t])
  }

  async function handleSubmit() {
    setLoading(true)
    setStatus(null)
    try {
      const res = await fetch('/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type, payload, maxRetries: Number(maxRetries) }),
      })
      const data = await res.json()
      if (res.ok) {
        setStatus({ ok: true, message: `Enqueued — ${data.taskId}` })
      } else {
        setStatus({ ok: false, message: data.message || 'Failed to enqueue' })
      }
    } catch (e) {
      setStatus({ ok: false, message: 'Cannot reach producer. Is it running?' })
    } finally {
      setLoading(false)
      setTimeout(() => setStatus(null), 4000)
    }
  }

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-4 h-full flex flex-col">
      <span className="text-xs font-mono text-slate-400 tracking-widest uppercase mb-4 block">Submit Task</span>

      <div className="space-y-3 flex-1">
        {/* Task type */}
        <div>
          <label className="block text-xs font-mono text-slate-500 mb-1.5">Task Type</label>
          <select
            value={type}
            onChange={handleTypeChange}
            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm font-mono text-slate-200 focus:outline-none focus:border-blue-500/50 transition-colors"
          >
            {TASK_TYPES.map(t => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </div>

        {/* Payload */}
        <div>
          <label className="block text-xs font-mono text-slate-500 mb-1.5">Payload (JSON)</label>
          <textarea
            value={payload}
            onChange={e => setPayload(e.target.value)}
            rows={4}
            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-xs font-mono text-slate-200 focus:outline-none focus:border-blue-500/50 transition-colors resize-none leading-relaxed"
          />
        </div>

        {/* Max retries */}
        <div>
          <label className="block text-xs font-mono text-slate-500 mb-1.5">Max Retries</label>
          <input
            type="number"
            min={0}
            max={10}
            value={maxRetries}
            onChange={e => setMaxRetries(e.target.value)}
            className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm font-mono text-slate-200 focus:outline-none focus:border-blue-500/50 transition-colors"
          />
        </div>
      </div>

      {/* Submit button */}
      <button
        onClick={handleSubmit}
        disabled={loading}
        className="mt-4 w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 disabled:bg-slate-700 disabled:text-slate-500 text-white text-sm font-mono font-medium rounded-lg py-2.5 transition-colors"
      >
        <Send size={13} />
        {loading ? 'Sending...' : 'Submit Task'}
      </button>

      {/* Status toast */}
      {status && (
        <div className={`mt-3 flex items-center gap-2 rounded-lg px-3 py-2 text-xs font-mono
          ${status.ok
            ? 'bg-emerald-400/10 border border-emerald-400/20 text-emerald-400'
            : 'bg-red-400/10 border border-red-400/20 text-red-400'
          }`}
        >
          {status.ok ? <CheckCircle size={12} /> : <AlertCircle size={12} />}
          <span className="truncate">{status.message}</span>
        </div>
      )}
    </div>
  )
}
