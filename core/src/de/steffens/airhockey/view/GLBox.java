package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import de.steffens.airhockey.model.Material;

/**
 * Class that is rendering a simple box.
 * 
 * @author Steffen Schreiber
 */
public class GLBox extends GLRenderable {


    public GLBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        ModelBuilder modelBuilder = new ModelBuilder();
        float w = x2-x1;
        float d = y2-y1;
        float h = z2-z1;
        meshModel = modelBuilder.createBox(w, d, h,
                GLMaterial.getGdxMaterial(Material.defaultMaterial),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        // the box instance holds location, rotation, scale
        instance = new ModelInstance(meshModel, x1 + w*0.5f, y1 + d*0.5f, z1 + h*0.5f);
        material = instance.materials.get(0);
    }

    public void setMaterial(Material material) {
        GLMaterial.applyMaterial(instance, material);
    }

    @Override
    public void render(ModelBatch modelBatch, Environment environment, boolean reflection) {
        modelBatch.render(instance, environment);
    }

}
