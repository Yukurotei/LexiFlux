package it.yuruni.graphics.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import it.yuruni.Main;
import it.yuruni.graphics.animation.Glyph;

public class ParallaxManager {

    private final Array<ParallaxLayer> layers = new Array<>();

    /**
     * Registers a Glyph to be part of the parallax effect.
     * The glyph will be centered and its original position stored.
     * @param glyph The glyph to be moved.
     * @param strength How much the glyph should move in response to input. A small value like 0.02 is a good start.
     * @param smoothness How smoothly the glyph should move to its target position. A small value like 0.1 is a good start.
     */
    public void addLayer(Glyph glyph, float strength, float smoothness) {
        glyph.setX(Main.WIDTH / 2f - glyph.getWidth() / 2f);
        glyph.setY(Main.HEIGHT / 2f - glyph.getHeight() / 2f);
        layers.add(new ParallaxLayer(glyph, strength, smoothness));
    }

    public void update(float delta) {
        float targetOffsetX = 0;
        float targetOffsetY = 0;

        targetOffsetX += Gdx.input.getX() - Main.WIDTH / 2f;
        targetOffsetY += (Main.HEIGHT - Gdx.input.getY()) - Main.HEIGHT / 2f; // Y is inverted

        //arrow keys
        float keyStrengthX = Main.WIDTH / 2f;
        float keyStrengthY = Main.HEIGHT / 2f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            targetOffsetX -= keyStrengthX;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            targetOffsetX += keyStrengthX;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            targetOffsetY -= keyStrengthY;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            targetOffsetY += keyStrengthY;
        }

        //Clamp to prevent excess movement
        targetOffsetX = MathUtils.clamp(targetOffsetX, -Main.WIDTH / 2f, Main.WIDTH / 2f);
        targetOffsetY = MathUtils.clamp(targetOffsetY, -Main.HEIGHT / 2f, Main.HEIGHT / 2f);

        //Apply
        for (ParallaxLayer layer : layers) {
            float finalTargetX = layer.originalX - targetOffsetX * layer.strength;
            float finalTargetY = layer.originalY - targetOffsetY * layer.strength;

            layer.glyph.setX(MathUtils.lerp(layer.glyph.getX(), finalTargetX, layer.smoothness));
            layer.glyph.setY(MathUtils.lerp(layer.glyph.getY(), finalTargetY, layer.smoothness));
        }
    }

    private static class ParallaxLayer {
        final Glyph glyph;
        final float strength;
        final float smoothness;
        final float originalX;
        final float originalY;

        ParallaxLayer(Glyph glyph, float strength, float smoothness) {
            this.glyph = glyph;
            this.strength = strength;
            this.smoothness = smoothness;
            this.originalX = glyph.getX();
            this.originalY = glyph.getY();
        }
    }
}
