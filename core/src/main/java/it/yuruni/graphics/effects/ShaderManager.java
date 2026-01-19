package it.yuruni.graphics.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.HashMap;
import java.util.Map;

public class ShaderManager {

    private FrameBuffer fbo;
    private final SpriteBatch batch;
    private final OrthographicCamera screenCamera;

    private Map<String, ShaderProgram> customShaders;
    private ShaderProgram activeShader;
    private float activeShaderDuration;
    private float activeShaderTime; // Time since active shader was activated

    // Punch effect specific variables, now managed as a regular shader property
    private float punchIntensity = 0f;
    private ShaderProgram punchShader; // Keep a reference to punchShader specifically


    public ShaderManager() {
        batch = new SpriteBatch();
        screenCamera = new OrthographicCamera();
        customShaders = new HashMap<>();

        // Load the default "punch" shader
        punchShader = new ShaderProgram(Gdx.files.internal("shaders/punch.vert"), Gdx.files.internal("shaders/punch.frag"));
        if (!punchShader.isCompiled()) {
            Gdx.app.error("PunchShader", "compilation failed:\n" + punchShader.getLog());
            punchShader = null; // Mark as invalid
        }
        if (punchShader != null) {
            customShaders.put("punch", punchShader);
        }
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (fbo != null) {
            fbo.dispose();
        }
        // Create FBO with stencil buffer for masking operations
        GLFrameBuffer.FrameBufferBuilder builder = new GLFrameBuffer.FrameBufferBuilder(width, height);
        builder.addBasicColorTextureAttachment(Pixmap.Format.RGBA8888);
        builder.addDepthRenderBuffer(GL20.GL_DEPTH_COMPONENT16);
        builder.addStencilRenderBuffer(GL20.GL_STENCIL_INDEX8);
        fbo = builder.build();
        Gdx.app.log("ShaderManager", "Created FBO with stencil buffer: " + width + "x" + height);
        screenCamera.setToOrtho(false, width, height);
    }

    /**
     * Loads a custom shader and makes it available by name.
     * @param name The unique name for this shader.
     * @param vertPath The path to the vertex shader file.
     * @param fragPath The path to the fragment shader file.
     */
    public void loadShader(String name, String vertPath, String fragPath) {
        if (customShaders.containsKey(name)) {
            Gdx.app.error("ShaderManager", "Shader '" + name + "' already loaded. Disposing old one.");
            customShaders.get(name).dispose();
        }

        ShaderProgram newShader = new ShaderProgram(Gdx.files.internal(vertPath), Gdx.files.internal(fragPath));
        if (!newShader.isCompiled()) {
            Gdx.app.error("ShaderManager", "Shader '" + name + "' compilation failed:\n" + newShader.getLog());
            newShader.dispose(); // Dispose the failed shader
        } else {
            customShaders.put(name, newShader);
            Gdx.app.log("ShaderManager", "Shader '" + name + "' loaded successfully.");
        }
    }

    /**
     * Activates a custom shader for post-processing.
     * @param name The name of the shader to activate.
     * @param duration The duration for which the shader should be active (0 for infinite).
     */
    public void activateShader(String name, float duration) {
        ShaderProgram shader = customShaders.get(name);
        if (shader == null) {
            Gdx.app.error("ShaderManager", "Attempted to activate unknown shader: " + name);
            return;
        }
        activeShader = shader;
        activeShaderDuration = duration;
        activeShaderTime = 0f;
        Gdx.app.log("ShaderManager", "Shader '" + name + "' activated for " + duration + "s.");
    }

    /**
     * Deactivates the currently active custom shader.
     */
    public void deactivateShader() {
        if (activeShader != null) {
            Gdx.app.log("ShaderManager", "Shader '" + getActiveShaderName() + "' deactivated.");
        }
        activeShader = null;
        activeShaderDuration = 0f;
        activeShaderTime = 0f;
        punchIntensity = 0f; // Reset punch intensity when deactivating any shader
    }

    public void setPunch(float intensity) {
        this.punchIntensity = intensity;
        // Ensure punch shader is active if intensity is set
        if (intensity > 0 && activeShader != punchShader) {
            activateShader("punch", 0f); // Activate punch shader for infinite duration
        } else if (intensity == 0 && activeShader == punchShader && activeShaderDuration == 0) {
            deactivateShader(); // Deactivate if intensity is zero and it was set for infinite
        }
    }

    private String getActiveShaderName() {
        for (Map.Entry<String, ShaderProgram> entry : customShaders.entrySet()) {
            if (entry.getValue() == activeShader) {
                return entry.getKey();
            }
        }
        return "Unknown";
    }

    private void update(float delta) {
        if (activeShader != null) {
            activeShaderTime += delta;
            if (activeShaderDuration > 0 && activeShaderTime >= activeShaderDuration) {
                deactivateShader();
            }
        }
        // Punch specific fade out, if punch shader is active
        if (activeShader == punchShader && punchIntensity > 0) {
            punchIntensity -= delta * 4.0f; // Fades out over ~0.25 seconds
            if (punchIntensity < 0) {
                punchIntensity = 0;
            }
            if (punchIntensity == 0 && activeShaderDuration == 0) { // If infinite punch and faded out
                deactivateShader();
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

        update(delta); // Update shader values (time, punch fade)

        screenCamera.update();
        batch.setProjectionMatrix(screenCamera.combined);

        ShaderProgram shaderToUse = activeShader;

        batch.setShader(shaderToUse); // Set the shader for the batch
        batch.begin();

        if (shaderToUse != null) {
            // Set common uniforms for custom shaders
            if (shaderToUse.hasUniform("u_time")) {
                shaderToUse.setUniformf("u_time", activeShaderTime);
            }

            // Set specific uniforms for the punch shader
            if (shaderToUse == punchShader) {
                if (punchIntensity > 0) {
                    shaderToUse.setUniformf("u_punch", punchIntensity);
                } else {
                    // If punch shader is active but intensity is 0, don't draw with it
                    batch.setShader(null); // Use default shader
                }
            }
        }

        // Draw the FBO content. The batch will use the shader set above (or default).
        batch.draw(fbo.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());

        batch.end();
        batch.setShader(null); // Always reset to default after drawing
    }

    public void dispose() {
        if (fbo != null) fbo.dispose();
        for (ShaderProgram shader : customShaders.values()) {
            shader.dispose();
        }
        batch.dispose();
    }
}
