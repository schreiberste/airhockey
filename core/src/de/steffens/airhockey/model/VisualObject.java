package de.steffens.airhockey.model;
/*
 * Created on 05.10.2010
 *
 */


import de.steffens.airhockey.view.GLViewer;

/**
 * Base class for visual objects.
 * 
 * @author Steffen Schreiber
 */
public class VisualObject {

    private Material material = null;

    private GLViewer viewer;

    private int index;


    public void setIndex(int index) {
        this.index = index;
    }


    public int getIndex() {
        return index;
    }


    /**
     * Get the material properties to use for this object.
     * @return the material
     */
    public Material getMaterial() {
        if (material == null) {
            return Material.defaultMaterial;
        }
        return material;
    }
    
    /**
     * Set the material properties to use for this object.
     * @param material the material
     */
    public void setMaterial(Material material) {
        this.material = material;
    }

    public void updateViewer() {
        if (viewer != null) {
            viewer.update();
        }
    }

    public GLViewer getViewer() {
        return viewer;
    }

    public void setViewer(GLViewer viewer) {
        this.viewer = viewer;
    }

}
