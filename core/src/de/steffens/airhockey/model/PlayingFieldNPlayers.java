/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.model;


import java.util.ArrayList;

import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.model.Wall.Face;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;


/**
 * Multi-player playing field for a given number N of players.
 * 
 * @author Steffen Schreiber
 */
public class PlayingFieldNPlayers extends AbstractPlayingFieldBase {

    // the area where breakout blocks will be put 
    private Rectangle breakoutZone;

    private double plDiskRadius = 0.6;
    private double plDiskHeight = 0.2;
    private double puckDiskRadius = 0.5;
    private double puckDiskHeight = 0.1;

    ArrayList<Disk> breakoutDisks = new ArrayList<Disk>();

    /**
     * Create a new playing field instance for the given number of players
     * 
     * @param numPlayers the number of players
     */
    public PlayingFieldNPlayers(int numPlayers) {
        super(numPlayers);

        double sizeFactor = Math.min(1.0, 3.5 / numPlayers);
        plDiskRadius *= sizeFactor;
        puckDiskRadius *= sizeFactor;
        // create playing field border walls:
        setupPlayingField();

        // add collision listeners to the collision objects
        // that will fire visual effects
        setupEffectCollisionListeners();

        setupFinished();
    }

    @Override
    public Disk createPlayerDisk(int playerIndex) {
        Disk disk = new Disk(plDiskRadius, plDiskHeight);
        disk.setMaterial(Material.playerMaterial.getCopy());
        return disk;
    }

    @Override
    public Disk createPuckDisk() {
        Disk disk = new Disk(puckDiskRadius, puckDiskHeight);
        disk.setMaterial(Material.puckMaterial.getCopy());
        return disk;
    }

    /**
     * @see de.steffens.airhockey.model.PlayingField#resetState(boolean)
     */
    @Override
    public synchronized void resetStateImpl() {
        // remove any breakout disks...
        for (Disk disk : breakoutDisks) {
            Game.getSimulation().removeDisk(disk);
            Game.getDisplay().removeObject(disk);
        }
        breakoutDisks.clear();

        if (Game.isBreakout()) {
            // remove any leftover breakout blocks
            ArrayList<Wall> wallsCopy = new ArrayList<Wall>(getWalls());
            for (Wall wall : wallsCopy) {
                if (wall instanceof DestroyableWall) {
                    removeWall(wall);
                }
            }

            // recreate the breakout blocks
            setupBreakoutBlocks();
        }
    }
    
    /**
     * Setup the playing field by creating walls and corners.
     */
    protected void setupPlayingField() {

        double fieldRadius = 10;
        // fraction of the field radius for the floor pattern 
        double floorPattern = 0.2;
        // size of the reachable area for players
        double reachableDistance = fieldRadius * 0.7;
        // wall dimensions
        double wHeight = 0.25;                   // wall height
        double wThickness = 0.7;                 // wall thickness for large walls
        
        Vector2D wallCenter = VectorFactory.getVector(0, 0);
        Vector2D camPos = VectorFactory.getVector(0, 0);
        Vector2D reachableAreaUp = VectorFactory.getVector(0, 0);
        
        
        int segments = 2 * numPlayers;
        double segmentAngle = (2.0 * Math.PI) / segments;
        double startAngle = (-0.5 * Math.PI) - segmentAngle / 2.0;
        
        ////////// define segments for each player ///////////
        
        for (int player = 0; player < numPlayers; player++) {
            // calculate the start / end angles of the wall segments
            double a1 = startAngle + (2.0 * player) * segmentAngle;
            double a2 = a1 + segmentAngle;
            double a3 = a2 + segmentAngle;
            
            // first the wall segments next to the player goal
            
            // coordinates of start end end of the segment
            double x1 = Math.cos(a1) * fieldRadius;
            double y1 = Math.sin(a1) * fieldRadius;
            double x2 = Math.cos(a2) * fieldRadius;
            double y2 = Math.sin(a2) * fieldRadius;
            double dx = x2 - x1;
            double dy = y2 - y1;

            Wall goalSegment1 = new Wall(
                    x1, y1, 
                    x1 + (dx * 0.25), y1 + (dy * 0.25), 
                    wThickness, wHeight);
            goalSegment1.setMaterial(Material.wallMaterial);
            addWall(goalSegment1, true);
            
            Wall goalSegment2 = new Wall(
                    x1 + (dx * 0.75), y1 + (dy * 0.75),
                    x2, y2, 
                    wThickness, wHeight);
            goalSegment2.setMaterial(Material.wallMaterial);
            addWall(goalSegment2, true);
            
            // coordinates of the goal wall behind the playing field wall
            double xGoal1 = Math.cos(a1) * (fieldRadius + wThickness);
            double yGoal1 = Math.sin(a1) * (fieldRadius + wThickness);
            double xGoal2 = Math.cos(a2) * (fieldRadius + wThickness);
            double yGoal2 = Math.sin(a2) * (fieldRadius + wThickness);

            Wall goalWall = new Wall(xGoal1, yGoal1, xGoal2, yGoal2, wThickness*0.1, wHeight);
            goalWall.setMaterial(Material.wallMaterial);
            addGoal(goalWall, player);
            
            // coordinates of the segment inside/outside the wall for the floor...
            double x1_ = Math.cos(a1) * fieldRadius * 1.2;
            double y1_ = Math.sin(a1) * fieldRadius * 1.2;
            double x2_ = Math.cos(a2) * fieldRadius * 1.2;
            double y2_ = Math.sin(a2) * fieldRadius * 1.2;


            Rectangle floorOutside = new Rectangle(
                    VectorFactory.getVector(x1_, y1_),
                    VectorFactory.getVector((x2-x1) * 2.0, (y2-y1) * 2.0),
                    VectorFactory.getVector(x1-x1_, y1-y1_),
                    1);
            floorOutside.setMaterial(Material.floorBlack);
            addRectangle(floorOutside);

            Rectangle floorInside = new Rectangle(
                    VectorFactory.getVector(x1, y1),
                    VectorFactory.getVector(x2-x1, y2-y1),
                    goalWall.getFrontFace().getNormalVector().copy()
                        .multiply(fieldRadius * (1.0 - floorPattern)),
                    8);
            floorInside.setMaterial(Material.floorMirror);
            addRectangle(floorInside);
            
            // now we can calculate goal and camera positions
            Face goalFace = goalWall.getFrontFace();
            wallCenter = wallCenter.reset(goalFace.getPositionVector())
                    .addMultiple(goalFace.getFaceVector(), 0.5);
            camPos = camPos.reset(wallCenter).addMultiple(goalFace.getNormalVector(), -10);
            
            cameraPositions[player][0] = (float)camPos.getX();
            cameraPositions[player][1] = (float)camPos.getY();
            cameraPositions[player][2] = 7f;
            
            Vector2D faceNormalized = goalFace.getFaceVector().copy().getNormalized();
            Vector2D reachableOrigin = goalFace.getPositionVector().copy()
                .addMultiple(goalFace.getNormalVector(), plDiskRadius + wThickness)
                .addMultiple(faceNormalized, plDiskRadius * 1.1);
            Vector2D reachableRight = faceNormalized.copy()
                .multiply(goalFace.getFaceVector().getValue() - (2.2*plDiskRadius));
            
            reachableAreaUp = reachableAreaUp.reset(goalFace.getNormalVector())
                    .multiply(reachableDistance);
            
            reachableAreas[player] = new Rectangle(
                    reachableOrigin,
                    reachableRight,
                    reachableAreaUp,
                    1);
            // debugging: show reachable areas...
//            reachableAreas[player].setMaterial(Material.floorReachable);
//            addRectangle(reachableAreas[player]);
            
            // now the second wall segment to the right of the player wall
            // we can use an infinite wall here for performance
            double x3 = Math.cos(a3) * fieldRadius;
            double y3 = Math.sin(a3) * fieldRadius;
            Wall wallSegment = new Wall(x2, y2, x3, y3, wThickness, wHeight);
            wallSegment.setMaterial(Material.wallMaterial);
            wallSegment.setInfinite(true);
            addWall(wallSegment, true);
            
            floorOutside = new Rectangle(
                    VectorFactory.getVector(x2_, y2_),
                    VectorFactory.getVector((x3-x2) * 2.0, (y3-y2) * 2.0),
                    VectorFactory.getVector(x2-x2_, y2-y2_),
                    1);
            floorOutside.setMaterial(Material.floorBlack);
            addRectangle(floorOutside);
            
            floorInside = new Rectangle(
                    VectorFactory.getVector(x2, y2),
                    VectorFactory.getVector(x3-x2, y3-y2),
                    wallSegment.getFrontFace().getNormalVector().copy()
                        .multiply(fieldRadius * (1.0 - floorPattern)),
                    8);
            floorInside.setMaterial(Material.floorMirror);
            addRectangle(floorInside);
            
            
            // the wall corners
            double cornerHeight = wHeight * 1.12;
            double cornerRadius = wThickness*0.6;
            x1 = Math.cos(a1) * (fieldRadius + cornerRadius);
            y1 = Math.sin(a1) * (fieldRadius + cornerRadius);
            x2 = Math.cos(a2) * (fieldRadius + cornerRadius);
            y2 = Math.sin(a2) * (fieldRadius + cornerRadius);
            addWallCorner(x1, y1, wThickness*0.6, cornerHeight, false);
            addWallCorner(x2, y2, wThickness*0.6, cornerHeight, false);
        }

        // setup the break-out blocks in the middle of the field
        breakoutZone = new Rectangle(
            fieldRadius * -0.3, fieldRadius * 0.3, fieldRadius * 0.3, fieldRadius * -0.3);
        setupBreakoutBlocks();

        // the floor
        addRect(-fieldRadius * floorPattern, -fieldRadius * floorPattern, 
                fieldRadius * floorPattern, fieldRadius * floorPattern, 
                8 , Material.floorDull);
        
    }

    /**
     * Add break-out blocks to the zone rectangle.
     */
    private void setupBreakoutBlocks() {
    	if (!Game.isBreakout()) {
    		return;
    	}
        int _ = 0;  // no block
        int x = 1;  // simple block with no action
        int d = 2;  // a block that spawns a disk
        int[][] breakoutBlocks = {
                {_, _, _, _, x, _, _, _, _},
                {_, _, _, _, _, _, _, _, _},
                {_, _, d, _, _, _, d, _, _},
                {_, _, _, _, _, _, _, _, _},
                {x, _, _, _, x, _, _, _, x},
                {_, _, _, _, _, _, _, _, _},
                {_, _, d, _, _, _, d, _, _},
                {_, _, _, _, _, _, _, _, _},
                {_, _, _, _, x, _, _, _, _}
        };


        double blockPlaceWidth =  1.0 / breakoutBlocks[0].length;
        double blockPlaceHeight = 1.0 / breakoutBlocks.length;
        double blockWidth = 0.6 * blockPlaceWidth;
        double blockHeight = 0.6 * blockPlaceHeight;
        double blockSpaceWidth = 0.2 * blockPlaceWidth;
        double blockSpaceHeight = 0.2 * blockPlaceHeight;
        for (int i=0; i<breakoutBlocks.length; i++) {
            double blockY1 = i * blockPlaceHeight + blockSpaceHeight;
            double blockY2 = blockY1 + blockHeight;
            for (int j=0; j<breakoutBlocks[i].length; j++) {
                int code = breakoutBlocks[i][j];
                if (code > 0) {
                    // create a block here
                    double blockX1 = j * blockPlaceWidth + blockSpaceWidth;
                    double blockX2 = blockX1 + blockWidth;
                    Vector2D block1 = 
                        breakoutZone.getMappedPosition(VectorFactory.getVector(blockX1, blockY1));
                    Vector2D block2 = 
                        breakoutZone.getMappedPosition(VectorFactory.getVector(blockX2, blockY1));
                    Vector2D block3 = 
                        breakoutZone.getMappedPosition(VectorFactory.getVector(blockX2, blockY2));
                    DestroyableWall block = new DestroyableWall(block1,
                            block2.copy().subtract(block1),
                            block2.copy().subtract(block3).getNormalized(),
                            block2.getDistance(block3),
                            0.3,
                            2);
                    block.setMaterial(getDestroyableWallMaterial(code));
                    block.setActionCode(breakoutBlocks[i][j]);
                    addWall(block, true);
                }
            }
        }
    }
    
    /**
     * Setup collision listeners for all objects of this playing field
     * that are suspect to collisions. The collision listeners may fire
     * visual effects on these objects. 
     */
    private void setupEffectCollisionListeners() {
        // add a collision listener that reacts on collisions with 
        // walls or disks in this playing field
        Game.getSimulation().addCollisionListener(new CollisionListener() {
            @Override
            public void collisionOccurred(DiskWallCollision e) {
                // check, if the collision happened with one of our walls
                if (getCollisionWalls().contains(e.wall)) {
                    // a wall was hit. is this a destroyable?
                    if (e.wall instanceof DestroyableWall) {
                        DestroyableWall dWall = (DestroyableWall) e.wall;
                        boolean destroyed = dWall.wallWasHit(e);

                        if (destroyed) {
                            fireAction(dWall, e.disk);
                            removeWall(dWall);
                        }
                        else {
                            fireDestroyHitEffect(dWall);
                        }
                        return;
                    }
                    // a collision with this playing field wall 
                    double strength = e.velocity.getValue() / MovingObject.MAX_VELOCITY_VALUE;
                    fireCollisionEffect(e.wall, strength);
                    return;
                }
            }

            @Override
            public void collisionOccurred(DiskDiskCollision e) {
                // check for a collision with a playing field corner
                if (getCollisionCorners().contains(e.disk1)) {
                    // a collision with this playing field corner
                    double strength = e.velocity.getValue() / MovingObject.MAX_VELOCITY_VALUE;
                    fireCollisionEffect(e.disk1, strength);
                    return;
                }
                else if (getCollisionCorners().contains(e.disk2)) {
                    // a collision with this playing field corner
                    double strength = e.velocity.getValue() / MovingObject.MAX_VELOCITY_VALUE;
                    fireCollisionEffect(e.disk2, strength);
                    return;
                }
            }
        });
    }


    /**
     * Add a wall corner at the given position and with the given properties.
     * 
     * @param x
     * @param y
     * @param rad
     * @param height
     * @param enableCollisions
     */
    private void addWallCorner(double x, double y, double rad,
            double height, boolean enableCollisions) {
        Disk corner = new Disk(rad, height);
        corner.setPosition(x, y);
        corner.setMaterial(Material.wallMaterial);
        addWallCorner(corner, enableCollisions);
    }


    /**
     * Add a new goal for the given wall model.
     * 
     * @param wall the wall model
     * @param playerIndex the player that is trying to defend this goal
     */
    private void addGoal(final Wall wall, final int playerIndex) {
        // add the wall model to the simulation
        addWall(wall, true);
        
        // if this wall is hit, we want to update player scores etc.
        Game.getSimulation().addCollisionListener(new CollisionListener() {
            @Override
            public void collisionOccurred(DiskWallCollision e) {
                if (e.wall == wall) {
                    score(playerIndex, e.disk.getLastHitPlayerIndex());

                    // if the disk is "the puck", reset the field and start a new round
                    if (e.disk == Game.getPuck()) {
                        e.disk.setPosition(getKickoffPosition(playerIndex));
                        e.disk.setVelocity(0, 0);
                        // reset the playing field
                        resetState(false);
                    }
                    // otherwise, just remove the disk
                    else if (e.disk != null) {
                        Game.getDisplay().removeObject(e.disk);
                        Game.getSimulation().removeDisk(e.disk);
                    }
                }
            }

            @Override
            public void collisionOccurred(DiskDiskCollision e) {
                // this listener is not interested in disk-disk events
            }
        });
    }


    private void score(int playerIndex, int hitByPlayerIndex) {
        Player player = Game.getPlayer(playerIndex);
        player.goalHit();
        if (Game.isClient()) {
            System.out.println("Client: The goal of " + playerIndex + " was hit by " + hitByPlayerIndex);
            return;
        }
        int[] score = Game.getScore();
        String msg;
        if (hitByPlayerIndex >= 0) {
            Player other = Game.getPlayer(hitByPlayerIndex);
            msg = "The goal of '" + player.getName() + "' was hit by '" + other.getName() + "'!";

            if (hitByPlayerIndex == playerIndex) {
                // own goal...
                score[playerIndex]--;
            }
            else {
                // other player scored
                score[hitByPlayerIndex]++;
            }
        }
        else {
            msg = "The goal of '" + player.getName() + "' was hit by no one!";
        }
        System.out.println(msg);
        Game.getConsole().addLine(msg, false);
    }


    /**
     * Add a new rectangle with the given coordinates and material.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param subdivide divide in sub-rectangles if > 1
     * @param material
     */
    private void addRect(double x1, double y1, double x2, double y2, int subdivide, 
            Material material) {
        Rectangle rect = new Rectangle(x1, y1, x2, y2, subdivide);
        rect.setMaterial(material);
        addRectangle(rect);
    }
    
    private void fireCollisionEffect(VisualObject object, double strength) {
        // add a material effect to the object
        MaterialEffect effect = new MaterialEffect(object, strength);
        object.setMaterial(effect);
    }

    /**
     * Fires an action when a destroyable wall was destroyed.
     *
     * @param dWall the wall that was hit.
     * @param disk the disk that destroyed the wall
     */
    private void fireAction(DestroyableWall dWall, Disk disk) {
        int actionCode = dWall.getActionCode();
        short playerIndex = disk.getLastHitPlayerIndex();
        System.out.println("Block destroyed! wall=" + dWall.getIndex() + ", action=" + actionCode + ", last player=" + playerIndex);
        if (actionCode <= 1) {
            return;
        }
        // replace the wall with a new disk...
        Disk newDisk = new Disk(puckDiskRadius * 0.8f, puckDiskHeight * 0.8f);
        Vector2D c1 = dWall.getCoords()[0].copy();
        Vector2D c2 = dWall.getCoords()[2].copy();
        newDisk.setPosition(c1.addMultiple(c2.subtract(c1), 0.5));
        newDisk.setMaterial(Material.doublePuckMaterial);
        newDisk.setMass(newDisk.getMass() / 2.0);
        newDisk.getVelocity().reset(disk.getVelocity()).getInverse();
        newDisk.setLastHitPlayerIndex(playerIndex);
        Game.getSimulation().addDisk(newDisk);
        Game.getDisplay().addObject(newDisk);
        breakoutDisks.add(newDisk);
    }

    /**
     * Fires an effect when a destroyable wall was hit. 
     * The effect will depend on the health status of the wall.
     * 
     * @param dWall the wall that was hit.
     */
    private void fireDestroyHitEffect(DestroyableWall dWall) {
        float h = 1.0f - (float)dWall.getWallHealth();
        Material mat = getDestroyableWallMaterial(dWall.getActionCode()).getCopy();
        float[] emission = mat.getEmission();
        float[] ambient = mat.getAmbient();
        float[] diffuse = mat.getDiffuse();
        for (int i=0; i<3; i++) {
            emission[i] *= h;
            ambient[i] *= h;
            diffuse[i] *= h;
        }
        dWall.setMaterial(mat);
    }

    private Material getDestroyableWallMaterial(int actionCode) {
        switch (actionCode) {
            case 2:
                return Material.destroyableWallMaterial2;
        }
        return Material.destroyableWallMaterial1;
    }
}
