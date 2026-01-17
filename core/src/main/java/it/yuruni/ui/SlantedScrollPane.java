package it.yuruni.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import it.yuruni.graphics.element.Glyph;

/**
 * A UI component that arranges and scrolls glyphs along a slanted path,
 * masking them to a specific rectangular area.
 * Uses a velocity and friction model for smooth, momentum-based scrolling.
 */
public class SlantedScrollPane {
    private final Array<ScrollPaneItem> items;
    private final Glyph background;
    private boolean renderBackgroundGlyph = true;

    private final Vector2 scrollVector;
    private final Rectangle clippingBounds;

    private float currentScrollPosition = 0f;
    private float scrollVelocity = 0f;

    private final float itemSpacing;
    private final float scrollSpeed;
    private final float friction;
    private final Camera camera;

    private ScrollPaneItem hoveredItem = null;
    private final Vector3 unprojectVec = new Vector3();

    private float lowerScrollBound = 0;
    private float upperScrollBound = 0;

    public SlantedScrollPane(Glyph background, Vector2 scrollVector, Camera camera) {
        this(background, scrollVector, camera, 120f, 250f, 0.99f);
    }

    public SlantedScrollPane(Glyph background, Vector2 scrollVector, Camera camera, float itemSpacing, float scrollSpeed, float friction) {
        this.background = background;
        this.scrollVector = scrollVector.cpy().nor();
        this.items = new Array<>();

        this.itemSpacing = itemSpacing;
        this.scrollSpeed = scrollSpeed;
        this.friction = friction;
        this.camera = camera;

        this.clippingBounds = new Rectangle(
            background.getX(),
            background.getY(),
            background.getWidth() * background.getScaleX(),
            background.getHeight() * background.getScaleY()
        );

        Gdx.app.log("SlantedScrollPane", "Initialized. Clipping Bounds: " + clippingBounds);
        Gdx.app.log("SlantedScrollPane", "Scroll Vector: " + this.scrollVector);
    }

    public void addItem(ScrollPaneItem item) {
        items.add(item);
        Gdx.app.log("SlantedScrollPane", "Added item. Total items: " + items.size);
        updateScrollBounds();
    }

    private void updateScrollBounds() {
        if (items.size <= 1) {
            upperScrollBound = 0;
        } else {
            //TODO: Need to calculate this based on card size and spacing
            upperScrollBound = (items.size - 1) * itemSpacing;
        }
        Gdx.app.log("SlantedScrollPane", "Scroll bounds updated. Upper: " + upperScrollBound);
    }

    public boolean scrolled(float amountY) {
        scrollVelocity += amountY * scrollSpeed * -1;
        Gdx.app.log("SlantedScrollPane", "Scrolled. New velocity: " + scrollVelocity);
        return true;
    }

    public void update(float delta) {
        background.update(delta);

        currentScrollPosition += scrollVelocity * delta;

        scrollVelocity *= friction;

        if (Math.abs(scrollVelocity) < 1f) {
            scrollVelocity = 0f;
        }

        if (currentScrollPosition < lowerScrollBound) {
            currentScrollPosition = lowerScrollBound;
            scrollVelocity = 0;
        }
        if (currentScrollPosition > upperScrollBound) {
            currentScrollPosition = upperScrollBound;
            scrollVelocity = 0;
        }


        // Update item positions based on the current scroll position
        Vector2 startPos = new Vector2(
                background.getX() + (background.getWidth() * background.getScaleX()) / 2,
                background.getY() + (background.getHeight() * background.getScaleY()) / 2
        );

        for (int i = 0; i < items.size; i++) {
            Glyph item = items.get(i);
            item.update(delta);
            float itemPosOnPath = i * itemSpacing - currentScrollPosition;
            float newX = startPos.x + scrollVector.x * itemPosOnPath;
            float newY = startPos.y + scrollVector.y * itemPosOnPath;
            item.setX(newX - (item.getWidth() * item.getScaleX() / 2f));
            item.setY(newY - (item.getHeight() * item.getScaleY() / 2f));
        }

        // Handle hover detection
        handleHover();
    }

    private void handleHover() {
        unprojectVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(unprojectVec);

        ScrollPaneItem currentlyFoundItem = null;
        // Check if mouse is within the main component bounds first
        if (clippingBounds.contains(unprojectVec.x, unprojectVec.y)) {
            for (int i = items.size - 1; i >= 0; i--) {
                ScrollPaneItem item = items.get(i);
                if (item.isVisible() && item.getBoundingRectangle().contains(unprojectVec.x, unprojectVec.y)) {
                    currentlyFoundItem = item;
                    break; // Found the topmost item
                }
            }
        }

        // Check for state changes to fire events only once
        if (currentlyFoundItem != hoveredItem) {
            if (hoveredItem != null) {
                hoveredItem.unhover();
            }
            hoveredItem = currentlyFoundItem;
            if (hoveredItem != null) {
                hoveredItem.hover();
            }
        }
    }


    public void render(Camera camera, SpriteBatch batch) {
        if (renderBackgroundGlyph) {
            background.render(batch);
        }

        batch.flush();

        clippingBounds.set(
                background.getX(),
                background.getY(),
                background.getWidth() * background.getScaleX(),
                background.getHeight() * background.getScaleY()
        );

        Rectangle scissors = new Rectangle();
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clippingBounds, scissors);

        if (ScissorStack.pushScissors(scissors)) {
            for (Glyph item : items) {
                item.render(batch);
            }
            batch.flush();
            ScissorStack.popScissors();
        }
    }

    public Rectangle getBoundingRectangle() {
        return background.getBoundingRectangle();
    }

    public Glyph getBackground() {
        return background;
    }

    public void setRenderBackgroundGlyph(boolean renderBackgroundGlyph) {
        this.renderBackgroundGlyph = renderBackgroundGlyph;
    }

    public ScrollPaneItem getFirstItem() {
        if (items.isEmpty()) return null;
        return items.first();
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // The hover logic in update() has already found the item under the cursor.
        if (button == Input.Buttons.LEFT && hoveredItem != null) {
            hoveredItem.click();
            return true;
        }
        return false;
    }
}