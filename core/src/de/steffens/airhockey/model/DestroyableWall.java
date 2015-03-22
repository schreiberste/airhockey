/*
 * Created on 25.04.2010
 *
 */

package de.steffens.airhockey.model;

import de.steffens.airhockey.model.CollisionListener.CollisionEvent;
import de.steffens.airhockey.model.vector.Vector2D;

/**
 * A wall that can be destroyed by collision hits.
 * 
 * @author Steffen Schreiber
 */
public class DestroyableWall extends Wall {

    private int nrHitsToDestroy;
    
    private int nrHits;

    private int actionCode;

    /**
     * @param start
     * @param face
     * @param normal
     * @param thickness
     * @param height
     * @param nrHits 
     */
    public DestroyableWall(Vector2D start, Vector2D face, Vector2D normal, 
            double thickness, double height, int nrHits) {
        super(start, face, normal, thickness, height);
        
        this.nrHitsToDestroy = nrHits;
        this.nrHits = 0;
    }


    public int getActionCode() {
        return actionCode;
    }


    public void setActionCode(int actionCode) {
        this.actionCode = actionCode;
    }


    public void resetHits() {
        nrHits = 0;
    }
    
    /**
     * Returns a value indicating the health state of this wall. 
     * The returned value is in range 0 to 1, where 1 means that the wall was never hit,
     * 0 means that the wall was destroyed.
     * 
     * @return the health state value
     */
    public double getWallHealth() {
        return (double) (nrHitsToDestroy - nrHits) / nrHitsToDestroy;
    }
    
    /**
     * Method that has to be called when the wall was hit.
     * This will return <code>true</code>, if the wall would be destroyed by this hit.
     * 
     * @param collision the collision event
     * @return flag indicating, whether the wall was destroyed
     */
    public boolean wallWasHit(CollisionEvent collision) {
        nrHits++;
        return (nrHits >= nrHitsToDestroy); 
    }
}
