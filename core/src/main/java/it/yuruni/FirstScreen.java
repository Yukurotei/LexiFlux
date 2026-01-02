package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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


    //particles
    private YParticleEffect concentration;


    @Override
    public void show() {
        batch = new SpriteBatch();

        audioManager = new AudioEffectManager(
                Gdx.files.internal("SECRET BOSS_muffled.mp3"),
                Gdx.files.internal("SECRET BOSS.mp3")
        );



        concentration = new YParticleEffect(true);
        concentration.load(Gdx.files.internal("./particles/downConcentration.p"), Gdx.files.internal("particles"));
        concentration.setPosition(Main.WIDTH / 2f - 670f, -370);
        concentration.scaleEffect(3);

        Glyph bg = new Glyph(new Texture("./sampleBGs/bg.png"), 0, 0, true);
        bg.setAlpha(0f);

        Glyph glyph = new Glyph(new Texture("./logo/LogoLayout.png"), 0, 0, true);
        glyph.setAlpha(0f);

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
            Main.shaderManager.setPunch(1.0f);
            concentration.start();
            animationManager.animatePulse(logo, 220, 1.05f);
            animationManager.animateFade(flash, 1f, 0.5f, AnimationManager.Easing.EASE_IN_OUT_EXPO);
        }));
        eventManager.addEvent(new Event(8.25f, () -> {
            animationManager.animateFade(bg, 1f, 1f, AnimationManager.Easing.EASE_IN_QUART);
        }));
        eventManager.addEvent(new Event(8.5f, () -> {
            animationManager.animateFade(flash, 0f, 1.5f, AnimationManager.Easing.LINEAR);
        }));
        eventManager.addEvent(new Event(9f, () -> {
            concentration.allowCompletion();
        }));
    }

        @Override
        public void render(float delta) {
            // --- Update logic ---
            timePassed += delta;
            eventManager.update(timePassed);
            audioManager.update(delta);
    
            // --- Render scene into ShaderManager's FBO ---
            Main.shaderManager.begin();
            
            ScreenUtils.clear(0, 0, 0, 1); // Clear the FBO
    
            // Use the main game camera
            batch.setProjectionMatrix(Main.camera.combined);
    
            batch.begin();
            animationManager.updateAndRenderGlyphs(delta, batch);
            for (YParticleEffect eff : Main.particles) {
                eff.update(delta);
                eff.draw(batch);
            }
            batch.end();
            
            // --- End FBO rendering and apply shaders to screen ---
            Main.shaderManager.end(delta);
        }
    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        if(width <= 0 || height <= 0) return;
        Main.viewport.update(width, height, true);
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
    }
}
