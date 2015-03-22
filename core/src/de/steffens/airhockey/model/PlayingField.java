/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import de.steffens.airhockey.model.vector.Vector2D;

/**
 * Common interface for playing fields.
 * 
 * @author Steffen Schreiber
 */
public interface PlayingField {

    /**
     * Returns the number of players for this playing field.
     * 
     * @return the number of players.
     */
    public int getNrOfPlayers();
    
    /**
     * Get the list of all walls of this playing field.
     * 
     * @return the list of wall models
     */
    public List<Wall> getWalls();

    /**
     * Get the list of all walls with enabled collision checks.
     * 
     * @return the list of wall models with enabled collision checks
     */
    public List<Wall> getCollisionWalls();
    
    /**
     * Get the list of all wall corner disks of this playing field.
     * 
     * @return the list of disk models
     */
    public List<Disk> getCorners();

    /**
     * Get the list of all walls corner disks with enabled collision checks.
     * 
     * @return the list of disk models with enabled collision checks
     */
    public List<Disk> getCollisionCorners();

    /**
     * Get the list of rectangles of this playing field (if any).
     * These are just the rectangles that may be used for visual presentation
     * of the playing field, they don't have any effect on gameplay - in particular,
     * the rectangles that define the reachable areas for each player are not
     * part of this list.
     * 
     * @return the list of rectangles
     */
    public List<Rectangle> getRectangles();
    
    /** 
     * Get the rectangle defining the reachable area of the given player.
     *  
     * @param playerIndex the index of the player
     * @return the reachable area rectangle
     */
    public Rectangle getReachableArea(int playerIndex);
    
    /**
     * Get the kick-off position for the given player
     * 
     * @param playerIndex the index of the player
     * @return the kick-off position
     */
    public Vector2D getKickoffPosition(int playerIndex);

    /**
     * Get the initial position for the given player
     * 
     * @param playerIndex the index of the player
     * @return the initial position
     */
    public Vector2D getInitialPosition(int playerIndex);
    
    /**
     * Get the camera position for the view of the given player.
     * 
     * @param playerIndex the index of the player
     * @return the camera position
     */
    public float[] getCameraPosition(int playerIndex);
    
    /**
     * Create puck disk with dimensions and material matching the playing field.
     * 
     * @return the new disk model
     */
    public Disk createPuckDisk();

    /**
     * Create a player disk with dimensions and material matching the playing field.
     * Different players may have different disks (different colors etc.).
     * 
     * @param playerIndex the index of the player to create a disk for.
     * @return the new disk model
     */
    public Disk createPlayerDisk(int playerIndex);

    /**
     * This is called before rendering to allow animations on the playing field
     * that are not handled by the simulation.
     */
    public void update();

    /**
     * Reset the playing field to its initial state.
     * This method is called when a new game starts or when a goal 
     * was hit. Playing fields that get modified while playing
     * (for example because of destroyable blocks) can restore their
     * state at this point.
     *  
     * @param newGame if this flag is set, reset the playing field
     *      for a new game. Otherwise, reset for a new round after
     *      the goal was hit. 
     */
    public void resetState(boolean newGame);
    
    /**
     * Writes the type of the playing field and its attributes so
     * that a remote client may set it up correctly.
     * 
     * @param os the output stream to use for writing
     * @throws IOException if an error occurs during writing
     */
    public void write(DataOutputStream os) throws IOException;
    
}
