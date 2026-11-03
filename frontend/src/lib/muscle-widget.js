/**
 * Android muscle widgets — full snapshot (activity / fatigue / strength / text).
 */
import { Capacitor, registerPlugin } from '@capacitor/core'
import { fatigueOf, strengthOf } from './recovery.js'
import { loadOfWorkouts, MUSCLES, MUSCLE_NAME } from './muscles.js'
import { t } from './i18n.js'

const MuscleWidget = registerPlugin('MuscleWidget')
const WRITE_DEBOUNCE_MS = 1500

const FA_NAME = {
  trapezius: 'ذوزنقه',
  deltoids: 'شانه',
  chest: 'سینه',
  'upper-back': 'زیربغل',
  serratus: 'دندانه‌ای',
  biceps: 'جلوبازو',
  triceps: 'پشت‌بازو',
  forearm: 'ساعد',
  abs: 'شکم',
  obliques: 'پهلو',
  'lower-back': 'کمر پایین',
  gluteal: 'باسن',
  quadriceps: 'چهارسر',
  hamstring: 'همسترینگ',
  adductors: 'نزدیک‌کننده',
  'hip-flexors': 'خم‌کننده ران',
  calves: 'ساق',
  tibialis: 'ساق جلو',
}

function nameOf(slug) {
  return FA_NAME[slug] || t(MUSCLE_NAME[slug] || slug)
}

function fatigueLabel(v) {
  if (v > 0.5) return 'fatigued'
  if (v >= 0.25) return 'recovering'
  return 'ready'
}

function pct(n) {
  if (!Number.isFinite(n)) return '—'
  return Math.round(Math.max(0, Math.min(1, n)) * 100) + '%'
}

export function buildMuscleSnapshot(S, now = Date.now()) {
  const workouts = S?.workouts || []
  const unit = S?.unit === 'lb' ? 'lb' : 'kg'
  const opts = { bodyweightKg: null, unit }

  let fatigueMap = {}, strengthMap = {}, weekLoad = {}
  try { fatigueMap = fatigueOf(workouts, now, opts) } catch {}
  try { strengthMap = strengthOf(workouts, now, opts) } catch {}
  try {
    const weekAgo = now - 7 * 24 * 60 * 60 * 1000
    const recent = workouts.filter(w => {
      const ts = typeof w?.start === 'number' ? w.start : (w?.d ? Date.parse(w.d + 'T12:00:00') : 0)
      return ts >= weekAgo
    })
    weekLoad = loadOfWorkouts(recent) || {}
  } catch {}

  const fVals = Object.values(fatigueMap).filter(Number.isFinite)
  const sVals = Object.values(strengthMap).filter(Number.isFinite)
  const avgF = fVals.length ? fVals.reduce((a, b) => a + b, 0) / fVals.length : 0
  const avgS = sVals.length ? sVals.reduce((a, b) => a + b, 0) / sVals.length : 0.5

  let balance = 0
  try {
    const vols = Object.values(weekLoad).filter(v => Number.isFinite(v) && v > 0)
    if (vols.length >= 2) {
      const mean = vols.reduce((a, b) => a + b, 0) / vols.length
      const variance = vols.reduce((a, v) => a + (v - mean) ** 2, 0) / vols.length
      const cv = mean > 0 ? Math.sqrt(variance) / mean : 1
      balance = Math.max(0, Math.min(1, 1 - cv / 1.5))
    } else if (vols.length === 1) balance = 0.4
  } catch {}

  const muscles = MUSCLES.map(slug => {
    const fatigue = Number(fatigueMap[slug]) || 0
    const strength = Number(strengthMap[slug]) || 0.5
    return {
      slug,
      name: nameOf(slug),
      trainedWeek: (weekLoad[slug] || 0) > 0,
      fatigue,
      strength,
      fatigueLabel: fatigueLabel(fatigue),
    }
  })

  return {
    balance: pct(balance),
    fatigue: pct(avgF),
    strength: pct(avgS),
    muscles,
  }
}

export async function updateMuscleWidget(values = {}) {
  if (Capacitor.getPlatform() !== 'android') return
  try {
    if (values && values.muscles) {
      await MuscleWidget.update({
        json: JSON.stringify(values),
        balance: values.balance ?? '—',
        fatigue: values.fatigue ?? '—',
        strength: values.strength ?? '—',
      })
    } else {
      await MuscleWidget.update({
        balance: values.balance ?? '—',
        fatigue: values.fatigue ?? '—',
        strength: values.strength ?? '—',
      })
    }
  } catch (e) {
    console.debug('[muscle-widget]', e)
  }
}

export async function startMuscleWidgetSync({ useStore }) {
  if (Capacitor.getPlatform() !== 'android') return () => {}
  let last = null
  let tm = null
  const write = async () => {
    tm = null
    try {
      const S = useStore.getState().S
      const snap = buildMuscleSnapshot(S)
      const key = JSON.stringify(snap)
      if (key === last) return
      await updateMuscleWidget(snap)
      last = key
    } catch {}
  }
  const schedule = () => { clearTimeout(tm); tm = setTimeout(write, WRITE_DEBOUNCE_MS) }
  const off = useStore.subscribe((s, prev) => { if (s.S !== prev.S) schedule() })
  const onVisible = () => { if (document.visibilityState === 'visible') schedule() }
  document.addEventListener('visibilitychange', onVisible)
  write()
  return () => { off(); clearTimeout(tm); document.removeEventListener('visibilitychange', onVisible) }
}
