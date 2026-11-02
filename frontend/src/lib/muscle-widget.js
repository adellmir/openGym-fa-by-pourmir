/**
 * Helper for the home-screen Muscle Status widget.
 * Safe to call on web too — it no-ops outside Capacitor Android.
 */
import { Capacitor, registerPlugin } from '@capacitor/core';

const MuscleWidget = registerPlugin('MuscleWidget');

/**
 * @param {{ balance?: string, fatigue?: string, strength?: string }} values
 */
export async function updateMuscleWidget(values = {}) {
  if (Capacitor.getPlatform() !== 'android') return;
  try {
    await MuscleWidget.update({
      balance: values.balance ?? '—',
      fatigue: values.fatigue ?? '—',
      strength: values.strength ?? '—',
    });
  } catch (e) {
    // Widget not present or plugin missing — ignore
    console.debug('[muscle-widget]', e);
  }
}
