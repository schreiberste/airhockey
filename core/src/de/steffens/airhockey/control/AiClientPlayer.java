/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.control;

import java.io.IOException;

import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.Rectangle;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;
import de.steffens.airhockey.net.Client;

public class AiClientPlayer extends Player {

    public final static double MAX_VELOCITY = 6.0 / 1000000000.0;

    // the initial position of the player.
    private final Vector2D initialPosition;

    // Temporary scratch vector used for temporary results.
    private Vector2D tmp = VectorFactory.getVector(0, 0);

    /**
     * Error state of the connection.
     */
    private boolean error = false;


    /**
     * Create a new computer player controlling the given disk.
     *
     * @param index          the player index
     * @param controlledDisk the player's disk
     * @param puck           the puck that the computer player is watching
     */
    public AiClientPlayer(int index, Disk controlledDisk, Disk puck) {
        super(index, controlledDisk, puck);
        initialPosition = controlledDisk.getPosition().copy();
    }


    /**
     * @see de.steffens.airhockey.control.Player#update(long)
     */
    @Override
    public void update(long newTime) {
        if (error) {
            return;
        }

        Vector2D oldPosition = controlledDisk.getPosition();

        // calculate the new position to move to, i.e. the virtual mouse
        Vector2D destinationPos;
        Vector2D puckPos = puck.getPosition();
        Vector2D puckVelocity = puck.getVelocity();

        // if the player should wait, go back to initial position
        if (wait) {
            // move in direction to the initial position
            destinationPos = initialPosition;
        } else {
            // if the puck is moving away from the player with any reasonable
            // speed, go back to the initial position
            Vector2D puckDirection = tmp.reset(puckPos).subtract(oldPosition);
            if ((puckVelocity.getScalarProduct(puckDirection) > 0) &&
                (puckVelocity.getValue() >= (0.05 * MAX_VELOCITY))) {
                // move in direction to the initial position
                destinationPos = initialPosition;
            } else {
                // the puck is moving towards the player. if it is in range, attack...
                Rectangle range = Game.getPlayingField().getReachableArea(playerIndex);
                if (range.isInside(tmp.reset(puckPos))) {
                    // move in direction to the puck
                    destinationPos = puckPos;
                } else {
                    // move in direction to the initial position
                    //                newVelocity = tmp.getVelocity(oldPosition, initialPosition, deltaT);
                    if (puckVelocity.getX() == 0.0 && puckVelocity.getY() == 0.0) {
                        // The puck has not moved, yet, and is not reachable
                        destinationPos = initialPosition;
                    } else {
                        // Move in parallel to the own goal.
                        double x = Math.max(0.0, Math.min(1.0, range.getMappedInverse(tmp.reset(puckPos)).getX()));
                        double y = range.getMappedInverse(tmp.reset(initialPosition)).getY();
                        destinationPos = range.getMappedPosition(tmp.reset(x, y));
                    }
                }
            }
        }
        try {
            Client.getOs().writeDouble(destinationPos.getX());
            Client.getOs().writeDouble(destinationPos.getY());
        } catch (IOException e) {
            error = true;
            System.err.println("Error writing new mouse position.");
            e.printStackTrace();
        }
    }
}
