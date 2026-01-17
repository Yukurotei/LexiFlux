package it.yuruni.game.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import it.yuruni.ui.LevelCard;

/**
 * Scans the levels directory for .lfl files and creates LevelCard instances.
 * Only parses metadata (not notes) for efficiency.
 */
public class LevelScanner {

    private static final String LEVELS_DIRECTORY = "levels";

    /**
     * Scans for all .lfl files and creates LevelCards.
     *
     * @param cardTexture The texture to use for all cards
     * @param onCardClick Callback when a card is clicked, receives the LevelCard
     * @return Array of LevelCards with metadata from each level file
     */
    public static Array<LevelCard> scanLevels(Texture cardTexture, CardClickListener onCardClick) {
        Array<LevelCard> cards = new Array<>();

        FileHandle levelsDir = Gdx.files.internal(LEVELS_DIRECTORY);
        if (!levelsDir.exists()) {
            Gdx.app.error("LevelScanner", "Levels directory not found: " + LEVELS_DIRECTORY);
            return cards;
        }

        FileHandle[] files = levelsDir.list(".lfl");
        Gdx.app.log("LevelScanner", "Found " + files.length + " .lfl files");

        for (FileHandle file : files) {
            LevelMetadata metadata = parseMetadata(file);
            if (metadata != null) {
                String levelPath = LEVELS_DIRECTORY + "/" + file.name();
                LevelCard card = new LevelCard(
                        cardTexture,
                        levelPath,
                        metadata.name,
                        metadata.artist,
                        metadata.bpm,
                        metadata.difficulty,
                        null
                );

                // Set click handler if provided
                if (onCardClick != null) {
                    final LevelCard finalCard = card;
                    card.setOnClick(() -> onCardClick.onClick(finalCard));
                }

                cards.add(card);
                Gdx.app.log("LevelScanner", "Loaded: " + metadata.name + " by " + metadata.artist);
            }
        }

        return cards;
    }

    /**
     * Parses only the metadata section of a .lfl file.
     */
    private static LevelMetadata parseMetadata(FileHandle file) {
        if (!file.exists()) {
            return null;
        }

        LevelMetadata metadata = new LevelMetadata();
        String fileContent = file.readString();
        String[] lines = fileContent.split("\\r?\\n");

        boolean inMetadata = false;

        for (String line : lines) {
            line = line.trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // Check for section headers
            if (line.startsWith("[") && line.endsWith("]")) {
                String section = line.substring(1, line.length() - 1).toUpperCase();
                if (section.equals("METADATA")) {
                    inMetadata = true;
                } else {
                    // Stop parsing once we leave metadata section
                    if (inMetadata) break;
                }
                continue;
            }

            // Parse metadata fields
            if (inMetadata) {
                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "Name":
                        metadata.name = value;
                        break;
                    case "Artist":
                        metadata.artist = value;
                        break;
                    case "BPM":
                        try {
                            metadata.bpm = Float.parseFloat(value);
                        } catch (NumberFormatException e) {
                            Gdx.app.error("LevelScanner", "Invalid BPM in " + file.name() + ": " + value);
                        }
                        break;
                    case "Difficulty":
                        try {
                            metadata.difficulty = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            Gdx.app.error("LevelScanner", "Invalid difficulty in " + file.name() + ": " + value);
                        }
                        break;
                }
            }
        }

        // Validate required fields
        if (metadata.name == null || metadata.name.isEmpty()) {
            metadata.name = file.nameWithoutExtension();
        }
        if (metadata.artist == null) {
            metadata.artist = "Unknown Artist";
        }

        return metadata;
    }

    /**
     * Holds parsed metadata from a level file.
     */
    private static class LevelMetadata {
        String name;
        String artist;
        float bpm = 120f;
        int difficulty = 1;
    }

    /**
     * Callback interface for card clicks.
     */
    public interface CardClickListener {
        void onClick(LevelCard card);
    }
}
