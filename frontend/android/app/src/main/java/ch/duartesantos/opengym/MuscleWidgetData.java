package ch.duartesantos.opengym;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Snapshot written by MuscleWidgetPlugin; read by all muscle widgets. */
public final class MuscleWidgetData {
    public static final String PREFS = "muscle_widget";
    public static final String KEY_JSON = "snapshot_json";

    public static class MuscleRow {
        public String slug;
        public String name;
        public boolean trainedWeek;
        public double fatigue;   // 0..1
        public double strength;  // 0.5..1
        public String fatigueLabel; // ready / recovering / fatigued
    }

    public static class Snapshot {
        public String balancePct = "—";
        public String fatiguePct = "—";
        public String strengthPct = "—";
        public final List<MuscleRow> muscles = new ArrayList<>();
    }

    private MuscleWidgetData() {}

    public static void save(Context ctx, String json) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_JSON, json).apply();
    }

    public static Snapshot load(Context ctx) {
        Snapshot s = new Snapshot();
        String json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_JSON, null);
        if (json == null || json.isEmpty()) return s;
        try {
            JSONObject root = new JSONObject(json);
            s.balancePct = root.optString("balance", "—");
            s.fatiguePct = root.optString("fatigue", "—");
            s.strengthPct = root.optString("strength", "—");
            JSONArray arr = root.optJSONArray("muscles");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    MuscleRow r = new MuscleRow();
                    r.slug = o.optString("slug", "");
                    r.name = o.optString("name", r.slug);
                    r.trainedWeek = o.optBoolean("trainedWeek", false);
                    r.fatigue = o.optDouble("fatigue", 0);
                    r.strength = o.optDouble("strength", 0.5);
                    r.fatigueLabel = o.optString("fatigueLabel", "ready");
                    if (!r.slug.isEmpty()) s.muscles.add(r);
                }
            }
        } catch (Exception ignored) {}
        return s;
    }

    public static int fatigueColor(String label) {
        if ("fatigued".equals(label)) return 0xFFE74C3C;
        if ("recovering".equals(label)) return 0xFFF39C12;
        return 0xFF2ECC71; // ready
    }

    public static int activityColor(boolean trained) {
        return trained ? 0xFF2ECC71 : 0xFF95A5A6;
    }

    /** Strength: 1.0 full green, down to 0.5 gray-red. */
    public static int strengthColor(double strength) {
        double t = Math.max(0, Math.min(1, (strength - 0.5) / 0.5));
        int r = (int) (231 + (46 - 231) * t);
        int g = (int) (76 + (204 - 76) * t);
        int b = (int) (60 + (113 - 60) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }


    public static android.graphics.Bitmap colorBitmap(int color, int sizePx) {
        android.graphics.Bitmap bm = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas c = new android.graphics.Canvas(bm);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        float r = sizePx / 2f;
        c.drawCircle(r, r, r, paint);
        return bm;
    }

    public static String pct(double v) {
        return Math.round(Math.max(0, Math.min(1, v)) * 100) + "%";
    }
}
