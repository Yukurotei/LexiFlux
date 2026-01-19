package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import it.yuruni.game.Note;
import it.yuruni.game.level.Level;
import it.yuruni.game.level.LevelManager;
import it.yuruni.graphics.Easing;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.Event;
import it.yuruni.graphics.animation.EventManager;
import it.yuruni.graphics.effects.CameraManager;
import it.yuruni.graphics.element.Glyph3D;
import it.yuruni.game.level.LevelEvent;
import it.yuruni.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class GameplayScreen implements Screen, InputProcessor {
    private PerspectiveCamera cam;
    private CameraManager cameraManager;
    private ModelBatch modelBatch;
    private DecalBatch decalBatch;
    private Viewport viewport;

    private float timePassed = Main.timePassed;

    // --- 2D Rendering ---
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();


    // --- Managers ---
    private final AnimationManager animationManager = Main.animationManager;
    private LevelManager levelManager;

    // --- Level Data ---
    private final Level level;
    private final Music music;

    // --- 3D Objects ---
    private Model cubeModel;
    private ModelInstance cubeInstance;

    // --- Textures ---
    private Texture noteTexture;
    private Texture overlayTexture;
    private Texture arrowTexture;
    private Texture gameplayBgTexture;
    private Map<Note.Lane, TextureRegion> rotatedArrowTextures;

    // --- Elements ---
    private Glyph3D gameplayBg;
    private Glyph3D leftArrowCover;
    private Glyph3D rightArrowCover;
    private Glyph3D upArrowCover;
    private Glyph3D downArrowCover;

    // --- Gameplay Area ---
    private Rectangle leftPlayArea;
    private Rectangle downPlayArea;
    private Rectangle upPlayArea;
    private Rectangle rightPlayArea;

    // --- Gameplay Variables ---
    private final List<Note> activeNotes = new ArrayList<>();
    private static final float JUDGEMENT_LINE_Z = 400f;
    private static final float PERFECT_WINDOW = 50f; // z-axis distance
    private static final float GOOD_WINDOW = 100f;
    private static final float BAD_WINDOW = 150f;
    private static final float SCROLL_SPEED = 800f; // z-axis units per second
    private static final float NOTE_BASE_SIZE = 150f;


    // --- Variables ---
    private final List<Integer> pressedArrowKeys = new ArrayList<>();
    private final Random random = new Random();
    private TextureRegion noteTextureRegion;

    private final EventManager eventManager = Main.eventManager;
    private boolean isCameraOffset, isInTransition;
    private boolean isExiting;
    private final float cameraShiftDelay = 0.1f;
    private static final float TIMING_OFFSET_MS = 350f;

    public GameplayScreen(Level level, Music music) {
        this.level = level;
        this.music = music;
        Gdx.app.log("GameplayScreen", "Initialized with level: " + level.getName());
    }

    @Override
    public void show() {
        isCameraOffset = isInTransition = false;
        isExiting = false;
        // --- Camera Setup ---
        cam = new PerspectiveCamera(67, Main.WIDTH, Main.HEIGHT);
        cam.position.set(Main.WIDTH / 2f, Main.HEIGHT / 2f, 1000f);
        cam.lookAt(Main.WIDTH / 2f, Main.HEIGHT / 2f, 0f);
        cam.near = 1f;
        cam.far = 500_000f;
        cam.update();

        Gdx.input.setInputProcessor(this);
        cameraManager = new CameraManager(cam);

        // --- Batches and Viewport Setup ---
        modelBatch = new ModelBatch();
        decalBatch = new DecalBatch(new CameraGroupStrategy(cam)); // Must be after cam is created
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(Main.WIDTH, Main.HEIGHT, cam);

        // --- 2D Content ---
        font = new BitmapFont();
        font.getData().setScale(2);
        font.setColor(Color.WHITE);

        // --- Load Textures ---
        overlayTexture = new Texture(Gdx.files.internal("Gameplay.png"));
        noteTexture = new Texture(Gdx.files.internal("KeycapWireframe.png"));
        arrowTexture = new Texture(Gdx.files.internal("arrow.png"));

        noteTextureRegion = new TextureRegion(noteTexture);
        rotatedArrowTextures = new HashMap<>();
        rotatedArrowTextures.put(Note.Lane.LEFT, new TextureRegion(ElementUtils.rotateTextureRightAngles(arrowTexture, 180)));
        rotatedArrowTextures.put(Note.Lane.DOWN, new TextureRegion(ElementUtils.rotateTextureRightAngles(arrowTexture, 270)));
        rotatedArrowTextures.put(Note.Lane.UP, new TextureRegion(ElementUtils.rotateTextureRightAngles(arrowTexture, 90)));
        rotatedArrowTextures.put(Note.Lane.RIGHT, new TextureRegion(ElementUtils.rotateTextureRightAngles(arrowTexture, 0)));

        // --- Create 3D Glyphs ---
        // Clear any old instances from a previous screen session
        Glyph3D.clearInstances();

        // --- Load Gameplay Background ---
        String bgPath = level.getBackgroundImage();
        if (bgPath != null && !bgPath.isEmpty()) {
            String fullPath = "sampleBGs/" + bgPath;
            if (Gdx.files.internal(fullPath).exists()) {
                gameplayBgTexture = new Texture(Gdx.files.internal(fullPath));
                gameplayBg = new Glyph3D(gameplayBgTexture, new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f, -6000f), true); // Position at Z=-4000f

                // Calculate the world dimensions visible by the camera at the background's Z-position
                float cameraDistanceToBg = cam.position.z - gameplayBg.position.z; // Distance from camera to background
                float fovYRad = cam.fieldOfView * MathUtils.degreesToRadians;

                // Visible height at background's Z
                float worldHeightAtBg = 2f * cameraDistanceToBg * MathUtils.tan(fovYRad / 2f);
                // Visible width at background's Z, maintaining camera's aspect ratio
                float worldWidthAtBg = worldHeightAtBg * cam.viewportWidth / cam.viewportHeight;

                // Scale the background texture to cover this calculated world area
                float textureWidth = gameplayBgTexture.getWidth();
                float textureHeight = gameplayBgTexture.getHeight();

                float scaleX = worldWidthAtBg / textureWidth;
                float scaleY = worldHeightAtBg / textureHeight;
                float finalScale = Math.max(scaleX, scaleY); // "Cover" scaling

                gameplayBg.dimension.set(textureWidth * finalScale, textureHeight * finalScale);
                Gdx.app.log("Gameplay", "Loaded gameplay background: " + bgPath);
            }
        }


        // Create Overlay
        Glyph3D overlay = new Glyph3D(new TextureRegion(overlayTexture), new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f, 100f), true);
        overlay.dimension.set(Main.WIDTH, Main.HEIGHT);
        animationManager.animateFade(overlay, 0.2f, 0.001f, Easing.LINEAR);

        // Create Overlay Covers
        upArrowCover = new Glyph3D(new Texture("upArrowCover.png"), new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f + 270.5f, 99f), true);
        downArrowCover = new Glyph3D(new Texture("downArrowCover.png"), new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f - 271, 99f), true);
        leftArrowCover = new Glyph3D(ElementUtils.resizeTo(new Texture("leftArrowCover.png"), 599, 1076), new Vector3(Main.WIDTH / 2f - 650, Main.HEIGHT / 2f - 0.5f, 99f), true);
        rightArrowCover = new Glyph3D(ElementUtils.resizeTo(new Texture("leftArrowCover.png"), 599, 1076), new Vector3(Main.WIDTH / 2f + 650, Main.HEIGHT / 2f - 0.5f, 99f), true);

        // Define the four play areas based on Gameplay.png
        final float verticalDivLineX1 = 690f;
        final float verticalDivLineX2 = 1230f;
        final float horizontalDivLineY = Main.HEIGHT / 2f; // 540f

        leftPlayArea = new Rectangle(0, 0, verticalDivLineX1, Main.HEIGHT);
        rightPlayArea = new Rectangle(verticalDivLineX2, 0, Main.WIDTH - verticalDivLineX2, Main.HEIGHT);
        upPlayArea = new Rectangle(verticalDivLineX1, horizontalDivLineY, verticalDivLineX2 - verticalDivLineX1, Main.HEIGHT - horizontalDivLineY);
        downPlayArea = new Rectangle(verticalDivLineX1, 0, verticalDivLineX2 - verticalDivLineX1, horizontalDivLineY);

        // --- Create Debug Cube ---
        ModelBuilder modelBuilder = new ModelBuilder();
        cubeModel = modelBuilder.createBox(50f, 50f, 50f,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        cubeInstance = new ModelInstance(cubeModel);
        cubeInstance.transform.setToTranslation(Main.WIDTH / 2f, Main.HEIGHT / 2f, 400f);

        //scheduleNextNote();
        // Use the level passed in from level selection
        levelManager = new LevelManager(
            level,
            activeNotes,
            noteTextureRegion,
            rotatedArrowTextures,
            leftPlayArea,
            downPlayArea,
            upPlayArea,
            rightPlayArea
        );

        // Schedule all level events
        for (LevelEvent levelEvent : level.getEvents()) {
            eventManager.addEvent(new Event(timePassed + levelEvent.triggerTime + 3, () -> handleLevelEvent(levelEvent)));
        }

        // Start the level with a 3 second countdown
        float countdownDuration = 3f;
        levelManager.start(countdownDuration);

        // Pre-load all custom shaders for level events
        for (LevelEvent levelEvent : level.getEvents()) {
            if (levelEvent.effectName.equals("custom_shader")) {
                String shaderName = levelEvent.getString("name", null);
                if (shaderName != null) {
                    // Assuming custom shaders use a standard vert shader (e.g., "passthrough.vert")
                    // The user's files have "punch.vert". I'll use that as a default.
                    String vertPath = "shaders/punch.vert";
                    String fragPath = "shaders/" + shaderName;
                    Main.shaderManager.loadShader(shaderName, vertPath, fragPath);
                }
            }
        }

        // Schedule music to start after the countdown
        eventManager.addEvent(new Event(Main.timePassed + countdownDuration, () -> {
            if (music != null) {
                music.setVolume(level.getVolume());
                music.play();
                Gdx.app.log("GameplayScreen", "Music started for level: " + level.getName());
            }
        }));

        float lastNoteSpawnAbsoluteTime = levelManager.getLastNoteSpawnTime();
        float fallbackExitTime = Main.timePassed + countdownDuration;
        float exitTime = (lastNoteSpawnAbsoluteTime > 0f ? lastNoteSpawnAbsoluteTime : fallbackExitTime) + 5f;
        eventManager.addEvent(new Event(exitTime, this::exitToMainMenu));
    }

    private void checkNotes() {
        Iterator<Note> iterator = activeNotes.iterator();
        while (iterator.hasNext()) {
            Note note = iterator.next();

            // A note is missed if it has passed the judgment line and wasn't hit
            if (!note.wasHit && note.glyph.position.z > JUDGEMENT_LINE_Z + BAD_WINDOW) {
                System.out.println("MISS");
                note.wasHit = true; // Mark it as "processed" to be removed
            }

            // Remove notes that have been processed (hit or missed) and are off-screen
            if (note.wasHit && note.glyph.position.z > cam.position.z) {
                note.glyph.isVisible = false; // Hide the glyph
                note.arrowGlyph.isVisible = false; // Hide the arrow glyph
                note.approachGlyph.isVisible = false; // Hide the approach circle
                iterator.remove(); // Remove from active notes
            }
        }
    }

    private void updateNotes(float delta) {

        for (Note note : activeNotes) {
            // Move the note and its arrow
            note.glyph.position.z += SCROLL_SPEED * delta;
            note.arrowGlyph.position.z = note.glyph.position.z;

            // Handle the approach circle, now based on distance
            float travelDuration = note.hitTime - note.spawnTime;
            if (travelDuration > 0) {
                float totalTravelDistance = SCROLL_SPEED * travelDuration;
                float spawnZ = JUDGEMENT_LINE_Z - totalTravelDistance;

                // Calculate progress (0 at spawn, 1 at judgement line)
                float progress = (note.glyph.position.z - spawnZ) / totalTravelDistance;
                progress = Math.max(0, Math.min(1, progress)); // Clamp progress to 0-1 range

                // Animate scale from small to large
                float startScale = 0.1f; // Starts "really small"
                float endScale = 1.0f;   // Ends at the note's normal size
                float currentScale = startScale + (endScale - startScale) * progress;

                float currentSize = NOTE_BASE_SIZE * currentScale;
                note.approachGlyph.dimension.set(currentSize, currentSize);
            } else {
                // If travel duration is zero, just set it to the final size
                note.approachGlyph.dimension.set(NOTE_BASE_SIZE, NOTE_BASE_SIZE);
            }

            // Always keep the approach circle at the same Z position as the note
            note.approachGlyph.position.z = note.glyph.position.z;

            // Hide the approach circle if the note has been processed (hit or missed and past the line)
            if (note.wasHit || note.glyph.position.z > JUDGEMENT_LINE_Z) {
                note.approachGlyph.dimension.set(0, 0);
            }
        }
    }

    @Override
    public void render(float delta) {
        // --- Update and Cleanup ---
        updateNotes(delta);
        checkNotes();

        viewport.apply();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        cam.update();
        cameraManager.update(delta);

        cameraManager.applyEffects();

        float targetCamX = Main.WIDTH / 2f;
        float targetCamY = Main.HEIGHT / 2f;
        final float targetCamZ = 1000f; // Z always stays 1000f

        final float offsetAmount = 30f; // Define the offset magnitude

        boolean anyArrowKeyPressed = !pressedArrowKeys.isEmpty();
        Integer lastPressedKey = null;

        if (anyArrowKeyPressed) {
            lastPressedKey = pressedArrowKeys.get(pressedArrowKeys.size() - 1);
            switch (lastPressedKey) {
                case Input.Keys.UP:
                    targetCamY += offsetAmount;
                    break;
                case Input.Keys.DOWN:
                    targetCamY -= offsetAmount;
                    break;
                case Input.Keys.LEFT:
                    targetCamX -= offsetAmount;
                    break;
                case Input.Keys.RIGHT:
                    targetCamX += offsetAmount;
                    break;
            }
        }


        if (anyArrowKeyPressed) {
            if (!isInTransition && (cam.position.x != targetCamX || cam.position.y != targetCamY)) {
                isCameraOffset = true;
                isInTransition = true;
                cameraManager.setPosition3D(targetCamX, targetCamY, targetCamZ, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);

                if (lastPressedKey == Input.Keys.UP) {
                    animationManager.animateFade(upArrowCover, 0.1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
                    resetCovers();
                } else if (lastPressedKey == Input.Keys.DOWN) {
                    animationManager.animateFade(downArrowCover, 0.1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
                    resetCovers();
                } else if (lastPressedKey == Input.Keys.LEFT) {
                    animationManager.animateFade(leftArrowCover, 0.1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
                    resetCovers();
                } else if (lastPressedKey == Input.Keys.RIGHT) {
                    animationManager.animateFade(rightArrowCover, 0.1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
                    resetCovers();
                }

                eventManager.addEvent(new Event(Main.timePassed + cameraShiftDelay, () -> {
                    isInTransition = false;
                }));
            }
        } else {
            if (isCameraOffset && !isInTransition) {
                isInTransition = true;
                cameraManager.setPosition3D(Main.WIDTH / 2f, Main.HEIGHT / 2f, 1000f, 0.2f, Easing.EASE_IN_OUT_QUAD);
                resetCovers();
                eventManager.addEvent(new Event(Main.timePassed + cameraShiftDelay, () -> {
                    isCameraOffset = false;
                    isInTransition = false;
                }));
            }
        }


        // Spin the cube
        cubeInstance.transform.rotate(Vector3.Y, 45 * delta);

        // Render all 3D Glyphs
        Glyph3D.updateAndRenderAll(cam, decalBatch);
        decalBatch.flush();

        // Render note text
        spriteBatch.begin();
        for (Note note : activeNotes) {
            boolean shouldDisplayKey = false;
            if (lastPressedKey != null) {
                shouldDisplayKey = switch (note.lane) {
                    case UP -> Objects.equals(lastPressedKey, Input.Keys.UP);
                    case DOWN -> Objects.equals(lastPressedKey, Input.Keys.DOWN);
                    case LEFT -> Objects.equals(lastPressedKey, Input.Keys.LEFT);
                    case RIGHT -> Objects.equals(lastPressedKey, Input.Keys.RIGHT);
                };
            }

            if (shouldDisplayKey) { // Renders the text as long as the arrow key is held
                // Only draw text for notes that are in front of the camera
                if (note.glyph.position.z < cam.position.z) {
                    Vector3 notePos = note.glyph.position;
                    // Project 3D position to 2D screen coordinates
                    Vector3 screenPos = cam.project(new Vector3(notePos));

                    // Sync font alpha with decal alpha
                    float alpha = note.glyph.decal.getColor().a;
                    font.setColor(1, 1, 1, alpha);

                    // Sync font scale with decal scale
                    float noteSize = 150f;
                    float scale = note.glyph.dimension.x / noteSize;
                    font.getData().setScale(2 * scale);

                    String keyStr = Input.Keys.toString(note.keycode);
                    layout.setText(font, keyStr);
                    float fontX = screenPos.x - layout.width / 2;
                    float fontY = screenPos.y + layout.height / 2;

                    font.draw(spriteBatch, layout, fontX, fontY);

                    // Reset font to default
                    font.setColor(Color.WHITE);
                    font.getData().setScale(2);
                }
            }
        }
        spriteBatch.end();

        // Render the cube
        modelBatch.begin(cam);
        //modelBatch.render(cubeInstance);
        modelBatch.end();

        cameraManager.resetEffects();
    }

    private void resetCovers() {
        animationManager.animateFade(upArrowCover, 1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
        animationManager.animateFade(downArrowCover, 1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
        animationManager.animateFade(leftArrowCover, 1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
        animationManager.animateFade(rightArrowCover, 1f, cameraShiftDelay, Easing.EASE_IN_OUT_QUAD);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, false);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        Glyph3D.clearInstances(); // Clear static list
        modelBatch.dispose();
        //cubeModel.dispose();
        if (decalBatch != null) decalBatch.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
        if (noteTexture != null) noteTexture.dispose();
        if (overlayTexture != null) overlayTexture.dispose();
        if (arrowTexture != null) arrowTexture.dispose();

        if (gameplayBgTexture != null) {
            gameplayBgTexture.dispose();
        }
        for (TextureRegion region : rotatedArrowTextures.values()) {
            region.getTexture().dispose();
        }
        // Clear all static managers
        Main.eventManager.clear();
        Main.animationManager.clear();
        // Stop and dispose music
        if (music != null) {
            music.stop();
            music.dispose();
        }
    }

    private void exitToMainMenu() {
        if (isExiting) {
            return;
        }
        isExiting = true;
        for (Note note : activeNotes) {
            note.glyph.isVisible = false;
            note.arrowGlyph.isVisible = false;
            note.approachGlyph.isVisible = false;
        }
        activeNotes.clear();
        if (music != null) {
            music.stop();
        }
        ((Main) Gdx.app.getApplicationListener()).setScreen(Main.mainScreen);
    }

    // --- InputProcessor Methods ---

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.GRAVE || keycode == Input.Keys.NUM_7 || keycode == Input.Keys.NUM_9) {
            float elapsedMs = -1f;
            if (music != null && music.isPlaying()) {
                elapsedMs = music.getPosition() * 1000f;
            } else if (levelManager != null && levelManager.getSongStartTime() >= 0f) {
                elapsedMs = (Main.timePassed - levelManager.getSongStartTime()) * 1000f;
            }
            if (elapsedMs < 0f) {
                Gdx.app.log("GameplayScreen", "Level progress: not started");
            } else {
                float adjustedMs = elapsedMs + TIMING_OFFSET_MS;
                Gdx.app.log("GameplayScreen", String.format("Level progress: %.0f ms", adjustedMs));
            }
            return true;
        }
        // --- Gameplay Input ---
        // Find the closest, hittable note that matches the pressed key
        Note noteToHit = null;
        float closestDist = Float.MAX_VALUE;

        for (Note note : activeNotes) {
            if (note.keycode == keycode && !note.wasHit) {
                float dist = Math.abs(note.glyph.position.z - JUDGEMENT_LINE_Z);
                if (dist < closestDist) {
                    closestDist = dist;
                    noteToHit = note;
                }
            }
        }

        // If we found a note, judge it
        if (noteToHit != null) {
            float dist = Math.abs(noteToHit.glyph.position.z - JUDGEMENT_LINE_Z);
            if (dist <= PERFECT_WINDOW) {
                System.out.println("PERFECT - " + Input.Keys.toString(keycode));
                noteToHit.wasHit = true;
                animationManager.animateScale(noteToHit.glyph, 200f, 200f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.glyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateScale(noteToHit.arrowGlyph, 100f, 100f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.arrowGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.approachGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
            } else if (dist <= GOOD_WINDOW) {
                System.out.println("GOOD - " + Input.Keys.toString(keycode));
                noteToHit.wasHit = true;
                animationManager.animateScale(noteToHit.glyph, 200f, 200f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.glyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateScale(noteToHit.arrowGlyph, 100f, 100f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.arrowGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.approachGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
            } else if (dist <= BAD_WINDOW) {
                System.out.println("BAD - " + Input.Keys.toString(keycode));
                noteToHit.wasHit = true;
                animationManager.animateScale(noteToHit.glyph, 200f, 200f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.glyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateScale(noteToHit.arrowGlyph, 100f, 100f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.arrowGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.approachGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
            }
        }


        // --- UI Input (Arrow Keys) ---
        if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN || keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
            pressedArrowKeys.remove((Integer) keycode);
            pressedArrowKeys.add(keycode);
            return true;
        }
        return false;
    }


    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN || keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
            pressedArrowKeys.remove((Integer) keycode);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
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

    private void handleLevelEvent(LevelEvent event) {
        Gdx.app.log("GameplayScreen", "Event fired: " + event.effectName + " at " + event.triggerTime);

        switch (event.effectName) {
            case "camera_shake":
                float shakeDuration = event.getFloat("duration", 0.1f);
                float shakeIntensity = event.getFloat("intensity", 5f);
                cameraManager.shake(shakeDuration, shakeIntensity, 0.0025f);
                break;
            case "camera_move":
                float targetX = event.getFloat("x", cam.position.x);
                float targetY = event.getFloat("y", cam.position.y);
                float targetZ = event.getFloat("z", cam.position.z);
                float moveDuration = event.getFloat("duration", 1f);
                String easingName = event.getString("easing", "LINEAR");
                Easing easing = Easing.valueOf(easingName.toUpperCase());
                cameraManager.setPosition3D(targetX, targetY, targetZ, moveDuration, easing);
                break;
            case "shader_punch":
                float punchAmount = event.getFloat("amount", 1f);
                float punchDuration = event.getFloat("duration", 0f); // Duration for the effect itself
                Main.shaderManager.setPunch(punchAmount);
                if (punchDuration > 0) {
                    // Schedule deactivation if a duration is specified
                    eventManager.addEvent(new Event(event.triggerTime + punchDuration, () -> {
                        Main.shaderManager.setPunch(0f);
                        Main.shaderManager.deactivateShader(); // Deactivate explicitly
                    }));
                }
                break;
            case "custom_shader":
                String shaderName = event.getString("name", null);
                float shaderDuration = event.getFloat("duration", 0f);
                if (shaderName != null) {
                    // Custom shaders need to be loaded once, preferably at the beginning of the screen.
                    // For now, assume it's loaded and just activate it.
                    Main.shaderManager.activateShader(shaderName, shaderDuration);
                }
                break;
            case "deactivate_shader": // For explicit deactivation
                Main.shaderManager.deactivateShader();
                break;
            default:
                Gdx.app.error("GameplayScreen", "Unknown event effect: " + event.effectName);
                break;
        }
    }
}
