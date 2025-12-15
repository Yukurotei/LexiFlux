package it.yuruni;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import it.yuruni.graphics.animation.AnimationManager;
import it.yuruni.graphics.animation.Glyph;

import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public static final List<Glyph> glyphs = new ArrayList<>();
    public static AnimationManager animationManager;

    @Override
    public void create() {
        animationManager = new AnimationManager();
        setScreen(new FirstScreen());
    }

    @Override
    public void render() {
        animationManager.update(Gdx.graphics.getDeltaTime());
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        while (!glyphs.isEmpty()) {
            glyphs.get(0).dispose();
        }
    }
}