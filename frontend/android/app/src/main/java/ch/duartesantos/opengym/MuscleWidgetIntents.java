package ch.duartesantos.opengym;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public final class MuscleWidgetIntents {
    public static final String EXTRA_MUSCLE = "muscle_slug";
    public static final String ACTION_OPEN_MUSCLE = "ch.duartesantos.opengym.OPEN_MUSCLE";

    private MuscleWidgetIntents() {}

    public static PendingIntent openMuscle(Context ctx, String slug, int requestCode) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.setAction(ACTION_OPEN_MUSCLE);
        i.putExtra(EXTRA_MUSCLE, slug == null ? "" : slug);
        i.setData(Uri.parse("opengym://muscles?m=" + Uri.encode(slug == null ? "" : slug)));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(ctx, requestCode, i, flags);
    }

    public static PendingIntent openApp(Context ctx, int requestCode) {
        return openMuscle(ctx, "", requestCode);
    }
}
