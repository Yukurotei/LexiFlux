package it.yuruni.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Interpolation;

public class AudioEffectManager {

    private final Music muffledTrack;
    private final Music clearTrack;

    private float transitionDuration;
    private float transitionTime;
    private float startVolume;
    private float endVolume;
    private boolean isTransitioning = false;

    public AudioEffectManager(FileHandle muffledFile, FileHandle clearFile) {
        this.muffledTrack = Gdx.audio.newMusic(muffledFile);
        this.clearTrack = Gdx.audio.newMusic(clearFile);
    }

    public void startTransition(float duration, float startVolume, float endVolume, float position) {
        this.transitionDuration = duration;
        this.startVolume = startVolume;
        this.endVolume = endVolume;
        this.transitionTime = 0;
        this.isTransitioning = true;

        muffledTrack.setVolume(startVolume);
        clearTrack.setVolume(0);

        muffledTrack.setLooping(true);
        clearTrack.setLooping(true);

        muffledTrack.play();
        clearTrack.play();

        muffledTrack.setPosition(position);
        clearTrack.setPosition(position);
    }

    public void update(float delta) {
        if (!isTransitioning) {
            return;
        }

        transitionTime += delta;
        float progress = Math.min(1f, transitionTime / transitionDuration);

        float easedProgress = Interpolation.pow2In.apply(progress);

        float overallVolume = startVolume + (endVolume - startVolume) * easedProgress;

        //Crossfade
        muffledTrack.setVolume((1 - easedProgress) * overallVolume);
        clearTrack.setVolume(easedProgress * overallVolume);

        if (progress >= 1f) {
            muffledTrack.stop();
            clearTrack.setVolume(endVolume);
            isTransitioning = false;
        }
    }

    public Music getClearTrack() {
        return clearTrack;
    }

    public Music getMuffledTrack() {
        return muffledTrack;
    }

    public void dispose() {
        muffledTrack.dispose();
        clearTrack.dispose();
    }
}
