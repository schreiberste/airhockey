/*
 * Created on 26.04.2010
 *
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * OpenGL rendering code for rendering {@link Disk disks}.
 *
 * @author Steffen Schreiber
 */
public class GLDisk extends GLRenderable {

    private final static boolean SHOW_DEBUG_VECTORS = false;

    /**
     * number of slices for the generated cylinder
     */
    private final static int NR_SLICES = 25;

    private final Disk model;

    ColorAttribute[] markerColors;



    /**
     * Create a new disk renderer for the given disk model.
     *
     * @param model the disk model
     */
    public GLDisk(Disk model) {
        super(model);
        this.model = model;

        float width = (float) (model.getRadius() * 2.0);
        float height = (float) model.getHeight();
        float markerWidth = width * 0.75f;

        ModelBuilder modelBuilder = new ModelBuilder();
//        meshModel = modelBuilder.createCylinder(
//            width, height, depth, NR_SLICES,
//            GLMaterial.getGdxMaterial(model.getMaterial()),
//            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
//
        modelBuilder.begin();

        MeshPartBuilder cylinderPart = modelBuilder.part("cylinder",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            GLMaterial.getGdxMaterial(model.getMaterial()));
        cylinderPart.cylinder(width, height, width, NR_SLICES);

        // should we add a marker?
        if (!model.isFixed() || model.getLastHitPlayerIndex() >= 0) {
            float[] initCol = model.getMaterial().getDiffuse();
            markerColors = new ColorAttribute[] {
                ColorAttribute.createDiffuse(initCol[0],  initCol[1], initCol[2], 1f),
                ColorAttribute.createSpecular(initCol[0],  initCol[1], initCol[2], 1f)
            };
            MeshPartBuilder markerPart = modelBuilder.part("marker",
                GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                GLMaterial.getGdxMaterial(model.getMaterial()));
            markerPart.cylinder(markerWidth, height * 1.2f, markerWidth, NR_SLICES);
        }

        meshModel = modelBuilder.end();


        instance = new ModelInstance(meshModel);
        material = instance.materials.get(0);
    }


    @Override
    public void update() {
        super.update();
        Vector2D position = model.getPosition();
        instance.transform.setToTranslation((float) position.getX(), (float) position.getY(), (float) (model.getHeight() / 2.0));
        instance.transform.rotate(1f, 0f, 0f, 90f);

        // update marker color
        short playerIndex = model.getLastHitPlayerIndex();
        if (playerIndex >= 0) {
            float[] playerCol = Game.getPlayer(playerIndex).getColor();
            markerColors[0].color.set(playerCol[0],  playerCol[1], playerCol[2], 1f);
            markerColors[1].color.set(playerCol[0],  playerCol[1], playerCol[2], 1f);
            instance.nodes.get(0).parts.get(1).material.set(markerColors);
        }
    }


    //    /**
    //     * Draw the disk at position (0,0).
    //     *
    //     * @param gl
    //     * @param model
    //     * @param nrSlices
    //     */
    //    private static void drawDisk(GL20 gl, Disk model, int nrSlices) {
    //        double radius = model.getRadius();
    //        double radius2 = radius * 0.95;
    //        double radius3 = radius * 0.8;
    //        double radius4 = radius * 0.75;
    //        double height = model.getHeight();
    //        double height1 = height * 0.1;
    //        double height2 = height * 0.8;
    //        double height3 = height * 0.1;
    //
    //        // bottom cap
    //        glu.gluDisk(quadric, 0, radius4, nrSlices, 1);
    //        // bottom edge
    //        glu.gluCylinder(quadric, radius2, radius, height1, nrSlices, 1);
    //        // the cylinder surface
    //        gl.glTranslated(0, 0, height1);
    //        glu.gluCylinder(quadric, radius, radius, height2, nrSlices, 1);
    //        gl.glTranslated(0, 0, height2);
    //        // outer edge
    //        glu.gluCylinder(quadric, radius, radius2, height3, nrSlices, 1);
    //        // thin ring
    //        gl.glTranslated(0, 0, height3);
    //        glu.gluDisk(quadric, radius3, radius2, nrSlices, 1);
    //        // inner edge
    //        glu.gluCylinder(quadric, -radius3, -radius4, -height3, nrSlices, 1);
    //        // the top cap
    //        gl.glTranslated(0, 0, -height3);
    //        glu.gluDisk(quadric, 0, radius4, nrSlices, 1);
    //    }
}
