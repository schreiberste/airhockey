/*
 * Created on 25.04.2010
 *
 */
package de.steffens.airhockey.view;


import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import de.steffens.airhockey.model.Wall;
import de.steffens.airhockey.model.vector.Vector2D;

/**
 * OpenGL rendering code for {@link Wall walls}.
 *
 * @author Steffen Schreiber
 */
public class GLWall extends GLRenderable {

    /**
     * show debugging elements for infinite walls
     */
    public final static boolean DEBUG_INFINITE_WALLS = false;
    private final Wall wallModel;


    /**
     * Create a new GL representation of the given wall model.
     *
     * @param wallModel the wall model
     */
    public GLWall(Wall wallModel) {
        super(wallModel);
        Material gdxMat = GLMaterial.getGdxMaterial(wallModel.getMaterial());

        // build the wall mesh model
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshBuilder;
        meshBuilder = modelBuilder.part("wall", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, gdxMat);
        build(meshBuilder, wallModel);
        meshModel = modelBuilder.end();

        instance = new ModelInstance(meshModel);
        material = instance.materials.get(0);
        this.wallModel = wallModel;
    }


    /**
     * @param wallModel the wall model
     */
    private void build(MeshPartBuilder builder, Wall wallModel) {
        float height = (float) wallModel.getHeight();
        Wall.Face[] face = wallModel.getWallFaces();
        Vector2D[] coords = wallModel.getCoords();

        // the wall front face itself
        builder.rect(
            toVector3(coords[0], 0f),
            toVector3(coords[0], height),
            toVector3(coords[1], height),
            toVector3(coords[1], 0f),
            toVector3(face[0].getNormalVector(), 0f));

        // the wall back side
        builder.rect(
            toVector3(coords[2], 0f),
            toVector3(coords[2], height),
            toVector3(coords[3], height),
            toVector3(coords[3], 0f),
            toVector3(face[2].getNormalVector(), 0f));

        // the wall top side
        builder.rect(
            toVector3(coords[3], height),
            toVector3(coords[2], height),
            toVector3(coords[1], height),
            toVector3(coords[0], height),
            new Vector3(0f, 0f, 1f));

        // the wall bottom side
        builder.rect(
            toVector3(coords[0], 0f),
            toVector3(coords[1], 0f),
            toVector3(coords[2], 0f),
            toVector3(coords[3], 0f),
            new Vector3(0f, 0f, -11f));

        // the wall start face
        builder.rect(
            toVector3(coords[0], 0f),
            toVector3(coords[3], 0f),
            toVector3(coords[3], height),
            toVector3(coords[0], height),
            toVector3(face[3].getNormalVector(), 0f));

        // the wall end face
        builder.rect(
            toVector3(coords[2], 0f),
            toVector3(coords[1], 0f),
            toVector3(coords[1], height),
            toVector3(coords[2], height),
            toVector3(face[1].getNormalVector(), 0f));
        //
        //
        //        if (DEBUG_INFINITE_WALLS) {
        //            if (wallModel.isInfinite()) {
        //                // show the infinite wall face
        //                gl.glEnable(GL.GL_BLEND);
        //                gl.glDisable(GL.GL_DEPTH_TEST);
        //                gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
        //
        //                GLMaterial.applyMaterial(gl, Material.debugMaterial.getBlended(0.4f));
        //
        //                Vector2D faceVect = face[0].getFaceVector().getNormalized();
        //                Vector2D p1 = coords[0].addMultiple(faceVect, -10);
        //                Vector2D p2 = coords[1].addMultiple(faceVect, +10);
        //
        //                gl.glBegin(GL.GL_QUADS);
        //                  x1 = p1.getX();
        //                  y1 = p1.getY();
        //                  x2 = p2.getX();
        //                  y2 = p2.getY();
        //                  gl.glNormal3d(face[0].getNormalVector().getX(),
        //                          face[0].getNormalVector().getY(), 0.0);
        //                  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(x1, y1, z1);
        //                  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(x1, y1, z2);
        //                  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(x2, y2, z2);
        //                  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(x2, y2, z1);
        //                  gl.glNormal3d(-face[0].getNormalVector().getX(),
        //                          -face[0].getNormalVector().getY(), 0.0);
        //                  gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3d(x1, y1, z1);
        //                  gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3d(x2, y2, z1);
        //                  gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3d(x2, y2, z2);
        //                  gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3d(x1, y1, z2);
        //                gl.glEnd();
        //
        //                gl.glDepthMask(true);
        //                gl.glDisable(GL.GL_BLEND);
        //                gl.glEnable(GL.GL_DEPTH_TEST);
        //            }
        //        }
    }
}
