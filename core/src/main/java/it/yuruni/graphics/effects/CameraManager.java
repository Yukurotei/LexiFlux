package it.yuruni.graphics.effects;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import it.yuruni.Utils;
import it.yuruni.graphics.animation.Easing;

public class CameraManager {
    private final OrthographicCamera camera;

    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    private float shakeInterval = 0f;
    private float intervalTimer = 0f;
    private float currentShakeX = 0f;
    private float currentShakeY = 0f;

    private final Array<CameraAnimation> animations = new Array<>();
    private final Pool<CameraAnimation> animationPool = new Pool<CameraAnimation>() {
        @Override
        protected CameraAnimation newObject() {
            return new CameraAnimation();
        }
    };
    private float currentRotation = 0f;

    public CameraManager(OrthographicCamera camera) {
        this.camera = camera;
    }


    /**
     * Applies a screen shake effect.
     * @param duration How long the shake should last, in seconds.
     * @param intensity The maximum pixel offset of the shake.
     * @param intervalMillis The time between shake position changes, in milliseconds.
     */
    public void shake(float duration, float intensity, float intervalMillis) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
        this.shakeInterval = intervalMillis / 1000f;
        this.intervalTimer = 0;
    }

    /**
     * Animates the camera's rotation TO a specific angle.
     * Any existing rotation animation will be stopped.
     */
    public void setRotation(float degrees, float durationMillis, Easing easing) {
        // Stop any current rotation animations
        clearRotationAnimations();

        CameraAnimation anim = animationPool.obtain();
        anim.init(currentRotation, degrees, durationMillis / 1000f, easing);
        animations.add(anim);
    }

    /**
     * Animates the camera's rotation BY a relative angle.
     * Any existing rotation animation will be stopped.
     */
    public void rotate(float degrees, float durationMillis, Easing easing) {
        // Stop any current rotation animations
        clearRotationAnimations();

        CameraAnimation anim = animationPool.obtain();
        anim.init(currentRotation, currentRotation + degrees, durationMillis / 1000f, easing);
        animations.add(anim);
    }

    private void clearRotationAnimations() {
        for (CameraAnimation anim : animations) {
            animationPool.free(anim);
        }
        animations.clear();
    }

    /**
     * Updates all active camera effects (shake and rotation).
     */
    public void update(float delta) {
        updateRotation(delta);
        updateShake(delta);
    }

    /**
     * Applies transient, per-frame effects like screenshake.
     * Call this immediately before rendering your scene.
     */
    public void applyEffects() {
        camera.translate(currentShakeX, currentShakeY);
        camera.update();
    }

    /**
     * Resets transient, per-frame effects like screenshake.
     * Call this immediately after rendering your scene.
     */
    public void resetEffects() {
        camera.translate(-currentShakeX, -currentShakeY);
        camera.update();
    }


    private void updateShake(float delta) {
        if (shakeTimer > 0) {
            shakeTimer -= delta;
            intervalTimer -= delta;

            if (shakeTimer <= 0) {
                shakeTimer = 0;
                shakeIntensity = 0;
                currentShakeX = 0;
                currentShakeY = 0;
            } else {
                if (intervalTimer <= 0) {
                    intervalTimer = shakeInterval;
                    currentShakeX = MathUtils.random(-1f, 1f) * shakeIntensity;
                    currentShakeY = MathUtils.random(-1f, 1f) * shakeIntensity;
                }
            }
        }
    }

    private void updateRotation(float delta) {
        if (animations.size == 0) return;

        for (int i = animations.size - 1; i >= 0; i--) {
            CameraAnimation anim = animations.get(i);
            if (anim.isFinished()) {
                animations.removeIndex(i);
                animationPool.free(anim);
            } else {
                float lastRotation = anim.getCurrentRotation();
                anim.update(delta);
                float newRotation = anim.getCurrentRotation();
                float diff = newRotation - lastRotation;
                camera.rotate(diff);
                currentRotation = newRotation;
            }
        }
        camera.update();
    }



    private static class CameraAnimation implements Pool.Poolable {
        private Easing easing;
        private float duration;
        private float time;
        private float startRotation, toRotation;
        private float currentRotation;

        public void init(float startRotation, float toRotation, float durationSeconds, Easing easing) {
            this.startRotation = startRotation;
            this.toRotation = toRotation;
            this.duration = durationSeconds;
            this.easing = easing;
            this.time = 0;
            this.currentRotation = startRotation;
        }

        public void update(float delta) {
            if (isFinished()) return;
            time = Math.min(duration, time + delta);

            float progress = (duration == 0) ? 1f : time / duration;
            float easedProgress = Utils.applyEasing(progress, this.easing);

            currentRotation = startRotation + (toRotation - startRotation) * easedProgress;
        }

        public float getCurrentRotation() {
            return currentRotation;
        }

        public boolean isFinished() {
            return time >= duration;
        }

        @Override
        public void reset() {
            easing = Easing.LINEAR;
            duration = time = 0;
            startRotation = toRotation = currentRotation = 0;
        }
    }
}
