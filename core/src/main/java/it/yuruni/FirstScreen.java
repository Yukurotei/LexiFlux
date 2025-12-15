package it.yuruni;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import it.yuruni.graphics.animation.Glyph;


/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private SpriteBatch batch;

    @Override
    public void show() {
        // Prepare your screen here.
        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        ScreenUtils.clear(0, 0, 0, 1);

        batch.begin();
        Main.animationManager.updateAndRenderGlyphs(delta, batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
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
    }
}