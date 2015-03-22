/*
 * Created on 25.04.2010
 *
 */
package de.steffens.airhockey.model;

import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * A model representing a wall.
 *
 * @author Steffen Schreiber
 */
public class Wall extends VisualObject {

    /**
     * Class representing a wall face.
     *
     * @author Steffen Schreiber
     */
    public static class Face {
        /** Offset (distance from origin) of the start point */
        private final Vector2D start;
        
        /** The normalized normal vector of the wall face */
        private final Vector2D normalVector;
        
        /** The vector of the wall face from start (direction and length) */
        private final Vector2D faceVector;

        /** Wall face end point */
        private final Vector2D end;
        
        /** 
         * Creates a new wall face object.
         * 
         * @param start Offset (distance from origin) of the start point 
         * @param normalVector The normalized normal vector of the wall face 
         * @param faceVector The vector of the wall face from start (direction and length) 
         */
        public Face(Vector2D start, Vector2D normalVector, Vector2D faceVector) {
            this.start = start;
            this.normalVector = normalVector;
            this.faceVector = faceVector;
            this.end = start.copy().add(faceVector);
        }
        
        public Vector2D getNormalVector() {
            return normalVector;
        }
        
        public Vector2D getPositionVector() {
            return start;
        }
        
        public Vector2D getFaceVector() {
            return faceVector;
        }
        
        public Vector2D getFaceEndVector() {
            return end;
        }
    }
    
    /** The 4 wall faces */
    private final Face[] wallFaces = new Face[4];
    
    /** Thickness of the wall */
    private double thickness = 0.2;
    
    /** The height of the wall */
    private double height = 1.0;
    
    /** can the wall be handled as infinite wall? */
    private boolean infinite = false;
    
    /** Coordinates of the starting points of the 4 wall faces */
    private final Vector2D[] coords;

    
    /**
     * Create a new wall from point (x1,y1) to point (x2,y2) 
     * with the given height and thickness.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param thickness
     * @param height
     */
    public Wall(double x1, double y1, double x2, double y2, 
            double thickness, double height) {
        this(VectorFactory.getVector(x1, y1), 
        		VectorFactory.getVector(x2 - x1, y2 - y1),
        		VectorFactory.getVector(-(y2 - y1), x2 - x1).getNormalized(),
                thickness, height);
    }

    /**
     * Create a new wall for the given start, face and normal vectors.
     * 
     * @param start the start position vector
     * @param face the wall face vector
     * @param normal the wall normal vector
     * @param thickness
     * @param height
     */
    public Wall(Vector2D start, Vector2D face, Vector2D normal, 
            double thickness, double height) {
        
        Vector2D face2 = normal.copy().multiply(-thickness);
        Vector2D normal2 = face.copy().getNormalized();
        
        coords = new Vector2D[4];
        coords[0] = start;
        coords[1] = coords[0].copy().add(face);
        coords[2] = coords[1].copy().add(face2);
        coords[3] = coords[2].copy().subtract(face);
        
        wallFaces[0] = new Face(coords[0], normal, face);
        wallFaces[1] = new Face(coords[1], normal2, face2);
        wallFaces[2] = new Face(coords[2], normal.copy().getInverse(), face.copy().getInverse());
        wallFaces[3] = new Face(coords[3], normal2.copy().getInverse(), face2.copy().getInverse());
            
        this.height = height;
        this.thickness = thickness;
        // set default wall material
        setMaterial(Material.wallMaterial);
    }
    
    /**
     * Returns coordinates of this wall in the following order (wall seen from
     * top in X-Y-plane): 
     * <pre>
     *               ^ normal
     *               |
     *               |       face
     *   P1----------------------->P2
     *   |                          |
     *   P4------------------------P3
     * </pre>
     * 
     * @return the coordinates as vector array
     */
    public Vector2D[] getCoords() {
        return coords;
    }
    
    public double getHeight() {
        return height;
    }
    
    public double getThickness() {
        return thickness;
    }
    
    public Face getFrontFace() {
        return wallFaces[0];
    }
    
    public Face[] getWallFaces() {
        return wallFaces;
    }
    
    /**
     * Can the wall be handled as infinite wall? 
     * This can help to optimize collision detection.
     * 
     * @return is infinitely long
     */
    public boolean isInfinite() {
        return infinite;
    }

    /**
     * Can the wall be handled as infinite wall? 
     * This can help to optimize collision detection.
     * 
     * @param infinite the wall can be handled as infinitely long
     */
    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    @Override
    public String toString() {
        return "wall " + getIndex() + coords[0] + " to " + coords[1] + ", normal "
                + wallFaces[0].normalVector; 
    }


    @Override
    public int hashCode() {
        return "wall".hashCode() + getIndex();
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return ((obj instanceof  Wall) && ((Wall) obj).getIndex() == getIndex());
    }
}
