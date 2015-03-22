/*
 * Created on 26.04.2010
 *
 */

package de.steffens.airhockey.model;

import de.steffens.airhockey.model.EffectLifetime.EffectFunctionType;

/**
 *
 * @author Steffen Schreiber
 */
public class MaterialEffect extends Material {

    private final VisualObject visualObject;
    private final Material originalMaterial;
    private final EffectLifetime lifetime;
    
    private final float strength;
    
    /**
     * Create a new material effect for the given visual object.
     * @param object the visual object
     * @param strength the effect strength, should be in [0..1] range
     */
    public MaterialEffect(VisualObject object, double strength) {
        super(object.getMaterial().getMaterialIdx());
        long startTime = Game.getSimulation().getSimulationTime();
        this.visualObject = object;
        this.originalMaterial = object.getMaterial();
        this.strength = (float)strength;
        
        setInitialValues();
        lifetime = new EffectLifetime(startTime, 
                EffectLifetime.DEFAULT_DURATION, 
                EffectFunctionType.LINEAR_ONE_TO_ZERO);
    }

    /**
     * Update the material effect.
     */
    public void update() {
        long time = Game.getSimulation().getSimulationTime();
        
        if (lifetime.isFinished(time)) {
            finished();
            return;
        }
        
        // effect is still running, do something
        float function = (float)lifetime.getFunctionValue(time) * strength;
        
        // example emmiting effect...
        float emmitingSummand = 0.1f * function;
        float emmitingFactor = 1.0f + function;
        float[] orgEmmision = originalMaterial.getEmission();
        emission[0] = emmitingFactor * (orgEmmision[0] + emmitingSummand);
        emission[1] = emmitingFactor * (orgEmmision[1] + emmitingSummand);
        emission[2] = emmitingFactor * (orgEmmision[2] + emmitingSummand);

        diffuse[0] = originalMaterial.diffuse[0] + function;
    }
    
    /**
     * Set the properties of this material to initial values, i.e. the
     * values of the original material.
     */
    private void setInitialValues() {
        // initialize this material with the original values
        for (int i=0; i<4; i++) {
            ambient[i] = originalMaterial.ambient[i];
            diffuse[i] = originalMaterial.diffuse[i];
            emission[i] = originalMaterial.emission[i];
            shininess[i] = originalMaterial.shininess[i];
            specular[i] = originalMaterial.specular[i];
        }
    }

    /**
     * Called when the effect is finished. This will remove the effect from the
     * visual object.
     * 
     */
    private void finished() {
        // reset this material to original
        setInitialValues();
        // remove this effect from the object and reset to the old material
        visualObject.setMaterial(originalMaterial);
    }
}
