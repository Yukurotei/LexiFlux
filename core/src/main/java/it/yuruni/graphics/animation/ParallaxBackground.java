package it.yuruni.graphics.animation;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A parallax scrolling background. The texture should have its wrap mode set to Repeat.
 * It supports both camera-based parallax and automatic scrolling.
 */
public class ParallaxBackground {

    private final Texture texture;
    private final Color tint;

    private float scrollX = 0;
    private float scrollY = 0;
    private float speedX = 0;
    private float speedY = 0;

    /**
     * Creates a new ParallaxBackground.
     * @param texture The texture to use for the background.
     */
    public ParallaxBackground(Texture texture, Color tint) {
        this.texture = texture;
        this.tint = tint;
        this.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    /**
     * Sets the automatic scrolling speed.
     * @param speedX The horizontal speed in world units per second.
     * @param speedY The vertical speed in world units per second.
     */
    public void setSpeed(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    /**
     * Updates the automatic scroll position. Should be called every frame.
     * @param delta The time in seconds since the last frame.
     */
    public void update(float delta) {
        this.scrollX += speedX * delta;
        this.scrollY += speedY * delta;
    }

    /**
     * Renders the parallax background. The SpriteBatch should be started before calling this method.
     * It should be drawn before other world objects.
     * @param camera The world camera. Must be an OrthographicCamera.
     * @param batch The SpriteBatch to draw on.
     */
    public void render(Camera camera, SpriteBatch batch) {
        Color oldColor = new Color(batch.getColor());
        batch.setColor(tint);

        OrthographicCamera orthoCam = (OrthographicCamera) camera;

        float viewWidth = orthoCam.viewportWidth * orthoCam.zoom;
        float viewHeight = orthoCam.viewportHeight * orthoCam.zoom;

        float x = orthoCam.position.x - viewWidth / 2;
        float y = orthoCam.position.y - viewHeight / 2;

        float totalScrollX = (orthoCam.position.x) + this.scrollX;
        float totalScrollY = (orthoCam.position.y) + this.scrollY;

        // Calculate texture coordinates
        float u = totalScrollX / texture.getWidth();
        float v = totalScrollY / texture.getHeight();
        float u2 = u + viewWidth / texture.getWidth();
        float v2 = v + viewHeight / texture.getHeight();

        batch.draw(texture, x, y, viewWidth, viewHeight, u, v, u2, v2);

        // Restore previous color
        batch.setColor(oldColor);
    }
}