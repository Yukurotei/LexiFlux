package it.yuruni.graphics.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import it.yuruni.Main;

public class TextGlyph extends Glyph {
    private String text;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout(); //for calculating text dimensions

    /**
     * Creates a new Glyph that renders text instead of a texture.
     * @param text The initial text to display.
     * @param font The BitmapFont to use for rendering.
     * @param x The x-coordinate of the glyph's bottom-left corner.
     * @param y The y-coordinate of the glyph's bottom-left corner.
     * @param addToQueue If true, the glyph will be automatically added to the global glyph list for rendering.
     */
    public TextGlyph(String text, BitmapFont font, float x, float y, boolean addToQueue) {
        super(null, x, y, addToQueue);
        this.font = font;
        this.text = text;
        updateHitbox();
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isVisible || font == null || text == null || text.isEmpty()) return;

        Color originalColor = font.getColor().cpy();
        float originalScaleX = font.getScaleX();
        float originalScaleY = font.getScaleY();

        font.setColor(originalColor.r, originalColor.g, originalColor.b, this.alpha);
        font.getData().setScale(this.scaleX, this.scaleY);

        //TODO: Fix rotation
        font.draw(batch, text, x, y);

        //restore to original
        font.setColor(originalColor);
        font.getData().setScale(originalScaleX, originalScaleY);
    }

    /**
     * Updates the hitbox to match the text's current size and the glyph's scale and position.
     */
    @Override
    protected void updateHitbox() {
        if (font == null || text == null) {
            hitbox.set(0, 0, 0, 0);
            return;
        }

        float originalScaleX = font.getScaleX();
        float originalScaleY = font.getScaleY();
        font.getData().setScale(1.0f, 1.0f);
        layout.setText(font, text);
        font.getData().setScale(originalScaleX, originalScaleY);

        float unscaledWidth = layout.width;
        float unscaledHeight = layout.height;

        float visualWidth = unscaledWidth * scaleX;
        float visualHeight = unscaledHeight * scaleY;

        float visualX = this.x + (unscaledWidth / 2f) * (1 - scaleX);
        float visualY = this.y + (unscaledHeight / 2f) * (1 - scaleY);

        hitbox.set(visualX, visualY, visualWidth, visualHeight);
    }

    public String getText() {
        return text;
    }

    /**
     * Sets the text for this glyph and updates its hitbox.
     * @param text The new text to display.
     */
    public void setText(String text) {
        this.text = text;
        updateHitbox();
    }

    public BitmapFont getFont() {
        return font;
    }

    public void setFont(BitmapFont font) {
        this.font = font;
        updateHitbox();
    }

    // Override to prevent null pointer exceptions, as this glyph has no texture.
    @Override
    public com.badlogic.gdx.graphics.Texture getTexture() {
        return null;
    }
}
