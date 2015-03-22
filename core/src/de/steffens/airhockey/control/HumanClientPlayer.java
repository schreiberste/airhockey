/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.control;

import java.io.IOException;

import de.steffens.airhockey.control.Mouse.MousePosition;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.MovingObject;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;
import de.steffens.airhockey.net.Client;

public class HumanClientPlayer extends Player {

    /**
     * Maximum speed for mouse movements.
     */
    public final static double MAX_SPEED = MovingObject.MAX_VELOCITY_VALUE * 0.95;

    /**
     * Last known mouse position.
     */
    private Vector2D mouse = VectorFactory.getVector(0, 0);
    
    /**
     * Error state of the connection.
     */
    private boolean error = false;
    
    /**
     * Create a new human player object.
     * 
     * @param index the player index 
     * @param controlledDisk the disk controlled by the player
     * @param puck the puck to play
     */
    public HumanClientPlayer(int index, Disk controlledDisk, Disk puck) {
        super(index, controlledDisk, puck);
    }
    
    /**
     * @see de.steffens.airhockey.control.Player#update(long)
     */
    @Override
    public void update(long newTime) {
    	if (error) {
    		return;
    	}
        // We just send the current mouse position to the server.
        // The server simulation will handle this in a RemotePlayer.

        MousePosition mousePos = Mouse.getFilteredMousePosition(newTime);

        if (mouse.getX() == mousePos.x && mouse.getY() == mousePos.y) {
        	return;
        }
        mouse = mouse.reset(mousePos.x, mousePos.y);
        try {
	        Client.getOs().writeDouble(mousePos.x);
	        Client.getOs().writeDouble(mousePos.y);
        } catch (IOException e) {
        	error = true;
	        System.err.println("Error writing new mouse position.");
	        e.printStackTrace();
        }
    }
}
