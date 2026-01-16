package it.yuruni.game.level;

import it.yuruni.game.Note;

public class NoteData {
    public final float time;
    public final Note.Lane lane;
    // For now, we only have tap notes. We can add type and duration later.

    public NoteData(float time, Note.Lane lane) {
        this.time = time;
        this.lane = lane;
    }
}
