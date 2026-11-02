package ch.duartesantos.opengym

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews

/**
 * ویجت خلاصه وضعیت عضلات (مرحله ۱)
 * نمایش سه عدد: Balance / Fatigue / Strength
 */
class MuscleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // اولین بار که ویجت اضافه می‌شود
    }

    override fun onDisabled(context: Context) {
        // وقتی آخرین ویجت حذف می‌شود
    }

    companion object {
        private const val PREFS_NAME = "MuscleWidgetPrefs"
        private const val KEY_BALANCE = "balance"
        private const val KEY_FATIGUE = "fatigue"
        private const val KEY_STRENGTH = "strength"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val balance = prefs.getString(KEY_BALANCE, "—") ?: "—"
            val fatigue = prefs.getString(KEY_FATIGUE, "—") ?: "—"
            val strength = prefs.getString(KEY_STRENGTH, "—") ?: "—"

            val views = RemoteViews(context.packageName, R.layout.muscle_widget)

            views.setTextViewText(R.id.widget_balance_value, balance)
            views.setTextViewText(R.id.widget_fatigue_value, fatigue)
            views.setTextViewText(R.id.widget_strength_value, strength)

            // کلیک روی ویجت اپ را باز می‌کند
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * این متد را بعداً از سمت جاوااسکریپت (از طریق Capacitor) صدا می‌زنیم
         * تا اعداد واقعی را در ویجت بنویسیم.
         */
        fun saveValues(context: Context, balance: String, fatigue: String, strength: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_BALANCE, balance)
                .putString(KEY_FATIGUE, fatigue)
                .putString(KEY_STRENGTH, strength)
                .apply()

            // همه ویجت‌های موجود را آپدیت کن
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, MuscleWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, manager, id)
            }
        }
    }
}
