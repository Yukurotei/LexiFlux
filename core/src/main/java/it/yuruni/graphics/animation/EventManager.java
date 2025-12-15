package it.yuruni.graphics.animation;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventManager {
    private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<>();

    public void addEvent(Event event) {
        events.add(event);
    }

    public void update(float timePassed) {
        for (Event event : events) {
            event.trigger(timePassed);
        }
    }
}