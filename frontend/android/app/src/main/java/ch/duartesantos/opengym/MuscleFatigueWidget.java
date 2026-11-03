package ch.duartesantos.opengym;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class MuscleFatigueWidget extends AppWidgetProvider {
    private static final int[] ROW_IDS = {
        R.id.mrow0, R.id.mrow1, R.id.mrow2, R.id.mrow3, R.id.mrow4,
        R.id.mrow5, R.id.mrow6, R.id.mrow7, R.id.mrow8, R.id.mrow9,
        R.id.mrow10, R.id.mrow11, R.id.mrow12, R.id.mrow13, R.id.mrow14,
        R.id.mrow15, R.id.mrow16, R.id.mrow17
    };
    private static final int[] DOT_IDS = {
        R.id.mdot0, R.id.mdot1, R.id.mdot2, R.id.mdot3, R.id.mdot4,
        R.id.mdot5, R.id.mdot6, R.id.mdot7, R.id.mdot8, R.id.mdot9,
        R.id.mdot10, R.id.mdot11, R.id.mdot12, R.id.mdot13, R.id.mdot14,
        R.id.mdot15, R.id.mdot16, R.id.mdot17
    };
    private static final int[] NAME_IDS = {
        R.id.mname0, R.id.mname1, R.id.mname2, R.id.mname3, R.id.mname4,
        R.id.mname5, R.id.mname6, R.id.mname7, R.id.mname8, R.id.mname9,
        R.id.mname10, R.id.mname11, R.id.mname12, R.id.mname13, R.id.mname14,
        R.id.mname15, R.id.mname16, R.id.mname17
    };
    private static final int[] VAL_IDS = {
        R.id.mval0, R.id.mval1, R.id.mval2, R.id.mval3, R.id.mval4,
        R.id.mval5, R.id.mval6, R.id.mval7, R.id.mval8, R.id.mval9,
        R.id.mval10, R.id.mval11, R.id.mval12, R.id.mval13, R.id.mval14,
        R.id.mval15, R.id.mval16, R.id.mval17
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) updateOne(context, mgr, id);
    }

    static void updateAll(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, MuscleFatigueWidget.class));
        for (int id : ids) updateOne(context, mgr, id);
    }

    private static void updateOne(Context context, AppWidgetManager mgr, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_muscle_fatigue);
        MuscleWidgetData.Snapshot snap = MuscleWidgetData.load(context);
        views.setTextViewText(R.id.wtitle, "خستگی / ریکاوری");
        views.setOnClickPendingIntent(R.id.wroot, MuscleWidgetIntents.openApp(context, 2000 + appWidgetId));

        int i = 0;
        for (MuscleWidgetData.MuscleRow r : snap.muscles) {
            if (i >= ROW_IDS.length) break;
            views.setViewVisibility(ROW_IDS[i], android.view.View.VISIBLE);
            views.setTextViewText(NAME_IDS[i], r.name);
            views.setTextViewText(VAL_IDS[i], MuscleWidgetData.pct(r.fatigue));
            views.setImageViewBitmap(DOT_IDS[i], MuscleWidgetData.colorBitmap(MuscleWidgetData.fatigueColor(r.fatigueLabel), 24));
            views.setOnClickPendingIntent(ROW_IDS[i], MuscleWidgetIntents.openMuscle(context, r.slug, 2100 + i));
            i++;
        }
        for (; i < ROW_IDS.length; i++) views.setViewVisibility(ROW_IDS[i], android.view.View.GONE);
        views.setTextViewText(R.id.wfooter, "سبز آماده · نارنجی ریکاوری · قرمز خسته");
        mgr.updateAppWidget(appWidgetId, views);
    }
}
