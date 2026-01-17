package it.yuruni.ui;

import com.badlogic.gdx.graphics.Texture;
import it.yuruni.graphics.element.Glyph;

/**
 * A specialized Glyph for use within a scroll pane, containing onClick, onHover,
 * and onUnhover actions.
 */
public class ScrollPaneItem extends Glyph {

    private Runnable onClick;
    private Runnable onHover;
    private Runnable onUnHover;
    private boolean isHovered = false;

    /**
     * @param texture The image for the item.
     * @param x The initial x-coordinate.
     * @param y The initial y-coordinate.
     * @param addToQueue Whether to add to the main render queue (should generally be false for scroll panes).
     * @param onClick The action to perform when this item is clicked.
     */
    public ScrollPaneItem(Texture texture, float x, float y, boolean addToQueue, Runnable onClick) {
        super(texture, x, y, addToQueue);
        this.onClick = onClick;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void addOnHoverListener(Runnable onHover) {
        this.onHover = onHover;
    }

    public void addOnUnHoverListener(Runnable onUnHover) {
        this.onUnHover = onUnHover;
    }

    /**
     * Executes the onClick runnable associated with this item.
     */
    public void click() {
        if (onClick != null) {
            onClick.run();
        }
    }

    /**
     * Executes the onHover runnable if the item is not already in a hovered state.
     */
    public void hover() {
        if (!isHovered && onHover != null) {
            onHover.run();
        }
        isHovered = true;
    }

    /**
     * Executes the onUnHover runnable if the item is currently in a hovered state.
     */
    public void unhover() {
        if (isHovered && onUnHover != null) {
            onUnHover.run();
        }
        isHovered = false;
    }

    public boolean isHovered() {
        return isHovered;
    }
}
