/*
 * Created on 01.05.2010
 *
 */
package de.steffens.airhockey.control;

import de.steffens.airhockey.control.Mouse.MousePosition;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.MovingObject;
import de.steffens.airhockey.model.Rectangle;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

public class HumanPlayer extends Player {

    /**
     * Maximum speed for mouse movements.
     */
    public final static double MAX_SPEED = MovingObject.MAX_VELOCITY_VALUE * 0.95;
    
    /**
     * Temporary scratch vector used for temporary results.
     */
    private Vector2D tmp = VectorFactory.getVector(0, 0);
    
    /**
     * Create a new human player object.
     * 
     * @param index the player index 
     * @param controlledDisk the disk controlled by the player
     * @param puck the puck to play
     */
    public HumanPlayer(int index, Disk controlledDisk, Disk puck) {
        super(index, controlledDisk, puck);
//        // get the initial position
//        Vector2D initialPosition = controlledDisk.getPosition();
//        // map this positions to coordinates in the reachable area
//        Rectangle area = Game.getPlayingField().getReachableArea(index);
//        Vector2D initMousePos = area.getMappedInverse(initialPosition);
    }
    
    /**
     * @see de.steffens.airhockey.control.Player#update(long)
     */
    @Override
    public void update(long newTime) {
        // get the player's disk position
        Vector2D oldPos = controlledDisk.getPosition();
        long deltaT = newTime - controlledDisk.getTimestampNs();

        if (wait) {
            controlledDisk.getVelocity().reset();
            return;
        }
        
        // calculate the new position from mouse position
        MousePosition mousePos = Mouse.getFilteredMousePosition(newTime);
        Rectangle playerArea = Game.getPlayingField().getReachableArea(playerIndex);
        Vector2D newPos = playerArea.clamp(tmp.reset(mousePos.x, mousePos.y));
        
        // calculate a new velocity
        Vector2D newVelocity = tmp.getVelocity(oldPos, newPos, deltaT);

        double speed = newVelocity.getValue();
        if (speed > MAX_SPEED) {
            // limit the mouse speed...
            newVelocity = newVelocity.getNormalized().multiply(MAX_SPEED);
        }
        
        // set the mouse velocity to the players disk
        controlledDisk.setVelocity(newVelocity);
    }


    @Override
    public void setWait(boolean wait) {
        super.setWait(wait);
        // if we start waiting, try to set the mouse position to
        // initial position
        if (wait) {
            Mouse.setMousePosition(controlledDisk.getPosition());
        }
    }
}
