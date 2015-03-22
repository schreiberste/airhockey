/*
 * Created on 26.04.2010
 *
 */
package de.steffens.airhockey.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.model.CollisionListener.CollisionEvent;
import de.steffens.airhockey.model.CollisionListener.DiskDiskCollision;
import de.steffens.airhockey.model.CollisionListener.DiskWallCollision;

/**
 * This is the main class of the game state simulation.
 * It takes care of the simulation time and updating objects positions and movements.
 *
 * @author Steffen Schreiber
 */
public class Simulation {
    
    public final static String SIMULATION_THREAD_NAME = "Simulation Update";

    private int wallIdx = 0;
    private int diskIdx = 0;

    /** list of all known disks */
    private final ArrayList<Disk> disks = new ArrayList<Disk>();
    /** list of all disks that should be updated by the simulation */
    private final ArrayList<Disk> updatePosDisks = new ArrayList<Disk>();
    
    /** list of listeners interested in collision events */ 
    private final ArrayList<CollisionListener> collisionListeners =
        new ArrayList<CollisionListener>();
    
    /** list of players */
    private final ArrayList<Player> players = new ArrayList<Player>();
    
    /** list of all walls */
    private final ArrayList<Wall> walls = new ArrayList<Wall>();
    
    /** the time stamp of the last simulation update */
    protected long lastTime = 0;
    
    /** the base time stamp at simulation start */
    private final long timeBase = System.nanoTime();
    
    /** flag indicating, whether object positions should be updated with time */
    private boolean advanceSim = true;
    
    /** counter for calls to blockSimulation */
    private int blockSimulationCt = 0;
    
    /** Timer used to regularly update the simulation */
    private final Timer timer = new Timer(SIMULATION_THREAD_NAME, true);
    
    /** list of simulation listeners interested in position updates */
    private final ArrayList<SimulationListener> simulationListeners =
        new ArrayList<SimulationListener>();
    
    /**
     * Add a new listener for collision events.
     * 
     * @param listener the new collision event listener.
     */
    public void addCollisionListener(CollisionListener listener) {
        collisionListeners.add(listener);
    }
    
    /**
     * Remove a collision listener from the list of registered collision 
     * listeners.
     * 
     * @param listener the listener to remove
     */
    public void removeCollisionListener(CollisionListener listener) {
        collisionListeners.remove(listener);
    }
    
    /**
     * Add a new listener for simulation updates.
     * 
     * @param listener the new simulation update listener.
     */
    public void addSimulationListener(SimulationListener listener) {
    	synchronized (simulationListeners) {
            simulationListeners.add(listener);
        }
    }
    
    /**
     * Remove a simulation update listener from the list of registered
     * simulation update listeners.
     * 
     * @param listener the listener to remove
     */
    public void removeSimulationListener(SimulationListener listener) {
    	synchronized (simulationListeners) {
	        simulationListeners.remove(listener);
        }
    }
    
    /**
     * Notifies all registered simulation listeners that the simulation was updated.
     */
    public void notifySimulationListeners() {
    	synchronized (simulationListeners) {
	        for (SimulationListener sl : simulationListeners) {
	        	sl.update();
	        }
        }
    }
    
    /**
     * Add a disk. This is equivalent to calling 
     * <code>addDisk(disk, true)</code>
     * 
     * @param disk the new disk
     */
    public synchronized void addDisk(Disk disk) {
        addDisk(disk, true);
    }
    
    /**
     * Add a disk to the simulation.
     * For fixed disks it is possible to exclude the disk from position updates.
     * 
     * @param disk the new disk model to add
     * @param updatePositions the simulation should update the position of the disk
     */
    public synchronized void addDisk(Disk disk, boolean updatePositions) {
        disk.setTimestampNs(lastTime);
        disk.setIndex(diskIdx++);
        disks.add(disk);
        if (updatePositions) {
            updatePosDisks.add(disk);
        }
    }

    public synchronized void removeDisk(Disk disk) {
        disks.remove(disk);
        updatePosDisks.remove(disk);
    }

    public List<Disk> getDisks() {
        return Collections.unmodifiableList(disks);
    }

    /**
     * Add a new player to the simulation.
     * 
     * @param player the new player
     */
    public synchronized void addPlayer(Player player) {
        players.add(player);
    }
    
    /**
     * Add a new wall to the simulation.
     * Walls will participate in collision checks.
     * 
     * @param wall the new wall
     */
    public synchronized void addWall(Wall wall) {
        wall.setIndex(wallIdx++);
        walls.add(wall);
    }
    
    /**
     * Remove a wall from the simulation.
     * This wall will no longer participate in collision checks.
     * 
     * @param wall the new wall
     */
    public synchronized void removeWall(Wall wall) {
        walls.remove(wall);
    }


    /**
     * Write current simulation state to the given output stream.
     * @param os the output stream
     * @throws IOException
     */
    public synchronized void writeSimulationUpdate(DataOutputStream os) throws IOException {
        blockSimulationUpdates();
        // write number and positions of the moving disks
        int disksNr = updatePosDisks.size();
        os.writeInt(disksNr);
        for (int i=0; i<disksNr; i++) {
            updatePosDisks.get(i).writeUpdate(os);
        }
        allowSimulationUpdates();
    }


    /**
     * Read simulation state from the given input stream.
     * @param is the input stream
     * @throws IOException
     */
    public synchronized void readSimulationUpdate(DataInputStream is) throws IOException {
        blockSimulationUpdates();
        int disksNr = is.readInt();
        int knownDisksSize = updatePosDisks.size();
        int toUpdate = Math.min(disksNr, knownDisksSize);
        int toSkip = Math.max(0, disksNr - toUpdate);
        if (toUpdate < knownDisksSize) {
            System.out.println("Client: no simulation data for " + (knownDisksSize - toUpdate) + " disks.");
        }
        for (int i=0; i<toUpdate; i++) {
            updatePosDisks.get(i).update(is);
        }
        allowSimulationUpdates();
        // skip update data for unknown disks
        if (toSkip > 0) {
            System.out.println("Client: Skipping simulation update for " + toSkip + " disks");
            for (int i = 0; i < toSkip; i++) {
                Disk.skipUpdate(is);
            }
        }
    }

    /**
     * Update the simulation.
     * This will update the simulation time, the players state and the positions of 
     * all moving objects.
     * 
     */
    public synchronized void update() {
        while (blockSimulationCt > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long newTime = System.nanoTime() - timeBase;
        
        if (!Collision.sanityChecks(disks, walls)) {
            System.out.println("Insane before updating time " + lastTime + " to " + newTime);
        }
        
        // let the players update their state
        for (Player player : players) {
            player.update(newTime);
        }

        if (!Collision.sanityChecks(disks, walls)) {
            System.out.println("Insane after updating players to time " + newTime);
        }
        
        // update simulation of all moving objects
        updatePositions(newTime);

        if (!Collision.sanityChecks(disks, walls)) {
            System.out.println("Insane after updating simulation to time " + newTime);
        }
        
        notifySimulationListeners();
    }

    /**
     * Block simulation updates until {@link Simulation#allowSimulationUpdates} is called.
     */
    public synchronized void blockSimulationUpdates() {
        blockSimulationCt++;
    }
    
    /**
     * Allow simulation updates again, after they were blocked using 
     * {@link Simulation#blockSimulationUpdates()}
     */
    public synchronized void allowSimulationUpdates() {
        assert blockSimulationCt > 0;
        
        blockSimulationCt--;
        if (blockSimulationCt == 0) {
            notifyAll();
        }
    }
    
    /**
     * Check for collisions and update positions of the moving objects.
     * 
     * @param newTime
     */
    private synchronized void updatePositions(long newTime) {
        if (newTime <= lastTime) {
            System.err.println("Ignoring time in the past!");
            return;
        }
        List<CollisionEvent> collisionEvents = null;
        
        if (advanceSim) {
            // handle all collisions
            collisionEvents = Collision.checkCollisions(
                    walls, disks, lastTime, newTime);

            if (!Collision.sanityChecks(disks, walls)) {
                System.out.println("Insane after handling collisions in time [" 
                        + lastTime + ", " + newTime + "]");
            }
            
            // move all updatable disks to the final position
            for (Disk disk : updatePosDisks) {
                disk.update(newTime);
            }
            
            if (!Collision.sanityChecks(disks, walls)) {
                System.out.println("Insane after updating positions in time [" 
                        + lastTime + ", " + newTime + "]");
            }
        }
        
        // make sure the new time is set in all objects
        for (Disk disk : disks) {
            disk.setTimestampNs(newTime);
        }
        
        lastTime = newTime;
        
        // inform collision listeners about any collision events
        if (collisionEvents != null) {
            for (CollisionEvent event : collisionEvents) {
                notifyCollisionListeners(event);
            }
        }
    }


    public void notifyCollisionListeners(CollisionEvent event) {
        if (event instanceof DiskDiskCollision) {
            DiskDiskCollision collision = (DiskDiskCollision) event;
            for (CollisionListener listener : collisionListeners) {
                listener.collisionOccurred(collision);
            }
        }
        else {
            DiskWallCollision collision = (DiskWallCollision) event;
            for (CollisionListener listener : collisionListeners) {
                listener.collisionOccurred(collision);
            }
        }
    }

    /**
     * Toggle pause on this simulation.
     * If the simulation is paused, time will continue to be updated, but objects
     * won't move.
     */
    public synchronized void togglePause() {
        advanceSim = !advanceSim;
        System.out.println("\n Simulation " + (advanceSim ? "running." : "paused."));
    }

    /**
     * Returns the current system time stamp in ns.
     * The system time is given in nanos since the same time base that the simulation
     * is relying on. The difference is, that this call will not return the time stamp
     * of the last (or current) simulation time update, but will fetch a new time stamp
     * at the time of the call. 
     * 
     * @return the current system time. 
     */
    public long getCurrentTime() {
        return System.nanoTime() - timeBase;
    }
    
    /**
     * Returns the current simulation time stamp in ns.
     * This is the time of the last simulation update, measured in nanos since a fixed
     * time base.
     * 
     * @return the current simulation time. 
     */
    public synchronized long getSimulationTime() {
        return lastTime;
    }

    /**
     * Starts the automatically updated simulation.
     */
    public void start() {
    	int fps = Game.getTargetFPS();
    	if (fps < 0) {
    		return;
    	}
    	int period = 1000 / fps;
    	timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				update();
			}
		}, 0, period);
    }
    
    /**
     * Stops the automatic updates of the simulation.
     */
    public void stop() {
    	timer.cancel();
    }


    public Disk getDisk(int diskIndex) {
        for (int i=0; i<disks.size(); i++) {
            Disk disk = disks.get(i);
            if (disk.getIndex() == diskIndex) {
                return disk;
            }
        }
        System.err.println("Could not get disk " + diskIndex);
        return null;
    }


    public Wall getWall(int wallIndex) {
        for (int i=0; i<walls.size(); i++) {
            Wall wall = walls.get(i);
            if (wall.getIndex() == wallIndex) {
                return wall;
            }
        }
        System.err.println("Could not get wall " + wallIndex);
        return null;
    }

    /**
	 * Interface used to propagate simulation updates.
	 */
	public interface SimulationListener {
		/**
		 * Called when then simulation was updated.
		 */
		public void update();
	}
}
