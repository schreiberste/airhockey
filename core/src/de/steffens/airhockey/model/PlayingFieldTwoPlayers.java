/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.model;

import java.util.ArrayList;

import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;


/**
 * Simple playing field for two players with hard-coded dimensions.
 *
 * @author Steffen Schreiber
 */
public class PlayingFieldTwoPlayers extends AbstractPlayingFieldBase {

    // the area where breakout blocks will be put
    private Rectangle breakoutZone;

    // the kick-off positions for the 2 players
    private Vector2D[] kickoffPositions = new Vector2D[2];

    private final static double plDiskRadius = 0.7;
    private final static double plDiskHeight = 0.3;
    private final static double puckDiskRadius = 0.5;
    private final static double puckDiskHeight = 0.2;


    Material wallMaterial = Material.wallMaterial;
    Material destroyableWallMaterial = Material.destroyableWallMaterial1;


    /**
     * Create a new playing field instance for 2 players
     */
    public PlayingFieldTwoPlayers() {
        super(2);
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
        disk.setMaterial(Material.playerMaterial);
        return disk;
    }


    @Override
    public Disk createPuckDisk() {
        Disk disk = new Disk(puckDiskRadius, puckDiskHeight);
        disk.setMaterial(Material.puckMaterial);
        return disk;
    }


    /**
     * @see de.steffens.airhockey.model.PlayingField#getKickoffPosition(int)
     */
    @Override
    public Vector2D getKickoffPosition(int playerIndex) {
        assert playerIndex <= reachableAreas.length;

        return kickoffPositions[playerIndex];
    }


    /**
     * @see de.steffens.airhockey.model.PlayingField#resetState(boolean)
     */
    @Override
    public synchronized void resetStateImpl() {
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


    /**
     * Setup the playing field by creating walls and corners.
     */
    protected void setupPlayingField() {
        double field_x1 = -6;        // playing field bounds
        double field_x2 = 6;
        double field_y1 = -9;
        double field_y2 = 9;
        double goal_x1 = -2;         // goal position      
        double goal_x2 = 2;

        // define the field that the players can reach by moving the mouse...
        reachableAreas[0] = new Rectangle(
            field_x1 + plDiskRadius, field_y1 + plDiskRadius,
            field_x2 - plDiskRadius, field_y2 / 3.0 - plDiskRadius);
        reachableAreas[1] = new Rectangle(
            field_x2 - plDiskRadius, field_y2 - plDiskRadius,
            field_x1 + plDiskRadius, 0 + plDiskRadius);
        reachableAreas[0].setMaterial(Material.floorReachable);
        reachableAreas[1].setMaterial(Material.floorReachable);

        // define the kick-off positions for the players
        kickoffPositions[0] = VectorFactory.getVector(0, field_y1 / 2.5);
        kickoffPositions[1] = VectorFactory.getVector(0, field_y2 / 2.5);

        // define the camera positions for the players
        cameraPositions[0][0] = 0.0f;
        cameraPositions[0][1] = -20.0f;
        cameraPositions[0][2] = 8.0f;
        cameraPositions[1][0] = 0.0f;
        cameraPositions[1][1] = 20.0f;
        cameraPositions[1][2] = 8.0f;

        double wHeight = 0.5;                   // wall height
        double wLargeThick = 1.2;               // wall thickness for large walls
        double wHalfThick = wLargeThick * 0.5;  // half wall thickness
        double wSmallThick = 0.1;               // wall thickness for small walls

        double wall_x1 = field_x1 - wHalfThick; // wall start / end coordinates
        double wall_x2 = field_x2 + wHalfThick;
        double wall_y1 = field_y1 - wHalfThick;
        double wall_y2 = field_y2 + wHalfThick;

        // left + right field border walls:
        // we can use infinite walls here for performance 
        addWall(field_x1, wall_y2, field_x1, wall_y1,
            true, wLargeThick, wHeight);
        addWall(field_x2, wall_y1, field_x2, wall_y2,
            true, wLargeThick, wHeight);

        // now the player side border walls and goal boxes...:

        // player side: border 
        addWall(wall_x1, field_y1, goal_x1, field_y1, wLargeThick, wHeight);
        addWall(goal_x2, field_y1, wall_x2, field_y1, wLargeThick, wHeight);
        addGoalBox(0, goal_x1, goal_x2, field_y1, wHeight, wLargeThick, wSmallThick);

        // opponent side: border
        addWall(wall_x2, field_y2, goal_x2, field_y2, wLargeThick, wHeight);
        addWall(goal_x1, field_y2, wall_x1, field_y2, wLargeThick, wHeight);
        addGoalBox(1, goal_x2, goal_x1, field_y2, wHeight, wLargeThick, wSmallThick);

        // round corners for the field 
        double cornerHeight = wHeight * 1.12;
        addWallCorner(wall_x1, wall_y1, wHalfThick, cornerHeight, false);
        addWallCorner(wall_x1, wall_y2, wHalfThick, cornerHeight, false);
        addWallCorner(wall_x2, wall_y1, wHalfThick, cornerHeight, false);
        addWallCorner(wall_x2, wall_y2, wHalfThick, cornerHeight, false);


        // setup the break-out blocks in the middle of the field
        breakoutZone = new Rectangle(
            field_x1 * 0.5, field_y2 * 0.2, field_x2 * 0.5, field_y1 * 0.2);
        setupBreakoutBlocks();

        // the floor made of rectangles:

        // some planes outside the playing field to cover reflections...
        addRect(-99, -99, 99, field_y1 - wHalfThick, 1, Material.floorBlack);
        addRect(-99, field_y2 + wHalfThick, 99, 999, 1, Material.floorBlack);
        addRect(-99, -99, field_x1 - wHalfThick, 999, 1, Material.floorBlack);
        addRect(field_x2 + wHalfThick, -99, 99, 999, 1, Material.floorBlack);

        // first the brilliant mirror floor 
        addRect(field_x1, field_y1, field_x2, field_y2, 4, Material.floorMirror);

        // now the more dull floor 
        // this covers the mirror where there are no lines:
        addRect(field_x1 - wHalfThick, field_y1 - wHalfThick,
            field_x2 + wHalfThick, field_y1 + 0.4, 1, Material.floorDull);
        addRect(field_x1 - wHalfThick, field_y1 - wHalfThick,
            field_x1 + 0.4, field_y2 + wHalfThick, 1, Material.floorDull);
        addRect(field_x2 - 0.4, field_y1 - wHalfThick,
            field_x2 + wHalfThick, field_y2 + wHalfThick, 1, Material.floorDull);
        addRect(field_x1 - wHalfThick, field_y2 - 0.4,
            field_x2 + wHalfThick, field_y2 + wHalfThick, 1, Material.floorDull);

        addRect(field_x1 + 0.6, field_y1 + 0.6, -0.1, -0.1, 6, Material.floorDull);
        addRect(field_x1 + 0.6, 0.1, -0.1, field_y2 - 0.6, 6, Material.floorDull);
        addRect(0.1, field_y1 + 0.6, field_x2 - 0.6, -0.1, 6, Material.floorDull);
        addRect(0.1, 0.1, field_x2 - 0.6, field_y2 - 0.6, 6, Material.floorDull);
    }


    /**
     * Add break-out blocks to the zone rectangle.
     */
    private void setupBreakoutBlocks() {
        if (!Game.isBreakout()) {
            return;
        }
        boolean x = true;
        boolean _ = false;
        boolean[][] breakoutBlocks = {
            {x, _, _, _, x, _, _, _, x},
            {_, _, _, _, _, _, _, _, _},
            {_, _, x, x, x, x, x, _, _},
            {_, _, x, x, x, x, x, _, _},
            {_, _, x, x, _, x, x, _, _},
            {_, _, x, x, x, x, x, _, _},
            {_, _, x, x, x, x, x, _, _},
            {_, _, _, _, _, _, _, _, _},
            {x, _, _, _, x, _, _, _, x}
        };

        //        boolean[][] breakoutBlocks = {
        //                { _, _, _},
        //                { _, x, _},
        //                { _, _, _}
        //        };

        double blockPlaceWidth = 1.0 / breakoutBlocks[0].length;
        double blockPlaceHeight = 1.0 / breakoutBlocks.length;
        double blockWidth = 0.8 * blockPlaceWidth;
        double blockHeight = 0.8 * blockPlaceHeight;
        double blockSpaceWidth = 0.1 * blockPlaceWidth;
        double blockSpaceHeight = 0.1 * blockPlaceHeight;
        for (int i = 0; i < breakoutBlocks.length; i++) {
            double blockY1 = i * blockPlaceHeight + blockSpaceHeight;
            double blockY2 = blockY1 + blockHeight;
            for (int j = 0; j < breakoutBlocks[i].length; j++) {
                if (breakoutBlocks[i][j]) {
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
                        0.5,
                        2);
                    block.setMaterial(destroyableWallMaterial);
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
                        System.out.println("Destroyable wall was hit! health="
                            + dWall.getWallHealth());

                        if (destroyed) {
                            removeWall(dWall);
                        } else {
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
                } else if (getCollisionCorners().contains(e.disk2)) {
                    // a collision with this playing field corner
                    double strength = e.velocity.getValue() / MovingObject.MAX_VELOCITY_VALUE;
                    fireCollisionEffect(e.disk2, strength);
                    return;
                }
            }
        });
    }


    /**
     * Add a wall at the given position and with the given properties.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param thickness
     * @param height
     */
    private void addWall(double x1, double y1, double x2, double y2,
                         double thickness, double height) {
        addWall(x1, y1, x2, y2, false, thickness, height);
    }


    /**
     * Add a wall at the given position and with the given properties.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param infinite
     * @param thickness
     * @param height
     */
    private void addWall(double x1, double y1, double x2, double y2,
                         boolean infinite, double thickness, double height) {
        Wall model = new Wall(x1, y1, x2, y2, thickness, height);
        model.setInfinite(infinite);
        model.setMaterial(wallMaterial);
        addWall(model, true);
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
        corner.setMaterial(wallMaterial);
        addWallCorner(corner, enableCollisions);
    }


    /**
     * Add a goal box from x1 to x2 at the y-position given by fieldY.
     *
     * @param playerIndex the player that tries to defend this goal
     * @param fieldY      the y-position at the playing field border
     * @param goalX1      the start of the goal box
     * @param goalX2      the end of the goal box
     * @param wHeight     the wall height
     * @param wThickLarge the thickness of large walls (size of the goal)
     * @param wThickSmall the thickness of small walls
     */
    private void addGoalBox(int playerIndex, double goalX1, double goalX2,
                            double fieldY, double wHeight,
                            double wThickLarge, double wThickSmall) {

        double invert = (goalX1 < goalX2) ? 1.0 : -1;

        double goal_yOut = fieldY - (wThickLarge * invert);
        double goal_yIn = goal_yOut + (wThickSmall * invert);
        double goal_height = wHeight - wThickSmall;

        // the roof top
        //        GLBox roofBox = new GLBox((float)goalX1, (float)fieldY, (float)goal_height,
        //                (float)goalX2, (float)goal_yOut, (float)wHeight);
        //        roofBox.setMaterial(Material.wallMaterial);
        //        Game.getDisplay().addObject(roofBox);

        // the inside wall of the goal
        // if this wall is hit, the goal counts....
        Wall goalWall = new Wall(goalX1, goal_yIn, goalX2, goal_yIn,
            0.1, goal_height);
        goalWall.setInfinite(true);
        goalWall.setMaterial(wallMaterial);
        addGoal(goalWall, playerIndex);
    }


    /**
     * Add a new goal for the given wall model.
     *
     * @param wall        the wall model
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
            return;
        }
        int[] score = Game.getScore();
        if (playerIndex == 0) {
            score[1] ++;
        }
        else {
            score[0] ++;
        }
        String msg = "The goal of '" + player.getName() + "' was hit!";
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
     * Fires an effect when a destroyable wall was hit.
     * The effect will depend on the health status of the wall.
     *
     * @param dWall the wall that was hit.
     */
    private void fireDestroyHitEffect(DestroyableWall dWall) {
        float h = 1.0f - (float) dWall.getWallHealth();
        Material mat = destroyableWallMaterial.getCopy();
        float[] emmision = mat.getEmission();
        float[] ambient = mat.getAmbient();
        float[] diffuse = mat.getDiffuse();
        for (int i = 0; i < 3; i++) {
            emmision[i] *= h;
            ambient[i] *= h;
            diffuse[i] *= h;
        }
        dWall.setMaterial(mat);
    }
}
