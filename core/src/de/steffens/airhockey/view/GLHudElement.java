/*
 * Created on 25.04.2010
 *
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import de.steffens.airhockey.model.vector.Vector2D;

/**
 * Common class for elements that are rendered as part of the HUD.
 */
abstract public class GLHudElement implements GLViewer {

    private boolean finished = false;

    /**
     * Render any shapes associated with this object.
     * This will be called before render().
     * @param shapeRenderer
     */
    public void renderShapes(ShapeRenderer shapeRenderer) {
        // nothing to do in default implementation.
    }

    /**
     * Render this object with the given sprite batch.
     *
     * @param spriteBatch the batch used for rendering
     */
    public abstract void render(SpriteBatch spriteBatch);


    public void dispose() {
        // nothing done here, may be overridden in sub-class
    }

    @Override
    public void update() {
        // nothing done here, may be overridden in sub-class
    }

    /**
     * Is this element finished?
     * If the element is finished, it can be removed from the display and disposed.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Is this element finished?
     * If the element is finished, it can be removed from the display and disposed.
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }


}

