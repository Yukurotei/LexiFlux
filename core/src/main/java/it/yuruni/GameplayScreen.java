package it.yuruni;

import com.badlogic.gdx.Gdx;
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
import it.yuruni.graphics.element.Glyph3D;

public class GameplayScreen implements Screen {
    private CameraInputController controller;
    private PerspectiveCamera cam;
    private ModelBatch modelBatch;
    private DecalBatch decalBatch;
    private Viewport viewport;

    // --- 3D Objects ---
    private Model cubeModel;
    private ModelInstance cubeInstance;

    // --- Textures (owned by the screen) ---
    private Texture noteTexture;
    private Texture overlayTexture;


    @Override
    public void show() {
        // --- Camera Setup ---
        cam = new PerspectiveCamera(67, Main.WIDTH, Main.HEIGHT);
        cam.position.set(Main.WIDTH / 2f, Main.HEIGHT / 2f, 800f);
        cam.lookAt(Main.WIDTH / 2f, Main.HEIGHT / 2f, 0f);
        cam.near = 1f;
        cam.far = 5000f;
        cam.update();

        controller = new CameraInputController(cam);
        Gdx.input.setInputProcessor(controller);

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
        controller.update();

        // Spin the cube
        cubeInstance.transform.rotate(Vector3.Y, 45 * delta);

        // Render all 3D Glyphs
        Glyph3D.updateAndRenderAll(null, decalBatch);
        decalBatch.flush();

        // Render the cube
        modelBatch.begin(cam);
        modelBatch.render(cubeInstance);
        modelBatch.end();
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
}
