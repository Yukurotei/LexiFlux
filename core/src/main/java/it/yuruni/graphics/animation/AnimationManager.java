package it.yuruni.graphics.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import it.yuruni.Main;

public class AnimationManager {

    public enum Easing {
        LINEAR,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_OUT_QUAD,
        EASE_IN_CUBIC,
        EASE_OUT_CUBIC,
        EASE_IN_OUT_CUBIC,
        EASE_IN_SINE,
        EASE_OUT_SINE,
        EASE_IN_OUT_SINE,
        EASE_IN_EXPO,
        EASE_OUT_EXPO,
        EASE_IN_OUT_EXPO,
        EASE_IN_QUART,
        EASE_OUT_QUART,
        EASE_IN_OUT_QUART,
        EASE_IN_QUINT,
        EASE_OUT_QUINT,
        EASE_IN_OUT_QUINT,
        EASE_IN_CIRC,
        EASE_OUT_CIRC,
        EASE_IN_OUT_CIRC,
        EASE_IN_BACK,
        EASE_OUT_BACK,
        EASE_IN_OUT_BACK,
        EASE_IN_ELASTIC,
        EASE_OUT_ELASTIC,
        EASE_IN_OUT_ELASTIC,
        EASE_IN_BOUNCE,
        EASE_OUT_BOUNCE,
        EASE_IN_OUT_BOUNCE,
        EASE_OSCILLATE_1,
        EASE_OSCILLATE_3,
        EASE_OSCILLATE_5,
        EASE_OSCILLATE_INFINITE
    }

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
            float progress = Math.min(1f, time / duration);
            float easedProgress = applyEasing(progress);

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
            return time >= duration;
        }

        private float applyEasing(float t) {
            switch (easing) {
                case EASE_IN_QUAD:
                    return t * t;
                case EASE_OUT_QUAD:
                    return t * (2 - t);
                case EASE_IN_OUT_QUAD:
                    return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
                case EASE_IN_CUBIC:
                    return t * t * t;
                case EASE_OUT_CUBIC:
                    return 1 - (float) Math.pow(1 - t, 3);
                case EASE_IN_OUT_CUBIC:
                    return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
                case EASE_IN_SINE:
                    return 1 - (float) Math.cos((t * Math.PI) / 2);
                case EASE_OUT_SINE:
                    return (float) Math.sin((t * Math.PI) / 2);
                case EASE_IN_OUT_SINE:
                    return -((float) Math.cos(Math.PI * t) - 1) / 2;
                case EASE_IN_EXPO:
                    return t == 0 ? 0 : (float) Math.pow(2, 10 * t - 10);
                case EASE_OUT_EXPO:
                    return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
                case EASE_IN_OUT_EXPO:
                    if (t == 0) return 0;
                    if (t == 1) return 1;
                    if (t < 0.5f) return (float) Math.pow(2, 20 * t - 10) / 2;
                    return (2 - (float) Math.pow(2, -20 * t + 10)) / 2;
                case EASE_IN_QUART:
                    return t * t * t * t;
                case EASE_OUT_QUART:
                    return 1 - (float) Math.pow(1 - t, 4);
                case EASE_IN_OUT_QUART:
                    return t < 0.5f ? 8 * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 4) / 2;
                case EASE_IN_QUINT:
                    return t * t * t * t * t;
                case EASE_OUT_QUINT:
                    return 1 - (float) Math.pow(1 - t, 5);
                case EASE_IN_OUT_QUINT:
                    return t < 0.5f ? 16 * t * t * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 5) / 2;
                case EASE_IN_CIRC:
                    return 1 - (float) Math.sqrt(1 - t * t);
                case EASE_OUT_CIRC:
                    return (float) Math.sqrt(1 - (float) Math.pow(t - 1, 2));
                case EASE_IN_OUT_CIRC:
                    return t < 0.5f ? (1 - (float) Math.sqrt(1 - (float) Math.pow(2 * t, 2))) / 2
                            : ((float) Math.sqrt(1 - (float) Math.pow(-2 * t + 2, 2)) + 1) / 2;
                case EASE_IN_BACK: {
                    final float c1 = 1.70158f;
                    final float c3 = c1 + 1f;
                    return c3 * t * t * t - c1 * t * t;
                }
                case EASE_OUT_BACK: {
                    final float c1 = 1.70158f;
                    final float c3 = c1 + 1f;
                    return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
                }
                case EASE_IN_OUT_BACK: {
                    final float c1 = 1.70158f;
                    final float c2 = c1 * 1.525f;
                    return t < 0.5f
                            ? ((float) Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                            : ((float) Math.pow(2 * t - 2, 2) * ((c2 + 1) * (2 * t - 2) + c2) + 2) / 2;
                }
                case EASE_IN_ELASTIC: {
                    final float c4 = (2 * (float) Math.PI) / 3;
                    if (t == 0) return 0;
                    if (t == 1) return 1;
                    return -(float) Math.pow(2, 10 * t - 10) * (float) Math.sin((t * 10 - 10.75) * c4);
                }
                case EASE_OUT_ELASTIC: {
                    final float c4 = (2 * (float) Math.PI) / 3;
                    if (t == 0) return 0;
                    if (t == 1) return 1;
                    return (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75) * c4) + 1;
                }
                case EASE_IN_OUT_ELASTIC: {
                    final float c5 = (2 * (float) Math.PI) / 4.5f;
                    if (t == 0) return 0;
                    if (t == 1) return 1;
                    if (t < 0.5f) {
                        return -((float) Math.pow(2, 20 * t - 10) * (float) Math.sin((20 * t - 11.125) * c5)) / 2;
                    }
                    return ((float) Math.pow(2, -20 * t + 10) * (float) Math.sin((20 * t - 11.125) * c5)) / 2 + 1;
                }
                case EASE_IN_BOUNCE:
                    return 1 - easeOutBounce(1 - t);
                case EASE_OUT_BOUNCE:
                    return easeOutBounce(t);
                case EASE_IN_OUT_BOUNCE:
                    return t < 0.5f ? (1 - easeOutBounce(1 - 2 * t)) / 2 : (1 + easeOutBounce(2 * t - 1)) / 2;
                case EASE_OSCILLATE_1:
                    return (1 - (float) Math.cos(t * 2 * Math.PI)) / 2;
                case EASE_OSCILLATE_3:
                    return (1 - (float) Math.cos(t * 3 * 2 * Math.PI)) / 2;
                case EASE_OSCILLATE_5:
                    return (1 - (float) Math.cos(t * 5 * 2 * Math.PI)) / 2;
                case EASE_OSCILLATE_INFINITE:
                    return (1 - (float) Math.cos(t * 9999 * 2 * Math.PI)) / 2;
                case LINEAR:
                default:
                    return t;
            }
        }

        private float easeOutBounce(float t) {
            final float n1 = 7.5625f;
            final float d1 = 2.75f;

            if (t < 1f / d1) {
                return n1 * t * t;
            } else if (t < 2f / d1) {
                t -= 1.5f / d1;
                return n1 * t * t + 0.75f;
            } else if (t < 2.5f / d1) {
                t -= 2.25f / d1;
                return n1 * t * t + 0.9375f;
            } else {
                t -= 2.625f / d1;
                return n1 * t * t + 0.984375f;
            }
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
        }
    }
}