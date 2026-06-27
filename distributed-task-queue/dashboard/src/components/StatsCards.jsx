import React from 'react'
import { Clock, Zap, CheckCircle, XCircle } from 'lucide-react'

const cards = [
  {
    key: 'pending',
    label: 'Pending',
    icon: Clock,
    color: 'text-amber-400',
    border: 'border-amber-400/20',
    bg: 'bg-amber-400/5',
    glow: 'shadow-amber-400/10',
  },
  {
    key: 'processing',
    label: 'Processing',
    icon: Zap,
    color: 'text-blue-400',
    border: 'border-blue-400/20',
    bg: 'bg-blue-400/5',
    glow: 'shadow-blue-400/10',
    pulse: true,
  },
  {
    key: 'completed',
    label: 'Completed',
    icon: CheckCircle,
    color: 'text-emerald-400',
    border: 'border-emerald-400/20',
    bg: 'bg-emerald-400/5',
    glow: 'shadow-emerald-400/10',
  },
  {
    key: 'failed',
    label: 'Failed / DLQ',
    icon: XCircle,
    color: 'text-red-400',
    border: 'border-red-400/20',
    bg: 'bg-red-400/5',
    glow: 'shadow-red-400/10',
  },
]

export default function StatsCards({ stats }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
      {cards.map(({ key, label, icon: Icon, color, border, bg, glow, pulse }) => {
        const value = stats?.[key] ?? '—'
        return (
          <div
            key={key}
            className={`relative rounded-xl border ${border} ${bg} p-4 shadow-lg ${glow} overflow-hidden`}
          >
            {/* subtle corner accent */}
            <div className={`absolute top-0 right-0 w-16 h-16 rounded-bl-full opacity-10 ${bg.replace('/5', '/30')}`} />
            <div className="flex items-center gap-2 mb-3">
              <Icon size={14} className={`${color} ${pulse ? 'blink' : ''}`} />
              <span className="text-xs font-mono text-slate-400 tracking-widest uppercase">{label}</span>
            </div>
            <div className={`font-mono text-3xl font-semibold ${color}`}>
              {value}
            </div>
          </div>
        )
      })}
    </div>
  )
}
