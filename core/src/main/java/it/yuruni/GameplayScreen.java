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
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class GameplayScreen implements Screen {
    private PerspectiveCamera cam;
    private CameraInputController controller;
    private ModelBatch modelBatch;
    private Model model;
    private ModelInstance instance;
    private DecalBatch decalBatch;
    private Viewport viewport;

    ArrayList<Decal> decals;
    private Texture noteTextureSheet;


    @Override
    public void show() {
        decals = new ArrayList<>();
        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(67, Main.WIDTH, Main.HEIGHT);
        cam.position.set(Main.WIDTH / 2f, Main.HEIGHT / 2f, 800f);
        cam.lookAt(Main.WIDTH / 2f, Main.HEIGHT / 2f, 0f);
        cam.near = 1f;
        cam.far = 5000f;
        cam.update();

        controller = new CameraInputController(cam);
        viewport = new FitViewport(Main.WIDTH, Main.HEIGHT, cam);
        decalBatch = new DecalBatch(new CameraGroupStrategy(cam));

        Gdx.input.setInputProcessor(controller);

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(50f, 50f, 50f, // Make cube much bigger
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);
        instance.transform.setToTranslation(Main.WIDTH / 2f, Main.HEIGHT / 2f, 0f); // Position cube closer to camera

        // Note Decals
        noteTextureSheet = new Texture(Gdx.files.internal("logo/ChromaKey.png"));
        TextureRegion noteTextureRegion = new TextureRegion(noteTextureSheet);

        float noteSize = 100f; // Standard size for notes

        Decal note1 = Decal.newDecal(noteTextureRegion, true);
        note1.setDimensions(noteSize, noteSize);
        note1.setPosition(Main.WIDTH / 2f - 200f, Main.HEIGHT / 2f, 0f);
        decals.add(note1);

        Decal note2 = Decal.newDecal(noteTextureRegion, true);
        note2.setDimensions(noteSize, noteSize);
        note2.setPosition(Main.WIDTH / 2f + 100f, Main.HEIGHT / 2f, -300f);
        decals.add(note2);

        Decal note3 = Decal.newDecal(noteTextureRegion, true);
        note3.setDimensions(noteSize, noteSize);
        note3.setPosition(Main.WIDTH / 2f - 100f, Main.HEIGHT / 2f, -600f);
        decals.add(note3);

        Decal note4 = Decal.newDecal(noteTextureRegion, true);
        note4.setDimensions(noteSize, noteSize);
        note4.setPosition(Main.WIDTH / 2f + 250f, Main.HEIGHT / 2f, -900f);
        decals.add(note4);
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        instance.transform.rotate(Vector3.Y, 45 * delta);
        controller.update();
        cam.update();

        //Add decals
        for (Decal decal : decals) {
            //decal.lookAt(cam.position, cam.up);
            decalBatch.add(decal);
        }

        decalBatch.flush();

        modelBatch.begin(cam);
        modelBatch.render(instance);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, false); // Set to false to not auto-center the camera
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        model.dispose();
        decalBatch.dispose();
        noteTextureSheet.dispose();
    }
}
