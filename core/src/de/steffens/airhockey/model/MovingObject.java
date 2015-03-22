/*
 * Created on 24.04.2010
 *
 */
package de.steffens.airhockey.model;

import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * Base class for linear moving objects.
 *
 * @author Steffen Schreiber
 */
public abstract class MovingObject extends VisualObject {
    
    /**
     * Maximum velocity that can be set for moving objects.
     * If a higher velocity is set, clamp to this value.
     */
    public static final double MAX_VELOCITY_VALUE = 55.0 / 1000000000.0;
    
    /**
     * A friction factor applied for high velocity objects in addition to
     * their standard acceleration value (only if acceleration != 1.0).
     * This enables higher (or lower...) friction for fast moving objects.
     * Positive values mean higher friction for fast objects.  
     */
    public static final double HIGH_VELOCITY_FRICTION = 0.0015;

    private Vector2D position = VectorFactory.getVector(0.0, 0.0);

    private Vector2D velocity = VectorFactory.getVector(0.0, 0.0);
    
    private double acceleration = 1.0;
    
    private long timestamp = 0;
    
    
    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Set position at current time.
     *     
     * @param x
     * @param y
     */
    public void setPosition(double x, double y) {
        position = position.reset(x, y);
    }
    
    /**
     * Set position at current time.
     * 
     * @param newPosition 
     */     
    public void setPosition(Vector2D newPosition) {
        position = position.reset(newPosition);
    }
    
    /**
     * Set velocity vector components at current time.
     * Velocity unit is 1.0 units per nano second
     * 
     * @param x
     * @param y
     */
    public void setVelocity(double x, double y) {
        velocity = velocity.reset(x, y);
        checkVelocity();
    }

    /**
     * Set velocity vector at current time.
     * Velocity unit is 1.0 units per nano second
     * 
     * @param newVelocity
     */
    public void setVelocity(Vector2D newVelocity) {
        velocity = velocity.reset(newVelocity);
        checkVelocity();
    }

    /**
     * Sanity check the current velocity and limit to MAX_VELOCITY_VALUE. 
     */
    private void checkVelocity() {
        if (velocity.getValue() > MAX_VELOCITY_VALUE) {
            System.out.println("Very high velocity set for " + this);
            System.out.println("  last collision: " + Collision.lastCollisionEvent);
            velocity = velocity.getNormalized().multiply(MAX_VELOCITY_VALUE);
        }
    }
    
    /**
     * Get velocity vector components at current time.
     * Velocity unit is 1.0 units per nano second
     * 
     * @return the velocity vector 
     */
    public Vector2D getVelocity() {
        return velocity;
    }
    
    /**
     * Get the current timestamp in ns.
     * 
     * @return the current time
     */
    public long getTimestampNs() {
        return timestamp;
    }
    
    /**
     * Set current time in ns.
     * 
     * @param time
     */
    public void setTimestampNs(long time) {
        timestamp = time;
    }
    
    /**
     * Get the position at the current timestamp.
     * 
     * @return the position
     */
    public Vector2D getPosition() {
        return position;
    }
    
    /**
     * Calculate the new position at the given time, given the current
     * velocity is constant.
     * 
     * @param time the new time that the new position is calculated for
     * @param tmp vector used during calculations
     * @return the calculated position as vector 
     */
    public Vector2D getPositionAt(long time, Vector2D tmp) {
        // get time difference in ns
        double deltaT = (time - timestamp);
        // calculate new position at that time
        Vector2D result = tmp.reset(position).addMultiple(velocity, deltaT);
        
        return result;
    }

    /** 
     * Update the object's position and timestamp.
     * 
     * @param time the new time
     */
    public void update(long time) {
        position = getPositionAt(time, position);
        timestamp = time;
        // simple friction model:
        // multiply velocity with an acceleration factor < 1 at each update.
        if (acceleration != 1.0) {
            // reduce the acceleration factor even more if the speed is near max speed.
            double acc = acceleration - 
                HIGH_VELOCITY_FRICTION * (velocity.getValue() / MAX_VELOCITY_VALUE);
            velocity = velocity.multiply(acc);
        }
    }

}