/*
 * Created on 24.04.2010
 *
 */
package de.steffens.airhockey.model.vector;


/**
 * A 2-dimensional immutable vector with some useful operations.
 *
 * @author Steffen Schreiber
 */
public class ImmutableVector2D extends Vector2D {
    
    private final double x;
    
    private final double y;
    
    public ImmutableVector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    /**
     * Get the normalized vector.
     * The normalized vector points at the same direction and has length 1.
     * 
     * @return this vector normalized.
     */
    @Override
    public ImmutableVector2D getNormalized() {
        double value = getValue();
        
        assert value != 0;
        
        if (Math.abs(value) < EPSILON) {
            System.out.println("WARNING: normalizing a very short vector");
        }
        
        return new ImmutableVector2D(
                x / value, 
                y / value
                );
    }
    
    /**
     * Get the inverse vector.
     * The inverse vector points in the opposite direction.
     * 
     * @return the inverse vector.
     */
    @Override
    public ImmutableVector2D getInverse() {
        return new ImmutableVector2D(x * -1.0, y * -1.0);
    }

    /**
     * Get the orthogonal vector.
     * If this vector points to the right, then the orthogonal vector will point up
     * and have length 1;
     *
     * @return the inverse vector.
     */
    public Vector2D getOrthogonal() {
        return new ImmutableVector2D(-y, x).getNormalized();
    }



    /**
     * Add this vector to the given second vector and return the result as a new vector.
     * 
     * @param vector2 the second vector in the addition
     * @return the resulting vector
     */
    @Override
    public ImmutableVector2D add(Vector2D vector2) {
        return new ImmutableVector2D(x + vector2.getX(), y + vector2.getY());        
    }
    
    /**
     * Add a the given vector multiplied with the given factor to 
     * this vector and return the result as a new vector.
     * 
     * This is a convenience method for linear combination and is slightly more
     * efficient than <code>this.add(vector2.multiply(factor))</code>.
     * 
     * @param vector2 the vector to add
     * @param factor the factor to multiply with the second vector before adding
     * @return the resulting vector
     */
    @Override
    public ImmutableVector2D addMultiple(Vector2D vector2, double factor) {
        return new ImmutableVector2D(x + (vector2.getX() * factor), 
                            y + (vector2.getY() * factor));        
    }
    
    /**
     * Subtract the given second vector from this vector.
     * 
     * @param vector2 the second vector to subtract
     * @return the resulting vector
     */
    @Override
    public ImmutableVector2D subtract(Vector2D vector2) {
        return new ImmutableVector2D(x - vector2.getX(), y - vector2.getY());
    }
    
    /**
     * Multiply this vector with the given scalar factor.
     * 
     * @param factor
     * @return the resulting vector.
     */
    @Override
    public ImmutableVector2D multiply(double factor) {
        if ((factor != 0) && (Math.abs(factor) < EPSILON)) {
            System.out.println("WARNING: multiplying vector with a very small factor");
        }
        return new ImmutableVector2D(x * factor, y * factor);
    }
    
    /**
     * Calculate the velocity vector that will move from this vector to the 
     * given destination vector in the given time.
     * 
     * @param position the current position
     * @param destination the destination vector to move to 
     * @param deltaT the time in ns
     * @return the calculated velocity vector
     */
    @Override
    public ImmutableVector2D getVelocity(Vector2D position, Vector2D destination, long deltaT) {
        return new ImmutableVector2D((destination.getX() - position.getX()) / deltaT, 
                            (destination.getY() - position.getY()) / deltaT);
    }
    
    /**
     * Returns a copy of this vector.
     * 
     * @return a copy of this vector.
     */
    @Override
    public Vector2D copy() {
        return this;
    }
    
    /**
     * Returns a vector with the given position.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return a vector with the given position.
     */
    @Override
    public ImmutableVector2D reset(double x, double y) {
    	return new ImmutableVector2D(x, y);
    }
    
    /**
     * Returns a vector with the given position.
     * 
     * @param vector2 the other vector
     * @return a vector with the given position.
     */
    @Override
    public Vector2D reset(Vector2D vector2) {
    	return vector2;
    }
}
