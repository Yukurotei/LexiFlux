package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ScreenUtils;

import it.yuruni.audio.AudioEffectManager;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.Event;
import it.yuruni.graphics.animation.EventManager;
import it.yuruni.graphics.animation.Glyph;
import it.yuruni.graphics.effects.YParticleEffect;


/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private final AnimationManager animationManager = Main.animationManager;
    private final EventManager eventManager = Main.eventManager;
    private AudioEffectManager audioManager;
    private float timePassed = 0f;

    //post-processing
    private FrameBuffer fbo;
    private ShaderProgram punchShader;
    private OrthographicCamera screenCamera;
    private float punchIntensity = 0f;


    @Override
    public void show() {
        batch = new SpriteBatch();
        screenCamera = new OrthographicCamera();

        audioManager = new AudioEffectManager(
                Gdx.files.internal("SECRET BOSS_muffled.mp3"),
                Gdx.files.internal("SECRET BOSS.mp3")
        );

        punchShader = new ShaderProgram(Gdx.files.internal("shaders/punch.vert"), Gdx.files.internal("shaders/punch.frag"));
        if (!punchShader.isCompiled()) {
            Gdx.app.error("PunchShader", "compilation failed:\n" + punchShader.getLog());
        }

        Glyph bg = new Glyph(new Texture("./sampleBGs/bg.png"), 0, 0, true);
        bg.setAlpha(0f);

        Glyph glyph = new Glyph(new Texture("./logo/LogoLayout.png"), 0, 0, true);
        glyph.setAlpha(0f);
        //animationManager.animateFade(glyph, 1f, 2f, AnimationManager.Easing.EASE_IN_QUAD);

        Glyph keyboard = new Glyph(new Texture("./keyboard.png"), Main.WIDTH / 2f - 670, 1000, true);
        keyboard.setScaleX(keyboard.getScaleX() * 0.67f);
        keyboard.setScaleY(keyboard.getScaleY() * 0.67f);
        animationManager.animateMove(keyboard, keyboard.getX(), keyboard.getY() - 1000, 2f, AnimationManager.Easing.EASE_IN_OUT_QUAD);

        Glyph flash = new Glyph(new Texture("./whiteCirc.png"), Main.WIDTH / 2f, Main.HEIGHT / 2f, true);
        flash.setScaleX(flash.getScaleX() * 100f);
        flash.setScaleY(flash.getScaleY() * 100f);
        flash.setAlpha(0f);

        Glyph logo = new Glyph(new Texture("./logo/shortLogo.png"), 323, 289 + 1000, true);
        logo.setScaleX(logo.getScaleX() * 0.15f);
        logo.setScaleY(logo.getScaleY() * 0.15f);
        animationManager.animateMove(logo, logo.getX(), logo.getY() - 1000, 2f, AnimationManager.Easing.EASE_IN_OUT_QUAD);

        eventManager.addEvent(new Event(2f, () -> {
            //downConcentration.start();
            //upConcentration.start();
        }));

        eventManager.addEvent(new Event(3f, () -> {
            audioManager.startTransition(5f, 0.05f, 1f, 13f);
            float factor = 3f;
            animationManager.animateScale(keyboard, keyboard.getScaleX() * factor, keyboard.getScaleY() * factor, 4f, AnimationManager.Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(keyboard, keyboard.getX() + 1000, keyboard.getY() + 180, 4f, AnimationManager.Easing.EASE_IN_OUT_QUAD);
            animationManager.animateScale(logo, logo.getScaleX() * factor, logo.getScaleY() * factor, 4f, AnimationManager.Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(logo, logo.getX() + 330, logo.getY() - 20, 4f, AnimationManager.Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(keyboard, 0f, 4f, AnimationManager.Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(bg, 0.1f, 4f, AnimationManager.Easing.EASE_IN_QUART);
        }));

        eventManager.addEvent(new Event(8f, () -> {
            punchIntensity = 1.0f;
            animationManager.animatePulse(logo, 220, 1.05f);
            animationManager.animateFade(flash, 1f, 0.5f, AnimationManager.Easing.EASE_IN_OUT_EXPO);
        }));
        eventManager.addEvent(new Event(8.25f, () -> {
            animationManager.animateFade(bg, 1f, 1f, AnimationManager.Easing.EASE_IN_QUART);
        }));
        eventManager.addEvent(new Event(8.5f, () -> {
            animationManager.animateFade(flash, 0f, 1.5f, AnimationManager.Easing.LINEAR);
        }));
        /*
        eventManager.addEvent(new Event(9f, () -> {
            downConcentration.allowCompletion();
        }));
         */
    }

    @Override
    public void render(float delta) {
        timePassed += delta;
        eventManager.update(timePassed);
        audioManager.update(delta);
        if (punchIntensity > 0) {
            punchIntensity -= delta;
            if (punchIntensity < 0) {
                punchIntensity = 0;
            }
        }


        // --- Scene Rendering (to FrameBuffer) ---
        fbo.begin();
        ScreenUtils.clear(0, 0, 0, 1); // Clear the FBO

        // Use the main game camera
        batch.setProjectionMatrix(Main.camera.combined);

        batch.begin();
        animationManager.updateAndRenderGlyphs(delta, batch);
        for (YParticleEffect eff : Main.particles) {
            eff.update(delta); // This should probably be outside the fbo block
            eff.draw(batch);
        }
        batch.end();
        fbo.end();


        // --- Post-processing (render FBO to screen) ---
        // Use the screen camera
        screenCamera.update();
        batch.setProjectionMatrix(screenCamera.combined);

        batch.setShader(punchShader);
        batch.begin();
        punchShader.setUniformf("u_punch", punchIntensity);

        // Draw the FBO texture flipped vertically to appear correctly
        batch.draw(fbo.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());

        batch.end();
        batch.setShader(null); // IMPORTANT: Reset to default shader
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        if(width <= 0 || height <= 0) return;
        Main.viewport.update(width, height, true);
        screenCamera.setToOrtho(false, width, height);

        if (fbo != null) {
            fbo.dispose();
        }
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        batch.dispose();
        audioManager.dispose();
        punchShader.dispose();
        fbo.dispose();
    }
}
