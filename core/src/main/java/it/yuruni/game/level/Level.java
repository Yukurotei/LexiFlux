package it.yuruni.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import it.yuruni.game.GameConstants;
import it.yuruni.game.Note;
import it.yuruni.game.NoteData;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private String name;
    private String artist;
    private float bpm;
    private String audioFile;
    private String backgroundImage;
    private int difficulty;

    private final List<NoteData> notes = new ArrayList<>();

    public Level(String lflFilePath) {
        loadFromFile(lflFilePath);
    }

    private void loadFromFile(String lflFilePath) {
        FileHandle handle = Gdx.files.internal(lflFilePath);
        if (!handle.exists()) {
            Gdx.app.error("Level", "File not found: " + lflFilePath);
            return;
        }

        String fileContent = handle.readString();
        String[] lines = fileContent.split("\\r?\\n");

        String currentSection = "";

        for (String line : lines) {
            line = line.trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Check for section headers
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).toUpperCase();
                continue;
            }

            // Parse based on current section
            switch (currentSection) {
                case "METADATA":
                    parseMetadata(line);
                    break;
                case "NOTES":
                    parseNote(line);
                    break;
            }
        }

        Gdx.app.log("Level", "Loaded level: " + name + " with " + notes.size() + " notes");
    }

    private void parseMetadata(String line) {
        String[] parts = line.split(":", 2);
        if (parts.length != 2) return;

        String key = parts[0].trim();
        String value = parts[1].trim();

        switch (key) {
            case "Name":
                this.name = value;
                break;
            case "Artist":
                this.artist = value;
                break;
            case "BPM":
                try {
                    this.bpm = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    Gdx.app.error("Level", "Invalid BPM: " + value);
                }
                break;
            case "AudioFile":
                this.audioFile = value;
                break;
            case "BackgroundImage":
                this.backgroundImage = value;
                break;
            case "Difficulty":
                try {
                    this.difficulty = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    Gdx.app.error("Level", "Invalid difficulty: " + value);
                }
                break;
        }
    }

    private void parseNote(String line) {
        String[] values = line.split(",");
        if (values.length < 3) {
            Gdx.app.error("Level", "Invalid note line (expected format: time, lane, key): " + line);
            return;
        }

        try {
            float timeMs = Float.parseFloat(values[0].trim());
            Note.Lane lane = Note.Lane.valueOf(values[1].trim().toUpperCase());
            String keyString = values[2].trim().toUpperCase();

            // Convert key string to Input.Keys constant
            int keycode = Input.Keys.valueOf(keyString);

            // Validate that the key is in PLAYABLE_KEYS
            if (!GameConstants.PLAYABLE_KEYS.contains(keycode)) {
                Gdx.app.error("Level", "Key '" + keyString + "' is not in PLAYABLE_KEYS list. Skipping note at " + timeMs + "ms");
                return;
            }

            notes.add(new NoteData(timeMs, lane, keycode));
        } catch (IllegalArgumentException e) {
            Gdx.app.error("Level", "Failed to parse note: " + line, e);
        }
    }

    // Getters
    public String getName() { return name; }
    public String getArtist() { return artist; }
    public float getBpm() { return bpm; }
    public String getAudioFile() { return audioFile; }
    public String getBackgroundImage() { return backgroundImage; }
    public int getDifficulty() { return difficulty; }
    public List<NoteData> getNotes() { return notes; }
}
