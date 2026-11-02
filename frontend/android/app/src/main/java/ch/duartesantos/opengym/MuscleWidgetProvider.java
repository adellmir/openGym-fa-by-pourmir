package ch.duartesantos.opengym;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class MuscleWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "MuscleWidgetPrefs";
    private static final String KEY_BALANCE = "balance";
    private static final String KEY_FATIGUE = "fatigue";
    private static final String KEY_STRENGTH = "strength";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String balance = prefs.getString(KEY_BALANCE, "—");
        String fatigue = prefs.getString(KEY_FATIGUE, "—");
        String strength = prefs.getString(KEY_STRENGTH, "—");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.muscle_widget);
        views.setTextViewText(R.id.widget_balance_value, balance);
        views.setTextViewText(R.id.widget_fatigue_value, fatigue);
        views.setTextViewText(R.id.widget_strength_value, strength);

        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void saveValues(Context context, String balance, String fatigue, String strength) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_BALANCE, balance)
            .putString(KEY_FATIGUE, fatigue)
            .putString(KEY_STRENGTH, strength)
            .apply();

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, MuscleWidgetProvider.class));
        for (int id : ids) {
            updateAppWidget(context, manager, id);
        }
    }
}
