/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.control;

import java.io.DataOutputStream;

import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.Rectangle;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * Instances of this class represent a player that is remotely moving the disk.
 * 
 * @author Johannes
 */
public class RemotePlayer extends Player {

    /**
     * Temporary scratch vector used for temporary results.
     */
    private Vector2D tmp = VectorFactory.getVector(0, 0);
    
    /**
     * The virtual mouse position of the remote player.
     */
    private Vector2D virtualMouse = VectorFactory.getVector(0, 0);

    /**
     * The output stream used to send server messages to this player.
     */
    private DataOutputStream os;
    
    /**
     * Create a new remote player object.
     * 
     * @param index the player index 
     * @param controlledDisk the disk controlled by the player
     * @param puck the puck to play
     */
    public RemotePlayer(int index, Disk controlledDisk, Disk puck) {
        super(index, controlledDisk, puck);
    }
    
    /**
     * @see de.steffens.airhockey.control.Player#update(long)
     */
    @Override
    public void update(long newTime) {
        // get the player's disk position
        Vector2D oldPos = controlledDisk.getPosition();
        long deltaT = newTime - controlledDisk.getTimestampNs();
        
        // calculate the new position from mouse position
        Rectangle playerArea = Game.getPlayingField().getReachableArea(playerIndex);
        Vector2D newPos = playerArea.clamp(tmp.reset(virtualMouse));

        // calculate a new velocity
        Vector2D newVelocity = tmp.getVelocity(oldPos, newPos, deltaT);

        double speed = newVelocity.getValue();
        if (speed > HumanPlayer.MAX_SPEED) {
            // limit the mouse speed...
            newVelocity = newVelocity.getNormalized().multiply(HumanPlayer.MAX_SPEED);
        }
        
        // set the mouse velocity to the players disk
        controlledDisk.setVelocity(newVelocity);
    }
    
    public void setMouse(double x, double y) {
    	virtualMouse = virtualMouse.reset(x, y);
    }

    /**
     * Set the output stream that can be used to send server messages to the remote player.
     * @param os the connection output stream
     */
    public void setConnection(DataOutputStream os) {
        this.os = os;
    }
    
    /**
     * Get the output stream that can be used to send server messages to the remote player.
     * @return the connection output stream
     */
    public DataOutputStream getConnection() {
        return os;
    }
}
