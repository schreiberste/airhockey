/**
 * Created on 21.02.15.
 */
package de.steffens.airhockey.net;

import java.io.DataOutputStream;
import java.io.IOException;

import de.steffens.airhockey.model.CollisionListener;
import de.steffens.airhockey.model.DestroyableWall;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.Simulation.SimulationListener;

/**
 * The listener for simulation and collision events on the server side that
 * sends updates to the connected client.
 */
public class ServerSimulationForward implements SimulationListener, CollisionListener {

    private final DataOutputStream os;
    private boolean error = false;


    public ServerSimulationForward(DataOutputStream os) {
        this.os = os;
    }

    @Override
    public void update() {
        if (error) {
            return;
        }
        try {
            os.writeInt(MSG.SIMULATION_UPDATE);
            Game.getSimulation().writeSimulationUpdate(os);
            os.writeInt(Game.getPlayerCount());
            int[] score = Game.getScore();
            for (int i = 0; i < Game.getPlayerCount(); i++) {
                os.writeInt(score[i]);
            }
            os.flush();
        } catch (IOException e) {
            disconnectOnError("Error sending simulation update.", e);
        }
    }


    @Override
    public void collisionOccurred(DiskWallCollision collision) {
        if (error) {
            return;
        }
        try {
            System.out.println("[" + Thread.currentThread().getName()+"]: Collision with wall " + collision.wall.getIndex() + (collision.wall instanceof DestroyableWall ? " (destroyable)" : ""));
            os.writeInt(MSG.COLLISION_DISK_WALL);
            collision.write(os);
            os.flush();
        } catch (IOException e) {
            disconnectOnError("Error sending disk-wall collision.", e);
        }
    }


    @Override
    public void collisionOccurred(DiskDiskCollision collision) {
        if (error) {
            return;
        }
        try {
            os.writeInt(MSG.COLLISION_DISK_DISK);
            collision.write(os);
            os.flush();
        } catch (IOException e) {
            disconnectOnError("Error sending disk-disk collision.", e);
        }
    }

    private void disconnectOnError(String msg, IOException e) {
        System.err.println(msg);
        e.printStackTrace();
        error = true;
        Game.getSimulation().removeSimulationListener(this);
        Game.getSimulation().removeCollisionListener(this);
    }
}
