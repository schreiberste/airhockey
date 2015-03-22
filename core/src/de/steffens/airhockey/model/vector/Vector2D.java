/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.model.vector;


/**
 * Abstract super class of all 2-dimensional vectors with some useful operations.
 * 
 * @author Johannes Scheerer
 */
public abstract class Vector2D {
	
    /**
     * Maximum allowed rounding error.
     */
    public final static double EPSILON = 1e-14;
    
	/**
	 * Returns the x coordinate of the vector.
	 * 
	 * @return the x coordinate of the vector.
	 */
    public abstract double getX();

	/**
	 * Returns the y coordinate of the vector.
	 * 
	 * @return the y coordinate of the vector.
	 */
    public abstract double getY();

    /**
     * Get the value (length) of this vector.
     * 
     * @return the value
     */
    public final double getValue() {
        return Math.sqrt(getX()*getX() + getY()*getY());
    }
    
    /**
     * Get the normalized vector.
     * The normalized vector points at the same direction and has length 1.
     * 
     * @return this vector normalized.
     */
    public abstract Vector2D getNormalized();
    
    /**
     * Get the inverse vector.
     * The inverse vector points in the opposite direction.
     * 
     * @return the inverse vector.
     */
    public abstract Vector2D getInverse();
    
    /**
     * Get the scalar product of this vector and the given vector.
     * 
     * @param vector2 the other vector
     * @return the scalar product
     */
    public final double getScalarProduct(Vector2D vector2) {
        return getX() * vector2.getX() + getY() * vector2.getY();
    }
    
    /**
     * Get the angle between this vector and the given vector.
     * 
     * @param vector2 the other vector
     * @return the angle in RAD (0..pi)
     */
    public final double getAngle(Vector2D vector2) {
        return Math.acos(
                getScalarProduct(vector2) / (getValue() * vector2.getValue()));
    }

    /**
     * Get the distance between this vector and the given vector.
     * 
     * @param vector2 the other vector
     * @return the distance
     */
    public final double getDistance(Vector2D vector2) {
        double xDist = getX() - vector2.getX();
        double yDist = getY() - vector2.getY();
        return Math.sqrt(xDist * xDist + yDist * yDist);
    }
    
    /**
     * Check, if this vector is orthogonal to the given vector.
     * 
     * @param vector2 the other vector
     * @return <code>true</code>, if the vectors are orthogonal.
     */
    public final boolean isOrthogonal(Vector2D vector2) {
        // the vectors are orthogonal, if their scalar product is 0, 
        // but we accept some rounding error
        double prod = getScalarProduct(vector2);
        return ((prod < EPSILON) && (prod > -EPSILON));
    }
    
    /**
     * Add this vector to the given second vector and return the result as a new vector.
     * 
     * @param vector2 the second vector in the addition
     * @return the resulting vector
     */
    public abstract Vector2D add(Vector2D vector2);
    
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
    public abstract Vector2D addMultiple(Vector2D vector2, double factor);
    
    /**
     * Subtract the given second vector from this vector.
     * 
     * @param vector2 the second vector to subtract
     * @return the resulting vector
     */
    public abstract Vector2D subtract(Vector2D vector2);
    
    /**
     * Multiply this vector with the given scalar factor.
     * 
     * @param factor
     * @return the resulting vector.
     */
    public abstract Vector2D multiply(double factor);
    
    /**
     * Calculate the velocity vector that will move from this vector to the 
     * given destination vector in the given time.
     * 
     * @param position the current position
     * @param destination the destination vector to move to 
     * @param deltaT the time in ns
     * @return the calculated velocity vector
     */
    public abstract Vector2D getVelocity(Vector2D position, Vector2D destination, long deltaT);
    
    @Override
    public final String toString() {
        return "[" + getX() + "," + getY() + "]";
    }
    
    /**
     * Get a string representation if this vector interpreted as a velocity.
     * 
     * @return the string representation.
     */
    public final String asVelocityString() {
        return "[" + (getX() * 1000000000.0) + "/s, " + (getY() * 1000000000.0) + "/s]"; 
    }
    
    /**
     * Returns a copy of this vector.
     * 
     * @return a copy of this vector.
     */
    public abstract Vector2D copy();
    
    /**
     * Returns a vector with the given position.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return a vector with the given position.
     */
    public abstract Vector2D reset(double x, double y);
    
    /**
     * Returns a vector with position (0,0).
     * @return the reset vector
     */
    public Vector2D reset() {
        return reset(0.0, 0.0);
    }
    
    /**
     * Returns a vector with the given position.
     * 
     * @param vector2 the other vector
     * @return a vector with the given position.
     */
    public abstract Vector2D reset(Vector2D vector2);
}
