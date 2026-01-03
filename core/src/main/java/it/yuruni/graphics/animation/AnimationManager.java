package it.yuruni.graphics.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import it.yuruni.Main;
import it.yuruni.Utils;
import it.yuruni.graphics.animation.Easing;

public class AnimationManager {

    private final Array<Animation> animations = new Array<>();
    private final Pool<Animation> animationPool = new Pool<Animation>() {
        @Override
        protected Animation newObject() {
            return new Animation();
        }
    };

    public void update(float delta) {
        for (int i = animations.size - 1; i >= 0; i--) {
            Animation anim = animations.get(i);
            if (anim.isFinished()) {
                animations.removeIndex(i);
                animationPool.free(anim);
            } else {
                anim.update(delta);
            }
        }
    }

    public void updateAndRenderGlyphs(float delta, SpriteBatch batch) {
        for (Glyph glyph : Main.glyphs) {
            glyph.update(delta);
            glyph.render(batch);
        }
    }

    public void animateMove(Glyph target, float toX, float toY, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initMove(target, toX, toY, duration, easing);
        animations.add(anim);
    }

    public void animateFade(Glyph target, float toOpacity, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initFade(target, toOpacity, duration, easing);
        animations.add(anim);
    }

    public void animateScale(Glyph target, float toScaleX, float toScaleY, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initScale(target, toScaleX, toScaleY, duration, easing);
        animations.add(anim);
    }

    public void animateRotation(Glyph target, float toRotation, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initRotation(target, toRotation, duration, easing);
        animations.add(anim);
    }

    public void animatePulse(Glyph target, float bpm, float magnitude) {
        Animation anim = animationPool.obtain();
        anim.initPulse(target, bpm, magnitude);
        animations.add(anim);
    }

    /**
     * Immediately stops and removes all animations running on the specified glyph.
     * @param target The glyph whose animations should be stopped.
     */
    public void stopAllAnimations(Glyph target) {
        for (int i = animations.size - 1; i >= 0; i--) {
            Animation anim = animations.get(i);
            if (anim.getAnimatedGlyph() == target) {
                animations.removeIndex(i);
                animationPool.free(anim);
            }
        }
    }

    private static class Animation implements Pool.Poolable {
        private Glyph target; // Changed from AnimatedGlyph
        private Easing easing;
        private float duration;
        private float time;

        // Position
        private float startX, startY;
        private float toX, toY;

        // Opacity
        private float startOpacity, toOpacity;

        // Scale
        private float startScaleX, startScaleY;
        private float toScaleX, toScaleY;

        // Rotation
        private float startRotation, toRotation;

        // Pulse
        private boolean isPulseAnimation = false;
        private float bpm;
        private float magnitude;


        public void initMove(Glyph target, float toX, float toY, float duration, Easing easing) { // Changed from AnimatedGlyph
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startX = target.getX();
            this.startY = target.getY();
            this.toX = toX;
            this.toY = toY;

            this.toOpacity = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation = Float.NaN;
        }

        public void initFade(Glyph target, float toOpacity, float duration, Easing easing) { // Changed from AnimatedGlyph
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startOpacity = target.getAlpha();
            this.toOpacity = toOpacity;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation = Float.NaN;
        }

        public void initScale(Glyph target, float toScaleX, float toScaleY, float duration, Easing easing) { // Changed from AnimatedGlyph
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startScaleX = target.getScaleX();
            this.startScaleY = target.getScaleY();
            this.toScaleX = toScaleX;
            this.toScaleY = toScaleY;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
        }

        public void initPulse(Glyph target, float bpm, float magnitude) {
            this.target = target;
            this.duration = Float.POSITIVE_INFINITY; // Makes it run indefinitely
            this.easing = null; // Easing is not used for pulsing
            this.time = 0;

            this.startScaleX = target.getScaleX();
            this.startScaleY = target.getScaleY();
            this.bpm = bpm;
            this.magnitude = magnitude;

            this.isPulseAnimation = true;

            // Invalidate other animation types
            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
            this.toScaleX = Float.NaN; // This is important to distinguish from a regular scale
            this.toScaleY = Float.NaN;
        }

        public void initRotation(Glyph target, float toRotation, float duration, Easing easing) { // Changed from AnimatedGlyph
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startRotation = target.getRotation();
            this.toRotation = toRotation;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toOpacity = Float.NaN;
        }

        public void update(float delta) {
            if (isFinished()) return;

            time += delta;

            if (isPulseAnimation) {
                float beatDuration = 60f / bpm;
                float timeInBeat = time % beatDuration;
                float beatProgress = timeInBeat / beatDuration;

                // A sine wave gives a smooth 0 -> 1 -> 0 pulse over the beat duration
                float pulseProgress = (float) Math.sin(beatProgress * Math.PI);

                float pulseAmountX = (startScaleX * magnitude) - startScaleX;
                float pulseAmountY = (startScaleY * magnitude) - startScaleY;

                target.setScaleX(startScaleX + pulseAmountX * pulseProgress);
                target.setScaleY(startScaleY + pulseAmountY * pulseProgress);
                return; // Pulse animation is done for this frame
            }

            float progress = Math.min(1f, time / duration);
            float easedProgress = Utils.applyEasing(progress, this.easing);

            if (!Float.isNaN(toX)) {
                target.setX(startX + (toX - startX) * easedProgress);
            }
            if (!Float.isNaN(toY)) {
                target.setY(startY + (toY - startY) * easedProgress);
            }
            if (!Float.isNaN(toOpacity)) {
                target.setAlpha(startOpacity + (toOpacity - startOpacity) * easedProgress);
            }
            if (!Float.isNaN(toScaleX)) {
                target.setScaleX(startScaleX + (toScaleX - startScaleX) * easedProgress);
            }
            if (!Float.isNaN(toScaleY)) {
                target.setScaleY(startScaleY + (toScaleY - startScaleY) * easedProgress);
            }
            if (!Float.isNaN(toRotation)) {
                target.setRotation(startRotation + (toRotation - startRotation) * easedProgress);
            }

            if (progress >= 1f) {
                time = duration; // Ensure time is exactly duration when finished
            }
        }

        public boolean isFinished() {
            if (isPulseAnimation) {
                return false; // Pulse animations run indefinitely by default
            }
            return time >= duration;
        }

        public Glyph getAnimatedGlyph() { // Changed from AnimatedGlyph
            return target;
        }


        @Override
        public void reset() {
            target = null;
            easing = Easing.LINEAR;
            duration = 0;
            time = 0;

            startX = startY = toX = toY = Float.NaN;
            startOpacity = toOpacity = Float.NaN;
            startScaleX = startScaleY = toScaleX = toScaleY = Float.NaN;
            startRotation = toRotation = Float.NaN;

            isPulseAnimation = false;
            bpm = 0;
            magnitude = 0;
        }
    }
}