/*
 * Created on 26.04.2010
 *
 */

package de.steffens.airhockey.model;

/**
 * A class managing the life time of a time-based effect.
 * This describes effects with a given duration and provides some 
 * function curves over the effect life time that can be used to
 * control the effect intensity or behaviour. 
 * 
 * @author Steffen Schreiber
 */
public class EffectLifetime {

    /** default effect duration = 1/2s */
    public final static long DEFAULT_DURATION = 1000 * 1000 * 500;
    
    /**
     * Enumeration of possible effect function types.
     *
     * @author Steffen Schreiber
     */
    public enum EffectFunctionType {
        /** A linear function rising from zero to one */
        LINEAR_ZERO_TO_ONE,
        /** A linear function declining from one to zero */
        LINEAR_ONE_TO_ZERO
    }
    
    private final long startTime;
    private final long duration;
    private final boolean repeat;
    private final EffectFunctionType type;
    
    /**
     * Creates a new effect life time object for the given starting time, 
     * duration and function type 
     * 
     * @param startTime the starting time of the effect
     * @param duration the duration of the effect
     * @param type the type of effect
     */
    public EffectLifetime(long startTime, long duration, EffectFunctionType type) {
        this(startTime, duration, type, false);
    }
    
    /**
     * Creates a new effect life time object for the given starting time, 
     * duration and function type. If the repeat parameter is set to <code>true</code>,
     * the effect life time will not end after the given duration but will
     * start again.
     * 
     * @param startTime the starting time of the effect
     * @param duration the duration of the effect
     * @param type the type of effect
     * @param repeat 
     */
    public EffectLifetime(long startTime, long duration, EffectFunctionType type, boolean repeat) {
        this.startTime = startTime;
        this.duration = duration;
        this.type = type;
        this.repeat = repeat;
    }
    
    /**
     * Calculates the effect function value for the given time.
     * The time must be >= the starting time.
     *   
     * @param time
     * @return the function value
     */
    public double getFunctionValue(long time) {
        assert time >= startTime;
        
        long elapsedTime = time - startTime;
        if (repeat) {
            elapsedTime = elapsedTime % duration;
        }
        double fraction = (double)elapsedTime / (double)duration;
        
        
        switch (type) {
        case LINEAR_ONE_TO_ZERO:
            return 1.0 - fraction;
            
        case LINEAR_ZERO_TO_ONE:
            return fraction;

        default:
            // should never reach here
            assert false;
            return 0.0;
        }
    }
    
    /** 
     * Returns <code>true</code>, if the effect life time has finished at the given time.
     *  
     * @param time the time
     * @return <code>true</code> if the effect has finished
     */
    public boolean isFinished(long time) {
        if (repeat) {
            return false;
        }
        return time > (startTime + duration);
    }
}
