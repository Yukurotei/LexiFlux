package it.yuruni.graphics.animation;

public class Event {
    private final float triggerTime;
    private final Runnable action;
    private boolean hasBeenTriggered = false;

    public Event(float triggerTime, Runnable action) {
        this.triggerTime = triggerTime;
        this.action = action;
    }

    public void trigger(float currentTime) {
        if (!hasBeenTriggered && currentTime >= triggerTime) {
            action.run();
            hasBeenTriggered = true;
        }
    }

    public boolean hasBeenTriggered() {
        return hasBeenTriggered;
    }
}