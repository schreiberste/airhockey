/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.model.vector;


/**
 * A 2-dimensional mutable vector with some useful operations.
 *
 * @author Johannes Scheerer
 */
public class MutableVector2D extends Vector2D {
    
    private double x;
    
    private double y;
    
    public MutableVector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public synchronized double getX() {
        return x;
    }

    @Override
    public synchronized double getY() {
        return y;
    }

    /**
     * Get the normalized vector.
     * The normalized vector points at the same direction and has length 1.
     * 
     * @return this vector normalized.
     */
    @Override
    public synchronized MutableVector2D getNormalized() {
        double value = getValue();
        
        assert value != 0;
        
        if (Math.abs(value) < EPSILON) {
            System.out.println("WARNING: normalizing a very short vector");
        }
        
        x = x / value;
        y = y / value;
        
        return this;
    }
    
    /**
     * Get the inverse vector.
     * The inverse vector points in the opposite direction.
     * 
     * @return the inverse vector.
     */
    @Override
    public synchronized MutableVector2D getInverse() {
    	x = x * -1.0;
    	y = y * -1.0;
        return this;
    }


    /**
     * Get the orthogonal vector.
     * If this vector points to the right, then the orthogonal vector will point up
     * and have length 1;
     *
     * @return the inverse vector.
     */
    public Vector2D getOrthogonal() {
        double tmpX = -y;
        y = x;
        x = tmpX;
        return getNormalized();
    }

    
    /**
     * Add this vector to the given second vector and return the result as a new vector.
     * 
     * @param vector2 the second vector in the addition
     * @return the resulting vector
     */
    @Override
    public synchronized MutableVector2D add(Vector2D vector2) {
    	synchronized (vector2) {
	        x = x + vector2.getX();
	        y = y + vector2.getY();
        }
        return this;        
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
    public synchronized MutableVector2D addMultiple(Vector2D vector2, double factor) {
    	synchronized (vector2) {
	        x = x + (vector2.getX() * factor);
	        y = y + (vector2.getY() * factor);
        }
        return this;        
    }
    
    /**
     * Subtract the given second vector from this vector.
     * 
     * @param vector2 the second vector to subtract
     * @return the resulting vector
     */
    @Override
    public synchronized MutableVector2D subtract(Vector2D vector2) {
    	synchronized (vector2) {
	        x = x - vector2.getX();
	        y = y - vector2.getY();
        }
        return this;
    }
    
    /**
     * Multiply this vector with the given scalar factor.
     * 
     * @param factor
     * @return the resulting vector.
     */
    @Override
    public synchronized MutableVector2D multiply(double factor) {
        if ((factor != 0) && (Math.abs(factor) < EPSILON)) {
            System.out.println("WARNING: multiplying vector with a very small factor");
        }
        x = x * factor;
        y = y * factor;
        return this;
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
    public synchronized MutableVector2D getVelocity(Vector2D position, Vector2D destination, long deltaT) {
    	synchronized (position) {
	        synchronized (destination) {
	            x = (destination.getX() - position.getX()) / deltaT;
	            y = (destination.getY() - position.getY()) / deltaT;
            }
        }
        return this;
    }
    
    /**
     * Returns a copy of this vector.
     * 
     * @return a copy of this vector.
     */
    @Override
    public synchronized Vector2D copy() {
        return VectorFactory.getVector(x, y);
    }
    
    /**
     * Returns a vector with the given position.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return a vector with the given position.
     */
    @Override
    public synchronized MutableVector2D reset(double x, double y) {
    	this.x = x;
    	this.y = y;
    	return this;
    }
    
    /**
     * Returns a vector with the given position.
     * 
     * @param vector2 the other vector
     * @return a vector with the given position.
     */
    @Override
    public synchronized Vector2D reset(Vector2D vector2) {
    	synchronized (vector2) {
	        x = vector2.getX();
	        y = vector2.getY();
        }
    	return this;
    }
}
