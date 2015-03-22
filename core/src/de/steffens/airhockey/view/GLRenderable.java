/*
 * Created on 25.04.2010
 *
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.math.Vector3;

import de.steffens.airhockey.model.VisualObject;
import de.steffens.airhockey.model.vector.Vector2D;

/**
 * Common interface for things that can be rendered to an OpenGL
 * rendering target
 */
abstract public class GLRenderable implements GLViewer {

    protected VisualObject visualObject;
    protected Model meshModel;
    protected ModelInstance instance;
    protected Material material;

    protected BlendingAttribute enableBlend;
    protected BlendingAttribute disableBlend;
    protected DepthTestAttribute disableDepthBufferWrites;
    protected DepthTestAttribute enableDepthBufferWrites;

    public GLRenderable() {
        this(null);
    }

    public GLRenderable(VisualObject object) {
        this.visualObject = object;
        enableBlend = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        disableDepthBufferWrites = new DepthTestAttribute(false);
    }

    /**
     * Render this object with the given model batch.
     * The reflection argument can be used to render a simplified version of the object,
     * if it is only rendered to show the reflection of the real object. Some implementations
     * might simply ignore this flag, others could skip rendering completely for reflections.
     *
     * @param modelBatch the model batch used for rendering
     * @param reflection render the object as a reflection
     */
    public void render(ModelBatch modelBatch, Environment environment, boolean reflection) {
        update();
        if (visualObject != null) {
            GLMaterial.applyMaterial(instance, visualObject.getMaterial());
            float alpha = visualObject.getMaterial().getAlpha();
            if (alpha < 0.99f) {
                if (reflection) {
                    return;
                }
                // enable alpha blending
                enableBlend.opacity = alpha;
                material.set(enableBlend);
                // disable depth buffer writes...
                material.set(disableDepthBufferWrites);
            } else {
                material.remove(BlendingAttribute.Type | DepthTestAttribute.Type);
            }
        }
        modelBatch.render(instance, environment);
    }


    public Vector3 toVector3 (Vector2D vec, float z) {
        return new Vector3((float)vec.getX(), (float)vec.getY(), z);
    }

    public void dispose() {
        if (meshModel != null) {
            meshModel.dispose();
            meshModel = null;
        }
    }

    @Override
    public void update() {
        // nothing done here, may be overridden in sub-class
    }

}
