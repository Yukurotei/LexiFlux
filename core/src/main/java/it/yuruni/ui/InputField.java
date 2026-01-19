package it.yuruni.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import it.yuruni.Main;
import it.yuruni.graphics.element.Glyph;
import it.yuruni.graphics.element.TextGlyph;

public class InputField {
    private final Glyph background;
    private final TextGlyph textRenderer;
    private final BitmapFont font;
    private String text;
    private boolean isActive;
    private float cursorBlinkTimer;
    private boolean showCursor;
    private Color activeBorderColor = Color.CYAN;
    private Color normalBorderColor = Color.DARK_GRAY;
    private Color textColor = Color.WHITE;
    private float padding = 5f;

    private Runnable onChangeListener;
    private String hintText;

    public InputField(Texture backgroundTexture, BitmapFont font, float x, float y, float width, float height, String initialText, String hintText) {
        this.background = new Glyph(backgroundTexture, x, y, false);
        // Scale the background glyph to match the desired width and height
        if (backgroundTexture.getWidth() > 0) {
            this.background.setScaleX(width / backgroundTexture.getWidth());
        }
        if (backgroundTexture.getHeight() > 0) {
            this.background.setScaleY(height / backgroundTexture.getHeight());
        }
        
        this.font = font;
        this.text = initialText != null ? initialText : "";
        this.hintText = hintText;
        
        // Text is positioned relative to the scaled background glyph
        this.textRenderer = new TextGlyph(this.text.isEmpty() ? hintText : this.text, font, 
                                          x + padding, y + height - font.getCapHeight() - padding, false);
        this.textRenderer.setColor(this.text.isEmpty() ? Color.GRAY : textColor);
        
        this.isActive = false;
        this.cursorBlinkTimer = 0f;
        this.showCursor = false;
    }

    public void render(SpriteBatch batch) {
        background.render(batch);
        background.setColor(isActive ? activeBorderColor : normalBorderColor); // Use color for border effect

        textRenderer.render(batch);

        if (isActive && showCursor) {
            // Draw a simple cursor
            font.setColor(textColor);
            float cursorX = textRenderer.getX() + textRenderer.layout.width;
            float cursorY = textRenderer.getY() - font.getCapHeight(); // CapHeight is approx line height
            font.draw(batch, "|", cursorX, cursorY + font.getCapHeight()); // Draw cursor at end of text
        }
        font.setColor(textColor); // Reset font color after drawing cursor
    }

    public void update(float delta) {
        // Cursor blinking
        if (isActive) {
            cursorBlinkTimer += delta;
            if (cursorBlinkTimer > 0.5f) {
                showCursor = !showCursor;
                cursorBlinkTimer = 0;
            }
        } else {
            showCursor = false; // Hide cursor when not active
        }
        
        // Update text renderer content and color
        if (text.isEmpty()) {
            textRenderer.setText(hintText);
            textRenderer.setColor(Color.GRAY);
        } else {
            textRenderer.setText(text);
            textRenderer.setColor(textColor);
        }

        background.update(delta); // Update background glyph (e.g., hitbox)
        textRenderer.update(delta);
    }

    // Handles keyboard input
    public boolean keyTyped(char character) {
        if (!isActive) return false;

        if (character == '\b') { // Backspace
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                if (onChangeListener != null) onChangeListener.run();
                return true;
            }
        } else if (character == '\n' || character == '\r') { // Enter key
            // Can be used to submit, or just keep focus
            if (onChangeListener != null) onChangeListener.run();
            return true;
        } else if (character >= 32 && character < 127) { // Printable characters
            text += character;
            if (onChangeListener != null) onChangeListener.run();
            return true;
        }
        return false;
    }

    // Handles mouse clicks to activate/deactivate
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) return false;

        Vector3 touchPoint = new Vector3(screenX, screenY, 0);
        Main.viewport.unproject(touchPoint); // Unproject to world coordinates

        if (background.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
            setActive(true);
            return true;
        } else {
            setActive(false); // Lose focus if clicked outside
            return false;
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String newText) {
        this.text = newText != null ? newText : "";
        if (onChangeListener != null) onChangeListener.run();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        if (isActive) {
            cursorBlinkTimer = 0;
            showCursor = true;
        } else {
            // Trigger change listener when focus is lost, in case it wasn't triggered by Enter or character input
            if (onChangeListener != null) onChangeListener.run();
        }
    }

    public void setOnChangeListener(Runnable listener) {
        this.onChangeListener = listener;
    }

    // Getters for position and size to align textRenderer properly
    public float getX() { return background.getX(); }
    public float getY() { return background.getY(); }
    public float getWidth() { return background.getWidth() * background.getScaleX(); }
    public float getHeight() { return background.getHeight() * background.getScaleY(); }

    public Rectangle getBoundingRectangle() {
        return background.getBoundingRectangle();
    }
}
