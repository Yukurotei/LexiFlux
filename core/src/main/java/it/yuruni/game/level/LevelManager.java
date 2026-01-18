package it.yuruni.game.level;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import it.yuruni.Main;
import it.yuruni.game.Note;
import it.yuruni.game.NoteData;
import it.yuruni.graphics.Easing;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.Event;
import it.yuruni.graphics.animation.EventManager;
import it.yuruni.graphics.element.Glyph3D;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class LevelManager {
    private final Level level;
    private final List<Note> activeNotes;
    private final AnimationManager animationManager;
    private final EventManager eventManager;

    // Note spawning constants
    private static final float START_Z = -3000f;
    private static final float END_Z = 1200f;
    private static final float NOTE_SPEED = 1050f; // units per second
    private static final float JUDGEMENT_LINE_Z = 400f;
    private static final float NOTE_SIZE = 150f;
    private static final float ARROW_SIZE = 75f;

    // Play areas
    private final Rectangle leftPlayArea;
    private final Rectangle downPlayArea;
    private final Rectangle upPlayArea;
    private final Rectangle rightPlayArea;

    // Textures
    private final TextureRegion noteTexture;
    private final Map<Note.Lane, TextureRegion> arrowTextures;

    private final Random random = new Random();
    private int nextNoteIndex = 0;
    private float songStartTime = -1f; // Will be set when song actually starts

    public LevelManager(Level level, List<Note> activeNotes,
                        TextureRegion noteTexture, Map<Note.Lane, TextureRegion> arrowTextures,
                        Rectangle leftArea, Rectangle downArea, Rectangle upArea, Rectangle rightArea) {
        this.level = level;
        this.activeNotes = activeNotes;
        this.noteTexture = noteTexture;
        this.arrowTextures = arrowTextures;
        this.animationManager = Main.animationManager;
        this.eventManager = Main.eventManager;

        this.leftPlayArea = leftArea;
        this.downPlayArea = downArea;
        this.upPlayArea = upArea;
        this.rightPlayArea = rightArea;
    }

    /**
     * Start the level. Call this when you want notes to start spawning.
     * @param delaySeconds How many seconds to wait before the first note should be hit
     */
    public void start(float delaySeconds) {
        this.songStartTime = Main.timePassed + delaySeconds;
        scheduleAllNotes();
    }

    private void scheduleAllNotes() {
        List<NoteData> notes = level.getNotes();

        for (NoteData noteData : notes) {
            // Convert milliseconds to seconds
            float hitTimeSeconds = noteData.time / 1000f;

            // Calculate when to spawn the note
            float travelTime = Math.abs(JUDGEMENT_LINE_Z - START_Z) / NOTE_SPEED;
            float spawnTimeSeconds = hitTimeSeconds - travelTime;

            // Schedule the spawn
            float absoluteSpawnTime = songStartTime + spawnTimeSeconds;

            eventManager.addEvent(new Event(absoluteSpawnTime, () -> {
                spawnNote(noteData, travelTime);
            }));
        }
    }

    private void spawnNote(NoteData noteData, float travelTime) {
        // Get the target area for this lane
        Rectangle targetArea;
        switch (noteData.lane) {
            case LEFT:
                targetArea = leftPlayArea;
                break;
            case DOWN:
                targetArea = downPlayArea;
                break;
            case UP:
                targetArea = upPlayArea;
                break;
            case RIGHT:
                targetArea = rightPlayArea;
                break;
            default:
                return;
        }

        // Calculate positions
        float startX = Main.WIDTH / 2f;
        float startY = Main.HEIGHT / 2f;

        float endX = targetArea.x + targetArea.width / 2f;
        float endY = targetArea.y + targetArea.height / 2f;

        // Add random spread within the lane
        float padding = 50f;
        float spread = (targetArea.width / 2f) - padding;
        if (spread > 0) {
            endX += random.nextFloat() * spread * 2 - spread;
        }

        // Create note glyph
        Glyph3D noteGlyph = new Glyph3D(noteTexture, new Vector3(startX, startY, START_Z), true);
        noteGlyph.dimension.set(NOTE_SIZE, NOTE_SIZE);

        // Create arrow glyph
        TextureRegion arrowTexture = arrowTextures.get(noteData.lane);
        Glyph3D arrowGlyph = new Glyph3D(arrowTexture, new Vector3(startX, startY, START_Z - 1f), true);
        arrowGlyph.dimension.set(ARROW_SIZE, ARROW_SIZE);

        // Use the keycode specified in the level file
        int keycode = noteData.keycode;

        // Create note object
        Note note = new Note(noteGlyph, arrowGlyph, noteData.lane, keycode, Main.timePassed, travelTime);
        activeNotes.add(note);

        // Animate movement
        animationManager.animateMove(noteGlyph, endX, endY, END_Z, travelTime, Easing.LINEAR);
        animationManager.animateMove(arrowGlyph, endX, endY, END_Z - 1f, travelTime, Easing.LINEAR);
    }

    public Level getLevel() {
        return level;
    }
}
