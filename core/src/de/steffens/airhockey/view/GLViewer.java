/**
 * Created on 20.02.15.
 */
package de.steffens.airhockey.view;

/**
 * Common interface for viewers that render models in OpenGL.
 *
 * @author Steffen Schreiber
 */
public interface GLViewer {

    /**
     * Update the viewer after the model content has changed.
     */
    public void update();

    /**
     * Dispose the viewer
     */
    public void dispose();
}
