package de.steffens.airhockey.model;

import static de.steffens.airhockey.model.vector.Vector2D.EPSILON;

import java.util.ArrayList;
import java.util.List;

import de.steffens.airhockey.model.CollisionListener.CollisionEvent;
import de.steffens.airhockey.model.CollisionListener.DiskDiskCollision;
import de.steffens.airhockey.model.CollisionListener.DiskWallCollision;
import de.steffens.airhockey.model.Wall.Face;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;

/**
 * The class that performs collision checks and handles collision events by
 * updating the velocities (speed and direction) of colliding objects.
 *
 * @author Steffen Schreiber
 */
public class Collision {
    // for testing
    static {
        VectorFactory.useMutableVector();
    }


    /**
     * maximum error for collision times in ns.
     */
    public static final long MAX_COLLISION_TIME_ERROR = 5;

    /**
     * enable debugging output for collisions
     */
    public static final boolean DEBUG_COLLISIONS = false;

    /**
     * factor to multiply with velocity after collision
     */
    public static double IMPULSE_LOSS = 0.9;

    /**
     * The last handled collision event, for debugging...
     */
    public static CollisionEvent lastCollisionEvent;


    /**
     * Scratch vectors used for temporary calculations.
     */
    private static final Vector2D scratchVectors[] = {
        VectorFactory.getVector(0.0, 0.0), VectorFactory.getVector(0.0, 0.0),
        VectorFactory.getVector(0.0, 0.0), VectorFactory.getVector(0.0, 0.0),
        VectorFactory.getVector(0.0, 0.0), VectorFactory.getVector(0.0, 0.0),
        VectorFactory.getVector(0.0, 0.0), VectorFactory.getVector(0.0, 0.0),
        VectorFactory.getVector(0.0, 0.0), VectorFactory.getVector(0.0, 0.0)
    };

    /**
     * Index of the next free scratch vector.
     */
    private static int nextFreeScratch = 0;


    /**
     * Check for collisions between the given list of disks and walls up to the
     * given time limit. Any found collisions will be handled by position updates.
     * In addition, the list of found collisions is returned.
     *
     * @param walls     the list of walls
     * @param disks     the list of disks
     * @param afterTime only handle collisions after this time, may be 0
     * @param timeLimit only handle collisions up to this time
     * @return the list of found collisions
     */
    public static synchronized List<CollisionEvent> checkCollisions(List<Wall> walls,
            List<Disk> disks, long afterTime, long timeLimit) {

        // This is the entry method for collision checks.

        // We keep a list of collisions that happen in the given time range 
        // to avoid bouncing of a disk between a heavy disk and a wall 
        // (or two heavy disks for that matter).
        // If a handled collision (i.e. with a disk) would result in a new collision
        // (i.e. with a wall) we try to find a 'new way out'...
        List<CollisionEvent> handledCollisions = new ArrayList<CollisionEvent>();

        // now perform the collision checks, starting with an empty list
        // (the implementation method is recursive)
        checkCollisionsImpl(walls, disks, handledCollisions, afterTime, timeLimit);

        return handledCollisions;
    }


    /**
     * Implementation of collision checks.
     *
     * @param walls
     * @param disks
     * @param handledCollisions
     * @param afterTime
     * @param timeLimit
     * @see #checkCollisions(List, List, long, long)
     */
    private static void checkCollisionsImpl(List<Wall> walls, List<Disk> disks,
            List<CollisionEvent> handledCollisions, long afterTime, long timeLimit) {

        // find the closest collision...

        CollisionEvent closestCollision;
        CollisionEvent lastHandled = null;
        int maxCollisions = 10;
        do {
            closestCollision = null;
            for (int mainDiskId = 0; mainDiskId < disks.size(); mainDiskId++) {
                Disk mainDisk = disks.get(mainDiskId);

                // first check collisions with walls
                for (Wall wall : walls) {
                    CollisionEvent collision = getCollision(mainDisk, wall, afterTime, timeLimit);
                    if (collision != null) {
                        assert afterTime <= collision.time;
                        assert collision.time <= timeLimit;
                        if (closestCollision == null
                            || collision.time < closestCollision.time) {
                            // don't handle the same collision twice!
                            // (this could happen because of rounding errors...)
                            if (!collision.equals(lastHandled)) {
                                closestCollision = collision;
                            }
                        }
                    }
                }

                // now check for other disks
                for (int otherId = mainDiskId + 1; otherId < disks.size(); otherId++) {
                    Disk otherDisk = disks.get(otherId);
                    CollisionEvent collision =
                        getCollision(mainDisk, otherDisk, afterTime, timeLimit);
                    assert collision == null || collision.time >= afterTime;
                    if (collision != null) {
                        if (closestCollision == null
                            || collision.time < closestCollision.time) {
                            // don't handle the same collision twice!
                            // (this could happen because of rounding errors...)
                            if (!collision.equals(lastHandled)) {
                                closestCollision = collision;
                            }
                        }
                    }
                }
            }
            if (closestCollision == null) {
                // no collision in time limit, finished
                return;
            }

            if (DEBUG_COLLISIONS) {
                sanityChecks(disks, walls);
                System.out.println("Found collision in time [" + afterTime
                    + " .. " + timeLimit + "]");
            }

            // we have found a collision event, handle this event ... 
            handleCollision(closestCollision, handledCollisions);
            handledCollisions.add(closestCollision);
            lastHandled = closestCollision;

            if (!sanityChecks(disks, walls)) {
                System.out.println("Insane after handleCollision at time " + closestCollision.time);
            }

            // ... and then go on checking for further collisions
            afterTime = closestCollision.time; // + MAX_COLLISION_TIME_ERROR

            if (--maxCollisions <= 0) {
                System.out.println("Insane number of collisions => Exiting collision check.");
                break;
            }
        } while (closestCollision.time < timeLimit);
    }


    /**
     * Handle a collision event by updating position and velocity of
     * the involved objects.
     * The second argument holds a list of collision events that happened in the very near
     * past (in the same simulation time step). This can be used to avoid objects being trapped
     * between other objects.
     *
     * @param event             the collision event.
     * @param handledCollisions
     */
    private static void handleCollision(CollisionEvent event, List<CollisionEvent> handledCollisions) {
        lastCollisionEvent = event;
        if (event instanceof DiskWallCollision) {
            // this is a disk - wall collision
            DiskWallCollision wallCollision = (DiskWallCollision) event;

            // find past collisions with the same disk and wall
            int pastCollisions = findPastCollisions(wallCollision, handledCollisions);
            if (pastCollisions > 0) {
                // reduce disk speed
                wallCollision.disk.getVelocity().multiply(Math.pow(IMPULSE_LOSS,pastCollisions));
            }

            if (wallCollision.wallEdge == null) {
                // a collision with the wall face
                handleWallFaceCollisionImpl(wallCollision);
            } else {
                // a collision with the wall edge
                handleWallEdgeCollisionImpl(wallCollision);
            }
        } else {
            // this is a disk - disk collision
            DiskDiskCollision ddEvent = (DiskDiskCollision) event;

            // find past collisions with the same disks
            if (findPastCollisions(ddEvent, handledCollisions) > 0) {
                // let the player disk bounce off...
                if (ddEvent.disk1.isFixed()) {
                    ddEvent.disk1.getVelocity().getInverse().multiply(0.4);
                }
                else if (ddEvent.disk2.isFixed()) {
                    ddEvent.disk2.getVelocity().getInverse().multiply(0.4);
                }
            }

            // check for special cases with infinite mass disks
            if (ddEvent.disk1.isFixed()) {
                handleCollisionFixedDiskImpl(ddEvent.disk1, ddEvent.disk2, ddEvent);
            } else if (ddEvent.disk2.isFixed()) {
                handleCollisionFixedDiskImpl(ddEvent.disk2, ddEvent.disk1, ddEvent);
            }
            // the general case...
            else {
                handleCollisionImpl(ddEvent);
            }
        }

    }

    private static int findPastCollisions(DiskWallCollision col, List<CollisionEvent> pastCollisions) {
        int result = 0;
        for (CollisionEvent event : pastCollisions) {
            if (col.equals(event)) {
                result++;
            }
        }
        return result;
    }

    private static int findPastCollisions(DiskDiskCollision col, List<CollisionEvent> pastCollisions) {
        int result = 0;
        for (CollisionEvent event : pastCollisions) {
            if (col.equals(event)) {
                result++;
            }
        }
        return result;
    }


    /**
     * Handle a disk - disk collision.
     *
     * @param event the collision event
     */
    private static void handleCollisionImpl(DiskDiskCollision event) {

        Disk disk1 = event.disk1;
        Disk disk2 = event.disk2;
        long time = event.time;
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D point = scratchVectors[nextFreeScratch++].reset(event.point);

        // update positions 
        disk1.setPosition(disk1.getPositionAt(time, disk1.getPosition()));
        disk2.setPosition(disk2.getPositionAt(time, disk2.getPosition()));

        if (DEBUG_COLLISIONS) {
            System.out.println(
                "\nHandle collision:  " + disk1 +
                "\n            with:  " + disk2 +
                "\n        at point:  " + point + " and time " + time + "ns");
        }

        // update timestamp
        disk1.setTimestampNs(time);
        disk2.setTimestampNs(time);

        // update direction:

        Vector2D v1 = disk1.getVelocity();
        Vector2D v2 = disk2.getVelocity();

        // find the normal on the solid disk surface at the collision point 
        Vector2D normal = point.subtract(disk1.getPosition());
        Vector2D centralDir = normal.getNormalized();
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D tangentialDir = scratchVectors[nextFreeScratch++].reset(-centralDir.getY(), centralDir.getX());

        // get the velocity components in tangential and central direction

        // the tangential velocity:
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D v1Tang = scratchVectors[nextFreeScratch++].reset(tangentialDir).multiply(tangentialDir.getScalarProduct(v1));
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D v2Tang = scratchVectors[nextFreeScratch++].reset(tangentialDir).multiply(tangentialDir.getScalarProduct(v2));

        // now the central velocity:
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D v1Cent = scratchVectors[nextFreeScratch++].reset(centralDir).multiply(centralDir.getScalarProduct(v1));
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D v2Cent = scratchVectors[nextFreeScratch++].reset(centralDir).multiply(centralDir.getScalarProduct(v2));

        if (DEBUG_COLLISIONS) {
            System.out.println("  v1 tang = " + v1Tang.asVelocityString()
                + ", cent = " + v1Cent.asVelocityString());
            System.out.println("  v2 tang = " + v2Tang.asVelocityString()
                + ", cent = " + v2Cent.asVelocityString());
        }

        // set the difference of central velocities as collision velocity
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        event.velocity = scratchVectors[nextFreeScratch++].reset(v1Cent).subtract(v2Cent).copy();
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;

        // now calculate the new velocities depending on the masses...
        double m1 = disk1.getMass();
        double m2 = disk2.getMass();
        Vector2D newV1;
        Vector2D newV2;

        // for equal masses: keep tangential velocity, swap central velocity
        if (m1 == m2) {
            newV1 = v1Tang.add(v2Cent);
            newV2 = v2Tang.add(v1Cent);
        }
        // in the general case: keep tangential velocity, calculate 
        // central velocity depending on the masses
        else {
            assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
            assert scratchVectors[nextFreeScratch + 1].getX() == 0.0 && scratchVectors[nextFreeScratch + 1].getY() == 0.0;
            // central vel:        v1*(m1-m2) + v2*(2*m2)
            //               v1' = ----------------------
            //                            m1 + m2
            newV1 = v1Tang.add(
                scratchVectors[nextFreeScratch].reset(v1Cent).multiply(m1 - m2)
                    .add(scratchVectors[nextFreeScratch + 1].reset(v2Cent).multiply(2 * m2)).multiply(
                    1.0 / (m1 + m2)));
            assert nextFreeScratch + 1 >= 0 && (scratchVectors[nextFreeScratch + 1] = scratchVectors[nextFreeScratch + 1].reset()) != null;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            newV2 = v2Tang.add(
                v2Cent.multiply(m2 - m1).add(v1Cent.multiply(2 * m1)).multiply(
                    1.0 / (m1 + m2)));
        }

        disk1.setVelocity(newV1.multiply(IMPULSE_LOSS));
        disk2.setVelocity(newV2.multiply(IMPULSE_LOSS));

        // make sure the collision is solved:
        // by just updating the velocities, the disks might run in the same
        // direction at the same speed, so the collision (overlapping) is not
        // solved. Add a small 'push' to the lighter disk...
        Disk lighterDisk;
        Disk heavyDisk;
        if (m1 > m2) {
            heavyDisk = disk1;
            lighterDisk = disk2;
        } else {
            heavyDisk = disk2;
            lighterDisk = disk1;
        }
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D pushDirection = scratchVectors[nextFreeScratch++].reset(lighterDisk.getPosition()).subtract(heavyDisk.getPosition());
        double minDistance = disk1.getRadius() + disk2.getRadius();
        if (pushDirection.getValue() <= minDistance) {
            // disks are still overlapping...
            Vector2D positionPush = pushDirection.getNormalized().multiply(minDistance * 1.001);
            assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
            lighterDisk.setPosition(scratchVectors[nextFreeScratch++].reset(heavyDisk.getPosition()).add(positionPush));
            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            if (DEBUG_COLLISIONS) {
                System.out.println("Added push " + positionPush
                    + " to " + lighterDisk);
            }
        }

        if (DEBUG_COLLISIONS) {
            System.out.println(
                "  handled:  " + disk1 + "\n" +
                "      and:  " + disk2 + "\n");
        }
        for (int i = 0; i < 7; i++) {
            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        }
    }


    /**
     * Handle a collision between a fixed disk (infinite mass) with a lighter disk.
     * This handles the special case of a disk-disk-collision where one disk has
     * infinite mass and will not receive any impulse from the other disk.
     *
     * @param fixedDisk the fixed disk with infinite mass
     * @param lightDisk the second disk
     * @param event     the collision event
     */
    private static void handleCollisionFixedDiskImpl(Disk fixedDisk, Disk lightDisk,
                                                     DiskDiskCollision event) {

        long time = event.time;
        // update positions 
        fixedDisk.setPosition(fixedDisk.getPositionAt(time, fixedDisk.getPosition()));
        lightDisk.setPosition(lightDisk.getPositionAt(time, lightDisk.getPosition()));

        if (DEBUG_COLLISIONS) {
            System.out.println(
                "\nHandle collision of fixed " + fixedDisk +
                "\n            with:  " + lightDisk +
                "\n        at point:  " + event.point + " and time " + time + "ns");
        }

        // update timestamp
        fixedDisk.setTimestampNs(time);
        lightDisk.setTimestampNs(time);

        // update direction:

        // we reduce this problem to the collision of one moving and one fixed disk
        // by subtracting the velocity of the heavy disk, then doing the reflection 
        // at the collision point and then adding the velocity again

        Vector2D vFixed = fixedDisk.getVelocity();
        Vector2D vLight = lightDisk.getVelocity().subtract(vFixed);

        // store the collision velocity in the event
        event.velocity = vLight.copy();

        // find the normal on the fixed disk surface at the collision point
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D normal = scratchVectors[nextFreeScratch++].reset(event.point).subtract(fixedDisk.getPosition()).getNormalized();

        // make sure the disks don't overlap
        double minDistance = lightDisk.getRadius() + fixedDisk.getRadius() + EPSILON;

        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D dist = scratchVectors[nextFreeScratch++].reset(lightDisk.getPosition()).subtract(fixedDisk.getPosition());
        double distance = dist.getValue();
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        if (distance < minDistance) {
            lightDisk.getPosition().addMultiple(normal, (minDistance-distance));
        }

        // reflect the lighter disk velocity at this normal just like with wall reflections
        double scalarVLight = normal.getScalarProduct(vLight);
        Vector2D vReflection = vLight.addMultiple(normal, -2.0 * scalarVLight);

        // now add the fixed disk velocity again -> we have the new velocity
        lightDisk.setVelocity(vReflection.add(vFixed).multiply(IMPULSE_LOSS));

        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        assert lightDisk.getVelocity().getValue() < (MovingObject.MAX_VELOCITY_VALUE * 1.001);

        if (DEBUG_COLLISIONS) {
            System.out.println(
                "  handled:  " + fixedDisk + "\n" +
                "      and:  " + lightDisk + "\n");
        }

        // update player index for the hit
        short playerIdx = fixedDisk.getLastHitPlayerIndex();
        if (playerIdx != -1 && !lightDisk.isFixed()) {
            // remember that the disk was last hit by this player
            lightDisk.setLastHitPlayerIndex(playerIdx);
        }
    }


    /**
     * Handle a disk - wall collision on the wall face.
     *
     * @param event the collision event
     */
    private static void handleWallFaceCollisionImpl(DiskWallCollision event) {

        Disk disk = event.disk;
        Wall wall = event.wall;
        Vector2D normal = event.wallFace.getNormalVector();
        Vector2D velocity = disk.getVelocity();

        if (DEBUG_COLLISIONS) {
            System.out.println("\nCollision " + wall + " with " + disk
                + " at point " + event.point + " and time " + event.time + "ns");
        }
        // store collision velocity in the event
        event.velocity = velocity.copy();

        // update position 
        disk.setPosition(disk.getPositionAt(event.time, disk.getPosition()));

        // if the disk is inside the wall at that time, move...
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D inverseNormal = scratchVectors[nextFreeScratch++].reset(normal).getInverse();
        double distance = planeRayIntersection(event.wallFace.getPositionVector(),
            event.wallFace.getNormalVector(), disk.getPosition(), inverseNormal);
        if (distance < disk.getRadius()) {
            // move disk outside of the wall
            disk.getPosition().addMultiple(normal, (disk.getRadius() - distance + EPSILON));
            // reduce speed so that
        }
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;


        // update direction
        double scalar = normal.getScalarProduct(velocity);
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D newVelocity = velocity.subtract(scratchVectors[nextFreeScratch++].reset(normal).multiply(scalar * 2));
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        disk.setVelocity(newVelocity.multiply(IMPULSE_LOSS));
        // update timestamp
        disk.setTimestampNs(event.time);
    }


    /**
     * Handle a disk - wall collision on the wall wall edge.
     *
     * @param event the collision event
     */
    private static void handleWallEdgeCollisionImpl(DiskWallCollision event) {

        Disk disk = event.disk;
        Wall wall = event.wall;
        Vector2D wallEdge = event.wallEdge;
        long time = event.time;

        // update positions 
        disk.setPosition(disk.getPositionAt(time, disk.getPosition()));

        if (DEBUG_COLLISIONS) {
            System.out.println(
                "\nHandle collision of disk " + disk +
                    "\n            with:  " + wall + "; edge " + wallEdge +
                    "\n        at point:  " + event.point + " and time " + time + "ns");
        }

        // update timestamp
        disk.setTimestampNs(time);

        // store the collision velocity in the event
        Vector2D vLight = disk.getVelocity();
        event.velocity = vLight.copy();

        // update direction:

        // find the normal on the wall edge surface at the collision point
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D normal = scratchVectors[nextFreeScratch++].reset(disk.getPosition()).subtract(wallEdge).getNormalized();

        // reflect the disk velocity at this normal just like with wall reflections
        double scalarVLight = normal.getScalarProduct(vLight);
        disk.setVelocity(vLight.addMultiple(normal, -2.0 * scalarVLight).multiply(IMPULSE_LOSS));
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;

        if (DEBUG_COLLISIONS) {
            System.out.println(
                "  handled:  " + disk + "\n");
        }
    }


    /**
     * Check for collisions between the given disks up to the given time
     * limit. If no collision is found, this will return <code>null</code>,
     * otherwise a collision event is generated.
     *
     * @param disk1     the first disk
     * @param disk2     the second disk
     * @param afterTime only handle collisions after this time, may be 0
     * @param timeLimit the time limit for collision checks
     * @return the found collision event
     */
    private static DiskDiskCollision getCollision(Disk disk1, Disk disk2,
                                                  long afterTime, long timeLimit) {

        // both disks should start at the same timestamp, but let's check this
        // assert disk1.getTimestampNs() == disk2.getTimestampNs();

        long startTime = Math.max(afterTime, Math.max(disk1.getTimestampNs(), disk2.getTimestampNs()));
        //            - (MAX_COLLISION_TIME_ERROR * 2);
        double maxDistance = disk1.getRadius() + disk2.getRadius() + EPSILON;

        // we check for collision at samples between start and end time
        // how many samples do we want?
        // at least 3 samples, add more for small and fast disks
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D tmpPos1 = disk1.getPositionAt(timeLimit, scratchVectors[nextFreeScratch++]);
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        double wayLength1 = tmpPos1.getDistance(disk1.getPositionAt(startTime, scratchVectors[nextFreeScratch++]));
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D tmpPos2 = disk2.getPositionAt(timeLimit, scratchVectors[nextFreeScratch++]);
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        double wayLength2 = tmpPos2.getDistance(disk2.getPositionAt(startTime, scratchVectors[nextFreeScratch++]));
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        double maxWayLength = Math.max(wayLength1, wayLength2);
        double minRadius = Math.min(disk1.getRadius(), disk2.getRadius());
        long samplesCount = Math.max(3, Math.round(5.0 * maxWayLength / minRadius));

        long step = (timeLimit - startTime) / samplesCount;
        for (long t = startTime; t <= timeLimit; t += step) {
            // don't step over time limit
            if (t > timeLimit) {
                t = timeLimit;
            }
            tmpPos1 = disk1.getPositionAt(t, tmpPos1);
            tmpPos2 = disk2.getPositionAt(t, tmpPos2);
            double distance = tmpPos1.getDistance(tmpPos2);
            if (distance < maxDistance) {
                // the disks already collided at time t!
                long collisionTime = t;
                if (t > startTime) {
                    // do some more steps to find a more exact collision time
                    long t1 = t - step;
                    long t2 = t;
                    // we can live with an error
                    while (t2 - t1 > MAX_COLLISION_TIME_ERROR) {
                        long tTest = t1 + (t2 - t1) / 2;
                        tmpPos1 = disk1.getPositionAt(tTest, tmpPos1);
                        tmpPos2 = disk2.getPositionAt(tTest, tmpPos2);
                        distance = tmpPos1.getDistance(tmpPos2);
                        if (distance < maxDistance) {
                            // we have to search before tTest
                            t2 = tTest;
                        } else {
                            // we have to search after tTest
                            t1 = tTest;
                        }
                    }
                    // at this point, we have a collision event between 
                    // t1 and t2 and these are < MAX_COLLISION_TIME_ERROR ns 
                    // apart.
                    collisionTime = t1;
                } else {
                    System.out.println("!! Disk-Disk Collision at start time of iteration: " + t);
                }

//                if (!(collisionTime > afterTime)) {
//                    nextFreeScratch--;
//                    assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
//                    nextFreeScratch--;
//                    assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
//                    // No collision in the given time frame.
//                    return null;
//                }

                Vector2D p1 = disk1.getPositionAt(collisionTime, tmpPos1);
                Vector2D p2 = disk2.getPositionAt(collisionTime, tmpPos2);

                // collision point is on the segment [p1 - p2], 
                // exact point depends on disk radii
                double cpDistance = disk1.getRadius() / (disk1.getRadius() + disk2.getRadius());
                Vector2D collisionPoint = p1.addMultiple(p2.subtract(p1), cpDistance);

                DiskDiskCollision result = new DiskDiskCollision();
                result.disk1 = disk1;
                result.disk2 = disk2;
                result.time = collisionTime;
                result.point = collisionPoint.copy();
                nextFreeScratch--;
                assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                nextFreeScratch--;
                assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                return result;
            }

        }

        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        // no collision found at any sample point.
        return null;
    }


    /**
     * Check for collision of the moving disk with the given wall.
     * The disk is moving from the current position at the current timestamp
     * along its current velocity. If it would hit the wall on this way,
     * this method will return a collision event, otherwise this
     * returns <code>null</code>.
     *
     * @param disk      the moving disk
     * @param wall      the wall to check
     * @param afterTime only handle collisions after this time, may be 0
     * @param timeLimit only handle collisions up to this time
     * @return the collision event, or <code>null</code>
     */
    private static DiskWallCollision getCollision(Disk disk, Wall wall, long afterTime, long timeLimit) {
        // if the disk is a player disk and the wall is a destroyable wall,
        // we ignore collisions...
        if (disk.isFixed() && wall instanceof DestroyableWall) {
            return null;
        }

        // if the wall is an infinite wall, we only need to check the front wall face...
        boolean infinite = wall.isInfinite();

        if (infinite) {
            return getCollisionInfiniteWall(disk, wall, wall.getFrontFace(), afterTime, timeLimit);
        }

        // check collisions for all 4 wall faces
        // return the nearest collision (minimum collision time).
        Face[] faces = wall.getWallFaces();
        DiskWallCollision result = null;
        for (Face face : faces) {
            // check collision for this face...
            DiskWallCollision event = getCollisionGeneralWall(disk, wall, face, afterTime, timeLimit);
            if (event != null) {
                if (result == null || event.time < result.time) {
                    result = event;
                }
            }
        }

        return result;
    }


    /**
     * Check for collision of the moving disk with the given wall face.
     * This method works for general wall faces.
     * For walls that can be handled as infinite, use method
     * {@link #getCollisionInfiniteWall(Disk, Wall, Face, long, long)}, which is optimized.
     *
     * @param disk      the moving disk
     * @param wall      the wall model
     * @param face      one specific face of that wall
     * @param afterTime only handle collisions after this time, may be 0
     * @param timeLimit only handle collisions up to this time
     * @return the collision event, or <code>null</code>
     */
    private static DiskWallCollision getCollisionGeneralWall(Disk disk,
            Wall wall, Face face, long afterTime, long timeLimit) {

        Vector2D diskVelocity = disk.getVelocity();
        Vector2D wallNormal = face.getNormalVector();

        if (diskVelocity.getScalarProduct(wallNormal) >= 0) {
            // disk is moving away from wall, no need to check
            return null;
        }

        // so the disk is moving against the wall normal, a collision is possible.
        // now check, if the disk center already crossed the wall face (disk is 
        // behind the wall
        // TODO: maybe an easier check: is disk center on "outside" or "inside"
        //       half of the wall???
        Vector2D diskCenter = disk.getPosition();
        Vector2D wallStart = face.getPositionVector();
        Vector2D wallEnd = face.getFaceEndVector();
        double lambdaCenter = planeRayIntersection(
            wallStart, wallNormal, diskCenter, diskVelocity);
        if (lambdaCenter == 0.0) {
            // no intersection, disk center is behind the wall plane -> no collision
            return null;
        }

        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        // Find the point on the disk surface that is nearest to the wall. 
        // Go from the disk center in inverted wall-normal direction:
        double diskRadius = disk.getRadius();
        Vector2D nearestPoint = scratchVectors[nextFreeScratch++].reset(diskCenter).addMultiple(wallNormal, -diskRadius);

        // shoot a ray in disk-velocity direction from this point to the wall to find
        // out, where the disk would hit the wall plane
        double lambdaNearest = planeRayIntersection(
            wallStart, wallNormal, nearestPoint, diskVelocity);
        Vector2D collisionPoint = null;
        double collisionLambda = 0.0;
        if (lambdaNearest != 0.0) {
            // the nearestPoint would hit the wall here:
            collisionPoint = nearestPoint.addMultiple(diskVelocity, lambdaNearest);
            collisionLambda = lambdaNearest;
        } else {
            // the nearest point has already crossed the wall plane, but as the 
            // disk center did NOT, we still check for a collision on the wall edge
            collisionPoint = nearestPoint;
            collisionLambda = 0.0;
        }

        // Now we know the point where the disk surface will hit the wall plane first.
        // If this point is on the wall face, we are lucky -> FINISHED
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D dirStart = scratchVectors[nextFreeScratch++].reset(wallStart).subtract(collisionPoint);
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D dirEnd = scratchVectors[nextFreeScratch++].reset(wallEnd).subtract(collisionPoint);
        boolean onWall = (dirStart.getScalarProduct(dirEnd) < 0);
        // NOTE: if we are in the second case (nearest point crossed the wall plane) and
        // onWall is still true, we assume that the disk went through the wall because
        // of precision problems. Handle this as if the collision happened now (lambda=0)

        if (onWall) {
            if (collisionLambda == 0) {
                System.out.println("!! Disk-WallFace Collision at start time");
            }
            // the point is on the wall, return the collision event with
            // the wall face.
            long collisionTime = disk.getTimestampNs() + Math.round(collisionLambda);
            if (!((afterTime == 0 || afterTime <= collisionTime) && collisionTime <= timeLimit)) {
//            if (!((afterTime == 0 || afterTime < collisionTime) && collisionTime <= timeLimit)) {
                nextFreeScratch--;
                assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                nextFreeScratch--;
                assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                nextFreeScratch--;
                assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                // No collision in the given time frame.
                return null;
            }
            DiskWallCollision result = new DiskWallCollision();
            result.disk = disk;
            result.point = collisionPoint.copy();
            result.time = collisionTime;
            result.wall = wall;
            result.wallFace = face;

            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            return result;
        }

        // the point is NOT on the wall, we need to check the distance to the 
        // wall edges. 
        long timeStart = getCollisionTime(disk, wallStart, afterTime, timeLimit);
        long timeEnd = getCollisionTime(disk, wallEnd, afterTime, timeLimit);

        Vector2D collisionEdge = null;
        long collisionTime = -1;
        // find the edge with the smaller time while ignoring time < 0 (no collision)
        if (timeStart >= 0 && (timeStart < timeEnd || timeEnd < 0)) {
            // found collision with wall start
            collisionEdge = wallStart;
            collisionTime = timeStart;
        } else if (timeEnd >= 0) {
            // found collision with wall end
            collisionEdge = wallEnd;
            collisionTime = timeEnd;
        }
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;

        if (collisionEdge == null) {
            // no collision
            return null;
        }

        // now we only have to calculate the disk position at collision time
        collisionPoint = disk.getPositionAt(collisionTime, VectorFactory.getVector(0.0, 0.0));

        DiskWallCollision result = new DiskWallCollision();
        result.disk = disk;
        result.point = collisionPoint;
        result.time = collisionTime;
        result.wall = wall;
        result.wallFace = face;
        result.wallEdge = collisionEdge;
        return result;
    }


    /**
     * Check for collisions between the given disk and the given wall edge.
     *
     * @param disk      the disk
     * @param edge      the wall edge
     * @param afterTime only handle collisions after this time, may be 0
     * @param timeLimit the time limit for collision checks
     * @return the found collision time, or < 0 if no collision happened
     */
    private static long getCollisionTime(Disk disk, Vector2D edge, long afterTime, long timeLimit) {

        long startTime = disk.getTimestampNs();
        double maxDistance = disk.getRadius() + EPSILON;

        // we check for collision at samples between start and end time
        // how many samples do we want?
        // at least 3 samples, add more for small and fast disk
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        Vector2D tmpPos = disk.getPositionAt(timeLimit, scratchVectors[nextFreeScratch++]);
        double wayLength = tmpPos.getDistance(disk.getPosition());

        long samplesCount = Math.max(3, Math.round(5.0 * wayLength / disk.getRadius()));

        long step = (timeLimit - startTime) / samplesCount;
        for (long t = startTime; t <= timeLimit; t += step) {
            // don't step over time limit
            if (t > timeLimit) {
                t = timeLimit;
            }
            tmpPos = disk.getPositionAt(t, tmpPos);
            double distance = tmpPos.getDistance(edge);
            if (distance < maxDistance) {
                // the disk already collided at time t!
                long collisionTime = t;
                if (t > startTime) {
                    // do some more steps to find a more exact collision time
                    long t1 = t - step;
                    long t2 = t;
                    // we can live with an error
                    while (t2 - t1 > MAX_COLLISION_TIME_ERROR) {
                        long tTest = t1 + (t2 - t1) / 2;
                        tmpPos = disk.getPositionAt(tTest, tmpPos);
                        distance = tmpPos.getDistance(edge);
                        if (distance < maxDistance) {
                            // we have to search before tTest
                            t2 = tTest;
                        } else {
                            // we have to search after tTest
                            t1 = tTest;
                        }
                    }
                    // at this point, we have a collision event between 
                    // t1 and t2 and these are < MAX_COLLISION_TIME_ERROR ns 
                    // apart.
                    collisionTime = t1;
                } else {
                    System.out.println("!! Disk-Edge Collision at start time of iteration: " + t);
                }

                if (!(collisionTime > afterTime)) {
                    nextFreeScratch--;
                    assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                    // No collision in the given time frame.
                    return -1;
                }

                // found a collision at collisionTime
                nextFreeScratch--;
                assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
                return collisionTime;
            }
        }

        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        // no collision found at any sample point.
        return -1;
    }


    /**
     * Check for collision of the moving disk with the given wall face.
     * This method works for wall faces that can be handled as infinite.
     *
     * @param disk      the moving disk
     * @param wall      the wall model
     * @param face      one specific face of that wall
     * @param afterTime only handle collisions after this time, may be 0
     * @param timeLimit only handle collisions up to this time
     * @return the collision event, or <code>null</code>
     */
    private static DiskWallCollision getCollisionInfiniteWall(Disk disk, Wall wall, Face face,
                                                              long afterTime, long timeLimit) {
        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;

        Vector2D diskVelocity = disk.getVelocity();
        Vector2D wallNormal = face.getNormalVector();

        if (diskVelocity.getScalarProduct(wallNormal) >= 0) {
            // disk is moving away from wall, no need to check
            return null;
        }
        Vector2D wallPosition = face.getPositionVector();

        // The possible collision point is the one on the disk surface
        // that is next to the wall. Go from the disk center in
        // inverted wall-normal direction to find this point
        double diskRadius = disk.getRadius();
        Vector2D point = scratchVectors[nextFreeScratch++].reset(disk.getPosition()).addMultiple(wallNormal, -diskRadius);

        // now get the intersection of the ray from this point to the 
        // wall in disk velocity direction
        double lambda = planeRayIntersection(wallPosition, wallNormal, point, diskVelocity);

        if (lambda == 0.0) {
            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            // no intersection, no collision
            return null;
        }
        long collisionTime = disk.getTimestampNs() + Math.round(lambda);
        // we have a collision after lambda nano seconds
        if (!(afterTime < collisionTime && collisionTime <= timeLimit)) {
            nextFreeScratch--;
            assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
            // No collision in the given time frame.
            return null;
        }
        Vector2D collisionPoint = point.copy().addMultiple(diskVelocity, lambda);
        DiskWallCollision result = new DiskWallCollision();
        result.disk = disk;
        result.point = collisionPoint;
        result.time = collisionTime;
        result.wall = wall;
        result.wallFace = face;

        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        return result;
    }


    /**
     * Calculate the intersection point between a plane (given as position and
     * normal vector) and a ray (given as start and direction vectors). If there
     * is an intersection, this will return the lambda value with:<br>
     * intersection point = rayPos + lambda * rayDirection
     * <p/>
     * If there is no intersection, this will return <code>0.0</code>
     *
     * @param planePos
     * @param planeNor
     * @param rayPos
     * @param rayDirection
     * @return lambda, or <code>0.0</code>
     */
    private static double planeRayIntersection(Vector2D planePos, Vector2D planeNor,
            Vector2D rayPos, Vector2D rayDirection) {

        // http://nehe.gamedev.net/data/lessons/lesson.asp?lesson=30

        // scalar product between plane normal and ray direction
        double scalarProd = rayDirection.getScalarProduct(planeNor);

        // no intersection if ray parallel to plane
        if (Math.abs(scalarProd) < 0) {
            return 0.0;
        }

        assert scratchVectors[nextFreeScratch].getX() == 0.0 && scratchVectors[nextFreeScratch].getY() == 0.0;
        // distance to intersection point
        double lambda =
            planeNor.getScalarProduct(scratchVectors[nextFreeScratch++].reset(planePos).subtract(rayPos)) / scalarProd;
        nextFreeScratch--;
        assert nextFreeScratch >= 0 && (scratchVectors[nextFreeScratch] = scratchVectors[nextFreeScratch].reset()) != null;
        // test if collision is behind start (lambda > 0)
        if (lambda < 0) {
            return 0.0;
        }

        // intersection point is at rayPos + lambda * rayDirection
        return lambda;
    }


    /**
     * Perform some sanity checks on object positions and scream if something
     * is wrong. For debugging only...
     *
     * @param disks list of disks to check
     * @param walls list of walls to check
     * @return flag indicating sanity ;-)
     */
    public static boolean sanityChecks(List<Disk> disks, List<Wall> walls) {
        if (!DEBUG_COLLISIONS) {
            return true;
        }
        boolean sane = true;
        for (int disk1idx = 0; disk1idx < disks.size(); disk1idx++) {
            Disk disk1 = disks.get(disk1idx);
            Vector2D pos1 = disk1.getPosition();
            double radius1 = disk1.getRadius();

            for (int disk2idx = disk1idx + 1; disk2idx < disks.size(); disk2idx++) {
                Disk disk2 = disks.get(disk2idx);
                Vector2D pos2 = disk2.getPosition();
                double radius2 = disk2.getRadius();

                if (pos1.getDistance(pos2) < (radius1 + radius2)) {
                    System.out.println("\nDisks overlapping:\n" +
                        "  " + disk1 + "\n" +
                        "  (time " + disk1.getTimestampNs() + ")\n" +
                        "  " + disk2 + "\n" +
                        "  (time " + disk1.getTimestampNs() + " )\n");
                    sane = false;
                }
            }
        }

        // check for disks inside walls...
        //        for (Disk disk : disks) {
        //            for (Wall wall : walls) {
        //                Vector2D[] wallCoords = wall.getCoords();
        //                for (int i=0; i<4; i++) {
        //                    double distance = disk.getPosition().getDistance(wallCoords[i]);
        //                    if (distance < 0.95 * disk.getRadius()) {
        //                        System.out.println("\nDisk " + disk + "\n is inside vertex " + i
        //                                + " of wall " + wall + ": " + wallCoords[i]);
        //                        sane = false;
        //                    }
        //                }
        //            }
        //        }
        return sane;
    }


    /**
     * A testing main method for checking some simple collisions.
     *
     * @param args
     */
    public static void main(String[] args) {
        // a disk of radius 
        Disk disk = new Disk(1.0, 1.0);

        // a wall from 0 to 10 in x-direction, y=0
        Wall wall = new Wall(0, 0, 10, 0, 1, 1);
        System.out.println("Wall from " + wall.getFrontFace().getPositionVector()
            + " to " + wall.getFrontFace().getFaceEndVector());


        double vel = MovingObject.MAX_VELOCITY_VALUE / 10;

        for (double x = -3.5; x < 15.0; x += 0.1) {
            disk.setPosition(VectorFactory.getVector(x, 0.5));
            disk.setVelocity(VectorFactory.getVector(3 * vel, -0.00000000000001));

            System.out.println("\nCollision for disk " + disk + ": ");
            CollisionEvent result = getCollisionGeneralWall(disk, wall, wall.getFrontFace(), 0, Integer.MAX_VALUE);
            if (result == null) {
                System.out.println("  -> none");
            } else {
                DiskWallCollision wallCollision = (DiskWallCollision) result;
                if (wallCollision.wallEdge == null) {
                    // a collision with the wall face
                    System.out.println("  -> Wall face, point " + result.point);
                } else {
                    // a collision with the wall edge
                    System.out.println("  -> Wall edge " + wallCollision.wallEdge
                        + ", point " + result.point
                        + " (distance " + result.point.getDistance(wallCollision.wallEdge) + ")");
                }
            }
        }
    }
}
