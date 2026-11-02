/**
 * Android home-screen Muscle Status widget.
 * Computes summary Balance / Fatigue / Strength from the same models as Stats
 * (lib/recovery.js) and pushes them to MuscleWidgetPlugin.
 */
import { Capacitor, registerPlugin } from '@capacitor/core'
import { fatigueOf, strengthOf } from './recovery.js'
import { loadOfWorkouts } from './muscles.js'

const MuscleWidget = registerPlugin('MuscleWidget')
const WRITE_DEBOUNCE_MS = 1500

function pct(n) {
  if (!Number.isFinite(n)) return '—'
  return Math.round(Math.max(0, Math.min(1, n)) * 100) + '%'
}

/**
 * Build the three widget values from store state.
 * - Fatigue: average fatigue across all muscles (0 = ready, 1 = fully fatigued)
 * - Strength: average retained strength (floor 0.5 … 1.0)
 * - Balance: how even this week's set volume is across worked muscles (1 = perfectly even)
 */
export function muscleWidgetValues(S, now = Date.now()) {
  const workouts = S?.workouts || []
  const unit = S?.unit === 'lb' ? 'lb' : 'kg'
  const bodyweightKg = null // recovery falls back to BODYWEIGHT_REF_LOAD
  const opts = { bodyweightKg, unit }

  let fatigueMap = {}
  let strengthMap = {}
  try { fatigueMap = fatigueOf(workouts, now, opts) } catch { fatigueMap = {} }
  try { strengthMap = strengthOf(workouts, now, opts) } catch { strengthMap = {} }

  const fVals = Object.values(fatigueMap).filter(v => Number.isFinite(v))
  const sVals = Object.values(strengthMap).filter(v => Number.isFinite(v))
  const avgF = fVals.length ? fVals.reduce((a, b) => a + b, 0) / fVals.length : 0
  const avgS = sVals.length ? sVals.reduce((a, b) => a + b, 0) / sVals.length : 0.5

  // Balance from last 7 days of set volume: 1 - normalized stddev of non-zero muscles
  let balance = 0
  try {
    const weekAgo = now - 7 * 24 * 60 * 60 * 1000
    const recent = workouts.filter(w => {
      const t = typeof w?.start === 'number' ? w.start : (w?.d ? Date.parse(w.d + 'T12:00:00') : 0)
      return t >= weekAgo
    })
    const load = loadOfWorkouts(recent) || {}
    const vols = Object.values(load).filter(v => Number.isFinite(v) && v > 0)
    if (vols.length >= 2) {
      const mean = vols.reduce((a, b) => a + b, 0) / vols.length
      const variance = vols.reduce((a, v) => a + (v - mean) ** 2, 0) / vols.length
      const cv = mean > 0 ? Math.sqrt(variance) / mean : 1
      balance = Math.max(0, Math.min(1, 1 - cv / 1.5))
    } else if (vols.length === 1) {
      balance = 0.4
    } else {
      balance = 0
    }
  } catch {
    balance = 0
  }

  return {
    balance: pct(balance),
    fatigue: pct(avgF),
    strength: pct(avgS),
  }
}

export async function updateMuscleWidget(values = {}) {
  if (Capacitor.getPlatform() !== 'android') return
  try {
    await MuscleWidget.update({
      balance: values.balance ?? '—',
      fatigue: values.fatigue ?? '—',
      strength: values.strength ?? '—',
    })
  } catch (e) {
    console.debug('[muscle-widget]', e)
  }
}

/**
 * Subscribe to the store and push muscle summary to the Android widget (debounced).
 * Same pattern as startWidgetSync for iOS.
 */
export async function startMuscleWidgetSync({ useStore }) {
  if (Capacitor.getPlatform() !== 'android') return () => {}
  let last = null
  let tm = null
  const write = async () => {
    tm = null
    try {
      const S = useStore.getState().S
      const values = muscleWidgetValues(S)
      const key = JSON.stringify(values)
      if (key === last) return
      await updateMuscleWidget(values)
      last = key
    } catch { /* next change retries */ }
  }
  const schedule = () => {
    clearTimeout(tm)
    tm = setTimeout(write, WRITE_DEBOUNCE_MS)
  }
  const off = useStore.subscribe((s, prev) => {
    if (s.S !== prev.S) schedule()
  })
  const onVisible = () => {
    if (document.visibilityState === 'visible') schedule()
  }
  document.addEventListener('visibilitychange', onVisible)
  write()
  return () => {
    off()
    clearTimeout(tm)
    document.removeEventListener('visibilitychange', onVisible)
  }
}
