package it.yuruni.graphics.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import it.yuruni.Main;

import java.util.ArrayList;
import java.util.List;

public class Glyph {
    protected Texture texture;
    protected List<Texture> textures;
    protected float x;
    protected float y;
    protected float alpha = 1f;
    protected float scaleX = 1f;
    protected float scaleY = 1f;
    protected float rotation = 0f;
    protected Rectangle hitbox;
    protected boolean isVisible;
    private float frameTime = 0.1f;
    private float animationTimer = 0f;
    private int currentFrame = 0;
    protected String glyphID; // New field

    /**
     *
     * @param texture the texture of the glyph
     * @param x the x-coordinate of the glyph
     * @param y the y-coordinate of the glyph
     * @param addToQueue if true, the glyph will be automatically added to the global glyph list
     */
    public Glyph(Texture texture, float x, float y, boolean addToQueue) {
        this(null, texture, x, y, addToQueue); // Call new primary constructor with null glyphID
    }

    /**
     *
     * @param glyphID the unique identifier for this glyph (optional)
     * @param texture the texture of the glyph
     * @param x the x-coordinate of the glyph
     * @param y the y-coordinate of the glyph
     * @param addToQueue if true, the glyph will be automatically added to the global glyph list
     */
    public Glyph(String glyphID, Texture texture, float x, float y, boolean addToQueue) {
        this.glyphID = glyphID;
        this.texture = texture;
        this.textures = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.isVisible = true;
        this.hitbox = new Rectangle();
        updateHitbox();
        if (addToQueue) Main.glyphs.add(this);
    }


    /**
     *
     * @param batch the SpriteBatch used for rendering
     */
    public void render(SpriteBatch batch) {
        if (!isVisible) return;

        Texture currentTexture = (textures != null && !textures.isEmpty()) ? textures.get(currentFrame) : texture;
        if (currentTexture == null) return;

        Color originalColor = batch.getColor();
        batch.setColor(originalColor.r, originalColor.g, originalColor.b, alpha);

        float width = currentTexture.getWidth();
        float height = currentTexture.getHeight();
        float originX = width / 2f;
        float originY = height / 2f;

        batch.draw(currentTexture,
                x, y,
                originX, originY,
                width, height,
                scaleX, scaleY,
                rotation,
                0, 0, (int) width, (int) height,
                false, false);

        batch.setColor(originalColor);
    }

    /**
     *
     * @param delta the time elapsed since the last update
     */
    public void update(float delta) {
        updateHitbox();

        if (textures != null && !textures.isEmpty() && frameTime > 0) {
            animationTimer += delta;
            if (animationTimer > frameTime) {
                animationTimer = 0f;
                currentFrame++;

                if (currentFrame >= textures.size()) currentFrame = 0;
            }
        }
    }

    protected void updateHitbox() {
        Texture currentTexture = getTexture();
        if (currentTexture == null) return;

        float unscaledWidth = currentTexture.getWidth();
        float unscaledHeight = currentTexture.getHeight();

        float visualWidth = unscaledWidth * scaleX;
        float visualHeight = unscaledHeight * scaleY;

        //visual bottom-left corner is offset from x y cuz scaling happens from the center origin
        float visualX = this.x + (unscaledWidth / 2f) * (1 - scaleX);
        float visualY = this.y + (unscaledHeight / 2f) * (1 - scaleY);

        hitbox.set(visualX, visualY, visualWidth, visualHeight);
    }

    public void dispose() {
        if (texture != null) texture.dispose();

        if (textures != null) for (Texture texture : textures) texture.dispose();

        Main.glyphs.remove(this);
    }

    /**
     *
     * @return the source texture of the glyph
     */
    public Texture getSourceTexture() {
        return texture;
    }

    /**
     *
     * @return the list of textures for animated glyphs
     */
    public List<Texture> getTextures() {
        return textures;
    }

    public Texture getTexture() {
        if (texture == null && textures.isEmpty()) return null;
        if (textures.isEmpty()) return texture;

        return textures.get(0);
    }

    /**
     *
     * @return the x-coordinate of the glyph
     */
    public float getX() {
        return x;
    }

    /**
     *
     * @return the y-coordinate of the glyph
     */
    public float getY() {
        return y;
    }

    public float getAlpha() {
        return alpha;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getRotation() {
        return rotation;
    }

    /**
     *
     * @return whether the glyph is visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     *
     * @param x the new x-coordinate of the glyph
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     *
     * @param y the new y-coordinate of the glyph
     */
    public void setY(float y) {
        this.y = y;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    /**
     *
     * @param scaleY the new y-scale of the glyph
     */
    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     *
     * @param texture the new texture of the glyph
     */
    public void setTexture(Texture texture) {
        this.texture = texture;
        this.textures = null;
    }

    /**
     *
     * @param textures the new list of textures for animated glyphs
     */
    public void setTextures(List<Texture> textures) {
        this.textures = textures;
        this.texture = null;
        this.currentFrame = 0;
        this.animationTimer = 0;
    }

    /**
     *
     * @param frameTime the time each frame is displayed in animated glyphs
     */
    public void setFrameTime(float frameTime) {
        this.frameTime = frameTime;
    }

    /**
     *
     * @param visible whether the glyph should be visible
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    /**
     *
     * @return the hitbox of the glyph
     */
    public Rectangle getHitbox() {
        return hitbox;
    }

    /**
     *
     * @param other the other glyph to check collision with
     * @return true if this glyph collides with the other glyph, false otherwise
     */
    public boolean collidesWith(Glyph other) {
        return this.hitbox.overlaps(other.getHitbox());
    }


    public float getWidth() {
        return this.texture.getWidth();
    }


    public float getHeight() {
        return this.texture.getHeight();
    }

    public float centerAxis(float objectAxis, float parentAxis) {
        return objectAxis - (parentAxis / 2F);
    }

    public void setGlyphID(String glyphID) {
        this.glyphID = glyphID;
    }

    public String getGlyphID() {
        return glyphID;
    }
}
