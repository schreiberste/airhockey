/*
 * Created on 26.04.2010
 *
 */

package de.steffens.airhockey.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * Interface for listeners interested in collision events.
 * 
 * @author Steffen Schreiber
 */
public interface CollisionListener {

    /**
     * Simple structure that holds a collision event.
     */
    public static class CollisionEvent {
        public long time;
        public Vector2D point;
        public Vector2D velocity;

        /**
         * Writes the collision event.
         *
         * @param os the output stream to use for writing
         * @throws java.io.IOException if an error occurs during writing
         */
        public void write(DataOutputStream os) throws IOException {
            os.writeLong(time);
            os.writeDouble(point.getX());
            os.writeDouble(point.getY());
            os.writeDouble(velocity.getX());
            os.writeDouble(velocity.getY());
        }

        protected void readBase(DataInputStream is) throws IOException {
            time = is.readLong();
            point = VectorFactory.getVector(is.readDouble(), is.readDouble());
            velocity = VectorFactory.getVector(is.readDouble(), is.readDouble());
        }
    }

    /**
     * A disk-wall-collision event.
     *
     * @author Steffen Schreiber
     */
    public static class DiskWallCollision extends CollisionEvent {
        public Disk disk;
        public Wall wall;
        public Wall.Face wallFace;
        public Vector2D wallEdge;

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            if (wallEdge == null) {
                return "[Disk - Wall face] Collision";
            }
            return "[Disk - Wall edge] Collision";
        }

        @Override
        public void write(DataOutputStream os) throws IOException {
            super.write(os);
            os.writeInt(disk.getIndex());
            os.writeInt(wall.getIndex());
        }

        public static DiskWallCollision read(DataInputStream is) throws IOException {
            DiskWallCollision result = new DiskWallCollision();
            result.readBase(is);
            result.disk = Game.getSimulation().getDisk(is.readInt());
            int wallIdx = is.readInt();
            System.out.println("Client: Collision with wall " + wallIdx);
            result.wall = Game.getSimulation().getWall(wallIdx);
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (! (obj instanceof DiskWallCollision)) return false;
            
            DiskWallCollision other = (DiskWallCollision)obj;
            return (other.disk == disk) && (other.wall == wall) 
                    && (other.wallFace == wallFace) 
                    && (other.wallEdge == wallEdge);
        }
        
        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int result = 23;
            result += 37 * disk.hashCode();
            result += 37 * wall.hashCode();
            result += 37 * wallFace.hashCode();
            if (wallEdge != null) {
                result += 37 * wallEdge.hashCode();
            }
            return result;
        }
    }

    /**
     * A disk-disk-collision event.
     *
     * @author Steffen Schreiber
     */
    public static class DiskDiskCollision extends CollisionEvent {
        public Disk disk1;
        public Disk disk2;

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            if (disk1.isFixed() || disk2.isFixed()) {
                return "[Disk - fixed Disk] Collision";
            }
            return "[Disk - Disk] Collision";
        }


        @Override
        public void write(DataOutputStream os) throws IOException {
            super.write(os);
            os.writeInt(disk1.getIndex());
            os.writeInt(disk2.getIndex());
        }

        public static DiskDiskCollision read(DataInputStream is) throws IOException {
            DiskDiskCollision result = new DiskDiskCollision();
            result.readBase(is);
            result.disk1 = Game.getSimulation().getDisk(is.readInt());
            result.disk2 = Game.getSimulation().getDisk(is.readInt());
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (! (obj instanceof DiskDiskCollision)) return false;
            
            DiskDiskCollision other = (DiskDiskCollision)obj;
            return (other.disk1 == disk1) && (other.disk2 == disk2); 
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int result = 23;
            result += 37 * disk1.hashCode();
            result += 37 * disk2.hashCode();
            return result;
        }
    }
    
    /**
     * A disk-wall-collision happened. 
     * @param e the collision event
     */
    public void collisionOccurred(DiskWallCollision e);

    /**
     * A disk-disk-collision happened. 
     * @param e the collision event
     */
    public void collisionOccurred(DiskDiskCollision e);
}
