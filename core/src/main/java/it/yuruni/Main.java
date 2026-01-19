package it.yuruni;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.EventManager;
import it.yuruni.graphics.element.Glyph;
import it.yuruni.graphics.effects.CameraManager;
import it.yuruni.graphics.effects.ParallaxManager;
import it.yuruni.graphics.effects.ShaderManager;
import it.yuruni.graphics.effects.YParticleEffect;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public static final List<Glyph> glyphs = new ArrayList<>();
    public static final List<YParticleEffect> particles = new ArrayList<>();
    public static AnimationManager animationManager;
    public static EventManager eventManager;
    public static ShaderManager shaderManager;
    public static CameraManager cameraManager;
    public static ParallaxManager parallaxManager;
    public static FirstScreen mainScreen;

    //VirtualViewport
    public static OrthographicCamera camera;
    public static Viewport viewport;
    public static final float WIDTH = 1920, HEIGHT = 1080;

    public static float timePassed = 0f;
    private static final float MAX_FRAME_DELTA = 0.1f;
    private boolean firstFrame = true;


    @Override
    public void create() {
        animationManager = new AnimationManager();
        eventManager = new EventManager();
        shaderManager = new ShaderManager();
        parallaxManager = new ParallaxManager();

        //Cam
        camera = new OrthographicCamera();
        cameraManager = new CameraManager(camera);
        viewport = new FitViewport(WIDTH, HEIGHT, camera);

        mainScreen = new FirstScreen();
        setScreen(mainScreen);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), MAX_FRAME_DELTA);

        if (firstFrame) {
            delta = 0f;
            firstFrame = false;
        }

        timePassed += delta;
        eventManager.update(timePassed);
        animationManager.update(delta);
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        shaderManager.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        shaderManager.dispose();
        while (!glyphs.isEmpty()) {
            glyphs.get(0).dispose();
        }
    }
}
