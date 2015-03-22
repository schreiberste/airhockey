/*
 * Created on 26.04.2010
 *
 */
package de.steffens.airhockey.model;


/**
 * Class representing a material.
 *
 * @author Steffen Schreiber
 */
public class Material {


    // some default materials
    
    /** The default material that is used when no material is set */
    public static final Material defaultMaterial;
    /** The material to use for debugging objects */
    public static final Material debugMaterial;
    /** The floor materials */
    public static final Material floorDull;
    public static final Material floorMirror;
    public static final Material floorBlack;
    public static final Material floorReachable;
    /** The default material for walls */
    public static final Material wallMaterial;
    /** The material for destroyable walls with code 1 */
    public static final Material destroyableWallMaterial1;
    /** The material for destroyable walls with code 2 */
    public static final Material destroyableWallMaterial2;
    /** The default material for emitting walls */
    public static final Material wallMaterialEmmiting;
    /** The puck material */
    public static final Material puckMaterial;
    /** The double puck material */
    public static final Material doublePuckMaterial;
    /** The player disk material */
    public static final Material playerMaterial;

    private final int materialIdx;
    public static Material[] defaultMaterials = new Material[13];

    private String name = "default";
    
    protected float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
    protected float[] diffuse = {0.8f, 0.8f, 0.8f, 1.0f};
    protected float[] specular = {0.0f, 0.0f, 0.0f, 1.0f};
    protected float[] emission = {0.0f, 0.0f, 0.0f, 1.0f};
    protected float[] shininess = {0.0f, 0.0f, 0.0f, 1.0f}; // 0..128
    
    protected float mirror = 0.0f;
    protected float alpha = 1.0f;

    static {
        ///////// initialize default materials...  /////////
        
        // the initial default material
        defaultMaterial = new Material(0);
        defaultMaterials[0] = defaultMaterial;
        
        // material for rendering of debugging objects
        debugMaterial = new Material(1);
        debugMaterial.setAmbient(0.5f, 0, 0, 0.5f);
        debugMaterial.setDiffuse(1, 0, 0, 0.5f);
        defaultMaterials[1] = debugMaterial;

        // the dull mirror floor material
        floorDull = new Material(2);
        floorDull.setAmbient(0.1f, 0.1f, 0.2f, 1.0f);
        floorDull.setDiffuse(0.4f, 0.4f, 0.6f, 1.0f);
        floorDull.setSpecular(0.1f, 0.1f, 0.1f, 1.0f);
        floorDull.setEmission(0.0f, 0.0f, 0.0f, 0.0f);
        floorDull.setShininess(0.0f, 0.0f, 0.0f, 1.0f);
        floorDull.setMirror(0.2f);
        floorDull.setName("floorDull");
        defaultMaterials[2] = floorDull;

        // the brilliant mirror floor material
        floorMirror = new Material(3);
        floorMirror.setAmbient(0.1f, 0.1f, 0.2f, 1.0f);
        floorMirror.setDiffuse(0.3f, 0.3f, 0.4f, 1.0f);
        floorMirror.setSpecular(0.1f, 0.1f, 0.1f, 1.0f);
        floorMirror.setEmission(0.0f, 0.0f, 0.0f, 0.0f);
        floorMirror.setShininess(0.0f, 0.0f, 0.0f, 1.0f);
        floorMirror.setMirror(0.4f);
        floorMirror.setName("floorMirror");
        defaultMaterials[3] = floorMirror;

        // the brilliant mirror floor material
        floorBlack = new Material(4);
        floorBlack.setAmbient(0.0f, 0.0f, 0.0f, 1.0f);
        floorBlack.setDiffuse(0.0f, 0.0f, 0.0f, 1.0f);
        floorBlack.setSpecular(0.0f, 0.0f, 0.0f, 1.0f);
        floorBlack.setEmission(0.0f, 0.0f, 0.0f, 0.0f);
        floorBlack.setShininess(0.0f, 0.0f, 0.0f, 1.0f);
        floorBlack.setName("floorBlack");
        defaultMaterials[4] = floorBlack;

        // the floor material for reachable areas
        floorReachable = new Material(5);
        floorReachable.setAmbient(0.2f, 0.0f, 0.0f, 1.0f);
        floorReachable.setDiffuse(0.2f, 0.0f, 0.0f, 1.0f);
        floorReachable.setSpecular(0.0f, 0.0f, 0.0f, 1.0f);
        floorReachable.setEmission(0.3f, 0.2f, 0.2f, 0.0f);
        floorReachable.setMirror(0.6f);
        floorReachable.setName("floorReachable");
        defaultMaterials[5] = floorReachable;

        // the wall material
        wallMaterial = new Material(6);
        wallMaterial.setDiffuse(0.4f, 0.4f, 0.8f, 1.0f);
        wallMaterial.setSpecular(0.8f, 0.8f, 1.6f, 1.0f);
        wallMaterial.setEmission(0.0f, 0f, 0.1f, 0f);
        wallMaterial.setShininess(120, 120, 120, 0);
        wallMaterial.setName("wallMaterial");
        defaultMaterials[6] = wallMaterial;

        // the destroyable wall material 1
        destroyableWallMaterial1 = new Material(7);
        destroyableWallMaterial1.setDiffuse(0.8f, 0.8f, 0.8f, 1.0f);
        destroyableWallMaterial1.setSpecular(0.2f, 0.2f, 1.0f, 1.0f);
        destroyableWallMaterial1.setEmission(0.0f, 0.0f, 1.0f, 0f);
        destroyableWallMaterial1.setShininess(120, 120, 120, 0);
        destroyableWallMaterial1.setAlpha(1f);
        destroyableWallMaterial1.setName("destroyableWallMaterial1");
        defaultMaterials[7] = destroyableWallMaterial1;

        // the destroyable wall material 2
        destroyableWallMaterial2 = new Material(8);
        destroyableWallMaterial2.setDiffuse(0.4f, 0.3f, 0.3f, 1.0f);
        destroyableWallMaterial2.setSpecular(8.0f, 0.5f, 0.5f, 1.0f);
        destroyableWallMaterial2.setEmission(0.6f, 0.0f, 0.0f, 0f);
        destroyableWallMaterial2.setShininess(120, 120, 120, 0);
        destroyableWallMaterial2.setAlpha(1f);
        destroyableWallMaterial2.setName("destroyableWallMaterial2");
        defaultMaterials[8] = destroyableWallMaterial2;

        // the material for emmiting walls
        wallMaterialEmmiting = new Material(9);
        wallMaterialEmmiting.setDiffuse(0.4f, 0.4f, 0.8f, 1.0f);
        wallMaterialEmmiting.setSpecular(0.8f, 0.8f, 1.6f, 1.0f);
        wallMaterialEmmiting.setEmission(0.2f, 0.2f, 1.0f, 0f);
        wallMaterialEmmiting.setShininess(120, 120, 120, 0);
        wallMaterialEmmiting.setName("wallMaterialEmitting");
        defaultMaterials[9] = wallMaterialEmmiting;

        // the puck material
        puckMaterial = new Material(10);
        puckMaterial.setAmbient(0.2f, 0.2f, 0.6f, 1.0f);
        puckMaterial.setDiffuse(0.7f, 0.7f, 1.0f, 1.0f);
        puckMaterial.setSpecular(0.8f, 0.8f, 1.6f, 1.0f);
        puckMaterial.setEmission(0.0f, 0f, 0.0f, 0f);
        puckMaterial.setShininess(120, 120, 120, 0);
        puckMaterial.setName("puckMaterial");
        defaultMaterials[10] = puckMaterial;

        // the double puck material
        doublePuckMaterial = new Material(11);
        doublePuckMaterial.setAmbient(0.4f, 0.2f, 0.2f, 1.0f);
        doublePuckMaterial.setDiffuse(0.8f, 0.4f, 0.4f, 1.0f);
        doublePuckMaterial.setSpecular(8.0f, 0.5f, 0.5f, 1.0f);
        doublePuckMaterial.setEmission(0.6f, 0.0f, 0.0f, 0f);
        doublePuckMaterial.setShininess(120, 120, 120, 0);
        doublePuckMaterial.setName("doublePuckMaterial");
        defaultMaterials[11] = doublePuckMaterial;

        // the player disk material
        playerMaterial = new Material(12);
        playerMaterial.setAmbient(0.2f, 0.2f, 0.6f, 1.0f);
        playerMaterial.setDiffuse(0.3f, 0.3f, 0.3f, 1.0f);
        playerMaterial.setSpecular(7.0f, 7.0f, 14.0f, 1.0f);
        playerMaterial.setEmission(0.0f, 0f, 0.0f, 0f);
        playerMaterial.setShininess(120, 120, 120, 0);
        playerMaterial.setAlpha(0.8f);
        playerMaterial.setName("playerMaterial");
        defaultMaterials[12] = playerMaterial;
    }

    public Material(int materialIdx) {
        this.materialIdx = materialIdx;
    }

    /**
     * Get a copy of this material with the given alpha value set for 
     * ambient and diffuse color.
     * 
     * @param alpha the new alpha value 
     * @return the new material
     */
    public Material getBlended(float alpha) {
        Material result = getCopy();
        result.ambient[3] = alpha;
        result.diffuse[3] = alpha;
        return result;
    }
    
    /**
     * Get a copy of this material.
     * 
     * @return the new material
     */
    public Material getCopy() {
        Material result = new Material(materialIdx);
        copyTo(result);
        return result;
    }
    
    /**
     * Copy the properties of this material to the given target material.
     * 
     * @param target the copy target
     */
    public void copyTo(Material target) {
        target.name = name;
        target.mirror = mirror;
        target.alpha = alpha;
        System.arraycopy(ambient, 0, target.ambient, 0, 4);
        System.arraycopy(diffuse, 0, target.diffuse, 0, 4);
        System.arraycopy(specular, 0, target.specular, 0, 4);
        System.arraycopy(emission, 0, target.emission, 0, 4);
        System.arraycopy(shininess, 0, target.shininess, 0, 4);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Specify the ambient RGBA reflectance of the material. 
     * The initial ambient reflectance for both front- and back-facing 
     * materials is (0.2, 0.2, 0.2, 1.0).
     * 
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void setAmbient(float r, float g, float b, float a) {
        ambient[0] = r;
        ambient[1] = g;
        ambient[2] = b;
        ambient[3] = a;
    }
    
    public float[] getAmbient() {
        return ambient;
    }
    
    /**
     * Specify the diffuse RGBA reflectance of the material. 
     * The initial diffuse reflectance for both front- and back-facing 
     * materials is (0.8, 0.8, 0.8, 1.0).
     *
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void setDiffuse(float r, float g, float b, float a) {
        diffuse[0] = r;
        diffuse[1] = g;
        diffuse[2] = b;
        diffuse[3] = a;
    }
    
    public float[] getDiffuse() {
        return diffuse;
    }
    
    /**
     * Specify the specular RGBA reflectance of the material. 
     * The initial specular reflectance for both front- and back-facing 
     * materials is (0, 0, 0, 1).
     *
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void setSpecular(float r, float g, float b, float a) {
        specular[0] = r;
        specular[1] = g;
        specular[2] = b;
        specular[3] = a;
    }
    
    public float[] getSpecular() {
        return specular;
    }
    
    /**
     * Specify the RGBA emitted light intensity of the material. 
     * The initial emission intensity for both front- and back-facing 
     * materials is (0, 0, 0, 1).
     *
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void setEmission(float r, float g, float b, float a) {
        emission[0] = r;
        emission[1] = g;
        emission[2] = b;
        emission[3] = a;
    }
    
    public float[] getEmission() {
        return emission;
    }
    
    /**
     * Specifies the RGBA specular exponent of the material. 
     * Only values in the range 0 128 are accepted. The initial shininess 
     * exponent for both front- and back-facing materials is 0.
     *
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void setShininess(float r, float g, float b, float a) {
        shininess[0] = r;
        shininess[1] = g;
        shininess[2] = b;
        shininess[3] = a;
    }
    
    public float[] getShininess() {
        return shininess;
    }
    
    /**
     * Set the mirror value for this material.
     * A value of 1 means full mirroring, 0 means no mirroring.
     * 
     * @param mirror the mirror strength to set.
     */
    public void setMirror(float mirror) {
        this.mirror = mirror;
    }


    /**
     * Get the mirror value of this material.
     * A value of 1 means full mirroring, 0 means no mirroring.
     * 
     * @return the mirror strength to set.
     */
    public float getMirror() {
        return mirror;
    }


    public float getAlpha() {
        return alpha;
    }


    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return ("Material " + name);
    }


    public int getMaterialIdx() {
        return materialIdx;
    }
}
