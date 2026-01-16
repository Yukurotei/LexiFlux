package it.yuruni.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import it.yuruni.game.Note;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private String name;
    private String artist;
    private float bpm;
    private String backgroundPath;
    private int difficulty;

    private final List<NoteData> notes = new ArrayList<>();

    public Level(String lflFilePath) {
        FileHandle handle = Gdx.files.internal(lflFilePath);
        String fileContent = handle.readString();
        String[] lines = fileContent.split("\\r?\\n");

        boolean readingNotes = false;
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.trim().equals("[NOTES]")) {
                readingNotes = true;
                continue;
            }

            if (readingNotes) {
                String[] values = line.split(",");
                if (values.length >= 2) {
                    try {
                        float time = Float.parseFloat(values[0].trim());
                        Note.Lane lane = Note.Lane.valueOf(values[1].trim().toUpperCase());
                        notes.add(new NoteData(time, lane));
                    } catch (IllegalArgumentException e) {
                        Gdx.app.error("LevelLoader", "Failed to parse note line: " + line, e);
                    }
                }
            } else { // Reading metadata
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
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
                            this.bpm = Float.parseFloat(value);
                            break;
                        case "Background":
                            this.backgroundPath = value;
                            break;
                        case "Difficulty":
                            this.difficulty = Integer.parseInt(value);
                            break;
                    }
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public float getBpm() {
        return bpm;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public List<NoteData> getNotes() {
        return notes;
    }

    public void dispose() {
        // No textures are owned by this class anymore
    }
}
