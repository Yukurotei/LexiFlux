package it.yuruni;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import it.yuruni.audio.AudioEffectManager;

import it.yuruni.graphics.Easing;
import it.yuruni.graphics.animation.*;

import it.yuruni.graphics.effects.CameraManager;
import it.yuruni.graphics.effects.ParallaxManager;
import it.yuruni.graphics.effects.ShaderManager;
import it.yuruni.graphics.effects.YParticleEffect;
import it.yuruni.graphics.element.Glyph;
import it.yuruni.graphics.element.TextGlyph;
import it.yuruni.tools.debug.GlyphEditor;
import it.yuruni.tools.debug.MouseInspector;
import it.yuruni.game.level.Level;
import it.yuruni.game.level.LevelScanner;
import it.yuruni.ui.Button;
import it.yuruni.ui.LevelCard;
import it.yuruni.ui.ScrollPaneItem;
import it.yuruni.ui.SlantedScrollPane;
import it.yuruni.utils.ElementUtils;


/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen, InputProcessor {

    private SpriteBatch batch;
    private final AnimationManager animationManager = Main.animationManager;
    private final ParallaxManager parallaxManager = Main.parallaxManager;
    private final CameraManager cameraManager = Main.cameraManager;
    private final ShaderManager shaderManager = Main.shaderManager;
    private final EventManager eventManager = Main.eventManager;
    private AudioEffectManager audioManager;

    private ShaderProgram maskShader;
    private com.badlogic.gdx.graphics.glutils.FrameBuffer maskFbo;
    private ShaderProgram alphaMaskShader;

    private final List<Glyph> ownedGlyphs = new ArrayList<>();
    private final List<Texture> ownedTextures = new ArrayList<>();
    private final List<BitmapFont> ownedFonts = new ArrayList<>();
    private final List<YParticleEffect> ownedParticles = new ArrayList<>();

    private Glyph bg;
    private Map<String, Texture> backgroundCache;
    private TextGlyph tutorialText;
    private Button mainButton;
    private Glyph playMenuRect;
    private Glyph playArrow;
    private Glyph logo;
    private Glyph logoLexi;
    private Glyph logoFlux;

    private Glyph levelInfo;
    private Glyph levelDifficulties;

    private Array<Glyph> fadeGlyphs;
    private float nextBeatTime = 0f;
    private final float beatInterval = 60f / 220f;
    private BitmapFont font;

    private boolean isInMainMenu = false, isAnimatingMenu = false, isMenuExtended = false;
    private float playMenuRectOriginY;
    private float playMenuRectExtendedY;
    private float playArrowOriginY;
    private float playArrowExtendedY;

    private GlyphEditor glyphEditor;
    private SlantedScrollPane slantedScrollPane;
    private MouseInspector mouseInspector;

    // Level selection
    private LevelCard selectedCard = null;
    private Level currentLevel = null;
    private Music currentLevelAudio = null;
    private Glyph levelBackgroundGlyph = null;

    // Level info display
    private TextGlyph levelNameText;
    private TextGlyph levelArtistText;
    private TextGlyph levelBpmText;
    private TextGlyph levelDifficultyText;
    private BitmapFont titleFont;
    private BitmapFont infoFont;
    private BitmapFont cardFont;
    private Texture levelCardTexture;
    private Sound slidingHeavy;
    private Sound doorOpenClose;
    private Sound slidingLight;
    private Sound monitorOn;
    private boolean isCleaned = false;


    @Override
    public void show() {
        if (!isCleaned) {
            cleanupResources();
        }
        isCleaned = false;
        ownedGlyphs.clear();
        ownedTextures.clear();
        ownedFonts.clear();
        ownedParticles.clear();

        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(this);

        glyphEditor = new GlyphEditor(Main.camera, Main.glyphs);
        mouseInspector = new MouseInspector(Main.viewport);

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
        trackParticle(concentration);
        trackParticle(concentration2);

        //Textures
        Texture defaultBgTexture = new Texture("./sampleBGs/bg.png");
        bg = new Glyph(defaultBgTexture, -192, -108, true);
        bg.setAlpha(0f);
        trackGlyph(bg);

        Glyph glyph = new Glyph(new Texture("./logo/LogoLayout.png"), 0, 0, true);
        glyph.setAlpha(0f);
        trackGlyph(glyph);

        Glyph keyboard = new Glyph(new Texture("./keyboard.png"), Main.WIDTH / 2f - 670, 1000, true);
        keyboard.setScaleX(keyboard.getScaleX() * 0.67f);
        keyboard.setScaleY(keyboard.getScaleY() * 0.67f);
        trackGlyph(keyboard);

        Glyph pc = new Glyph(new Texture("./PC.png"), Main.WIDTH / 2f + 550 + 2000, Main.HEIGHT / 2f - 300, true);
        pc.setAlpha(0f);
        trackGlyph(pc);

        Glyph soundMemo = new Glyph(new Texture("./sound.png"), 20, 200, true);
        Glyph soundCover = new Glyph(new Texture("./soundCover.png"), 20, 200, true);
        trackGlyph(soundMemo);
        trackGlyph(soundCover);

        Glyph flash = new Glyph(new Texture("./whiteCirc.png"), Main.WIDTH / 2f, Main.HEIGHT / 2f, true);
        flash.setScaleX(flash.getScaleX() * 100f);
        flash.setScaleY(flash.getScaleY() * 100f);
        flash.setAlpha(0f);
        trackGlyph(flash);

        Glyph upFade = new Glyph(new Texture("./upwardsFade.png"), 0, 200, true);
        upFade.setAlpha(0f);
        Glyph downFade = new Glyph(ElementUtils.rotateTextureRightAngles(new Texture("./upwardsFade.png"), 180), 0, 200, true);
        downFade.setAlpha(0f);
        downFade.setY(-200);
        fadeGlyphs = new Array<>(new Glyph[]{upFade, downFade});
        trackGlyph(upFade);
        trackGlyph(downFade);

        //LV selections
        Glyph levelScroll = new Glyph(new Texture("LevelScroll.png"), -600f, 50f, false);
        levelInfo = new Glyph(new Texture("LevelInfo.png"), 300f, 1500f, true);
        levelDifficulties =  new Glyph(new Texture("LevelDifficulties.png"), 520f, -400f, true);
        trackGlyph(levelScroll);
        trackGlyph(levelInfo);
        trackGlyph(levelDifficulties);

        // Setup Slanted Scroll Pane
        slantedScrollPane = new SlantedScrollPane(levelScroll, new Vector2(108.7f, -803.2f), Main.camera);
        levelCardTexture = new Texture("LevelCard.png");
        trackTexture(levelCardTexture);

        // Scan for level files and create cards
        Array<LevelCard> levelCards = LevelScanner.scanLevels(levelCardTexture, this::selectCard);
        for (LevelCard card : levelCards) {
            slantedScrollPane.addItem(card);
        }

        // --- Pre-cache all level backgrounds ---
        backgroundCache = new HashMap<>();
        List<String> bgPaths = new ArrayList<>();
        for (LevelCard card : levelCards) {
            try {
                Level level = new Level(card.getLevelPath());
                String bgPath = level.getBackgroundImage();
                if (bgPath != null && !bgPath.isEmpty()) {
                    String fullPath = "sampleBGs/" + bgPath;
                    if (Gdx.files.internal(fullPath).exists() && !backgroundCache.containsKey(bgPath)) {
                        // Perform resizing using Pixmaps to avoid consumePixmap issues
                        Pixmap rawPixmap = new Pixmap(Gdx.files.internal(fullPath));
                        Pixmap pixmap1080 = new Pixmap(1920, 1080, rawPixmap.getFormat());
                        pixmap1080.setFilter(Pixmap.Filter.NearestNeighbour);
                        pixmap1080.drawPixmap(rawPixmap, 0, 0, rawPixmap.getWidth(), rawPixmap.getHeight(), 0, 0, 1920, 1080);
                        rawPixmap.dispose();

                        int finalWidth = (int)(1920 * 1.2);
                        int finalHeight = (int)(1080 * 1.2);
                        Pixmap finalPixmap = new Pixmap(finalWidth, finalHeight, pixmap1080.getFormat());
                        finalPixmap.setFilter(Pixmap.Filter.NearestNeighbour);
                        finalPixmap.drawPixmap(pixmap1080, 0, 0, pixmap1080.getWidth(), pixmap1080.getHeight(), 0, 0, finalWidth, finalHeight);
                        pixmap1080.dispose();

                        Texture finalTexture = new Texture(finalPixmap);
                        finalPixmap.dispose();

                        backgroundCache.put(bgPath, finalTexture);
                        bgPaths.add(bgPath);
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("Startup", "Could not read or process level background for: " + card.getLevelPath(), e);
            }
        }

        // If we found any backgrounds, pick one at random from the cache for the startup screen.
        if (!bgPaths.isEmpty()) {
            String randomBgKey = bgPaths.get(new Random().nextInt(bgPaths.size()));
            bg.setTexture(backgroundCache.get(randomBgKey));
            defaultBgTexture.dispose();
        }


        playMenuRect = new Glyph(new Texture("./ui/menuRect.png"), -1000, -1000, true);
        playArrow = new Glyph(new Texture("./ui/playButton.png"), -1000, -1000, true);
        trackGlyph(playMenuRect);
        trackGlyph(playArrow);

        logo = new Glyph(new Texture("./logo/shortLogo.png"), 323, 289 + 1000, true);
        logo.setScaleX(logo.getScaleX() * 0.15f);
        logo.setScaleY(logo.getScaleY() * 0.15f);
        trackGlyph(logo);

        logoLexi = new Glyph(new Texture("./logo/Lexi.png"), 200 - 1000, 920, true);
        logoLexi.setScaleX(logoLexi.getScaleX() * 0.5f);
        logoLexi.setScaleY(logoLexi.getScaleY() * 0.5f);
        logoFlux = new Glyph(new Texture("./logo/Flux.png"), 50 - 1000, 700, true);
        logoFlux.setScaleX(logoFlux.getScaleX() * 0.4f);
        logoFlux.setScaleY(logoLexi.getScaleY() * 0.4f);
        trackGlyph(logoLexi);
        trackGlyph(logoFlux);

        //Some text
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/josefin-sans-latin-400-normal.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        trackFont(font);
        tutorialText = new TextGlyph("Arrow keys to navigate, space to select", font, Main.WIDTH / 2f - 200, Main.HEIGHT / 2f - 400 - 500, true);
        trackGlyph(tutorialText);

        // Initialize level info text glyphs
        parameter.size = 32;
        titleFont = generator.generateFont(parameter);
        parameter.size = 24;
        infoFont = generator.generateFont(parameter);
        trackFont(titleFont);
        trackFont(infoFont);

        // Create card text font and set on each card
        parameter.size = 18;
        cardFont = generator.generateFont(parameter);
        trackFont(cardFont);
        for (LevelCard card : levelCards) {
            card.setFont(cardFont);
        }

        // Position text relative to levelInfo panel (starts off-screen with levelInfo)
        // levelInfo starts at (300f, 1500f) and moves to (406.5f, 345f)
        // Text offsets relative to levelInfo position
        float textOffsetX = 50f;
        float textStartY = 250f;
        float textSpacing = 50f;

        levelNameText = new TextGlyph("", titleFont, levelInfo.getX() + textOffsetX, levelInfo.getY() + textStartY, false);
        levelArtistText = new TextGlyph("", infoFont, levelInfo.getX() + textOffsetX, levelInfo.getY() + textStartY - textSpacing, false);
        levelBpmText = new TextGlyph("", infoFont, levelInfo.getX() + textOffsetX, levelInfo.getY() + textStartY - textSpacing * 2, false);
        levelDifficultyText = new TextGlyph("", infoFont, levelInfo.getX() + textOffsetX, levelInfo.getY() + textStartY - textSpacing * 3, false);

        // Initially hide the text
        levelNameText.setAlpha(0f);
        levelArtistText.setAlpha(0f);
        levelBpmText.setAlpha(0f);
        levelDifficultyText.setAlpha(0f);

        alphaMaskShader = new ShaderProgram(Gdx.files.internal("shaders/alphamask.vert"), Gdx.files.internal("shaders/alphamask.frag"));
        if (!alphaMaskShader.isCompiled()) {
            Gdx.app.error("Shader", "Alpha mask shader compilation failed: " + alphaMaskShader.getLog());
        }

        generator.dispose();

        //Button
        mainButton = new Button(new Texture("./logo/shortLogo.png"), -1000, -1000, () -> {

        });
        mainButton.setScaleX(mainButton.getScaleX() * 0.15f);
        mainButton.setScaleY(mainButton.getScaleY() * 0.15f);
        mainButton.setAlpha(0f);
        trackGlyph(mainButton);
        mainButton.addOnHoverListener(() -> {
            animationManager.animateRotation(logo, -3f, 0.2f, Easing.EASE_IN_OUT_CIRC); //Do 20 for menu
        });
        mainButton.addOnUnHoverListener(() -> {
            animationManager.animateRotation(logo, 0f, 0.2f, Easing.EASE_IN_OUT_CIRC);
        });

        //sfx
        slidingHeavy = Gdx.audio.newSound(Gdx.files.internal("./audio/heavy-sliding.mp3"));
        doorOpenClose = Gdx.audio.newSound(Gdx.files.internal("./audio/door-open-close.mp3"));
        slidingLight = Gdx.audio.newSound(Gdx.files.internal("./audio/object-sliding.mp3"));
        monitorOn = Gdx.audio.newSound(Gdx.files.internal("./audio/monitor-on.mp3"));

        /////////
        //Setup//
        /////////
        //PC sliding and phasing in
        animationManager.animateFade(pc, 1f, 3f, Easing.EASE_IN_OUT_QUAD);
        animationManager.animateMove(pc, pc.getX() - 2000, pc.getY(), 2f, Easing.EASE_IN_OUT_CIRC);
        slidingHeavy.play();
        doorOpenClose.play();

        //Sound visualization (door open)
        eventManager.addEvent(new Event(2.4f, () -> { // Timing changed from 2f
            monitorOn.play();
            cameraManager.shake(0.2f, 3f, 0.0025f);
            animationManager.animateMove(soundCover, soundCover.getX() + 400, soundCover.getY(), 0.25f,Easing.LINEAR);
        }));
        eventManager.addEvent(new Event(2.65f, () -> { // Timing changed from 2.25f
            soundCover.setX(soundCover.getX() - 800);
            animationManager.animateMove(soundCover, soundCover.getX() + 400, soundCover.getY(), 0.4f,Easing.LINEAR);
        }));
        //Keyboard slide down
        eventManager.addEvent(new Event(3f, () -> {
            slidingLight.play();
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
            audioManager.startTransition(5f, 0.005f, 0.3f, 13f, false);
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
            monitorOn.stop();
            shaderManager.setPunch(1.0f);
            concentration.start();
            concentration2.start();
            //animationManager.animatePulse(logo, 220, 1.05f); //TODO: REPLACE WITH ACTUAL BASS DETECTION
            //animationManager.animatePulse(logoLexi, 220, 1.05f);
            //animationManager.animatePulse(logoFlux, 220, 1.05f);
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
            mainButton.setX(logo.getX());
            mainButton.setY(logo.getY());
            mainButton.setScaleX(logo.getScaleX());
            mainButton.setScaleY(logo.getScaleY());
            playMenuRect.setScaleX(logo.getScaleX());
            playMenuRect.setScaleY(logo.getScaleY());
            playMenuRect.setX(logo.getX() + 5f);
            playMenuRect.setY(logo.getY() + 50);
            playArrow.setX(logo.getX() + 205);
            playArrow.setY(logo.getY() + 150);
            playArrow.setScaleX(playArrow.getScaleX() * 0.6f);
            playArrow.setScaleY(playArrow.getScaleY() * 0.6f);

            concentration.allowCompletion();
            concentration2.allowCompletion();
            if (slidingLight != null) {
                slidingLight.dispose();
                slidingLight = null;
            }
            if (slidingHeavy != null) {
                slidingHeavy.dispose();
                slidingHeavy = null;
            }
            if (doorOpenClose != null) {
                doorOpenClose.dispose();
                doorOpenClose = null;
            }
            if (monitorOn != null) {
                monitorOn.dispose();
                monitorOn = null;
            }

            animationManager.animateMove(tutorialText, tutorialText.getX(), tutorialText.getY() + 500, 2f, Easing.EASE_IN_OUT_EXPO);

            playMenuRectOriginY = playMenuRect.getY();
            playMenuRectExtendedY = playMenuRect.getY() + 200;
            playArrowOriginY = playArrow.getY() + 20;
            playArrowExtendedY = playArrow.getY() + 220;
            isInMainMenu = true;
        }));
    }

        @Override
        public void render(float delta) {
            // --- Update logic ---
            audioManager.update(delta);
            cameraManager.update(delta);
            parallaxManager.update(delta);
            if (slantedScrollPane != null) {
                slantedScrollPane.update(delta);
            }

            if (mainButton != null) mainButton.update(delta);

            if (Main.timePassed >= nextBeatTime && nextBeatTime > 0) {
                nextBeatTime += beatInterval;
                Glyph targetGlyph = fadeGlyphs.random();
                animationManager.animateFade(targetGlyph, 0.5f, 0.1f, Easing.EASE_OUT_SINE);
                eventManager.addEvent(new Event(Main.timePassed + 0.1f, () -> {
                    animationManager.animateFade(targetGlyph, 0f, 0.3f, Easing.EASE_IN_SINE);
                }));
            }

            // --- menu switching stuff ---
            if (isInMainMenu) {
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                    if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && playMenuRect.getAlpha() == 0.99f) {
                        animationManager.stopPulsing(logo);
                        animationManager.stopPulsing(logoLexi);
                        animationManager.stopPulsing(logoFlux);
                        playMenuRect.setAlpha(0.98f);
                        animationManager.animateMove(tutorialText, tutorialText.getX(), tutorialText.getY() - 500, 1f, Easing.EASE_IN_OUT_EXPO);
                        animationManager.animateMove(playMenuRect, playMenuRect.getX(), playMenuRectOriginY, 0.3f, Easing.EASE_IN_OUT_QUINT);
                        animationManager.animateMove(playArrow, playArrow.getX(), playArrowOriginY, 0.5f, Easing.EASE_IN_OUT_QUINT);
                        animationManager.animateRotation(playArrow, -5f, 0.5f, Easing.EASE_IN_OUT_BACK);
                        animationManager.animateScale(logo, 0.3f, 0.3f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                        animationManager.animateScale(logoLexi, 0.35f, 0.35f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                        animationManager.animateScale(logoFlux, 0.25f, 0.25f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                        eventManager.addEvent(new Event(Main.timePassed + 0.4f, () -> {
                            playMenuRect.setVisible(false);
                            animationManager.animateMove(playArrow, playArrow.getX() - 1500, playArrow.getY(), 1f, Easing.EASE_IN_OUT_QUINT);
                            eventManager.addEvent(new Event(Main.timePassed + 0.2f, () -> {
                                playMenuRect.setAlpha(0f);
                                playMenuRect.setVisible(true);
                                animationManager.animateMove(logo, -192.5f, 715, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(logoLexi, 41, 940, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(logoFlux, -121, 750, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                //LV menus
                                animationManager.animateMove(slantedScrollPane.getBackground(), 20f, 50f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(levelInfo, 406.5f, 345f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(levelDifficulties, 482, 50f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                // Move text with levelInfo panel
                                animationManager.animateMove(levelNameText, 456.5f, 595f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(levelArtistText, 456.5f, 545f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(levelBpmText, 456.5f, 495f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                animationManager.animateMove(levelDifficultyText, 456.5f, 445f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                                mainButton.setY(670);
                                mainButton.setX(-140);
                                eventManager.addEvent(new Event(Main.timePassed + 1f, () -> {
                                    isInMainMenu = false;
                                }));
                            }));
                        }));
                        ElementUtils.putAfter(Main.glyphs, playArrow, logo);
                    }
                    if (playMenuRect.getAlpha() == 1f) {
                        animationManager.stopAllAnimations(playArrow);
                        animationManager.stopAllAnimations(playMenuRect);
                        animationManager.animateMove(playMenuRect, playMenuRect.getX(), playMenuRectExtendedY, 0.3f, Easing.EASE_IN_OUT_QUINT);
                        animationManager.animateMove(playArrow, playArrow.getX(), playArrowExtendedY, 0.4f, Easing.EASE_IN_OUT_QUINT);
                        animationManager.animateRotation(playArrow, 360, 0.4f, Easing.EASE_IN_OUT_EXPO);
                        playMenuRect.setAlpha(0.98f);
                        eventManager.addEvent(new Event(Main.timePassed + 0.4f, () -> {
                            playMenuRect.setAlpha(0.99f);
                        }));
                    }
                } else {
                    if (playMenuRect.getAlpha() == 0.99f) {
                        animationManager.stopAllAnimations(playArrow);
                        animationManager.stopAllAnimations(playMenuRect);
                        animationManager.animateMove(playMenuRect, playMenuRect.getX(), playMenuRectOriginY, 0.3f, Easing.EASE_IN_OUT_QUINT);
                        animationManager.animateMove(playArrow, playArrow.getX(), playArrowOriginY, 0.4f, Easing.EASE_IN_OUT_QUINT);
                        animationManager.animateRotation(playArrow, -360, 0.4f, Easing.EASE_IN_OUT_CIRC);
                        playMenuRect.setAlpha(0.98f);
                        eventManager.addEvent(new Event(Main.timePassed + 0.4f, () -> {
                            playMenuRect.setAlpha(1f);
                        }));
                    }
                }
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
                    isInMainMenu = true;
                    animationManager.animateMove(tutorialText, tutorialText.getX(), tutorialText.getY() + 500, 1f, Easing.EASE_IN_OUT_EXPO);
                    animationManager.animateRotation(playArrow, 0, 0.5f, Easing.EASE_IN_OUT_BACK);
                    animationManager.animateMove(playArrow, playArrow.getX(), playArrowOriginY, 0.5f, Easing.EASE_IN_OUT_QUINT);
                    animationManager.animateMove(logo, playMenuRect.getX() - 5f, playMenuRect.getY() - 50, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateScale(logo, 0.45f, 0.45f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateScale(logoLexi, 0.5f, 0.5f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateScale(logoFlux, 0.4f, 0.4f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(logoLexi, logoLexi.getX() - 850, logoLexi.getY(), 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(logoFlux, logoFlux.getX() - 850, logoFlux.getY(), 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(slantedScrollPane.getBackground(), -600f, 50f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(levelInfo, 300f, 1500f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(levelDifficulties, 520f, -400f, 1f, Easing.EASE_IN_OUT_ELASTIC);

                    // Move text back off-screen with levelInfo panel
                    animationManager.animateMove(levelNameText, 350f, 1750f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(levelArtistText, 350f, 1700f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(levelBpmText, 350f, 1650f, 1f, Easing.EASE_IN_OUT_ELASTIC);
                    animationManager.animateMove(levelDifficultyText, 350f, 1600f, 1f, Easing.EASE_IN_OUT_ELASTIC);

                    // Fade out level info text and background
                    animationManager.animateFade(levelNameText, 0f, 0.3f, Easing.EASE_IN_OUT_QUAD);
                    animationManager.animateFade(levelArtistText, 0f, 0.3f, Easing.EASE_IN_OUT_QUAD);
                    animationManager.animateFade(levelBpmText, 0f, 0.3f, Easing.EASE_IN_OUT_QUAD);
                    animationManager.animateFade(levelDifficultyText, 0f, 0.3f, Easing.EASE_IN_OUT_QUAD);
                    if (levelBackgroundGlyph != null) {
                        animationManager.animateFade(levelBackgroundGlyph, 0f, 0.3f, Easing.EASE_IN_OUT_QUAD);
                    }

                    mainButton.setY(playMenuRect.getY() - 5f);
                    mainButton.setX(playMenuRect.getX() - 50);
                    eventManager.addEvent(new Event(Main.timePassed + 0.4f, () -> {
                        animationManager.animateMove(playArrow, playArrow.getX() + 1500, playArrow.getY(), 1f, Easing.EASE_IN_OUT_QUINT);
                        eventManager.addEvent(new Event(Main.timePassed + 0.2f, () -> {
                            eventManager.addEvent(new Event(Main.timePassed + 1f, () -> {
                                playMenuRect.setAlpha(1f);
                            }));
                        }));
                    }));
                    ElementUtils.putBefore(Main.glyphs, playArrow, logo);
                }
            }

            // Synchronize level background with the info panel's transform each frame
            if (levelBackgroundGlyph != null && levelInfo != null) {
                // Since the background texture is now cropped to match the info panel,
                // we can just copy the panel's transform directly.
                levelBackgroundGlyph.setX(levelInfo.getX());
                levelBackgroundGlyph.setY(levelInfo.getY());
                levelBackgroundGlyph.setScaleX(levelInfo.getScaleX());
                levelBackgroundGlyph.setScaleY(levelInfo.getScaleY());
                levelBackgroundGlyph.setRotation(levelInfo.getRotation());
            }

            // --- Apply camera effects ---
            cameraManager.applyEffects();

            // --- Render scene into ShaderManager's FBO ---
            shaderManager.begin();

            ScreenUtils.clear(0, 0, 0, 1); // Clear the FBO

            batch.setProjectionMatrix(Main.camera.combined);


            batch.begin();

            animationManager.updateAndRenderGlyphs(delta, batch);

            // Render background with alpha masking using levelInfo as the mask
            if (levelBackgroundGlyph != null && levelBackgroundGlyph.isVisible() && levelInfo != null && levelInfo.isVisible()) {
                levelBackgroundGlyph.update(delta);

                batch.end(); // End current batch

                // Start batch with alpha mask shader
                batch.setShader(alphaMaskShader);
                batch.begin();

                // Bind the mask texture (levelInfo) to texture unit 1
                levelInfo.getTexture().bind(1);

                // Bind the background texture to texture unit 0 (default)
                levelBackgroundGlyph.getTexture().bind(0);

                // Set shader uniforms
                alphaMaskShader.setUniformi("u_texture", 0); // Background texture
                alphaMaskShader.setUniformi("u_mask", 1);    // Mask texture (levelInfo)

                // Calculate screen positions and scales
                float bgCenterX = levelBackgroundGlyph.getX();
                float bgCenterY = levelBackgroundGlyph.getY();
                float bgWidth = levelBackgroundGlyph.getWidth() * levelBackgroundGlyph.getScaleX();
                float bgHeight = levelBackgroundGlyph.getHeight() * levelBackgroundGlyph.getScaleY();

                float maskCenterX = levelInfo.getX();
                float maskCenterY = levelInfo.getY();
                float maskWidth = levelInfo.getWidth() * levelInfo.getScaleX();
                float maskHeight = levelInfo.getHeight() * levelInfo.getScaleY();

                // Set offset and scale uniforms for coordinate transformation
                // Convert from screen space to texture space
                alphaMaskShader.setUniformf("u_bgOffset", bgCenterX - bgWidth / 2f, bgCenterY - bgHeight / 2f);
                alphaMaskShader.setUniformf("u_bgScale", bgWidth, bgHeight);
                alphaMaskShader.setUniformf("u_maskOffset", maskCenterX - maskWidth / 2f, maskCenterY - maskHeight / 2f);
                alphaMaskShader.setUniformf("u_maskScale", maskWidth, maskHeight);

                // Render the background with the shader
                levelBackgroundGlyph.render(batch);

                batch.end();

                // Reset shader and restart batch
                batch.setShader(null);
                batch.begin();
            }

            // Render levelInfo frame on top
            if (levelInfo != null && levelInfo.isVisible()) {
                levelInfo.render(batch);
            }

            if (levelNameText != null) levelNameText.render(batch);
            if (levelArtistText != null) levelArtistText.render(batch);
            if (levelBpmText != null) levelBpmText.render(batch);
            if (levelDifficultyText != null) levelDifficultyText.render(batch);

            for (YParticleEffect eff : Main.particles) {
                eff.update(delta);
                eff.draw(batch);
            }
            slantedScrollPane.render(Main.camera, batch);

            // Render the glyph editor UI
            if (glyphEditor != null) {
                glyphEditor.render(batch, font);
            }
            if (mouseInspector != null) {
                mouseInspector.render(batch, font);
            }

            // Render debug text for the scroll pane
            if (mouseInspector != null) {
                if (mouseInspector.isEnabled()) {
                    if (slantedScrollPane != null) {
                        Glyph firstCard = slantedScrollPane.getFirstItem();
                        if (firstCard != null) {
                            font.setColor(Color.RED);
                            String coords = String.format("Card1: %.1f, %.1f", firstCard.getX(), firstCard.getY());
                            font.draw(batch, coords, firstCard.getX(), firstCard.getY() - 20);
                            font.setColor(Color.WHITE);
                        }
                    }
                }
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
        cleanupResources();
    }

    @Override
    public void dispose() {
        cleanupResources();
    }

    // --- Level Selection ---

    /**
     * Selects the given level card, deselecting the previous one if any.
     */
    private void selectCard(LevelCard card) {
        if (card == selectedCard) return;

        // Deselect previous card
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }

        // Select new card
        selectedCard = card;
        if (selectedCard != null) {
            selectedCard.setSelected(true);
            Gdx.app.log("LevelSelect", "Selected: " + selectedCard.getLevelName() +
                    " by " + selectedCard.getArtist() +
                    " (" + selectedCard.getBpm() + " BPM, Difficulty " + selectedCard.getDifficulty() + ")");

            // Load and display level info
            loadLevelInfo(selectedCard);
        }
    }

    /**
     * Loads the full level data, audio, and background for the selected card.
     */
    private void loadLevelInfo(LevelCard card) {
        // Dispose previous resources
        if (currentLevelAudio != null) {
            currentLevelAudio.dispose();
            currentLevelAudio = null;
        }
        if (levelBackgroundGlyph != null) {
            levelBackgroundGlyph.dispose();
            levelBackgroundGlyph = null;
        }

        try {
            // Load the full level object
            currentLevel = new Level(card.getLevelPath());

            // Load audio file (but don't play it)
            String audioPath = currentLevel.getAudioFile();
            if (audioPath != null && !audioPath.isEmpty()) {
                String fullAudioPath = "audio/song/" + audioPath;
                if (!Gdx.files.internal(fullAudioPath).exists()) {
                    Gdx.app.error("LevelSelect", "Audio file does not exist: " + fullAudioPath);
                } else {
                    try {
                        currentLevelAudio = Gdx.audio.newMusic(Gdx.files.internal(fullAudioPath));
                        Gdx.app.log("LevelSelect", "Loaded audio: " + audioPath);
                    } catch (Exception e) {
                        Gdx.app.error("LevelSelect", "Failed to load audio: " + audioPath, e);
                    }
                }
            }

            // Load background image and create glyph
            String bgPath = currentLevel.getBackgroundImage();
            if (bgPath != null && !bgPath.isEmpty()) {
                String fullPath = "sampleBGs/" + bgPath;
                if (!Gdx.files.internal(fullPath).exists()) {
                    Gdx.app.error("LevelSelect", "Background file does not exist: " + fullPath);
                } else {
                    try {
                        Texture rawBgTexture = new Texture(Gdx.files.internal(fullPath));
                        // Crop the background to match the levelInfo panel's dimensions and aspect ratio.
                        Texture bgTexture = ElementUtils.cropToMatch(rawBgTexture, levelInfo.getTexture());
                        rawBgTexture.dispose(); // We no longer need the raw, uncropped texture.

                        levelBackgroundGlyph = new Glyph(bgTexture, 0, 0, false);
                        levelBackgroundGlyph.setAlpha(0f);
                        Gdx.app.log("LevelSelect", "Loaded and processed background: " + bgPath);

                        // Fade in the background
                        animationManager.animateFade(levelBackgroundGlyph, 1f, 0.3f, Easing.EASE_IN_OUT_QUAD);
                    } catch (Exception e) {
                        Gdx.app.error("LevelSelect", "Failed to load background: " + bgPath, e);
                    }
                }
            }

            // Update text displays
            levelNameText.setText(currentLevel.getName());
            levelArtistText.setText("by " + currentLevel.getArtist());
            levelBpmText.setText("BPM: " + (int)currentLevel.getBpm());
            levelDifficultyText.setText("Difficulty: " + currentLevel.getDifficulty());

            // Fade in the text
            animationManager.animateFade(levelNameText, 1f, 0.3f, Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(levelArtistText, 1f, 0.3f, Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(levelBpmText, 1f, 0.3f, Easing.EASE_IN_OUT_QUAD);
            animationManager.animateFade(levelDifficultyText, 1f, 0.3f, Easing.EASE_IN_OUT_QUAD);

            // Update the main background by retrieving the pre-cached texture
            String newBgPath = currentLevel.getBackgroundImage();
            if (newBgPath != null && backgroundCache.containsKey(newBgPath)) {
                bg.setTexture(backgroundCache.get(newBgPath));
            }

        } catch (Exception e) {
            Gdx.app.error("LevelSelect", "Failed to load level: " + card.getLevelPath(), e);
        }
    }

    private void cleanupResources() {
        if (isCleaned) {
            return;
        }
        isCleaned = true;

        if (audioManager != null) {
            audioManager.dispose();
            audioManager = null;
        }
        if (currentLevelAudio != null) {
            currentLevelAudio.dispose();
            currentLevelAudio = null;
        }
        if (levelBackgroundGlyph != null) {
            levelBackgroundGlyph.dispose();
            levelBackgroundGlyph = null;
        }

        if (slidingHeavy != null) {
            slidingHeavy.dispose();
            slidingHeavy = null;
        }
        if (doorOpenClose != null) {
            doorOpenClose.dispose();
            doorOpenClose = null;
        }
        if (slidingLight != null) {
            slidingLight.dispose();
            slidingLight = null;
        }
        if (monitorOn != null) {
            monitorOn.dispose();
            monitorOn = null;
        }

        if (parallaxManager != null) {
            parallaxManager.clear();
        }
        if (eventManager != null) {
            eventManager.clear();
        }
        if (animationManager != null) {
            animationManager.clear();
        }

        if (bg != null && backgroundCache != null && !backgroundCache.isEmpty()) {
            Texture bgTexture = bg.getTexture();
            if (bgTexture != null) {
                for (Map.Entry<String, Texture> entry : backgroundCache.entrySet()) {
                    if (entry.getValue() == bgTexture) {
                        bg.setTexture(null);
                        break;
                    }
                }
            }
        }

        for (Glyph glyph : ownedGlyphs) {
            if (glyph != null) {
                glyph.dispose();
            }
        }
        ownedGlyphs.clear();
        Main.glyphs.clear();

        for (YParticleEffect effect : ownedParticles) {
            if (effect != null) {
                Main.particles.remove(effect);
                if (effect instanceof com.badlogic.gdx.utils.Disposable) {
                    ((com.badlogic.gdx.utils.Disposable) effect).dispose();
                }
            }
        }
        ownedParticles.clear();
        Main.particles.clear();

        for (BitmapFont fontToDispose : ownedFonts) {
            if (fontToDispose != null) {
                fontToDispose.dispose();
            }
        }
        ownedFonts.clear();
        font = null;
        titleFont = null;
        infoFont = null;
        cardFont = null;

        if (backgroundCache != null) {
            for (Texture texture : backgroundCache.values()) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            backgroundCache.clear();
        }

        for (Texture texture : ownedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        ownedTextures.clear();

        if (alphaMaskShader != null) {
            alphaMaskShader.dispose();
            alphaMaskShader = null;
        }
        if (maskShader != null) {
            maskShader.dispose();
            maskShader = null;
        }
        if (maskFbo != null) {
            maskFbo.dispose();
            maskFbo = null;
        }
        if (slantedScrollPane != null) {
            slantedScrollPane.clearItems();
            slantedScrollPane = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }

    private void trackGlyph(Glyph glyph) {
        if (glyph != null) {
            ownedGlyphs.add(glyph);
        }
    }

    private void trackTexture(Texture texture) {
        if (texture != null) {
            ownedTextures.add(texture);
        }
    }

    private void trackFont(BitmapFont fontToTrack) {
        if (fontToTrack != null) {
            ownedFonts.add(fontToTrack);
        }
    }

    private void trackParticle(YParticleEffect effect) {
        if (effect != null) {
            ownedParticles.add(effect);
        }
    }

    // --- InputProcessor Methods ---

    @Override
    public boolean keyDown(int keycode) {
        if (glyphEditor != null && glyphEditor.keyDown(keycode)) {
            return true;
        }
        if (mouseInspector != null && mouseInspector.keyDown(keycode)) {
            return true;
        }

        // Level selection navigation (when not in main menu)
        if (!isInMainMenu && slantedScrollPane != null) {
            int currentIndex = selectedCard != null
                    ? slantedScrollPane.getItemIndex(selectedCard)
                    : -1;

            // Launch gameplay when Enter/Space is pressed
            if ((keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) && selectedCard != null && currentLevel != null) {
                Gdx.app.log("LevelSelect", "Launching level: " + currentLevel.getName());
                // Create and switch to gameplay screen with the loaded level and audio
                GameplayScreen gameplayScreen = new GameplayScreen(currentLevel, currentLevelAudio);
                ((Main) Gdx.app.getApplicationListener()).setScreen(gameplayScreen);

                // Stop main menu music
                if (audioManager != null) {
                    audioManager.stop();
                }

                // Transfer ownership of music to GameplayScreen - don't dispose it here
                currentLevelAudio = null;
                return true;
            }

            if (keycode == Input.Keys.DOWN || keycode == Input.Keys.RIGHT) {
                // Navigate to next card (or first if nothing selected)
                int nextIndex = currentIndex + 1;
                if (nextIndex < slantedScrollPane.getItemCount()) {
                    ScrollPaneItem nextItem = slantedScrollPane.getItem(nextIndex);
                    if (nextItem instanceof LevelCard) {
                        selectCard((LevelCard) nextItem);
                        slantedScrollPane.scrollToItem(nextIndex);
                    }
                } else if (currentIndex == -1 && slantedScrollPane.getItemCount() > 0) {
                    // Nothing selected, select first
                    ScrollPaneItem firstItem = slantedScrollPane.getFirstItem();
                    if (firstItem instanceof LevelCard) {
                        selectCard((LevelCard) firstItem);
                        slantedScrollPane.scrollToItem(0);
                    }
                }
                return true;
            } else if (keycode == Input.Keys.UP || keycode == Input.Keys.LEFT) {
                // Navigate to previous card
                int prevIndex = currentIndex - 1;
                if (prevIndex >= 0) {
                    ScrollPaneItem prevItem = slantedScrollPane.getItem(prevIndex);
                    if (prevItem instanceof LevelCard) {
                        selectCard((LevelCard) prevItem);
                        slantedScrollPane.scrollToItem(prevIndex);
                    }
                } else if (currentIndex == -1 && slantedScrollPane.getItemCount() > 0) {
                    // Nothing selected, select first
                    ScrollPaneItem firstItem = slantedScrollPane.getFirstItem();
                    if (firstItem instanceof LevelCard) {
                        selectCard((LevelCard) firstItem);
                        slantedScrollPane.scrollToItem(0);
                    }
                }
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
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (glyphEditor != null && glyphEditor.touchDown(screenX, screenY, pointer, button)) {
            return true;
        }
        if (slantedScrollPane != null && slantedScrollPane.touchDown(screenX, screenY, pointer, button)) {
            return true;
        }
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
        if (glyphEditor != null && glyphEditor.touchDragged(screenX, screenY, pointer)) {
            return true;
        }
        // other touch dragged logic for the screen can go here
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (slantedScrollPane != null) {
            Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            Main.viewport.unproject(touchPos);
            boolean isContained = slantedScrollPane.getBoundingRectangle().contains(touchPos.x, touchPos.y);
            Gdx.app.log("FirstScreen#scrolled", "Mouse: " + touchPos + " | Bounds: " + slantedScrollPane.getBoundingRectangle() + " | Contained: " + isContained);
            if (isContained) {
                return slantedScrollPane.scrolled(amountY);
            }
        }
        return false;
    }
}
