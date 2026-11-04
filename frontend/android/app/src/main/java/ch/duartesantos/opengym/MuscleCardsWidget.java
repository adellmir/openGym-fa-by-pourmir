package ch.duartesantos.opengym;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Single home-screen widget: muscle cards (5 per row), sorted by readiness,
 * footer with تقویت / خستگی / تعادل. Tap card → open exercises for that muscle.
 */
public class MuscleCardsWidget extends AppWidgetProvider {

    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int MAX = COLS * ROWS; // 20

    private static final int[] CARD_IDS = new int[MAX];
    private static final int[] CNAME_IDS = new int[MAX];
    private static final int[] CFAT_IDS = new int[MAX];
    private static final int[] CREC_IDS = new int[MAX];

    static {
        // Resolved at runtime via getIdentifier — IDs filled in bind()
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateOne(context, mgr, id);
        }
    }

    public static void updateAll(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, MuscleCardsWidget.class));
        for (int id : ids) updateOne(context, mgr, id);
    }

    private static void updateOne(Context ctx, AppWidgetManager mgr, int widgetId) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_muscle_cards);
        MuscleWidgetData.Snapshot snap = MuscleWidgetData.load(ctx);

        rv.setTextViewText(R.id.footer_strength, snap.strengthPct);
        rv.setTextViewText(R.id.footer_fatigue, snap.fatiguePct);
        rv.setTextViewText(R.id.footer_balance, snap.balancePct);

        List<MuscleWidgetData.MuscleRow> sorted = sortMuscles(snap.muscles);

        for (int i = 0; i < MAX; i++) {
            int cardId = id(ctx, "card" + i);
            int nameId = id(ctx, "cname" + i);
            int fatId = id(ctx, "cfat" + i);
            int recId = id(ctx, "crec" + i);
            if (cardId == 0) continue;

            if (i < sorted.size()) {
                MuscleWidgetData.MuscleRow m = sorted.get(i);
                double fat = m.fatigue;
                double rec = Math.max(0, Math.min(1, 1.0 - fat));
                String label = m.fatigueLabel != null ? m.fatigueLabel : "ready";

                rv.setViewVisibility(cardId, View.VISIBLE);
                rv.setTextViewText(nameId, m.name != null ? m.name : m.slug);
                rv.setTextViewText(fatId, "خ " + MuscleWidgetData.pct(fat));
                rv.setTextViewText(recId, "ر " + MuscleWidgetData.pct(rec));

                int bg = R.drawable.widget_card_ready;
                if ("fatigued".equals(label)) bg = R.drawable.widget_card_fatigued;
                else if ("recovering".equals(label)) bg = R.drawable.widget_card_recovering;
                rv.setInt(cardId, "setBackgroundResource", bg);

                rv.setOnClickPendingIntent(cardId, MuscleWidgetIntents.openMuscle(ctx, m.slug, 1000 + i));
            } else {
                rv.setViewVisibility(cardId, View.INVISIBLE);
                rv.setTextViewText(nameId, "");
                rv.setTextViewText(fatId, "");
                rv.setTextViewText(recId, "");
                rv.setOnClickPendingIntent(cardId, MuscleWidgetIntents.openApp(ctx, 2000 + i));
            }
        }

        // Footer / root opens app
        rv.setOnClickPendingIntent(R.id.footer, MuscleWidgetIntents.openApp(ctx, 99));
        rv.setOnClickPendingIntent(R.id.title, MuscleWidgetIntents.openApp(ctx, 98));

        mgr.updateAppWidget(widgetId, rv);
    }

    /**
     * Priority:
     * 1) ready — lowest fatigue first
     * 2) recovering — lowest fatigue first
     * 3) fatigued — highest fatigue first
     */
    static List<MuscleWidgetData.MuscleRow> sortMuscles(List<MuscleWidgetData.MuscleRow> src) {
        List<MuscleWidgetData.MuscleRow> ready = new ArrayList<>();
        List<MuscleWidgetData.MuscleRow> recovering = new ArrayList<>();
        List<MuscleWidgetData.MuscleRow> fatigued = new ArrayList<>();
        for (MuscleWidgetData.MuscleRow m : src) {
            String l = m.fatigueLabel != null ? m.fatigueLabel : "ready";
            if ("fatigued".equals(l)) fatigued.add(m);
            else if ("recovering".equals(l)) recovering.add(m);
            else ready.add(m);
        }
        Collections.sort(ready, Comparator.comparingDouble(a -> a.fatigue));
        Collections.sort(recovering, Comparator.comparingDouble(a -> a.fatigue));
        Collections.sort(fatigued, (a, b) -> Double.compare(b.fatigue, a.fatigue));
        List<MuscleWidgetData.MuscleRow> out = new ArrayList<>(ready.size() + recovering.size() + fatigued.size());
        out.addAll(ready);
        out.addAll(recovering);
        out.addAll(fatigued);
        return out;
    }

    private static int id(Context ctx, String name) {
        return ctx.getResources().getIdentifier(name, "id", ctx.getPackageName());
    }
}
