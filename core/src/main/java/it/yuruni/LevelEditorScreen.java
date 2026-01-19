package it.yuruni;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector3;
import it.yuruni.graphics.element.TextGlyph;
import it.yuruni.ui.InputField;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * A screen for creating and editing level files (.lfl).
 */
public class LevelEditorScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;

    private Music currentMusic;
    private List<FileHandle> musicFiles;
    private List<TextGlyph> musicFileButtons;
    private TextGlyph playPauseButton;
    private TextGlyph timeDisplay;
    private float bpm = 120f; // Default BPM, user will set later.
    private float songDuration = 60f; // Default duration in seconds

    private Texture inputFieldBackgroundTexture;
    private InputField bpmInputField;
    private InputField durationInputField;
    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);

        shapeRenderer = new ShapeRenderer();

        musicFiles = new ArrayList<>();
        musicFileButtons = new ArrayList<>();

        FileHandle audioDir = Gdx.files.internal("audio/song");
        if (audioDir.exists() && audioDir.isDirectory()) {
            Gdx.app.log("LevelEditorScreen", "Scanning for audio files in: " + audioDir.path());
            for (FileHandle file : audioDir.list()) {
                if (file.extension().equalsIgnoreCase("mp3") || file.extension().equalsIgnoreCase("ogg")) {
                    musicFiles.add(file);
                    Gdx.app.log("LevelEditorScreen", "Found audio file: " + file.name());
                }
            }
        } else {
            Gdx.app.error("LevelEditorScreen", "Audio directory not found or is not a directory: " + audioDir.path());
        }

        // Create TextGlyph buttons for each music file
        float yPos = Main.HEIGHT - 50;
        for (FileHandle file : musicFiles) {
            TextGlyph button = new TextGlyph(file.nameWithoutExtension(), font, 50, yPos, false);
            musicFileButtons.add(button);
            yPos -= 30; // Spacing
        }

        playPauseButton = new TextGlyph("PLAY / PAUSE", font, Main.WIDTH - 200, Main.HEIGHT - 50, false);
        timeDisplay = new TextGlyph("Time: 0.00s", font, Main.WIDTH - 200, Main.HEIGHT - 80, false);

        // Initialize InputField for BPM
        inputFieldBackgroundTexture = new Texture("ui/menuRect.png");
        bpmInputField = new InputField(
            inputFieldBackgroundTexture,
            font,
            Main.WIDTH - 200, Main.HEIGHT - 150, // Position
            150, 40, // Width, Height
            String.valueOf(bpm), // Initial text
            "BPM" // Hint text
        );

        bpmInputField.setOnChangeListener(() -> {
            try {
                float newBpm = Float.parseFloat(bpmInputField.getText());
                if (newBpm > 0) {
                    bpm = newBpm;
                    Gdx.app.log("LevelEditorScreen", "BPM set to: " + bpm);
                }
            } catch (NumberFormatException e) {
                Gdx.app.error("LevelEditorScreen", "Invalid BPM input: " + bpmInputField.getText());
            }
        });

        // Initialize InputField for Duration
        durationInputField = new InputField(
            inputFieldBackgroundTexture,
            font,
            Main.WIDTH - 200, Main.HEIGHT - 200, // Position below BPM
            150, 40, // Width, Height
            String.valueOf(songDuration), // Initial text
            "Duration (s)" // Hint text
        );

        durationInputField.setOnChangeListener(() -> {
            try {
                float newDuration = Float.parseFloat(durationInputField.getText());
                if (newDuration > 0) {
                    songDuration = newDuration;
                    Gdx.app.log("LevelEditorScreen", "Duration set to: " + songDuration + "s");
                }
            } catch (NumberFormatException e) {
                Gdx.app.error("LevelEditorScreen", "Invalid duration input: " + durationInputField.getText());
            }
        });

        Gdx.input.setInputProcessor(new LevelEditorInputProcessor());
        Gdx.app.log("LevelEditorScreen", "Editor screen shown.");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update UI components
        bpmInputField.update(delta);
        durationInputField.update(delta);

        Main.camera.update();
        batch.setProjectionMatrix(Main.camera.combined);
        shapeRenderer.setProjectionMatrix(Main.camera.combined);

        // --- Draw Timeline ---
        float timelineY = 100f;
        float timelineHeight = 5f;
        float timelineWidth = Main.WIDTH - 100;
        float timelineX = 50;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(timelineX, timelineY, timelineWidth, timelineHeight); // Main timeline bar

        // Draw beat lines if BPM and duration are valid
        if (bpm > 0 && songDuration > 0) {
            float beatsPerSecond = bpm / 60f;
            int totalBeats = (int) (songDuration * beatsPerSecond);

            for (int i = 0; i <= totalBeats; i++) {
                float beatTime = i / beatsPerSecond;
                // Position of the beat line on the timeline
                float beatX = timelineX + (beatTime / songDuration) * timelineWidth;

                // Draw main beats (every 4th beat thicker/longer)
                if (i % 4 == 0) {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.rect(beatX, timelineY, 2, timelineHeight * 3); // Thicker, longer
                } else {
                    shapeRenderer.setColor(Color.LIGHT_GRAY);
                    shapeRenderer.rect(beatX, timelineY, 1, timelineHeight * 2); // Thinner, shorter
                }
            }
        }
        shapeRenderer.end();

        // Draw Playhead
        if (currentMusic != null && songDuration > 0) {
            float playheadTime = currentMusic.getPosition();
            float playheadX = timelineX + (playheadTime / songDuration) * timelineWidth;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(playheadX - 2, timelineY, 4, timelineHeight * 5); // Playhead
            shapeRenderer.end();
        }

        batch.begin();
        // Render music file buttons
        for (TextGlyph button : musicFileButtons) {
            button.render(batch);
        }

        // Render play/pause button and time display
        playPauseButton.render(batch);
        timeDisplay.render(batch);
        bpmInputField.render(batch); // Render the BPM input field
        durationInputField.render(batch); // Render the duration input field

        // Update and render song time
        if (currentMusic != null && currentMusic.isPlaying()) {
            timeDisplay.setText(String.format("Time: %.2fs", currentMusic.getPosition()));
        } else if (currentMusic != null) {
            timeDisplay.setText(String.format("Time (Paused): %.2fs", currentMusic.getPosition()));
        } else {
            timeDisplay.setText("No song loaded.");
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        Main.viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (currentMusic != null) {
            currentMusic.dispose();
        }
        batch.dispose();
        font.dispose();
        inputFieldBackgroundTexture.dispose();
        shapeRenderer.dispose();
        Gdx.app.log("LevelEditorScreen", "Editor screen disposed.");
    }

    private class LevelEditorInputProcessor implements com.badlogic.gdx.InputProcessor {

        @Override
        public boolean keyDown(int keycode) {
            // Handle input field specific keys
            if (bpmInputField.isActive()) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.TAB) {
                    bpmInputField.setActive(false);
                    return true;
                }
            }

            if (durationInputField.isActive()) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.TAB) {
                    durationInputField.setActive(false);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            // Route keyTyped to active InputField
            if (bpmInputField.isActive()) {
                return bpmInputField.keyTyped(character);
            }

            if (durationInputField.isActive()) {
                return durationInputField.keyTyped(character);
            }

            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Vector3 touchPoint = new Vector3(screenX, screenY, 0);
            Main.viewport.unproject(touchPoint);

            // Handle InputField touchDown first
            if (bpmInputField.touchDown(screenX, screenY, pointer, button)) {
                return true;
            }

            if (durationInputField.touchDown(screenX, screenY, pointer, button)) {
                return true;
            }

            // Check music file buttons
            for (int i = 0; i < musicFileButtons.size(); i++) {
                TextGlyph btn = musicFileButtons.get(i);
                if (btn.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
                    if (currentMusic != null) {
                        currentMusic.stop();
                        currentMusic.dispose();
                    }
                    FileHandle selectedFile = musicFiles.get(i);
                    currentMusic = Gdx.audio.newMusic(selectedFile);
                    currentMusic.play();
                    Gdx.app.log("LevelEditorScreen", "Loaded and playing: " + selectedFile.name());
                    return true;
                }
            }

            // Check play/pause button
            if (playPauseButton.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
                if (currentMusic != null) {
                    if (currentMusic.isPlaying()) {
                        currentMusic.pause();
                        Gdx.app.log("LevelEditorScreen", "Music paused.");
                    } else {
                        currentMusic.play();
                        Gdx.app.log("LevelEditorScreen", "Music playing.");
                    }
                }
                return true;
            }

            // If nothing else is hit, deactivate any active input fields
            bpmInputField.setActive(false);
            durationInputField.setActive(false);

            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }
    }
}
