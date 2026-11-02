package ch.duartesantos.opengym;

import android.content.Context;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Updates the home-screen Muscle Status widget from the WebView.
 *
 * Usage from JS:
 *   import { registerPlugin } from '@capacitor/core';
 *   const MuscleWidget = registerPlugin('MuscleWidget');
 *   await MuscleWidget.update({ balance: '72%', fatigue: '41%', strength: '88%' });
 */
@CapacitorPlugin(name = "MuscleWidget")
public class MuscleWidgetPlugin extends Plugin {

    @PluginMethod
    public void update(PluginCall call) {
        Context ctx = getContext();
        if (ctx == null) {
            call.reject("no context");
            return;
        }

        String balance = call.getString("balance", "—");
        String fatigue = call.getString("fatigue", "—");
        String strength = call.getString("strength", "—");

        try {
            MuscleWidgetProvider.saveValues(ctx, balance, fatigue, strength);
            call.resolve();
        } catch (Exception e) {
            call.reject("failed to update widget: " + e.getMessage());
        }
    }
}
