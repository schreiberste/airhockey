/*
 * Created on 05.10.2010
 *
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import de.steffens.airhockey.model.Material;
import de.steffens.airhockey.model.MaterialEffect;

/**
 * OpenGL code for rendering {@link Material materials}.
 *
 * @author Steffen Schreiber
 */
public class GLMaterial {

    private final Material material;
    private final Material materialBack;


    /**
     * Creates a new material renderer for the given material.
     * The material will apply to both, front and back faces.
     *
     * @param material the material to render
     */
    public GLMaterial(Material material) {
        this.material = material;
        this.materialBack = null;
    }


    /**
     * Creates a new material renderer for the given materials for front
     * and back face. If the back face material is <code>null</code>, the
     * front material will apply for both faces.
     *
     * @param frontFace the material to use for front face
     * @param backFace  the material to use for back face
     */
    public GLMaterial(Material frontFace, Material backFace) {
        this.material = frontFace;
        this.materialBack = backFace;
    }

    /**
     * Apply this material to the given model instance
     *
     * @param instance the model instance
     */
    //    public void applyMaterial(ModelInstance instance) {
    //        applyMaterial(instance, material, materialBack);
    //    }


    /**
     * Apply the given materials to the given GL context. The material
     * will apply for both, front and back face.
     *
     * @param instance the model instance
     * @param material the material to use for front and back face
     */
    public static void applyMaterial(ModelInstance instance, Material material) {
        applyMaterial(instance, material, null);
    }


    /**
     * Apply the given materials to the given GL context. If the back face
     * material is <code>null</code>, the front material will apply for both
     * faces.
     *
     * @param instance  the model instance
     * @param frontFace the material to use for front face
     * @param backFace  the material to use for back face
     */
    public static void applyMaterial(ModelInstance instance, Material frontFace, Material backFace) {
        if (frontFace instanceof MaterialEffect) {
            MaterialEffect effect = (MaterialEffect) frontFace;
            effect.update();
        }
        if (backFace instanceof MaterialEffect) {
            MaterialEffect effect = (MaterialEffect) backFace;
            effect.update();
        }

        if (backFace == null) {
            applyMaterial(instance, frontFace, GL20.GL_FRONT_AND_BACK);
        } else {
            applyMaterial(instance, frontFace, GL20.GL_FRONT);
            applyMaterial(instance, backFace, GL20.GL_BACK);
        }
    }


    /**
     * Apply the material to the given face.
     *
     * @param instance
     * @param material
     * @param face
     */
    private static void applyMaterial(ModelInstance instance, Material material, int face) {
        // set material colors:
        //        gl.glMaterialfv(face, GL20.GL_AMBIENT, material.getAmbient() , 0);
        //        gl.glMaterialfv(face, GL20.GL_DIFFUSE, material.getDiffuse() , 0);
        //        gl.glMaterialfv(face, GL20.GL_SPECULAR, material.getSpecular() , 0);
        //        gl.glMaterialfv(face, GL20.GL_EMISSION, material.getEmission() , 0);
        //        gl.glMaterialfv(face, GL20.GL_SHININESS, material.getShininess() , 0);

        float[] ambient = material.getAmbient();
        float[] diffuse = material.getDiffuse();
        float[] specular = material.getSpecular();
        float[] emission = material.getEmission();

        com.badlogic.gdx.graphics.g3d.Material gdxMaterial = instance.materials.get(0);
        ColorAttribute ambientAtr = (ColorAttribute) gdxMaterial.get(ColorAttribute.Ambient);
        ambientAtr.color.set(ambient[0], ambient[1], ambient[2], ambient[3]);
        ColorAttribute diffuseAtr = (ColorAttribute) gdxMaterial.get(ColorAttribute.Diffuse);
        diffuseAtr.color.set(diffuse[0], diffuse[1], diffuse[2], diffuse[3]);
        ColorAttribute specularAtr = (ColorAttribute) gdxMaterial.get(ColorAttribute.Specular);
        specularAtr.color.set(specular[0], specular[1], specular[2], specular[3]);
        ColorAttribute emissionAtr = (ColorAttribute) gdxMaterial.get(ColorAttribute.Emissive);
        emissionAtr.color.set(emission[0], emission[1], emission[2], emission[3]);
    }


    public static com.badlogic.gdx.graphics.g3d.Material getGdxMaterial(Material material) {
        float[] ambient = material.getAmbient();
        float[] diffuse = material.getDiffuse();
        float[] specular = material.getSpecular();
        float[] emission = material.getEmission();

        return new com.badlogic.gdx.graphics.g3d.Material(
            ColorAttribute.createDiffuse(diffuse[0], diffuse[1], diffuse[2], diffuse[3]),
            ColorAttribute.createAmbient(ambient[0], ambient[1], ambient[2], ambient[3]),
            ColorAttribute.createSpecular(specular[0], specular[1], specular[2], specular[3]),
            new ColorAttribute(ColorAttribute.Emissive,
                emission[0], emission[1], emission[2], emission[3])

        );
    }
}
