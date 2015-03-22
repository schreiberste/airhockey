/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.model.vector;

import java.lang.reflect.Constructor;

/**
 * Factory for creating vectors depending on currently set policy.
 * 
 * @author Johannes Scheerer
 */
public class VectorFactory {
	
	private static Constructor<? extends Vector2D> constructor = null;

	/**
	 * Returns a new instance of the currently selected vector implementation.
	 * 
	 * @param x the x coordinate of the vector
	 * @param y the y coordinate of the vector
	 * @return a new instance of the currently selected vector implementation.
	 */
	public static Vector2D getVector(double x, double y) {
		try {
	        return constructor.newInstance(x, y);
        } catch (Exception e) {
	        throw new RuntimeException(e);
        }
	}

	/**
	 * Set the creation policy to immutable vectors.
	 */
	public static void useImmutableVector() {
		try {
	        constructor = ImmutableVector2D.class.getConstructor(Double.TYPE, Double.TYPE);
        } catch (Exception e) {
	        throw new RuntimeException(e);
        }
	}

	/**
	 * Set the creation policy to mutable vectors.
	 */
	public static void useMutableVector() {
		try {
	        constructor = MutableVector2D.class.getConstructor(Double.TYPE, Double.TYPE);
        } catch (Exception e) {
	        throw new RuntimeException(e);
        }
	}
}
