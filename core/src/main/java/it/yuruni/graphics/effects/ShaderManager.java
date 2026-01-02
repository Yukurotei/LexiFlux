package it.yuruni.graphics.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ScreenUtils;

public class ShaderManager {

    private FrameBuffer fbo;
    private final SpriteBatch batch;
    private final OrthographicCamera screenCamera;

    private final ShaderProgram punchShader;
    private float punchIntensity = 0f;

    public ShaderManager() {
        batch = new SpriteBatch();
        screenCamera = new OrthographicCamera();

        //TODO: TEMP HARD CODED, CHANGE THIS TO BE MORE MODULAR
        punchShader = new ShaderProgram(Gdx.files.internal("shaders/punch.vert"), Gdx.files.internal("shaders/punch.frag"));
        if (!punchShader.isCompiled()) {
            Gdx.app.error("PunchShader", "compilation failed:\n" + punchShader.getLog());
        }
    }

    public void resize(int width, int height) {
        if (fbo != null) {
            fbo.dispose();
        }
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        screenCamera.setToOrtho(false, width, height);
    }

    public void setPunch(float intensity) {
        this.punchIntensity = intensity;
    }

    private void update(float delta) {
        if (punchIntensity > 0) {
            punchIntensity -= delta * 4.0f; // Fades out over ~0.25 seconds
            if (punchIntensity < 0) {
                punchIntensity = 0;
            }
        }
    }

    public void begin() {
        if (fbo == null) return;
        fbo.begin();
        ScreenUtils.clear(0, 0, 0, 1);
    }

    public void end(float delta) {
        if (fbo == null) return;
        fbo.end();

        update(delta); // Update shader values

        screenCamera.update();
        batch.setProjectionMatrix(screenCamera.combined);

        batch.setShader(punchShader);
        batch.begin();
        punchShader.setUniformf("u_punch", punchIntensity);
        batch.draw(fbo.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
        batch.end();
        batch.setShader(null);
    }

    public void dispose() {
        if (fbo != null) fbo.dispose();
        punchShader.dispose();
        batch.dispose();
    }
}
