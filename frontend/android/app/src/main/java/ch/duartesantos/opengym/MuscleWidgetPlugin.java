package ch.duartesantos.opengym;

import android.content.Context;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "MuscleWidget")
public class MuscleWidgetPlugin extends Plugin {

    @PluginMethod
    public void update(PluginCall call) {
        Context ctx = getContext();
        if (ctx == null) {
            call.reject("no context");
            return;
        }
        try {
            String json = call.getString("json", null);
            if (json == null || json.isEmpty()) {
                String balance = call.getString("balance", "—");
                String fatigue = call.getString("fatigue", "—");
                String strength = call.getString("strength", "—");
                json = "{\"balance\":\"" + balance + "\",\"fatigue\":\"" + fatigue
                    + "\",\"strength\":\"" + strength + "\",\"muscles\":[]}";
            }
            MuscleWidgetData.save(ctx, json);
            MuscleActivityWidget.updateAll(ctx);
            MuscleFatigueWidget.updateAll(ctx);
            MuscleStrengthWidget.updateAll(ctx);
            MuscleTextWidget.updateAll(ctx);
            call.resolve();
        } catch (Exception e) {
            call.reject("failed: " + e.getMessage());
        }
    }
}
