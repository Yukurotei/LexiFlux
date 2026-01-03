package it.yuruni.game.level;

import com.badlogic.gdx.graphics.Texture;

public class Level {
    private final String name;
    private final int difficulty;
    private final Texture background; //TODO: Use a path cuz textures are large and loaded dynamically

    public Level(String name, int difficulty, Texture background) {
        if (difficulty < 1 || difficulty > 10) {
            throw new IllegalArgumentException("Difficulty must be between 1 and 10 (inclusive).");
        }
        this.name = name;
        this.difficulty = difficulty;
        this.background = background;
    }

    public String getName() {
        return name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public Texture getBackground() {
        return background;
    }

    public void dispose() {
        if (background != null) {
            background.dispose();
        }
    }
}
