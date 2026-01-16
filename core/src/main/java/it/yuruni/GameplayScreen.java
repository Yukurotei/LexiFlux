package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import it.yuruni.game.Note;
import it.yuruni.graphics.Easing;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.Event;
import it.yuruni.graphics.animation.EventManager;
import it.yuruni.graphics.effects.CameraManager;
import it.yuruni.graphics.element.Glyph3D;
import it.yuruni.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class GameplayScreen implements Screen, InputProcessor {
    private CameraInputController controller; //TODO: TEMPORARY, REMOVE
    private PerspectiveCamera cam;
    private CameraManager cameraManager;
    private ModelBatch modelBatch;
    private DecalBatch decalBatch;
    private Viewport viewport;

    // --- 2D Rendering ---
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();


    // --- static imports from Main ---
    private final AnimationManager animationManager = Main.animationManager;

    // --- 3D Objects ---
    private Model cubeModel;
    private ModelInstance cubeInstance;

    // --- Textures ---
    private Texture noteTexture;
    private Texture overlayTexture;
    private Texture arrowTexture;
    private Map<Note.Lane, TextureRegion> rotatedArrowTextures;

    // --- Elements ---
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
    private static final List<Integer> PLAYABLE_KEYS = new ArrayList<Integer>() {{
        add(Input.Keys.Q); add(Input.Keys.W); add(Input.Keys.E); add(Input.Keys.R); add(Input.Keys.T);
        add(Input.Keys.Y); add(Input.Keys.U); add(Input.Keys.I); add(Input.Keys.O); add(Input.Keys.P);
        add(Input.Keys.A); add(Input.Keys.S); add(Input.Keys.D); add(Input.Keys.F); add(Input.Keys.G);
        add(Input.Keys.H); add(Input.Keys.J); add(Input.Keys.K); add(Input.Keys.L); add(Input.Keys.SEMICOLON);
        add(Input.Keys.Z); add(Input.Keys.X); add(Input.Keys.C); add(Input.Keys.V); add(Input.Keys.B);
        add(Input.Keys.N); add(Input.Keys.M); add(Input.Keys.COMMA); add(Input.Keys.PERIOD);
    }};


    // --- Variables ---
    private final List<Integer> pressedArrowKeys = new ArrayList<>();
    private final Random random = new Random();
    private TextureRegion noteTextureRegion;

    private final EventManager eventManager = Main.eventManager;
    private boolean isCameraOffset, isInTransition;
    private final float cameraShiftDelay = 0.1f;


    @Override
    public void show() {
        isCameraOffset = isInTransition = false;
        // --- Camera Setup ---
        cam = new PerspectiveCamera(67, Main.WIDTH, Main.HEIGHT);
        cam.position.set(Main.WIDTH / 2f, Main.HEIGHT / 2f, 1000f);
        cam.lookAt(Main.WIDTH / 2f, Main.HEIGHT / 2f, 0f);
        cam.near = 1f;
        cam.far = 5000f;
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

        scheduleNextNote();
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
                iterator.remove(); // Remove from active notes
            }
        }
    }

    @Override
    public void render(float delta) {
        // --- Update and Cleanup ---
        checkNotes();


        viewport.apply();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        cam.update();
        cameraManager.update(delta);
        //controller.update();

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

    private void spawnRandomNote() {
        // 1. Select random lane
        Note.Lane lane;
        Rectangle targetArea;
        int laneIndex = random.nextInt(4);

        switch (laneIndex) {
            case 0:
                targetArea = leftPlayArea;
                lane = Note.Lane.LEFT;
                break;
            case 1:
                targetArea = downPlayArea;
                lane = Note.Lane.DOWN;
                break;
            case 2:
                targetArea = upPlayArea;
                lane = Note.Lane.UP;
                break;
            case 3:
                targetArea = rightPlayArea;
                lane = Note.Lane.RIGHT;
                break;
            default:
                return; // Should not happen
        }

        // 2. Define start and end positions
        float startX = Main.WIDTH / 2f;
        float startY = Main.HEIGHT / 2f;
        float startZ = -3000f; // Start far away
        float endZ = 1200f;   // End far behind the player camera

        float endX = targetArea.x + targetArea.width / 2f;
        float endY = targetArea.y + targetArea.height / 2f;

        // Add some random horizontal spread within the lane
        float padding = 50f; // Prevent notes from being right on the edge
        float spread = (targetArea.width / 2f) - padding;
        if (spread > 0) {
            endX += random.nextFloat() * spread * 2 - spread;
        }

        // 3. Create note glyph and Note object
        Glyph3D noteGlyph = new Glyph3D(noteTextureRegion, new Vector3(startX, startY, startZ), true);
        float noteSize = 150f;
        noteGlyph.dimension.set(noteSize, noteSize);

        // Get the pre-rotated texture for the arrow
        TextureRegion arrowTextureRegion = rotatedArrowTextures.get(lane);
        Glyph3D arrowGlyph = new Glyph3D(arrowTextureRegion, new Vector3(startX, startY, startZ - 1f), true); // Slightly in front
        float arrowSize = 75f;
        arrowGlyph.dimension.set(arrowSize, arrowSize);

        // Assign a random key
        int keycode = PLAYABLE_KEYS.get(random.nextInt(PLAYABLE_KEYS.size()));

        // Calculate duration based on a fixed speed
        float speed = 1050f; // units per second
        float distance = Math.abs(endZ - startZ);
        float duration = distance / speed;

        // Calculate the actual hit time based on the speed and distance to the judgment line
        float travelDurationToJudgement = Math.abs(JUDGEMENT_LINE_Z - startZ) / speed;

        Note note = new Note(noteGlyph, arrowGlyph, lane, keycode, Main.timePassed, travelDurationToJudgement);
        activeNotes.add(note);


        // 4. Animate it
        animationManager.animateMove(noteGlyph, endX, endY, endZ, duration, Easing.LINEAR);
        animationManager.animateMove(arrowGlyph, endX, endY, endZ - 1f, duration, Easing.LINEAR);
    }

    private void scheduleNextNote() {
        eventManager.addEvent(new Event(Main.timePassed + 3f, () -> { // Spawn a new note every second
            spawnRandomNote();
            scheduleNextNote(); // Schedule the next one
        }));
    }

    @Override
    public void dispose() {
        Glyph3D.clearInstances(); // Clear static list
        modelBatch.dispose();
        cubeModel.dispose();
        decalBatch.dispose();
        spriteBatch.dispose();
        font.dispose();
        noteTexture.dispose();
        overlayTexture.dispose();
        arrowTexture.dispose();
        for (TextureRegion region : rotatedArrowTextures.values()) {
            region.getTexture().dispose();
        }
    }

    // --- InputProcessor Methods ---

    @Override
    public boolean keyDown(int keycode) {
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
            } else if (dist <= GOOD_WINDOW) {
                System.out.println("GOOD - " + Input.Keys.toString(keycode));
                noteToHit.wasHit = true;
                animationManager.animateScale(noteToHit.glyph, 200f, 200f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.glyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateScale(noteToHit.arrowGlyph, 100f, 100f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.arrowGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
            } else if (dist <= BAD_WINDOW) {
                System.out.println("BAD - " + Input.Keys.toString(keycode));
                noteToHit.wasHit = true;
                animationManager.animateScale(noteToHit.glyph, 200f, 200f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.glyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateScale(noteToHit.arrowGlyph, 100f, 100f, 0.2f, Easing.EASE_OUT_QUAD);
                animationManager.animateFade(noteToHit.arrowGlyph, 0f, 0.2f, Easing.EASE_OUT_QUAD);
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
}
