package de.steffens.airhockey.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import org.w3c.dom.css.Rect;

import javax.swing.text.AttributeSet;

import de.steffens.airhockey.model.Rectangle;
import de.steffens.airhockey.model.vector.Vector2D;

/**
 * OpenGL rendering code that is rendering {@link Rectangle} objects.
 *
 * @author Steffen Schreiber
 */
public class GLRectangle extends GLRenderable {


    /**
     * the rectangle data model
     */
    private Rectangle rectModel;


    /**
     * Creates a new renderer for the given rectangle data model.
     *
     * @param rectModel the rectangle model to render
     */
    public GLRectangle(Rectangle rectModel) {
        super(rectModel);
        this.rectModel = rectModel;
        Vector2D[] coords = rectModel.getCoords();

        // build the mesh model
        ModelBuilder modelBuilder = new ModelBuilder();
        meshModel = modelBuilder.createRect(
            (float) coords[0].getX(),
            (float) coords[0].getY(),
            0f,
            (float) coords[1].getX(),
            (float) coords[1].getY(),
            0f,
            (float) coords[2].getX(),
            (float) coords[2].getY(),
            0f,
            (float) coords[3].getX(),
            (float) coords[3].getY(),
            0f,
            0f, 0f, 1f,  // normal
            GLMaterial.getGdxMaterial(rectModel.getMaterial()),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        instance = new ModelInstance(meshModel);
        material = instance.materials.get(0);
    }



    @Override
    public void render(ModelBatch modelBatch, Environment environment, boolean reflection) {
        GLMaterial.applyMaterial(instance, rectModel.getMaterial());

        float mirror = rectModel.getMaterial().getMirror();
        boolean reflectingMaterial = mirror != 0.0f;
        if (reflectingMaterial) {
            // This is a reflecting material.
            // We fake reflections by rendering in two passes:
            // The first pass renders the reflections (mirrored objects along z-axis),
            // the second pass renders objects with reflective material as transparent
            // with low alpha for high reflectivity...

            // enable blending
            enableBlend.opacity = 1.0f - mirror;
            material.set(enableBlend);
            // disable depth buffer writes...
            material.set(disableDepthBufferWrites);
        } else {
            material.remove(BlendingAttribute.Type | DepthTestAttribute.Type);
        }

        // if this is the first pass (= rendering the reflections), we only draw
        // the rectangle, if it is not reflecting (in this case, it will block
        // reflected objects from appearing)
        if (reflection && reflectingMaterial) {
            // do nothing, will be rendered in second pass
            return;
        }

        if (!reflection && !reflectingMaterial) {
            // non-reflecting rectangles will be rendered in the first pass,
            // no need to render them again
            return;
        }

        //        // we are now in the right pass to actually render the rectangle.
        //        // (set the back face material to debug material to check, if we see the right side...)
        //        GLMaterial.applyMaterial(gl, mat, Material.debugMaterial);

        // if this is a reflecting material, then it will be rendered transparent, so
        // that previously rendererd reflections will show through

        modelBatch.render(instance, environment);

        //        // render the subdivided rectangle as triangle strips
        //        Vector2D[][] coords = rect.getSubdividedCoords();
        //
        //        gl.glNormal3d(0, 0, 1.0);
        //
        //        for (int row=0; row < coords.length; row++) {
        //            Vector2D[] strip = coords[row];
        //
        //            gl.glBegin(GL.GL_TRIANGLE_STRIP);
        //              for (int i=0; i<strip.length; i++) {
        //                  // TODO: set texture coordinates as well
        //                  gl.glVertex3d(strip[i].getX(), strip[i].getY(), 0.0);
        //              }
        //            gl.glEnd();
        //        }
    }
}
