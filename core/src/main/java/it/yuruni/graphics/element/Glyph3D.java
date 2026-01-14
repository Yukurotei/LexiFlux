package it.yuruni.graphics.element;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class Glyph3D {

    // --- Static Fields for Management ---
    private static final ArrayList<Glyph3D> instances = new ArrayList<>();

    /**
     * Updates and renders all managed Glyph3D instances.
     * @param cam The perspective camera to billboard towards.
     * @param decalBatch The DecalBatch to render with.
     */
    public static void updateAndRenderAll(PerspectiveCamera cam, DecalBatch decalBatch) {
        for (Glyph3D glyph : instances) {
            if (glyph.isVisible) {
                glyph.update(cam);
                decalBatch.add(glyph.decal);
            }
        }
    }

    /**
     * Clears all instances from the manager. Call this when changing screens.
     */
    public static void clearInstances() {
        instances.clear();
    }


    // --- Instance Fields ---
    public final Decal decal;
    public Vector3 position;
    public Vector3 rotation;
    public Vector2 dimension;
    public boolean isVisible = true;


    /**
     * Creates a 3DGlyph from an existing TextureRegion. The texture is NOT owned by this object.
     */
    public Glyph3D(TextureRegion textureRegion, Vector3 position, boolean addToQueue) {
        this.decal = Decal.newDecal(textureRegion, true);

        this.position = position;
        this.rotation = new Vector3();
        this.dimension = new Vector2(textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        this.decal.setDimensions(this.dimension.x, this.dimension.y);
        this.decal.setPosition(this.position);

        if (addToQueue) {
            instances.add(this);
        }
    }

    public Glyph3D(Texture texture, Vector3 position, boolean addToQueue) {
        this(new TextureRegion(texture), position, addToQueue);
    }

    /**
     * Updates the internal Decal to face the camera and match the object's properties.
     * @param cam The camera to look at.
     */
    public void update(PerspectiveCamera cam) {
        decal.setPosition(this.position);
        decal.setRotation(this.rotation.x, this.rotation.y, this.rotation.z);
        decal.setDimensions(this.dimension.x, this.dimension.y);
        if (cam != null) decal.lookAt(cam.position, cam.up);
    }
}
