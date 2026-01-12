package it.yuruni.graphics.effects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import it.yuruni.Utils;
import it.yuruni.graphics.Easing;

public class CameraManager {
    private final Camera camera;
    private final boolean is3D;

    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    private float shakeInterval = 0f;
    private float intervalTimer = 0f;
    private float currentShakeX = 0f;
    private float currentShakeY = 0f;
    private float currentShakeZ = 0f; // For 3D shake

    private final Array<CameraAnimation> rotationAnimations = new Array<>();
    private final Pool<CameraAnimation> rotationAnimationPool = new Pool<CameraAnimation>() {
        @Override
        protected CameraAnimation newObject() {
            return new CameraAnimation();
        }
    };

    private final Array<CameraPositionAnimation> positionAnimations = new Array<>();
    private final Pool<CameraPositionAnimation> positionAnimationPool = new Pool<CameraPositionAnimation>() {
        @Override
        protected CameraPositionAnimation newObject() {
            return new CameraPositionAnimation();
        }
    };

    private float currentRotation = 0f;

    // 3D-specific rotation tracking
    private final Vector3 currentEulerRotation = new Vector3(); // pitch, yaw, roll

    // 2D Constructor (backward compatible)
    public CameraManager(OrthographicCamera camera) {
        this.camera = camera;
        this.is3D = false;
    }

    // 3D Constructor
    public CameraManager(PerspectiveCamera camera) {
        this.camera = camera;
        this.is3D = true;
    }

    /**
     * Applies a screen shake effect.
     *
     * @param duration       How long the shake should last, in seconds.
     * @param intensity      The maximum pixel offset of the shake.
     * @param intervalMillis The time between shake position changes, in milliseconds.
     */
    public void shake(float duration, float intensity, float intervalMillis) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
        this.shakeInterval = intervalMillis / 1000f;
        this.intervalTimer = 0;
    }

    /**
     * 2D ONLY: Animates the camera's rotation TO a specific angle.
     * Any existing rotation animation will be stopped.
     */
    public void setRotation(float degrees, float durationMillis, Easing easing) {
        if (is3D) {
            throw new UnsupportedOperationException("Use setRotation3D for 3D cameras");
        }
        clearRotationAnimations();

        CameraAnimation anim = rotationAnimationPool.obtain();
        anim.init2D(currentRotation, degrees, durationMillis / 1000f, easing);
        rotationAnimations.add(anim);
    }

    /**
     * 2D ONLY: Animates the camera's rotation BY a relative angle.
     * Any existing rotation animation will be stopped.
     */
    public void rotate(float degrees, float durationMillis, Easing easing) {
        if (is3D) {
            throw new UnsupportedOperationException("Use rotate3D for 3D cameras");
        }
        clearRotationAnimations();

        CameraAnimation anim = rotationAnimationPool.obtain();
        anim.init2D(currentRotation, currentRotation + degrees, durationMillis / 1000f, easing);
        rotationAnimations.add(anim);
    }

    /**
     * 3D ONLY: Animates the camera's rotation TO specific euler angles (pitch, yaw, roll).
     * Any existing rotation animation will be stopped.
     */
    public void setRotation3D(float pitch, float yaw, float roll, float durationMillis, Easing easing) {
        if (!is3D) {
            throw new UnsupportedOperationException("Use setRotation for 2D cameras");
        }
        clearRotationAnimations();

        CameraAnimation anim = rotationAnimationPool.obtain();
        anim.init3D(currentEulerRotation.x, currentEulerRotation.y, currentEulerRotation.z,
            pitch, yaw, roll, durationMillis / 1000f, easing);
        rotationAnimations.add(anim);
    }

    /**
     * 3D ONLY: Animates the camera's rotation BY relative euler angles (pitch, yaw, roll).
     * Any existing rotation animation will be stopped.
     */
    public void rotate3D(float pitchDelta, float yawDelta, float rollDelta, float durationMillis, Easing easing) {
        if (!is3D) {
            throw new UnsupportedOperationException("Use rotate for 2D cameras");
        }
        clearRotationAnimations();

        CameraAnimation anim = rotationAnimationPool.obtain();
        anim.init3D(currentEulerRotation.x, currentEulerRotation.y, currentEulerRotation.z,
            currentEulerRotation.x + pitchDelta,
            currentEulerRotation.y + yawDelta,
            currentEulerRotation.z + rollDelta,
            durationMillis / 1000f, easing);
        rotationAnimations.add(anim);
    }

    /**
     * 2D ONLY: Animates the camera position TO specific coordinates.
     * Any existing position animation will be stopped.
     */
    public void setPosition(float x, float y, float durationMillis, Easing easing) {
        if (is3D) {
            throw new UnsupportedOperationException("Use setPosition3D for 3D cameras");
        }
        clearPositionAnimations();

        CameraPositionAnimation anim = positionAnimationPool.obtain();
        anim.init2D(camera.position.x, camera.position.y, x, y, durationMillis / 1000f, easing);
        positionAnimations.add(anim);
    }

    /**
     * 2D ONLY: Animates the camera position BY a relative offset.
     * Any existing position animation will be stopped.
     */
    public void moveTo(float dx, float dy, float durationMillis, Easing easing) {
        if (is3D) {
            throw new UnsupportedOperationException("Use moveTo3D for 3D cameras");
        }
        clearPositionAnimations();

        CameraPositionAnimation anim = positionAnimationPool.obtain();
        anim.init2D(camera.position.x, camera.position.y,
            camera.position.x + dx, camera.position.y + dy,
            durationMillis / 1000f, easing);
        positionAnimations.add(anim);
    }

    /**
     * 3D ONLY: Animates the camera position TO specific coordinates.
     * Any existing position animation will be stopped.
     */
    public void setPosition3D(float x, float y, float z, float durationMillis, Easing easing) {
        if (!is3D) {
            throw new UnsupportedOperationException("Use setPosition for 2D cameras");
        }
        clearPositionAnimations();

        CameraPositionAnimation anim = positionAnimationPool.obtain();
        anim.init3D(camera.position.x, camera.position.y, camera.position.z,
            x, y, z, durationMillis / 1000f, easing);
        positionAnimations.add(anim);
    }

    /**
     * 3D ONLY: Animates the camera position BY a relative offset.
     * Any existing position animation will be stopped.
     */
    public void moveTo3D(float dx, float dy, float dz, float durationMillis, Easing easing) {
        if (!is3D) {
            throw new UnsupportedOperationException("Use moveTo for 2D cameras");
        }
        clearPositionAnimations();

        CameraPositionAnimation anim = positionAnimationPool.obtain();
        anim.init3D(camera.position.x, camera.position.y, camera.position.z,
            camera.position.x + dx, camera.position.y + dy, camera.position.z + dz,
            durationMillis / 1000f, easing);
        positionAnimations.add(anim);
    }

    private void clearRotationAnimations() {
        for (CameraAnimation anim : rotationAnimations) {
            rotationAnimationPool.free(anim);
        }
        rotationAnimations.clear();
    }

    private void clearPositionAnimations() {
        for (CameraPositionAnimation anim : positionAnimations) {
            positionAnimationPool.free(anim);
        }
        positionAnimations.clear();
    }

    /**
     * Updates all active camera effects (shake, rotation, and position).
     */
    public void update(float delta) {
        updatePosition(delta);
        updateRotation(delta);
        updateShake(delta);
    }

    /**
     * Applies transient, per-frame effects like screenshake.
     * Call this immediately before rendering your scene.
     */
    public void applyEffects() {
        if (is3D) {
            camera.translate(currentShakeX, currentShakeY, currentShakeZ);
        } else {
            ((OrthographicCamera) camera).translate(currentShakeX, currentShakeY);
        }
        camera.update();
    }

    /**
     * Resets transient, per-frame effects like screenshake.
     * Call this immediately after rendering your scene.
     */
    public void resetEffects() {
        if (is3D) {
            camera.translate(-currentShakeX, -currentShakeY, -currentShakeZ);
        } else {
            ((OrthographicCamera) camera).translate(-currentShakeX, -currentShakeY);
        }
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
                currentShakeZ = 0;
            } else {
                if (intervalTimer <= 0) {
                    intervalTimer = shakeInterval;
                    currentShakeX = MathUtils.random(-1f, 1f) * shakeIntensity;
                    currentShakeY = MathUtils.random(-1f, 1f) * shakeIntensity;
                    if (is3D) {
                        currentShakeZ = MathUtils.random(-1f, 1f) * shakeIntensity;
                    }
                }
            }
        }
    }

    private void updateRotation(float delta) {
        if (rotationAnimations.size == 0) return;

        for (int i = rotationAnimations.size - 1; i >= 0; i--) {
            CameraAnimation anim = rotationAnimations.get(i);
            if (anim.isFinished()) {
                rotationAnimations.removeIndex(i);
                rotationAnimationPool.free(anim);
            } else {
                if (is3D) {
                    Vector3 lastRot = new Vector3(anim.getCurrentRotation3D());
                    anim.update(delta);
                    Vector3 newRot = anim.getCurrentRotation3D();

                    // Calculate delta and apply rotation
                    float pitchDelta = newRot.x - lastRot.x;
                    float yawDelta = newRot.y - lastRot.y;
                    float rollDelta = newRot.z - lastRot.z;

                    camera.direction.rotate(camera.up, yawDelta);
                    camera.direction.rotate(camera.direction.cpy().crs(camera.up).nor(), pitchDelta);
                    camera.up.rotate(camera.direction, rollDelta);

                    currentEulerRotation.set(newRot);
                } else {
                    float lastRotation = anim.getCurrentRotation2D();
                    anim.update(delta);
                    float newRotation = anim.getCurrentRotation2D();
                    float diff = newRotation - lastRotation;
                    ((OrthographicCamera) camera).rotate(diff);
                    currentRotation = newRotation;
                }
            }
        }
        camera.update();
    }

    private void updatePosition(float delta) {
        if (positionAnimations.size == 0) return;

        for (int i = positionAnimations.size - 1; i >= 0; i--) {
            CameraPositionAnimation anim = positionAnimations.get(i);
            if (anim.isFinished()) {
                positionAnimations.removeIndex(i);
                positionAnimationPool.free(anim);
            } else {
                anim.update(delta);
                if (is3D) {
                    Vector3 pos = anim.getCurrentPosition3D();
                    camera.position.set(pos);
                } else {
                    camera.position.x = anim.getCurrentPosition2D().x;
                    camera.position.y = anim.getCurrentPosition2D().y;
                }
            }
        }
        camera.update();
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean is3D() {
        return is3D;
    }

    private static class CameraAnimation implements Pool.Poolable {
        private boolean is3D;
        private Easing easing;
        private float duration;
        private float time;

        // 2D rotation
        private float startRotation, toRotation;
        private float currentRotation;

        // 3D rotation (euler angles: pitch, yaw, roll)
        private final Vector3 startRotation3D = new Vector3();
        private final Vector3 toRotation3D = new Vector3();
        private final Vector3 currentRotation3D = new Vector3();

        public void init2D(float startRotation, float toRotation, float durationSeconds, Easing easing) {
            this.is3D = false;
            this.startRotation = startRotation;
            this.toRotation = toRotation;
            this.duration = durationSeconds;
            this.easing = easing;
            this.time = 0;
            this.currentRotation = startRotation;
        }

        public void init3D(float startPitch, float startYaw, float startRoll,
                           float toPitch, float toYaw, float toRoll,
                           float durationSeconds, Easing easing) {
            this.is3D = true;
            this.startRotation3D.set(startPitch, startYaw, startRoll);
            this.toRotation3D.set(toPitch, toYaw, toRoll);
            this.duration = durationSeconds;
            this.easing = easing;
            this.time = 0;
            this.currentRotation3D.set(startRotation3D);
        }

        public void update(float delta) {
            if (isFinished()) return;
            time = Math.min(duration, time + delta);

            float progress = (duration == 0) ? 1f : time / duration;
            float easedProgress = Utils.applyEasing(progress, this.easing);

            if (is3D) {
                currentRotation3D.x = startRotation3D.x + (toRotation3D.x - startRotation3D.x) * easedProgress;
                currentRotation3D.y = startRotation3D.y + (toRotation3D.y - startRotation3D.y) * easedProgress;
                currentRotation3D.z = startRotation3D.z + (toRotation3D.z - startRotation3D.z) * easedProgress;
            } else {
                currentRotation = startRotation + (toRotation - startRotation) * easedProgress;
            }
        }

        public float getCurrentRotation2D() {
            return currentRotation;
        }

        public Vector3 getCurrentRotation3D() {
            return currentRotation3D;
        }

        public boolean isFinished() {
            return time >= duration;
        }

        @Override
        public void reset() {
            is3D = false;
            easing = Easing.LINEAR;
            duration = time = 0;
            startRotation = toRotation = currentRotation = 0;
            startRotation3D.set(0, 0, 0);
            toRotation3D.set(0, 0, 0);
            currentRotation3D.set(0, 0, 0);
        }
    }

    private static class CameraPositionAnimation implements Pool.Poolable {
        private boolean is3D;
        private Easing easing;
        private float duration;
        private float time;

        // 2D position
        private final Vector3 startPosition2D = new Vector3();
        private final Vector3 toPosition2D = new Vector3();
        private final Vector3 currentPosition2D = new Vector3();

        // 3D position (reusing same vectors)
        private final Vector3 startPosition3D = new Vector3();
        private final Vector3 toPosition3D = new Vector3();
        private final Vector3 currentPosition3D = new Vector3();

        public void init2D(float startX, float startY, float toX, float toY,
                           float durationSeconds, Easing easing) {
            this.is3D = false;
            this.startPosition2D.set(startX, startY, 0);
            this.toPosition2D.set(toX, toY, 0);
            this.duration = durationSeconds;
            this.easing = easing;
            this.time = 0;
            this.currentPosition2D.set(startPosition2D);
        }

        public void init3D(float startX, float startY, float startZ,
                           float toX, float toY, float toZ,
                           float durationSeconds, Easing easing) {
            this.is3D = true;
            this.startPosition3D.set(startX, startY, startZ);
            this.toPosition3D.set(toX, toY, toZ);
            this.duration = durationSeconds;
            this.easing = easing;
            this.time = 0;
            this.currentPosition3D.set(startPosition3D);
        }

        public void update(float delta) {
            if (isFinished()) return;
            time = Math.min(duration, time + delta);

            float progress = (duration == 0) ? 1f : time / duration;
            float easedProgress = Utils.applyEasing(progress, this.easing);

            if (is3D) {
                currentPosition3D.x = startPosition3D.x + (toPosition3D.x - startPosition3D.x) * easedProgress;
                currentPosition3D.y = startPosition3D.y + (toPosition3D.y - startPosition3D.y) * easedProgress;
                currentPosition3D.z = startPosition3D.z + (toPosition3D.z - startPosition3D.z) * easedProgress;
            } else {
                currentPosition2D.x = startPosition2D.x + (toPosition2D.x - startPosition2D.x) * easedProgress;
                currentPosition2D.y = startPosition2D.y + (toPosition2D.y - startPosition2D.y) * easedProgress;
            }
        }

        public Vector3 getCurrentPosition2D() {
            return currentPosition2D;
        }

        public Vector3 getCurrentPosition3D() {
            return currentPosition3D;
        }

        public boolean isFinished() {
            return time >= duration;
        }

        @Override
        public void reset() {
            is3D = false;
            easing = Easing.LINEAR;
            duration = time = 0;
            startPosition2D.set(0, 0, 0);
            toPosition2D.set(0, 0, 0);
            currentPosition2D.set(0, 0, 0);
            startPosition3D.set(0, 0, 0);
            toPosition3D.set(0, 0, 0);
            currentPosition3D.set(0, 0, 0);
        }
    }
}
