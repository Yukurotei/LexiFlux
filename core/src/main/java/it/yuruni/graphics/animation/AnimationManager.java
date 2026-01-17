package it.yuruni.graphics.animation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import it.yuruni.Main;
import it.yuruni.graphics.element.Glyph3D;
import it.yuruni.utils.ElementUtils;
import it.yuruni.graphics.Easing;
import it.yuruni.graphics.element.Glyph;

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

    public void animateMove(Glyph3D target, float toX, float toY, float toZ, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initMove(target, toX, toY, toZ, duration, easing);
        animations.add(anim);
    }

    public void animateFade(Glyph target, float toOpacity, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initFade(target, toOpacity, duration, easing);
        animations.add(anim);
    }

    public void animateFade(Glyph3D target, float toOpacity, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initFade(target, toOpacity, duration, easing);
        animations.add(anim);
    }

    public void animateScale(Glyph target, float toScaleX, float toScaleY, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initScale(target, toScaleX, toScaleY, duration, easing);
        animations.add(anim);
    }

    public void animateScale(Glyph3D target, float toScaleX, float toScaleY, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initScale(target, toScaleX, toScaleY, duration, easing);
        animations.add(anim);
    }

    public void animateRotation(Glyph target, float toRotation, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initRotation(target, toRotation, duration, easing);
        animations.add(anim);
    }

    public void animateRotation(Glyph3D target, Vector3 toRotation, float duration, Easing easing) {
        Animation anim = animationPool.obtain();
        anim.initRotation(target, toRotation, duration, easing);
        animations.add(anim);
    }

    public void animatePulse(Glyph target, float bpm, float magnitude) {
        Animation anim = animationPool.obtain();
        anim.initPulse(target, bpm, magnitude);
        animations.add(anim);
    }

    public void animatePulse(Glyph3D target, float bpm, float magnitude) {
        Animation anim = animationPool.obtain();
        anim.initPulse(target, bpm, magnitude);
        animations.add(anim);
    }

    /**
     * Stops any pulsing animations on the specified Glyph and restores its scale
     * to the value it had before the pulse began.
     * @param target The Glyph to stop pulsing.
     */
    public void stopPulsing(Glyph target) {
        for (int i = animations.size - 1; i >= 0; i--) {
            Animation anim = animations.get(i);
            if (anim.getAnimatedObject() == target && anim.isPulseAnimation) {
                // Restore the original scale
                target.setScaleX(anim.startScaleX);
                target.setScaleY(anim.startScaleY);

                // Stop and free the animation
                animations.removeIndex(i);
                animationPool.free(anim);
            }
        }
    }

    /**
     * Stops any pulsing animations on the specified Glyph3D and restores its scale
     * to the value it had before the pulse began.
     * @param target The Glyph3D to stop pulsing.
     */
    public void stopPulsing(Glyph3D target) {
        for (int i = animations.size - 1; i >= 0; i--) {
            Animation anim = animations.get(i);
            if (anim.getAnimatedObject() == target && anim.isPulseAnimation) {
                // Restore the original scale
                target.dimension.x = anim.startScaleX;
                target.dimension.y = anim.startScaleY;

                // Stop and free the animation
                animations.removeIndex(i);
                animationPool.free(anim);
            }
        }
    }

    /**
     * Immediately stops and removes all animations running on the specified glyph.
     * @param target The glyph whose animations should be stopped.
     */
    public void stopAllAnimations(Object target) {
        for (int i = animations.size - 1; i >= 0; i--) {
            Animation anim = animations.get(i);
            if (anim.getAnimatedObject() == target) {
                animations.removeIndex(i);
                animationPool.free(anim);
            }
        }
    }

    private static class Animation implements Pool.Poolable {
        private Object target;
        private Easing easing;
        private float duration;
        private float time;

        // Position
        private float startX, startY, startZ;
        private float toX, toY, toZ;

        // Opacity
        private float startOpacity, toOpacity;

        // Scale
        private float startScaleX, startScaleY;
        private float toScaleX, toScaleY;

        // Rotation
        private float startRotation, toRotation;
        private Vector3 startRotation3D, toRotation3D;

        // Pulse
        private boolean isPulseAnimation = false;
        private float bpm;
        private float magnitude;


        public void initMove(Glyph target, float toX, float toY, float duration, Easing easing) {
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startX = target.getX();
            this.startY = target.getY();
            this.toX = toX;
            this.toY = toY;

            this.toZ = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation = Float.NaN;
            this.toRotation3D = null;
        }

        public void initMove(Glyph3D target, float toX, float toY, float toZ, float duration, Easing easing) {
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startX = target.position.x;
            this.startY = target.position.y;
            this.startZ = target.position.z;
            this.toX = toX;
            this.toY = toY;
            this.toZ = toZ;

            this.toOpacity = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation = Float.NaN;
            this.toRotation3D = null;
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
            this.toZ = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation = Float.NaN;
            this.toRotation3D = null;
        }

        public void initFade(Glyph3D target, float toOpacity, float duration, Easing easing) {
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startOpacity = target.decal.getColor().a;
            this.toOpacity = toOpacity;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toZ = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation = Float.NaN;
            this.toRotation3D = null;
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
            this.toZ = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
            this.toRotation3D = null;
        }

        public void initScale(Glyph3D target, float toScaleX, float toScaleY, float duration, Easing easing) {
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            this.startScaleX = target.dimension.x;
            this.startScaleY = target.dimension.y;
            this.toScaleX = toScaleX;
            this.toScaleY = toScaleY;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toZ = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
            this.toRotation3D = null;
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
            this.toZ = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
            this.toScaleX = Float.NaN; // This is important to distinguish from a regular scale
            this.toScaleY = Float.NaN;
            this.toRotation3D = null;
        }

        public void initPulse(Glyph3D target, float bpm, float magnitude) {
            this.target = target;
            this.duration = Float.POSITIVE_INFINITY;
            this.easing = null;
            this.time = 0;

            this.startScaleX = target.dimension.x;
            this.startScaleY = target.dimension.y;
            this.bpm = bpm;
            this.magnitude = magnitude;

            this.isPulseAnimation = true;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toZ = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toRotation3D = null;
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
            this.toZ = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation3D = null;
        }

        public void initRotation(Glyph3D target, Vector3 toRotation, float duration, Easing easing) {
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.time = 0;

            if (this.startRotation3D == null) {
                this.startRotation3D = new Vector3();
            }
            this.startRotation3D.set(target.rotation);
            this.toRotation3D = toRotation;

            this.toX = Float.NaN;
            this.toY = Float.NaN;
            this.toZ = Float.NaN;
            this.toScaleX = Float.NaN;
            this.toScaleY = Float.NaN;
            this.toOpacity = Float.NaN;
            this.toRotation = Float.NaN;
        }

        public void update(float delta) {
            if (isFinished()) return;

            time += delta;

            if (isPulseAnimation) {
                float beatDuration = 60f / bpm;
                float timeInBeat = time % beatDuration;
                float beatProgress = timeInBeat / beatDuration;

                float pulseProgress = (float) Math.sin(beatProgress * Math.PI);

                float pulseAmountX = (startScaleX * magnitude) - startScaleX;
                float pulseAmountY = (startScaleY * magnitude) - startScaleY;

                if (target instanceof Glyph) {
                    Glyph g = (Glyph) target;
                    g.setScaleX(startScaleX + pulseAmountX * pulseProgress);
                    g.setScaleY(startScaleY + pulseAmountY * pulseProgress);
                } else if (target instanceof Glyph3D) {
                    Glyph3D g3d = (Glyph3D) target;
                    g3d.dimension.x = startScaleX + pulseAmountX * pulseProgress;
                    g3d.dimension.y = startScaleY + pulseAmountY * pulseProgress;
                }
                return; // Pulse animation is done for this frame
            }

            float progress = Math.min(1f, time / duration);
            float easedProgress = ElementUtils.applyEasing(progress, this.easing);

            if (target instanceof Glyph) {
                Glyph g = (Glyph) target;
                if (!Float.isNaN(toX)) {
                    g.setX(startX + (toX - startX) * easedProgress);
                }
                if (!Float.isNaN(toY)) {
                    g.setY(startY + (toY - startY) * easedProgress);
                }
                if (!Float.isNaN(toOpacity)) {
                    g.setAlpha(startOpacity + (toOpacity - startOpacity) * easedProgress);
                }
                if (!Float.isNaN(toScaleX)) {
                    g.setScaleX(startScaleX + (toScaleX - startScaleX) * easedProgress);
                }
                if (!Float.isNaN(toScaleY)) {
                    g.setScaleY(startScaleY + (toScaleY - startScaleY) * easedProgress);
                }
                if (!Float.isNaN(toRotation)) {
                    g.setRotation(startRotation + (toRotation - startRotation) * easedProgress);
                }
            } else if (target instanceof Glyph3D) {
                Glyph3D g3d = (Glyph3D) target;
                if (!Float.isNaN(toX)) {
                    g3d.position.x = startX + (toX - startX) * easedProgress;
                }
                if (!Float.isNaN(toY)) {
                    g3d.position.y = startY + (toY - startY) * easedProgress;
                }
                if (!Float.isNaN(toZ)) {
                    g3d.position.z = startZ + (toZ - startZ) * easedProgress;
                }
                if (!Float.isNaN(toOpacity)) {
                    com.badlogic.gdx.graphics.Color c = g3d.decal.getColor();
                    g3d.decal.setColor(c.r, c.g, c.b, startOpacity + (toOpacity - startOpacity) * easedProgress);
                }
                if (!Float.isNaN(toScaleX)) {
                    g3d.dimension.x = startScaleX + (toScaleX - startScaleX) * easedProgress;
                }
                if (!Float.isNaN(toScaleY)) {
                    g3d.dimension.y = startScaleY + (toScaleY - startScaleY) * easedProgress;
                }
                if (toRotation3D != null) {
                    g3d.rotation.set(startRotation3D).lerp(toRotation3D, easedProgress);
                }
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

        public Object getAnimatedObject() {
            return target;
        }


        @Override
        public void reset() {
            target = null;
            easing = Easing.LINEAR;
            duration = 0;
            time = 0;

            startX = startY = startZ = toX = toY = toZ = Float.NaN;
            startOpacity = toOpacity = Float.NaN;
            startScaleX = startScaleY = toScaleX = toScaleY = Float.NaN;
            startRotation = toRotation = Float.NaN;
            startRotation3D = null;
            toRotation3D = null;

            isPulseAnimation = false;
            bpm = 0;
            magnitude = 0;
        }
    }
}
