package it.yuruni.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import it.yuruni.Main;
import it.yuruni.graphics.animation.Glyph;


public class Button extends Glyph {
    private final Runnable onClick;
    private Runnable onHover;
    private Runnable onUnHover;

    private boolean justHovered = false;

    /**
     * @param texture The image that will be used as a button
     * @param x The x-coordinate where the top-left corner of the button will be
     * @param y The y-coordinate where the top-left corner of the button will be
     * @param onClick when user click btn
     */
    public Button(Texture texture, float x, float y, Runnable onClick) {
        super(texture, x, y, false);

        this.onClick = onClick;
    }

    public void addOnHoverListener(Runnable onHover) {
        this.onHover = onHover;
    }

    public void addOnUnHoverListener(Runnable onUnHover) {
        this.onUnHover = onUnHover;
    }

    /**
     * @param batch The sprite contexted used to render thy button
     */
    public void renderButton(SpriteBatch batch) {
        super.render(batch);
    }

    /**
     * @param delta The value that keeps the FPS locked
     */
    @Override
    public void update(float delta) {
        super.update(delta);

        Vector2 touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Main.viewport.unproject(touchPos);

        if (getHitbox().contains(touchPos.x, touchPos.y)) {
            if (onHover != null && !justHovered) {
                justHovered = true;
                onHover.run();
            }
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                onClick.run();
            }
        } else {
            if (justHovered) {
                justHovered = false;
                if (onUnHover != null) onUnHover.run();
            }
        }
    }
}
