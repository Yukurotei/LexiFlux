package it.yuruni.ui;

import com.badlogic.gdx.graphics.Texture;
import it.yuruni.Main;
import it.yuruni.graphics.Easing;

/**
 * A scroll pane item representing a playable level.
 * Stores level metadata and provides animated visual feedback when selected.
 */
public class LevelCard extends ScrollPaneItem {

    private final String levelPath;
    private final String levelName;
    private final String artist;
    private final float bpm;
    private final int difficulty;

    private boolean isSelected = false;

    // Base scale values (set when card is first displayed)
    private float baseScaleX = -1f;
    private float baseScaleY = -1f;

    // Animation settings
    private static final float SELECTED_SCALE_MULTIPLIER = 1.05f;
    private static final float SELECTION_ANIM_DURATION = 0.2f;
    private static final Easing SELECTION_EASING = Easing.EASE_OUT_BACK;

    /**
     * Creates a new LevelCard with the given metadata.
     *
     * @param texture    The card's texture
     * @param levelPath  Path to the .lfl file
     * @param levelName  Display name of the level
     * @param artist     Artist/composer name
     * @param bpm        Beats per minute
     * @param difficulty Difficulty rating
     * @param onClick    Action when card is clicked
     */
    public LevelCard(Texture texture, String levelPath, String levelName, String artist,
                     float bpm, int difficulty, Runnable onClick) {
        super(texture, 0, 0, false, onClick);
        this.levelPath = levelPath;
        this.levelName = levelName;
        this.artist = artist;
        this.bpm = bpm;
        this.difficulty = difficulty;
    }

    /**
     * Captures the current scale as the base scale.
     * Call this after the card has been positioned/scaled by the scroll pane.
     */
    public void captureBaseScale() {
        this.baseScaleX = getScaleX();
        this.baseScaleY = getScaleY();
    }

    /**
     * Sets the selected state with animated visual feedback.
     */
    public void setSelected(boolean selected) {
        if (this.isSelected == selected) return;
        this.isSelected = selected;

        // Ensure base scale is captured
        if (baseScaleX < 0) {
            captureBaseScale();
        }

        // Stop any existing scale animations on this card
        Main.animationManager.stopAllAnimations(this);

        if (selected) {
            float targetScaleX = baseScaleX * SELECTED_SCALE_MULTIPLIER;
            float targetScaleY = baseScaleY * SELECTED_SCALE_MULTIPLIER;
            Main.animationManager.animateScale(this, targetScaleX, targetScaleY,
                    SELECTION_ANIM_DURATION, SELECTION_EASING);
        } else {
            Main.animationManager.animateScale(this, baseScaleX, baseScaleY,
                    SELECTION_ANIM_DURATION, Easing.EASE_OUT_QUAD);
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    // Getters for level metadata
    public String getLevelPath() {
        return levelPath;
    }

    public String getLevelName() {
        return levelName;
    }

    public String getArtist() {
        return artist;
    }

    public float getBpm() {
        return bpm;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
