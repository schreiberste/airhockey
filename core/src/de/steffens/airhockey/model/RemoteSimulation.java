/*
 * Created on 13.02.2011
 */
package de.steffens.airhockey.model;


/**
 * This special class of simulation does no simulation by itself
 * but receives all position updates from a server.
 * 
 * @author Johannes Scheerer
 */
public class RemoteSimulation extends Simulation {

	@Override
	public synchronized void update() {
	    // Just update the time stamp and notify the players.
	    long newTime = getCurrentTime();
        for (int i = 0; i < Game.getPlayerCount(); i++) {
            Game.getPlayer(i).update(newTime);
        }
		lastTime = newTime;
	}
}
