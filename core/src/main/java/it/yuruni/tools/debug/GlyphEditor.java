package it.yuruni.tools.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import it.yuruni.graphics.element.Glyph;

import java.util.List;

/**
 * A modular tool for selecting, moving, and resizing Glyphs in a scene.
 * Toggle with F12.
 * - Click to select a glyph.
 * - Drag to move.
 * - Ctrl + Drag to resize.
 * - Ctrl + Shift + Drag to resize with locked aspect ratio.
 */
public class GlyphEditor {
    private final OrthographicCamera camera;
    private final List<Glyph> glyphs;

    private Glyph selectedGlyph;
    private boolean isEditMode = false;
    private float dragOffsetX, dragOffsetY;

    // Resize-specific fields
    private float initialScaleX, initialScaleY;
    private float initialScreenX, initialScreenY;

    private final Vector3 unprojectVec = new Vector3();

    public GlyphEditor(OrthographicCamera camera, List<Glyph> glyphs) {
        this.camera = camera;
        this.glyphs = glyphs;
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!isEditMode) return;

        font.setColor(Color.YELLOW);
        font.draw(batch, "EDIT MODE (F12) | Ctrl+Drag=Resize | +Shift=Lock Ratio", 10, camera.viewportHeight - 10);

        if (selectedGlyph != null) {
            String info = String.format(
                "X: %.1f, Y: %.1f\nScaleX: %.2f, ScaleY: %.2f",
                selectedGlyph.getX(), selectedGlyph.getY(),
                selectedGlyph.getScaleX(), selectedGlyph.getScaleY()
            );
            font.draw(batch, info, 10, camera.viewportHeight - 40);
            font.draw(batch, info, selectedGlyph.getX(), selectedGlyph.getY() + selectedGlyph.getHeight() * selectedGlyph.getScaleY() + 20);
        }
        font.setColor(Color.WHITE); // Reset color
    }

    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F12) {
            isEditMode = !isEditMode;
            if (!isEditMode && selectedGlyph != null) {
                selectedGlyph.setColor(Color.WHITE); // Reset color on exit
                selectedGlyph = null;
            }
            return true;
        }
        return false;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!isEditMode || button != Input.Buttons.LEFT) return false;

        initialScreenX = screenX;
        initialScreenY = screenY;

        unprojectVec.set(screenX, screenY, 0);
        camera.unproject(unprojectVec);

        if (selectedGlyph != null) {
            selectedGlyph.setColor(Color.WHITE);
        }
        selectedGlyph = null;

        for (int i = glyphs.size() - 1; i >= 0; i--) {
            Glyph g = glyphs.get(i);
            if (g.getAlpha() > 0 && g.getBoundingRectangle().contains(unprojectVec.x, unprojectVec.y)) {
                selectedGlyph = g;
                selectedGlyph.setColor(Color.CYAN);
                // Store offsets for moving
                dragOffsetX = unprojectVec.x - g.getX();
                dragOffsetY = unprojectVec.y - g.getY();
                // Store initial scale for resizing
                initialScaleX = g.getScaleX();
                initialScaleY = g.getScaleY();
                return true;
            }
        }
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!isEditMode || selectedGlyph == null) return false;

        // --- Resize Logic ---
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            float dx = screenX - initialScreenX;
            float dy = screenY - initialScreenY;
            float sensitivity = 0.005f;

            // --- Aspect Ratio Lock Logic ---
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                float ratio = (initialScaleX != 0) ? initialScaleY / initialScaleX : 1.0f;
                float newScaleX = initialScaleX + dx * sensitivity;
                if (newScaleX < 0.01f) newScaleX = 0.01f; // Prevent flipping/disappearing

                selectedGlyph.setScaleX(newScaleX);
                selectedGlyph.setScaleY(newScaleX * ratio);
            } else {
                // --- Free Resize Logic ---
                float newScaleX = initialScaleX + dx * sensitivity;
                float newScaleY = initialScaleY - dy * sensitivity; // Y is inverted on screen
                if (newScaleX < 0.01f) newScaleX = 0.01f;
                if (newScaleY < 0.01f) newScaleY = 0.01f;

                selectedGlyph.setScaleX(newScaleX);
                selectedGlyph.setScaleY(newScaleY);
            }
            return true; // Event consumed by resize logic
        }

        // --- Move Logic ---
        unprojectVec.set(screenX, screenY, 0);
        camera.unproject(unprojectVec);
        selectedGlyph.setX(unprojectVec.x - dragOffsetX);
        selectedGlyph.setY(unprojectVec.y - dragOffsetY);
        return true; // Event consumed by move logic
    }
}
