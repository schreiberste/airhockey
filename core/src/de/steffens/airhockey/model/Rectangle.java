/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.model;

import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * Class representing a rectangular area.
 * 
 * @author Steffen Schreiber
 */
public class Rectangle extends VisualObject {

    private final Vector2D originPos;
    
    private final Vector2D right;
    
    private final Vector2D up;

    private Vector2D[] cachedCoords;
    private Vector2D[][] cachedSubdividedCoords;
    
    private int subdivisions;
    
    private double[] inverseBaseMatrix;
    
    /** 
     * Create a new rectangle with edges parallel to the x- and y-axis.
     * The point (x1, y1) must be the bottom-left corner vertex (origin), while 
     * the point (x2, y2) is the top-right corner vertex.
     *   
     * @param x1 the x-coordinate of the bottom-left corner
     * @param y1 the y-coordinate of the bottom-left corner
     * @param x2 the x-coordinate of the top-right corner
     * @param y2 the y-coordinate of the top-right corner
     */
    public Rectangle (double x1, double y1, double x2, double y2) {
        this (x1,y1,x2,y2, 1);
    }
    
    /** 
     * Create a new rectangle with edges parallel to the x- and y-axis.
     * The point (x1, y1) must be the bottom-left corner vertex (origin), while 
     * the point (x2, y2) is the top-right corner vertex.
     *   
     * @param x1 the x-coordinate of the bottom-left corner
     * @param y1 the y-coordinate of the bottom-left corner
     * @param x2 the x-coordinate of the top-right corner
     * @param y2 the y-coordinate of the top-right corner
     * @param subdivisions 
     */
    public Rectangle (double x1, double y1, double x2, double y2, int subdivisions) {
        originPos = VectorFactory.getVector(x1, y1);
        right = VectorFactory.getVector(x2-x1, 0);
        up = VectorFactory.getVector(0, y2-y1);
        calculateInverse();
        this.subdivisions = subdivisions;
    }
    
    /**
     * Create a new rectangle using the given vectors for origin, right and up direction.
     * 
     * @param origin
     * @param right
     * @param up
     * @param subdivisions
     */
    public Rectangle (Vector2D origin, Vector2D right, Vector2D up, int subdivisions) {
        this.originPos = origin.copy();
        this.right = right.copy();
        this.up = up.copy();
        calculateInverse();
        this.subdivisions = subdivisions;
    }
    
    /**
     * Returns coordinates of this rectangle in the following order (as seen from
     * top in X-Y-plane): 
     * <pre>
     *
     *   P4------------------------P3
     *   ^                         |
     *   | up                      |   
     *   |                         |
     *   P1----------------------->P2
     *                right
     *   
     *   P1 = origin
     *   
     * </pre>
     * 
     * @return the coordinates as vector array
     */
    public Vector2D[] getCoords() {
        if (cachedCoords != null) {
            return cachedCoords;
        }
        
        cachedCoords = new Vector2D[4];
        cachedCoords[0] = originPos;
        cachedCoords[1] = originPos.copy().add(right);
        cachedCoords[2] = cachedCoords[1].copy().add(up);
        cachedCoords[3] = cachedCoords[0].copy().add(up);
        return cachedCoords;
    }

    /**
     * @return the origin position of this rectangle.
     */
    public Vector2D getOrigin() {
        return originPos;
    }
    
    /**
     * @return the right vector of this rectangle.
     */
    public Vector2D getRight() {
        return right;
    }
    
    /**
     * @return the up vector of this rectangle.
     */
    public Vector2D getUp() {
        return up;
    }
    
    /**
     * Returns coordinates of the subdivided rectangle in the following order 
     * (as seen from top in X-Y-plane, example subdivision=2): 
     * <pre>
     *
     *   P7-----------P9----------P11
     *   |             |           ^
     *   |             |           |  up   
     *   |             |           |
     *   P8-----------P10-------->P12
     *   P1-----------P3----------P5
     *   |             |           |
     *   |             |           |   
     *   |             |           |
     *   P2-----------P4----------P6
     *                right
     *
     *   P2 = origin
     *   
     * </pre>
     * 
     * So for "subdivision=4" this would result in 4 stripes of rectangles,
     * each of them containing 4 rectangles given in a way that could be
     * used in a triangle strip. 
     * 
     * @return the coordinates as vector array
     */
    public Vector2D[][] getSubdividedCoords() {
        if (cachedSubdividedCoords != null) {
            return cachedSubdividedCoords;
        }
        
        int size = 2 * (subdivisions + 1);
        cachedSubdividedCoords = new Vector2D[subdivisions][size];
        
        double w = 1.0 / subdivisions;
        double h = 1.0 / subdivisions;
        
        for (int i=0; i<subdivisions; i++) {
            Vector2D p1 = originPos.copy().addMultiple(up, (i + 1) * h);
            Vector2D p2 = originPos.copy().addMultiple(up, i * h);
            cachedSubdividedCoords[i][0] = p1;
            cachedSubdividedCoords[i][1] = p2;
            for (int j=1; j<=subdivisions; j++) {
                int index = j * 2;
                cachedSubdividedCoords[i][index] = 
                    cachedSubdividedCoords[i][index-2].copy().addMultiple(right, w);
                index++;
                cachedSubdividedCoords[i][index] = 
                    cachedSubdividedCoords[i][index-2].copy().addMultiple(right, w);
            }
        }
        return cachedSubdividedCoords;
    }

    /**
     * @return the number of subdivisions that should be used for visual representation.
     */
    public int getSubdivisions() {
        return subdivisions;
    }
    
    /**
     * Map the given coordinates from the range <code>[0..1]</code> to coordniates
     * in the area of this rectangle. The point (0,0) is mapped to the bottom-left
     * vertex (origin), (1,1) is mapped to the top-right vertex.
     * 
     * @param vector the vector to map
     * @return the mapped point
     */
    public Vector2D getMappedPosition(Vector2D vector) {
    	double x = vector.getX();
    	double y = vector.getY();
        assert (0.0 <= x) && (x <= 1.0);
        assert (0.0 <= y) && (y <= 1.0);
        
        return vector.reset(originPos).addMultiple(right, x).addMultiple(up, y);
    }
    
    /**
     * Map the given coordinates from R2 to coordinates given in the base
     * system spanned by this rectangle. The coordinates of the bottom-left 
     * vertex will map to (0,0). The coordinates of the top-right vertex 
     * will map to (1,1).
     * 
     * @param coordinates the position coordinates in R2
     * @return the mapped vector in this rectangle's base 
     */
    public Vector2D getMappedInverse(Vector2D coordinates) {
        // get position vector using our origin
        Vector2D posStandardBase = coordinates.subtract(originPos);
        double p1 = posStandardBase.getX();
        double p2 = posStandardBase.getY();
     
        // multiply position with inverse matrix to get coordinates in our base
        double p1_ = p1 * inverseBaseMatrix[0] + p2 * inverseBaseMatrix[1];
        double p2_ = p1 * inverseBaseMatrix[2] + p2 * inverseBaseMatrix[3];
        
        return posStandardBase.reset(p1_, p2_);
    }
    
    /**
     * Returns <code>true</code>, if the given position is inside the 
     * rectangle.
     * 
     * @param position the position to check
     * @return is inside
     */
    public boolean isInside(Vector2D position) {
        // switch the 'position' vector (which is given in standard basis)
        // to our basis...
        Vector2D mappedPos = getMappedInverse(position);
        double p1 = mappedPos.getX();
        double p2 = mappedPos.getY();
        
        // P is inside, if 0 <= p1,p2 <= 1
        if ((p1 < 0) || (p2 < 0))
            return false;
        if ((p1 > 1) || (p2 > 1))
            return false;
        
        return true;
    }


    /**
     * Clamp the given position vector to the range of this rectangle.
     * @param position the input position
     * @return the clamped position
     */
    public Vector2D clamp(Vector2D position) {
        // switch the 'position' vector (which is given in standard basis)
        // to our basis...
        Vector2D mappedPos = getMappedInverse(position);

        // clamp the mapped coords to range [0..1]
        double p1 = Math.min(1.0, Math.max(0.0, mappedPos.getX()));
        double p2 = Math.min(1.0, Math.max(0.0, mappedPos.getY()));

        // switch to world coords again
        position.reset(p1, p2);
        return getMappedPosition(position);
    }

    /**
     * Calculate the inverse matrix of our base.
     * The two vectors 'right' and 'up' form a base in R2. For some calculations
     * it is beneficial to know the coordinates of a vector P that is given in the
     * standard base { (1,0); (0,1) } converted to coordinates given in this
     * base { right; up }.
     * To speed up this conversion, we need the inverse matrix of our base written 
     * as a matrix.
     */
    private void calculateInverse() {
        // calculate the inverse matrix of our base
        // 
        //    | a  b |                      |  d -b |
        //    | c  d | -->  1 / (ad - bc) * | -c  a |
        //
        double a = right.getX();
        double c = right.getY();
        double b = up.getX();
        double d = up.getY();
        
        double f = 1.0 / (a*d - b*c);
        
        // these are the elements of our inverse base matrix
        inverseBaseMatrix = new double[4];
        inverseBaseMatrix[0] = d*f;
        inverseBaseMatrix[1] = -b*f;
        inverseBaseMatrix[2] = -c*f;
        inverseBaseMatrix[3] = a*f;
    }
}
