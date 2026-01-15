package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import it.yuruni.graphics.Easing;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.Event;
import it.yuruni.graphics.animation.EventManager;
import it.yuruni.graphics.effects.CameraManager;
import it.yuruni.graphics.element.Glyph3D;
import it.yuruni.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameplayScreen implements Screen, InputProcessor {
    private CameraInputController controller; //TODO: TEMPORARY, REMOVE
    private PerspectiveCamera cam;
    private CameraManager cameraManager;
    private ModelBatch modelBatch;
    private DecalBatch decalBatch;
    private Viewport viewport;

    // --- static imports from Main ---
    private AnimationManager animationManager = Main.animationManager;

    // --- 3D Objects ---
    private Model cubeModel;
    private ModelInstance cubeInstance;

    // --- Textures (owned by the screen) ---
    private Texture noteTexture;
    private Texture overlayTexture;

    // --- Elements ---
    private Glyph3D leftArrowCover;
    private Glyph3D rightArrowCover;
    private Glyph3D upArrowCover;
    private Glyph3D downArrowCover;

    // --- Variables ---
    private final List<Integer> pressedArrowKeys = new ArrayList<>();
    //private boolean upWasPressed, downWasPressed, leftWasPressed, rightWasPressed;

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

        // controller = new CameraInputController(cam); //TODO: TEMPORARY, REMOVE
        Gdx.input.setInputProcessor(this);
        cameraManager = new CameraManager(cam);

        // --- Batches and Viewport Setup ---
        modelBatch = new ModelBatch();
        decalBatch = new DecalBatch(new CameraGroupStrategy(cam)); // Must be after cam is created
        viewport = new FitViewport(Main.WIDTH, Main.HEIGHT, cam);

        // --- Load Textures ---
        overlayTexture = new Texture(Gdx.files.internal("Gameplay.png"));
        noteTexture = new Texture(Gdx.files.internal("KeycapWireframe.png"));

        TextureRegion noteTextureRegion = new TextureRegion(noteTexture);

        // --- Create 3D Glyphs ---
        // Clear any old instances from a previous screen session
        Glyph3D.clearInstances();

        // Create Overlay
        Glyph3D overlay = new Glyph3D(new TextureRegion(overlayTexture), new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f, 100f), true);
        overlay.dimension.set(Main.WIDTH, Main.HEIGHT);

        // Create Overlay Covers
        upArrowCover = new Glyph3D(new Texture("upArrowCover.png"), new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f + 270.5f, 99f), true);
        downArrowCover = new Glyph3D(new Texture("downArrowCover.png"), new Vector3(Main.WIDTH / 2f, Main.HEIGHT / 2f - 271, 99f), true);
        leftArrowCover = new Glyph3D(ElementUtils.resizeTo(new Texture("leftArrowCover.png"), 599, 1076), new Vector3(Main.WIDTH / 2f - 650, Main.HEIGHT / 2f - 0.5f, 99f), true);
        rightArrowCover = new Glyph3D(ElementUtils.resizeTo(new Texture("leftArrowCover.png"), 599, 1076), new Vector3(Main.WIDTH / 2f + 650, Main.HEIGHT / 2f - 0.5f, 99f), true);

        // Create Notes
        float noteSize = 100f;
        new Glyph3D(noteTextureRegion, new Vector3(Main.WIDTH / 2f - 200f, Main.HEIGHT / 2f, 0f), true).dimension.set(noteSize, noteSize);
        new Glyph3D(noteTextureRegion, new Vector3(Main.WIDTH / 2f + 100f, Main.HEIGHT / 2f, -300f), true).dimension.set(noteSize, noteSize);
        new Glyph3D(noteTextureRegion, new Vector3(Main.WIDTH / 2f - 100f, Main.HEIGHT / 2f, -600f), true).dimension.set(noteSize, noteSize);
        new Glyph3D(noteTextureRegion, new Vector3(Main.WIDTH / 2f + 250f, Main.HEIGHT / 2f, -900f), true).dimension.set(noteSize, noteSize);

        // --- Create Debug Cube ---
        ModelBuilder modelBuilder = new ModelBuilder();
        cubeModel = modelBuilder.createBox(50f, 50f, 50f,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        cubeInstance = new ModelInstance(cubeModel);
        cubeInstance.transform.setToTranslation(Main.WIDTH / 2f, Main.HEIGHT / 2f, 400f);
    }

    @Override
    public void render(float delta) {
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
        Glyph3D.updateAndRenderAll(null, decalBatch);
        decalBatch.flush();

        // Render the cube
        modelBatch.begin(cam);
        modelBatch.render(cubeInstance);
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
        cubeModel.dispose();
        decalBatch.dispose();
        noteTexture.dispose();
        overlayTexture.dispose();
    }

    // --- InputProcessor Methods ---

    @Override
    public boolean keyDown(int keycode) {
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
