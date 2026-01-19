package it.yuruni.game.level;

import java.util.Map;

public class LevelEvent {
    public final float triggerTime;
    public final String effectName;
    public final Map<String, String> parameters;

    public LevelEvent(float triggerTime, String effectName, Map<String, String> parameters) {
        this.triggerTime = triggerTime;
        this.effectName = effectName;
        this.parameters = parameters;
    }

    public float getFloat(String key, float defaultValue) {
        if (parameters.containsKey(key)) {
            try {
                return Float.parseFloat(parameters.get(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }
}
