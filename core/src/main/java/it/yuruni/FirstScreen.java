package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import it.yuruni.audio.AudioEffectManager;

import it.yuruni.graphics.animation.*;

import it.yuruni.graphics.effects.CameraManager;
import it.yuruni.graphics.effects.ParallaxManager;
import it.yuruni.graphics.effects.ShaderManager;
import it.yuruni.graphics.effects.YParticleEffect;
import it.yuruni.ui.Button;

import javax.swing.*;


/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private final AnimationManager animationManager = Main.animationManager;
    private final ParallaxManager parallaxManager = Main.parallaxManager;
    private final CameraManager cameraManager = Main.cameraManager;
    private final ShaderManager shaderManager = Main.shaderManager;
    private final EventManager eventManager = Main.eventManager;
    private AudioEffectManager audioManager;
    private float timePassed = 0f;

    Button mainButton;

    private Array<Glyph> fadeGlyphs;
    private float nextBeatTime = 0f;
    private final float beatInterval = 60f / 220f;


    @Override
    public void show() {
        batch = new SpriteBatch();

        audioManager = new AudioEffectManager(
                Gdx.files.internal("./audio/song/SECRET BOSS_muffled.mp3"),
                Gdx.files.internal("./audio/song/SECRET BOSS.mp3")
        );

        //particles
        YParticleEffect concentration = new YParticleEffect(true);
        concentration.load(Gdx.files.internal("./particles/downConcentration.p"), Gdx.files.internal("particles"));
        concentration.setPosition(Main.WIDTH / 2f - 670f, -370);
        concentration.scaleEffect(3);
        YParticleEffect concentration2 = new YParticleEffect(true);
        concentration2.load(Gdx.files.internal("./particles/downConcentration.p"), Gdx.files.internal("particles"));
        concentration2.setPosition(Main.WIDTH / 2f + 600f, -370);
        concentration2.scaleEffect(3);

        //Textures
        Glyph bg = new Glyph(Utils.resizeTo(new Texture("./sampleBGs/bg.png"), 120), -192, -108, true);
        bg.setAlpha(0f);

        Glyph glyph = new Glyph(new Texture("./logo/LogoLayout.png"), 0, 0, true);
        glyph.setAlpha(0f);

        Glyph keyboard = new Glyph(new Texture("./keyboard.png"), Main.WIDTH / 2f - 670, 1000, true);
        keyboard.setScaleX(keyboard.getScaleX() * 0.67f);
        keyboard.setScaleY(keyboard.getScaleY() * 0.67f);

        Glyph pc = new Glyph(new Texture("./PC.png"), Main.WIDTH / 2f + 550 + 2000, Main.HEIGHT / 2f - 300, true);
        pc.setAlpha(0f);

        Glyph soundMemo = new Glyph(new Texture("./sound.png"), 20, 200, true);
        Glyph soundCover = new Glyph(new Texture("./soundCover.png"), 20, 200, true);

        Glyph flash = new Glyph(new Texture("./whiteCirc.png"), Main.WIDTH / 2f, Main.HEIGHT / 2f, true);
        flash.setScaleX(flash.getScaleX() * 100f);
        flash.setScaleY(flash.getScaleY() * 100f);
        flash.setAlpha(0f);

        Glyph upFade = new Glyph(new Texture("./upwardsFade.png"), 0, 200, true);
        upFade.setAlpha(0f);
        Glyph downFade = new Glyph(Utils.rotateTextureRightAngles(new Texture("./upwardsFade.png"), 180), 0, 200, true);
        downFade.setAlpha(0f);
        downFade.setY(-200);

        fadeGlyphs = new Array<>(new Glyph[]{upFade, downFade});

        Glyph logo = new Glyph(new Texture("./logo/shortLogo.png"), 323, 289 + 1000, true);
        logo.setScaleX(logo.getScaleX() * 0.15f);
        logo.setScaleY(logo.getScaleY() * 0.15f);

        //Button
        mainButton = new Button(new Texture("./logo/shortLogo.png"), -1000, -1000, () -> {

        });
        mainButton.setScaleX(mainButton.getScaleX() * 0.15f);
        mainButton.setScaleY(mainButton.getScaleY() * 0.15f);
        mainButton.setAlpha(0f);
        mainButton.addOnHoverListener(() -> {
            animationManager.animateRotation(logo, -5f, 0.2f, Easing.EASE_IN_OUT_CIRC); //Do 20 for menu
        });
        mainButton.addOnUnHoverListener(() -> {
            animationManager.animateRotation(logo, 0f, 0.2f, Easing.EASE_IN_OUT_CIRC);
        });

        //sfx
        Sound sliding_heavy = Gdx.audio.newSound(Gdx.files.internal("./audio/heavy-sliding.mp3"));
        Sound door_open_close = Gdx.audio.newSound(Gdx.files.internal("./audio/door-open-close.mp3"));
        Sound sliding_light = Gdx.audio.newSound(Gdx.files.internal("./audio/object-sliding.mp3"));
        Sound monitor_on = Gdx.audio.newSound(Gdx.files.internal("./audio/monitor-on.mp3"));

        /////////
        //Setup//
        /////////
        //PC sliding and phasing in
        animationManager.animateFade(pc, 1f, 3f, Easing.EASE_IN_OUT_QUAD);
        animationManager.animateMove(pc, pc.getX() - 2000, pc.getY(), 2f, Easing.EASE_IN_OUT_CIRC);
        sliding_heavy.play();
        door_open_close.play();

        //Sound visualization (door open)
        eventManager.addEvent(new Event(2f, () -> {
            monitor_on.play();
            cameraManager.shake(0.2f, 3f, 25f);
            animationManager.animateMove(soundCover, soundCover.getX() + 400, soundCover.getY(), 0.25f,Easing.LINEAR);
        }));
        eventManager.addEvent(new Event(2.25f, () -> {
            soundCover.setX(soundCover.getX() - 800);
            animationManager.animateMove(soundCover, soundCover.getX() + 400, soundCover.getY(), 0.4f,Easing.LINEAR);
        }));
        //Keyboard slide down
        eventManager.addEvent(new Event(3f, () -> {
            sliding_light.play();
            animationManager.animateMove(keyboard, keyboard.getX(), keyboard.getY() - 1000, 2f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(logo, logo.getX(), logo.getY() - 1000, 2f,Easing.EASE_IN_OUT_QUAD);
        }));
        //Monitor flickering
        eventManager.addEvent(new Event(5f, () -> {
            animationManager.animateFade(upFade, 1f, 1f,Easing.EASE_IN_EXPO);
        }));
        eventManager.addEvent(new Event(6f, () -> {
            animationManager.animateFade(upFade, 0.5f, 5000f,Easing.EASE_OSCILLATE_INFINITE);
        }));

        //Start focus on logo - move everything away, sound start transition
        eventManager.addEvent(new Event(8f, () -> {
            audioManager.startTransition(5f, 0.005f, 0.5f, 13f, false);
            float factor = 3f;
            animationManager.animateScale(keyboard, keyboard.getScaleX() * factor, keyboard.getScaleY() * factor, 4f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(keyboard, keyboard.getX() + 1000, keyboard.getY() + 180, 4f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateScale(logo, logo.getScaleX() * factor, logo.getScaleY() * factor, 4f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(logo, logo.getX() + 330, logo.getY() - 20, 4f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(keyboard, 0f, 4f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(bg, 0.1f, 4f,Easing.EASE_IN_QUART);
            animationManager.animateMove(pc, pc.getX() + 1000, pc.getY() + 20, 2f,Easing.EASE_IN_OUT_CIRC);
            animationManager.animateMove(soundMemo, soundMemo.getX() - 1000, soundMemo.getY(), 2f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(soundCover, soundMemo.getX() - 1000, soundMemo.getY(), 2f,Easing.EASE_IN_OUT_QUAD);
            animationManager.animateMove(upFade, upFade.getX(), upFade.getY() + 500, 4f,Easing.EASE_IN_OUT_ELASTIC);
        }));

        this.nextBeatTime = 13f;
        eventManager.addEvent(new Event(13f, () -> {
            upFade.setY(upFade.getY() - 500);
            monitor_on.stop();
            shaderManager.setPunch(1.0f);
            concentration.start();
            concentration2.start();
            animationManager.animatePulse(logo, 220, 1.05f); //TODO: REPLACE WITH ACTUAL BASS DETECTION
            animationManager.animateFade(flash, 1f, 0.5f,Easing.EASE_IN_OUT_EXPO);
            parallaxManager.addLayer(bg, 0.02f, 0.1f);
        }));
        eventManager.addEvent(new Event(13.25f, () -> {
            animationManager.animateFade(bg, 1f, 1f,Easing.EASE_IN_QUART);
        }));
        eventManager.addEvent(new Event(13.5f, () -> {
            animationManager.animateFade(flash, 0f, 1.5f,Easing.LINEAR);
        }));
        eventManager.addEvent(new Event(14f, () -> {
            //Button display
            System.out.println("[DEBUG] Positioning button at logo coords: X=" + logo.getX() + ", Y=" + logo.getY());
            mainButton.setX(logo.getX());
            mainButton.setY(logo.getY());
            mainButton.setScaleX(logo.getScaleX());
            mainButton.setScaleY(logo.getScaleY());

            concentration.allowCompletion();
            concentration2.allowCompletion();
            sliding_light.dispose();
            sliding_heavy.dispose();
            door_open_close.dispose();
            monitor_on.dispose();
            //cameraManager.rotate(20f, 9999f,Easing.EASE_OSCILLATE_INFINITE);
        }));
    }

        @Override
        public void render(float delta) {
            // --- Update logic ---
            timePassed += delta;
            eventManager.update(timePassed);
            audioManager.update(delta);
            cameraManager.update(delta);
            parallaxManager.update(delta);

            if (mainButton != null) mainButton.update(delta);

            if (timePassed >= nextBeatTime && nextBeatTime > 0) {
                nextBeatTime += beatInterval;

                Glyph targetGlyph = fadeGlyphs.random();
                animationManager.animateFade(targetGlyph, 0.5f, 0.1f, Easing.EASE_OUT_SINE);
                eventManager.addEvent(new Event(timePassed + 0.1f, () -> {
                    animationManager.animateFade(targetGlyph, 0f, 0.3f, Easing.EASE_IN_SINE);
                }));
            }

            // --- Apply camera effects ---
            cameraManager.applyEffects();

            // --- Render scene into ShaderManager's FBO ---
            shaderManager.begin();

            ScreenUtils.clear(0, 0, 0, 1); // Clear the FBO

            batch.setProjectionMatrix(Main.camera.combined);

            batch.begin();
            animationManager.updateAndRenderGlyphs(delta, batch);
            for (YParticleEffect eff : Main.particles) {
                eff.update(delta);
                eff.draw(batch);
            }
            batch.end();

            // --- End FBO rendering and apply shaders to screen ---
            shaderManager.end(delta);

            // --- Reset camera effects ---
            cameraManager.resetEffects();
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
