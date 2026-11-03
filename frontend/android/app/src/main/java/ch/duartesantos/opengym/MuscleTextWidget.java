package ch.duartesantos.opengym;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.List;

/**
 * Widget 4: summary % + untrained (white) then trained (red) with fatigue %.
 */
public class MuscleTextWidget extends AppWidgetProvider {
    private static final int[] ROW_IDS = {
        R.id.mrow0, R.id.mrow1, R.id.mrow2, R.id.mrow3, R.id.mrow4,
        R.id.mrow5, R.id.mrow6, R.id.mrow7, R.id.mrow8, R.id.mrow9,
        R.id.mrow10, R.id.mrow11, R.id.mrow12, R.id.mrow13, R.id.mrow14,
        R.id.mrow15, R.id.mrow16, R.id.mrow17
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
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, MuscleTextWidget.class));
        for (int id : ids) updateOne(context, mgr, id);
    }

    private static void updateOne(Context context, AppWidgetManager mgr, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_muscle_text);
        MuscleWidgetData.Snapshot snap = MuscleWidgetData.load(context);

        views.setTextViewText(R.id.w_balance, snap.balancePct);
        views.setTextViewText(R.id.w_fatigue, snap.fatiguePct);
        views.setTextViewText(R.id.w_strength, snap.strengthPct);
        views.setOnClickPendingIntent(R.id.wroot, MuscleWidgetIntents.openApp(context, 4000 + appWidgetId));

        List<MuscleWidgetData.MuscleRow> untrained = new ArrayList<>();
        List<MuscleWidgetData.MuscleRow> trained = new ArrayList<>();
        for (MuscleWidgetData.MuscleRow r : snap.muscles) {
            if (r.trainedWeek) trained.add(r);
            else untrained.add(r);
        }

        int i = 0;
        for (MuscleWidgetData.MuscleRow r : untrained) {
            if (i >= ROW_IDS.length) break;
            views.setViewVisibility(ROW_IDS[i], android.view.View.VISIBLE);
            views.setTextViewText(NAME_IDS[i], r.name);
            views.setTextViewText(VAL_IDS[i], MuscleWidgetData.pct(r.fatigue));
            views.setTextColor(NAME_IDS[i], Color.WHITE);
            views.setTextColor(VAL_IDS[i], Color.WHITE);
            views.setOnClickPendingIntent(ROW_IDS[i], MuscleWidgetIntents.openMuscle(context, r.slug, 4100 + i));
            i++;
        }
        for (MuscleWidgetData.MuscleRow r : trained) {
            if (i >= ROW_IDS.length) break;
            views.setViewVisibility(ROW_IDS[i], android.view.View.VISIBLE);
            views.setTextViewText(NAME_IDS[i], r.name);
            views.setTextViewText(VAL_IDS[i], MuscleWidgetData.pct(r.fatigue));
            views.setTextColor(NAME_IDS[i], 0xFFE74C3C);
            views.setTextColor(VAL_IDS[i], 0xFFE74C3C);
            views.setOnClickPendingIntent(ROW_IDS[i], MuscleWidgetIntents.openMuscle(context, r.slug, 4100 + i));
            i++;
        }
        for (; i < ROW_IDS.length; i++) views.setViewVisibility(ROW_IDS[i], android.view.View.GONE);

        mgr.updateAppWidget(appWidgetId, views);
    }
}
