/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * Abstract base class for playing field models.
 * 
 * @author Steffen Schreiber
 */
public abstract class AbstractPlayingFieldBase implements PlayingField {

    public final static long MILLI = 1000*1000;
    public final static long PHASE = 500 * MILLI;
    public final static long SHOW_TIME = 0 * MILLI;
    public final static long FADE_TIME = 1000 * MILLI;
    public final static long WAIT_TIME = 3 * PHASE;

    private final ArrayList<Wall> allWalls = new ArrayList<Wall>();
    private final ArrayList<Wall> collisionWalls = new ArrayList<Wall>();
    private final ArrayList<Disk> allDisks = new ArrayList<Disk>();
    private final ArrayList<Disk> collisionDisks = new ArrayList<Disk>();
    private final ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
    
    private final Vector2D defaultKickOff = VectorFactory.getVector(0.45, 0.5);
    private final Vector2D defaultInitial = VectorFactory.getVector(0.5, 0.1);

    protected final int numPlayers;

    // the reachable areas for the players
    protected final Rectangle[] reachableAreas;
    // the backup copies of the up-vectors of the reachable areas
    protected final Vector2D[] reachableAreaUps;
    // the camera positions for the players
    protected float[][] cameraPositions;

    protected long lastResetTime = 0;
    protected int inWait = 0;


    public AbstractPlayingFieldBase(int numPlayers) {
        this.numPlayers = numPlayers;
        reachableAreas = new Rectangle[numPlayers];
        reachableAreaUps = new Vector2D[numPlayers];
        cameraPositions = new float[numPlayers][3];
    }

    /**
     * Called by sub-classes when playing field setup is finished.
     */
    public void setupFinished() {
        // make backup copies of the up-vectors
        for (int i=0; i<numPlayers; i++) {
            reachableAreaUps[i] = reachableAreas[i].getUp().copy();
        }
    }


    /**
     * @see de.steffens.airhockey.model.PlayingField#getNrOfPlayers()
     */
    @Override
    public int getNrOfPlayers() {
        return numPlayers;
    }

    /**
     * @see de.steffens.airhockey.model.PlayingField#getReachableArea(int)
     */
    @Override
    public Rectangle getReachableArea(int playerIndex) {
        assert playerIndex < numPlayers;

        return reachableAreas[playerIndex];
    }

    /**
     * Get the camera position for the view of the given player.
     *
     * @param playerIndex the index of the player
     * @return the camera position
     */
    public float[] getCameraPosition(int playerIndex) {
        assert playerIndex < numPlayers;

        return cameraPositions[playerIndex];
    }

    /**
     * Add a new wall to the playing field.
     * For performance optimization, it is possible to disable 
     * collision checks for unreachable walls.
     * 
     * @param wall
     * @param enableCollisionChecks enable collision checks for this wall
     */
    public void addWall(Wall wall, boolean enableCollisionChecks) {
        allWalls.add(wall);
        if (enableCollisionChecks) {
            collisionWalls.add(wall);
            Game.getSimulation().addWall(wall);
        }
    }

    public void removeWall(Wall wall) {
        allWalls.remove(wall);
        if (collisionWalls.remove(wall)) {
            // this is a collision wall, remove it from the simulation as well
            Game.getSimulation().removeWall(wall);
        }
    }
    
    /**
     * Add a round wall corner to the playing field. 
     * The corner is a disk cylinder placed at the given center position with the given
     * radius and height. For performance optimization, it is possible to disable 
     * collision checks for unreachable corners.
     * 
     * @param cornerDisk 
     * @param enableCollisionChecks enable collision checks for this corner
     */
    public void addWallCorner(Disk cornerDisk, boolean enableCollisionChecks) {
        allDisks.add(cornerDisk);
        cornerDisk.setFixed();
        if (enableCollisionChecks) {
            collisionDisks.add(cornerDisk);
            Game.getSimulation().addDisk(cornerDisk, false);
        }
    }

    /**
     * Add a rectangle to the playing field.
     * 
     * @param rectangle the rectangle to add
     */
    public void addRectangle(Rectangle rectangle) {
        rectangles.add(rectangle);
    }
    
    
    /**
     * @see de.steffens.airhockey.model.PlayingField#getCollisionCorners()
     */
    @Override
    public List<Disk> getCollisionCorners() {
        return collisionDisks;
    }

    /**
     * @see de.steffens.airhockey.model.PlayingField#getCollisionWalls()
     */
    @Override
    public List<Wall> getCollisionWalls() {
        return collisionWalls;
    }

    /**
     * @see de.steffens.airhockey.model.PlayingField#getCorners()
     */
    @Override
    public List<Disk> getCorners() {
        return allDisks;
    }

    /**
     * @see de.steffens.airhockey.model.PlayingField#getWalls()
     */
    @Override
    public List<Wall> getWalls() {
        return allWalls;
    }

    /**
     * @see de.steffens.airhockey.model.PlayingField#getRectangles()
     */
    @Override
    public List<Rectangle> getRectangles() {
        return rectangles;
    }
    
    @Override
    public void write(DataOutputStream os) throws IOException {
        // Currently, we only need to write the type of the playing field.
    	if (this instanceof PlayingFieldTwoPlayers) {
    		os.writeByte(1);
    	} else if (this instanceof PlayingFieldNPlayers) {
    	        os.writeByte(2);
    	        os.writeInt(getNrOfPlayers());
    	} else {
    		throw new RuntimeException("Unknown playing field: " + this);
    	}
    }
    
    /**
     * Reads the playing field and its attributes so
     * that a remote client may set it up correctly.
     * 
     * @param is the input stream to use for reading
     * @return the playing field read from the stream
     * @throws IOException if an error occurs during reading
     */
    public static PlayingField read(DataInputStream is) throws IOException {
    	byte type = is.readByte();
    	switch (type) {
        case 1:
	        return new PlayingFieldTwoPlayers();
        case 2:
            int numPlayers = is.readInt();
            return new PlayingFieldNPlayers(numPlayers);
            
        default:
	        throw new RuntimeException("Unknown playing field: " + type);
        }
    }
    
    
    /**
     * Get the kick-off position for the given player
     * 
     * @param playerIndex the index of the player
     * @return the kick-off position
     */
    public Vector2D getKickoffPosition(int playerIndex) {
        return getReachableArea(playerIndex).getMappedPosition(defaultKickOff.copy());
    }

    /**
     * Get the initial position for the given player
     * 
     * @param playerIndex the index of the player
     * @return the initial position
     */
    public Vector2D getInitialPosition(int playerIndex) {
        return getReachableArea(playerIndex).getMappedPosition(defaultInitial.copy());
    }

    private int resetCount = 0;

    @Override
    public final void resetState(boolean newGame) {
        // start waiting time before a new round begins
        lastResetTime = Game.getSimulation().getCurrentTime();
        inWait = 3;
        Game.getDisplay().showFlashMsg("3", SHOW_TIME, FADE_TIME);

        for (int i = 0; i < Game.getPlayerCount(); i++) {
        	Game.getPlayer(i).getControlledDisk().setPosition(getInitialPosition(i));
            Game.getPlayer(i).setWait(true);
        }
        resetStateImpl();

        System.out.println("\n\nPlaying field reset " + (++resetCount));
        System.out.print("Walls now: ");
        for (Wall wall : getWalls()) {
            System.out.print(wall.getIndex() + " ");
        }
        System.out.println();

        Game.getSimulation().update();
    }

    protected void stopGame() {
        int winner = 0;
        int[] score = Game.getScore();
        for (int i = 1; i < numPlayers; i++) {
            if (score[i] > score[winner]) {
                winner = i;
            }
        }
        Game.getDisplay().showFlashMsg("Player " + Game.getPlayer(winner).getName() + " won", Long.MAX_VALUE, 0);
        for (int i = 0; i < Game.getPlayerCount(); i++) {
            Game.getPlayer(i).getControlledDisk().setPosition(getInitialPosition(i));
            Game.getPlayer(i).setWait(true);
        }
        System.out.println("\n\nPlayer " + Game.getPlayer(winner).getName() + " won");
    }

    protected abstract void resetStateImpl();

    @Override
    public void update() {
        if (inWait > 0) {
            long nanosSinceReset = Game.getSimulation().getCurrentTime() - lastResetTime;
            if (nanosSinceReset > WAIT_TIME) {
                // finished waiting
                inWait = 0;
                Game.getDisplay().showFlashMsg("Go!", SHOW_TIME / 2, FADE_TIME / 2);
                Game.getPuck().getMaterial().setAlpha(1f);
                // restore reachable areas
                for (int i=0; i<numPlayers; i++) {
                    reachableAreas[i].getUp().reset(reachableAreaUps[i]);
                }
                for (int i = 0; i < Game.getPlayerCount(); i++) {
                    Game.getPlayer(i).setWait(false);
                }
            }
            else {
                if (nanosSinceReset > 2 * PHASE && inWait == 2) {
                    inWait = 1;
                    Game.getDisplay().showFlashMsg("1", SHOW_TIME, FADE_TIME);
                }
                else if (nanosSinceReset > PHASE && inWait == 3) {
                    inWait = 2;
                    Game.getDisplay().showFlashMsg("2", SHOW_TIME, FADE_TIME);
                }
                double fraction = (double) nanosSinceReset / (double) WAIT_TIME;
                float alpha = Math.max(0f, (3f * (float)fraction) -2f);
                double factor = 0.1 + 0.9 * fraction;
                Game.getPuck().getMaterial().setAlpha(alpha);
                // restrict reachable areas
                for (int i=0; i<numPlayers; i++) {
                    reachableAreas[i].getUp().reset().addMultiple(reachableAreaUps[i], factor);
                }
            }
        }
    }
}
