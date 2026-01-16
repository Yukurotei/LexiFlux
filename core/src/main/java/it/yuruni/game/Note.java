package it.yuruni.game;

import it.yuruni.graphics.element.Glyph3D;

public class Note {
    public enum Lane {
        LEFT, DOWN, UP, RIGHT
    }

    public final Glyph3D glyph;
    public final Glyph3D arrowGlyph;
    public final Lane lane;
    public final int keycode;
    public final float spawnTime;
    public final float hitTime;
    public boolean wasHit = false;

    public Note(Glyph3D glyph, Glyph3D arrowGlyph, Lane lane, int keycode, float spawnTime, float travelDuration) {
        this.glyph = glyph;
        this.arrowGlyph = arrowGlyph;
        this.lane = lane;
        this.keycode = keycode;
        this.spawnTime = spawnTime;
        this.hitTime = spawnTime + travelDuration;
    }
}
