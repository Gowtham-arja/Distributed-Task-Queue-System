import React from 'react'

const STATE_STYLES = {
  IDLE:    { box: 'border-emerald-500/50 bg-emerald-500/10 text-emerald-400', label: 'IDLE',    dot: 'bg-emerald-400' },
  BUSY:    { box: 'border-blue-500/50 bg-blue-500/10 text-blue-400',         label: 'BUSY',    dot: 'bg-blue-400 animate-pulse' },
  DEAD:    { box: 'border-red-500/50 bg-red-500/10 text-red-400',           label: 'DEAD',    dot: 'bg-red-400' },
  UNKNOWN: { box: 'border-slate-700 bg-slate-800/50 text-slate-500',        label: 'OFFLINE', dot: 'bg-slate-600' },
}

function QueueBox({ task }) {
  return (
    <div className="flex-shrink-0 w-24 rounded-lg border border-slate-700 bg-slate-800/70 px-2 py-2 text-center">
      <div className="text-[10px] font-mono text-slate-400 truncate">{task.type}</div>
      <div className="text-[9px] font-mono text-slate-600 truncate">{task.id?.slice(0, 8)}</div>
    </div>
  )
}

function WorkerBox({ worker }) {
  const style = STATE_STYLES[worker.state] || STATE_STYLES.UNKNOWN
  const showConnector = worker.state === 'IDLE'

  return (
    <div className="flex flex-col items-center gap-2 w-32">
      <div
        className={`w-px h-8 border-l-2 transition-colors duration-500 ${
          showConnector ? 'border-dashed border-emerald-500/50' : 'border-transparent'
        }`}
      />
      <div className={`w-full rounded-xl border-2 px-3 py-4 text-center transition-colors duration-500 ${style.box}`}>
        <div className="flex items-center justify-center gap-1.5 mb-1">
          <span className={`w-1.5 h-1.5 rounded-full ${style.dot}`} />
          <span className="text-[10px] font-mono tracking-widest">{style.label}</span>
        </div>
        <div className="text-xs font-mono font-semibold">{worker.workerId}</div>
        {worker.state === 'BUSY' && worker.currentTask && (
          <div className="text-[9px] font-mono opacity-70 truncate mt-1">{worker.currentTask.slice(0, 8)}</div>
        )}
      </div>
    </div>
  )
}

export default function PipelineView({ stats }) {
  const queue = stats?.queue || []
  const workers = stats?.workers || []

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-4">
      <span className="text-xs font-mono text-slate-400 tracking-widest uppercase mb-4 block">Live Pipeline</span>

      <div className="rounded-lg border border-slate-800 bg-slate-950/50 px-3 py-2 overflow-x-auto mb-3">
        <div className="flex gap-2 min-h-[52px] items-center">
          {queue.length === 0 ? (
            <span className="text-xs font-mono text-slate-700">Queue empty</span>
          ) : (
            queue.map((t, i) => <QueueBox key={t.id || i} task={t} />)
          )}
        </div>
      </div>

      <div className="h-px bg-slate-800 mx-1" />

      <div className="flex justify-center gap-10 mt-1 pb-1 flex-wrap">
        {workers.length === 0 ? (
          <span className="text-xs font-mono text-slate-700 py-4">No workers reporting</span>
        ) : (
          workers.map(w => <WorkerBox key={w.workerId} worker={w} />)
        )}
      </div>
    </div>
  )
}