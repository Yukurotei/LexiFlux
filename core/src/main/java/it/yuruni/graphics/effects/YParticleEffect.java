package it.yuruni.graphics.effects;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import it.yuruni.Main;

public class YParticleEffect extends ParticleEffect {

    private boolean isEnabled = false;

    public YParticleEffect(boolean addToUpdateQueue) {
        super();
        if (addToUpdateQueue) {
            Main.particles.add(this);
        }
    }

    @Override
    public void start() {
        super.start();
        isEnabled = true;
    }

    public boolean started() {
        return isEnabled;
    }

    @Override
    public void update(float delta) {
        if (isComplete()) {
            isEnabled = false;
        }
        if (isEnabled) super.update(delta);
    }
}
