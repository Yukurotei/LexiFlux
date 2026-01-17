package it.yuruni.tools.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A simple debug tool to display the current mouse coordinates in world space.
 * Toggle with F1.
 */
public class MouseInspector {
    private boolean isEnabled = false;
    private final Viewport viewport;
    private final Vector2 mousePos = new Vector2();

    public MouseInspector(Viewport viewport) {
        this.viewport = viewport;
    }

    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F1) {
            isEnabled = !isEnabled;
            Gdx.app.log("MouseInspector", "Inspector " + (isEnabled ? "ON" : "OFF"));
            return true;
        }
        return false;
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!isEnabled) {
            return;
        }

        // Get screen coordinates and unproject them to world coordinates
        mousePos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mousePos);

        String coords = String.format("X: %.1f | Y: %.1f", mousePos.x, mousePos.y);

        font.setColor(Color.YELLOW);
        // Draw in the top-right corner
        font.draw(batch, coords, viewport.getWorldWidth() - 200, viewport.getWorldHeight() - 10);
        font.setColor(Color.WHITE);
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
