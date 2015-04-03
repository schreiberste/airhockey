package de.steffens.airhockey.control;

import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.Rectangle;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * A simple implementation of a computer opponent player.
 *
 * @author Steffen Schreiber
 */
public class AIPlayer extends Player {

    public final static double MAX_VELOCITY = 6.0 / 1000000000.0;

    // the initial position of the player.
    private final Vector2D initialPosition;

    // Temporary scratch vector used for temporary results.
    private Vector2D tmp = VectorFactory.getVector(0, 0);


    /**
     * Create a new computer player controlling the given disk.
     *
     * @param index          the player index
     * @param controlledDisk the player's disk
     * @param puck           the puck that the computer player is watching
     */
    public AIPlayer(int index, Disk controlledDisk, Disk puck) {
        super(index, controlledDisk, puck);
        initialPosition = controlledDisk.getPosition().copy();
        color[0] = 0.5f;
        color[1] = 0.5f;
        color[2] = 0.5f;
    }


    /**
     * @see de.steffens.airhockey.control.Player#update(long)
     */
    @Override
    public void update(long newTime) {
        long deltaT = newTime - controlledDisk.getTimestampNs();
        Vector2D oldPosition = controlledDisk.getPosition();

        // calculate the new velocity for the controlled disk:
        Vector2D newVelocity;
        Vector2D puckPos = puck.getPosition();
        Vector2D puckVelocity = puck.getVelocity();

        // if the player should wait, go back to initial position
        if (wait) {
            // move in direction to the initial position
            newVelocity = tmp.getVelocity(oldPosition, initialPosition, deltaT);
        } else {
            // if the puck is moving away from the player with any reasonable
            // speed, go back to the initial position
            Vector2D puckDirection = tmp.reset(puckPos).subtract(oldPosition);
            if ((puckVelocity.getScalarProduct(puckDirection) > 0) &&
                (puckVelocity.getValue() >= (0.05 * MAX_VELOCITY))) {
                // move in direction to the initial position
                newVelocity = tmp.getVelocity(oldPosition, initialPosition, deltaT);
            } else {
                // the puck is moving towards the player. if it is in range, attack...
                Rectangle range = Game.getPlayingField().getReachableArea(playerIndex);
                if (range.isInside(tmp.reset(puckPos))) {
                    // move in direction to the puck
                    newVelocity = tmp.getVelocity(oldPosition, puckPos, deltaT);
                    // add some randomness
                    double x = newVelocity.getX();
                    x = x + (0.02 * x * (Math.random() - 0.5));
                    double y = newVelocity.getY();
                    y = y + (0.02 * y * (Math.random() - 0.5));
                    newVelocity.reset(x, y);
                } else {
                    // FIXME: moving at the base line is not working for generic playing fields
                    // if the puck is out of range, go back to the initial position,
                    // but move side-ways to the puck
                    //                Vector2D destination = tmp.reset(puckPos.getX(), initialPosition.getY());
                    //                newVelocity = tmp.getVelocity(oldPosition, destination, deltaT);

                    // move in direction to the initial position
                    //                newVelocity = tmp.getVelocity(oldPosition, initialPosition, deltaT);
                    if (puckVelocity.getX() == 0.0 && puckVelocity.getY() == 0.0) {
                        // The puck has not moved, yet, and is not reachable
                        newVelocity = tmp.getVelocity(oldPosition, initialPosition, deltaT);
                    } else {
                        // Move in parallel to the own goal.
                        double x = Math.max(0.0, Math.min(1.0, range.getMappedInverse(tmp.reset(puckPos)).getX()));
                        double y = range.getMappedInverse(tmp.reset(initialPosition)).getY();
                        newVelocity = tmp.getVelocity(oldPosition, range.getMappedPosition(tmp.reset(x, y)), deltaT);
                    }
                }
            }
        }
        // make sure we don't exceed MAX_VELOCITY
        double speed = newVelocity.getValue();
        if (speed > MAX_VELOCITY) {
            newVelocity =
                newVelocity.getNormalized().multiply(MAX_VELOCITY);
        }

        controlledDisk.setVelocity(newVelocity);
    }
}
