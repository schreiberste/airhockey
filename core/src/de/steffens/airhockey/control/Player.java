package de.steffens.airhockey.control;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;

/**
 * Base class for player implementations (human or AI players).
 * 
 * @author Steffen Schreiber
 */
public abstract class Player {

    protected final Disk controlledDisk;

    protected final Disk puck;

    protected final int playerIndex;

    protected boolean wait = false;

    protected String name = "unnamed player";

    protected float[] color = new float[] {0.6f, 0.6f, 0.8f};

    public Player(int index, Disk controlledDisk, Disk puck) {
        this.playerIndex = index;
        this.controlledDisk = controlledDisk;
        this.puck = puck;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the player index
     */
    public int getIndex() {
        return playerIndex;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    public float[] getColor() {
        return color;
    }


    public void setColor(float[] color) {
        this.color = color;
    }


    /**
     * @return the controlledDisk
     */
    public Disk getControlledDisk() {
        return controlledDisk;
    }

    /**
     * This is called by the simulation before it starts to perform collision checks and position
     * updates. The player implementation can do any player-related position updates here.
     * 
     * @param newTime the current simulation time
     */
    public abstract void update(long newTime);

    /**
     * The goal that the player is trying to defend was hit.
     */
    public void goalHit() {
        // nothing to do here
    }


    /**
     * Set the player in wait mode.
     * Wait mode is started when a goal was hit. It ends when the next round will start.
     * @param wait
     */
    public void setWait(boolean wait) {
        this.wait = wait;
    }


    /**
     * Read player settings data from the given stream
     * @param is the input stream
     * @throws IOException
     */
    public void readData(DataInputStream is) throws IOException {
        name = is.readUTF();
        color[0] = is.readFloat();
        color[1] = is.readFloat();
        color[2] = is.readFloat();
    }

    /**
     * Write this players data to the given output stream
     * @param os the output stream
     * @throws IOException
     */
    public void writeData(DataOutputStream os) throws IOException {
        os.writeUTF(name);
        os.writeFloat(color[0]);
        os.writeFloat(color[1]);
        os.writeFloat(color[2]);
    }

    /**
     * Write player data to the given output stream.
     * @param os the output stream
     * @throws IOException
     */
    public static void writeData(DataOutputStream os, GameConfiguration config) throws IOException {
        os.writeUTF(config.getPlayerName());
        float[] color = config.getPlayerColor();
        os.writeFloat(color[0]);
        os.writeFloat(color[1]);
        os.writeFloat(color[2]);
    }
}
