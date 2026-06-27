import React from 'react'
import { useWebSocket } from './hooks/useWebSocket'
import StatsCards from './components/StatsCards'
import TaskChart from './components/TaskChart'
import WorkerList from './components/WorkerList'
import TaskSubmitForm from './components/TaskSubmitForm'
import DLQViewer from './components/DLQViewer'
import { Activity } from 'lucide-react'

const WS_URL = `ws://${window.location.host}/ws/stats`

export default function App() {
  const { stats, isConnected } = useWebSocket(WS_URL)

  return (
    <div className="min-h-screen bg-[#0a0e1a] text-slate-200 p-4 md:p-6">

      {/* Navbar */}
      <header className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center">
            <Activity size={15} className="text-blue-400" />
          </div>
          <div>
            <h1 className="font-mono font-semibold text-sm text-slate-100 leading-none">
              Distributed Task Queue
            </h1>
            <p className="font-mono text-xs text-slate-600 mt-0.5">
              Redis · Spring Boot · Java Workers
            </p>
          </div>
        </div>

        {/* Connection indicator */}
        <div className={`flex items-center gap-2 px-3 py-1.5 rounded-full border text-xs font-mono
          ${isConnected
            ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-400'
            : 'border-red-500/30 bg-red-500/10 text-red-400'
          }`}
        >
          <div className={`w-1.5 h-1.5 rounded-full ${isConnected ? 'bg-emerald-400 animate-pulse' : 'bg-red-400'}`} />
          {isConnected ? 'Live' : 'Reconnecting...'}
        </div>
      </header>

      {/* Stats cards row */}
      <div className="mb-4">
        <StatsCards stats={stats} />
      </div>

      {/* Chart + Submit form */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-4">
        <div className="md:col-span-3">
          <TaskChart stats={stats} />
        </div>
        <div className="md:col-span-2">
          <TaskSubmitForm />
        </div>
      </div>

      {/* Workers + DLQ */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <WorkerList stats={stats} />
        <DLQViewer />
      </div>

      {/* Footer */}
      <footer className="mt-6 text-center font-mono text-xs text-slate-700">
        Broadcasting every 2s via WebSocket · DLQ polling every 10s
      </footer>
    </div>
  )
}
