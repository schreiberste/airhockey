/*
 * Created on 25.04.2010
 *
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;

import de.steffens.airhockey.control.TextInput;
import de.steffens.airhockey.control.TextInputGdx;
import de.steffens.airhockey.control.TextInputKeyboard;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.GameMenu;
import de.steffens.airhockey.model.PlayingField;
import de.steffens.airhockey.model.Simulation;
import de.steffens.airhockey.model.VisualObject;
import de.steffens.airhockey.model.Wall;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;
import de.steffens.airhockey.net.Client;


/**
 * OpenGL based rendering display. This class creates a new window with OpenGL
 * rendered content. It will display all objects that are added using the
 * {@link #addObject} method.
 * 
 * @author Steffen Schreiber
 */
public class GLDisplay {

    // virtual display for the menu
    public final static int ORTHO_WIDTH = 1024;
    public final static int ORTHO_HEIGHT = 576;

    public final static int HUD_TOP_Y = (int) Math.round(ORTHO_HEIGHT * 0.98);

    /** light constants */
    public final static PointLight topLight = new PointLight().set(
            0.6f, 0.5f, 0.5f,   // color
            0f, 0f, 5f,         // position
            20f);               // intensity

    // light from camera position
    public final static PointLight camLight = new PointLight().set(
            1f, 1f, 1f,      // color
            0f, 0f, 0f,      // position, will change with cam
            30);             // intensity
    public static float[] camLightPosOffset = new float[] { 0f, 0f, -5f };
    public static float camLightPosFactor = 0.35f;

    // puck light
    public final static PointLight puckLight = new PointLight().set(
            0.6f, 0.6f, 1f,  // color, will change with player
            0f, 0f, 0f,      // position, will change with puck
            20);             // intensity

    // display screen dimension.
    public int dispWidth;
    public int dispHeight;


    // objects to render
    private ArrayList<GLRenderable> objects;
    private ArrayList<GLHudElement> hudObjects;
    private PlayingField field;
    private GLPlayingField fieldViewer;
    // objects waiting to get a viewer and be added to the display
    private ArrayList<VisualObject> pendingObjects;
    // objects waiting to be removed from display
    private ArrayList<VisualObject> pendingRemoves;

    private GLConsole console;
    private GLBitmapText fpsText;
    private GLBitmapText[] score;
    private GLMenu menu;

    private final TextInput input =
        Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard)
            ? new TextInputKeyboard() : new TextInputGdx();

    // some rendering quality options...
    private boolean renderReflections = true;
    private boolean renderWireframe = false;


    private float[] camPos = new float[3];
    private int followPlayer = -1;

    // Temporary scratch vector used for temporary results.
    private Vector2D tmp = VectorFactory.getVector(0, 0);

    private Simulation simulation;

    private boolean runAsFastAsPossible = true;
    private double fps = 0;
    private long lastFpsTime = 0;
    private int lastSecondFrames = 0;

    private int[] goals;
    private boolean doNotExit = false;


    /**
     * Create a new window that renders the game state of the given simulation.
     *
     * @param title the window title text
     * @param simulation the game simulation to visualize
     */
    public GLDisplay(String title, Simulation simulation) {
        init(title, simulation);
    }

    /**
     * Set the display screen dimension.
     *
     * @param width  the display width in pixels
     * @param height the display height in pixels
     */
    public void setSize(int width, int height) {
        dispWidth = width;
        dispHeight = height;
    }


    /**
     * Reset this display to initial state using the given window title and simulation.
     *
     * @param title the window title text
     * @param simulation the game simulation to visualize
     */
    public void init(String title, Simulation simulation) {
        this.simulation = simulation;
        objects = new ArrayList<GLRenderable>();
        hudObjects = new ArrayList<GLHudElement>();
        pendingObjects = new ArrayList<VisualObject>();
        pendingRemoves = new ArrayList<VisualObject>();

        // score board, centered
        int players = Game.getPlayerCount();
        score = new GLBitmapText[players];
        goals = new int[players];
        float letterWidth = 70;
        float center = ORTHO_WIDTH / 2f;
        float scoresStart = center - (letterWidth * (float) players) / 2f;
        for (int i = 0; i < goals.length; i++) {
            float x = scoresStart + i * letterWidth;
            score[i] = new GLBitmapText("0", x, x + letterWidth, HUD_TOP_Y);
            score[i].setColor(0.8f, 0.8f, 0.8f);
            goals[i] = -1;
        }

        fpsText = new GLBitmapText("FPS: " + fps, (int)Math.round(ORTHO_WIDTH * 0.9), HUD_TOP_Y);
        fpsText.scale = 0.4f;
        console = new GLConsole(Game.getConsole());

        menu = new GLMenu();
        GameMenu.create(menu, input);
        hudObjects.add(menu);

        Gdx.graphics.setTitle(title);

        // probably stop old rendering here?

        if (Game.hasHumanPlayer()) {
            menu.disable();
        } else {
            menu.enable();
        }

//        setInframeCursor(frame);

//        int targetFps = Game.getTargetFPS();
//        animator = targetFps > 0
//                ? new FPSAnimator(canvas, targetFps, true)
//                : new Animator(canvas);
//        animator.setRunAsFastAsPossible(runAsFastAsPossible);

        addViewer(console);
        addViewer(fpsText);
        for (int i=0; i<score.length; i++) {
            addViewer(score[i]);
        }
    }


    /**
     * Set the playing field to render
     * @param field the playing field
     */
    public void setPlayingField(PlayingField field) {
        if (fieldViewer != null) {
            objects.remove(fieldViewer);
        }
        this.field = field;
        fieldViewer = new GLPlayingField(field);
        // playing field renderer should be the first object for now.
        objects.add(0, fieldViewer);
    }


    public void showFlashMsg(String text, long nanosToShow, long nanosToFade) {
        synchronized (hudObjects) {
            hudObjects.add(new GLFlashMsg(text, nanosToShow, nanosToFade));
        }
    }


    /**
     * Add a new visual object to the display.
     * This will create a viewer for the object and render it at the next render time.
     * @param visualObject the new visual object to add
     */
    public void addObject(VisualObject visualObject) {
        synchronized (pendingObjects) {
            pendingObjects.add(visualObject);
        }
    }


    /**
     * Remove a visual object from the display.
     * @param visualObject the visual object to remove
     **/
    public void removeObject(VisualObject visualObject) {
        synchronized (pendingRemoves) {
            pendingRemoves.add(visualObject);
        }
    }


    /**
     * Add a new renderable object to this display.
     * The object will be rendered on all calls to the
     * {@link #display(com.badlogic.gdx.graphics.g3d.ModelBatch, com.badlogic.gdx.graphics.g3d.Environment, com.badlogic.gdx.graphics.PerspectiveCamera)}
     * method from now on.
     *
     * @param viewer the new renderable object to add
     */
    private void addViewer(GLViewer viewer) {
        if (viewer instanceof GLRenderable) {
            objects.add((GLRenderable) viewer);
        }
        else {
            hudObjects.add((GLHudElement) viewer);
        }
    }

    private void removeViewer(GLViewer viewer) {
        if (viewer instanceof GLRenderable) {
            objects.remove(viewer);
        }
        else {
            hudObjects.remove(viewer);
        }
        viewer.dispose();
    }

    /**
     * Set the camera position.
     *
     * @param pos  the camera position
     */
    private void setCameraPosition(float[] pos) {
        for (int i = 0; i < 3; i++) {
            camPos[i] = pos[i];
        }
        camLight.position.set(
                camLightPosFactor * pos[0] + camLightPosOffset[0],
                camLightPosFactor * pos[1] + camLightPosOffset[1],
                camLightPosFactor * pos[2] + camLightPosOffset[2]);
    }

    /**
     * Set the player to follow in this display.
     *
     * @param playerIndex  the player index
     */
    public void followPlayer(int playerIndex) {
        if (playerIndex == -1) {
            setCameraPosition(new float[]{0.1f, 0f, 30f});
            followPlayer = -1;
        }

        else if (Game.getPlayerCount() > playerIndex) {
            setCameraPosition(Game.getPlayingField().getCameraPosition(
                    playerIndex));
            followPlayer = playerIndex;
        }
    }

    public void display(ModelBatch modelBatch, Environment environment, PerspectiveCamera cam) {
        if (Game.getTargetFPS() < 0) {
            simulation.update();
        }
        // handle adding / removing viewers for pending objects
        handlePendingObjects();

        // block simulation updates during rendering
        simulation.blockSimulationUpdates();

        // update any playing field animations
        field.update();

        long newTime = simulation.getSimulationTime();
        lastSecondFrames++;
        if (newTime - lastFpsTime > 1000000000.0) {
            // a new second
            fps = lastSecondFrames;
            lastSecondFrames = 0;
            lastFpsTime = newTime;
            fpsText.text = "FPS: " + fps;
        }
        boolean scoreChanged = false;
        int playerIdx = Math.max(0, Client.getPlayer());
        int[] gameScore = Game.getScore();
        for (int i = 0; i < goals.length; i++) {
            score[i].setBorderColor(Game.getPlayer((i + playerIdx) % goals.length).getColor());
            if (goals[i] != gameScore[i]) {
                goals[i] = gameScore[i];
                scoreChanged = true;
            }
        }
        if (scoreChanged) {
            for (int i = 0; i < goals.length; i++) {
                score[i].text = String.valueOf(goals[(i + playerIdx) % goals.length]);
            }
        }

        // setup camera and lights
        setupCamera(cam);
        setupLight(environment);

        modelBatch.begin(cam);
        displayImpl(modelBatch, environment, cam);

        // allow simulation updates again
        simulation.allowSimulationUpdates();

        // this is where most time is spent for rendering, so we do this after allowing
        // the simulation to update again.
        modelBatch.end();
    }

    /**
     * Handle creation or removal of viewers for pending objects.
     * This must be called in the render thread.
     */
    private void handlePendingObjects() {
        synchronized (pendingObjects) {
            for (int i=0; i<pendingObjects.size(); i++) {
                VisualObject visObj = pendingObjects.get(i);
                GLViewer viewer = visObj.getViewer();
                if (viewer == null) {
                    if (visObj instanceof Disk) {
                        viewer = new GLDisk((Disk) visObj);
                    }
                    else if (visObj instanceof Wall) {
                        viewer = new GLWall((Wall) visObj);
                    }
                    visObj.setViewer(viewer);
                }
                addViewer(viewer);
            }
            pendingObjects.clear();
        }

        synchronized (pendingRemoves) {
            for (int i=0; i<pendingRemoves.size(); i++) {
                VisualObject visObj = pendingRemoves.get(i);
                GLViewer viewer = visObj.getViewer();
                if (viewer != null) {
                    visObj.setViewer(null);
                    removeViewer(viewer);
                }
            }
            pendingRemoves.clear();
        }
    }


    public void display(SpriteBatch spriteBatch) {
        synchronized (hudObjects) {
            Iterator<GLHudElement> it = hudObjects.iterator();
            while (it.hasNext()) {
                GLHudElement element = it.next();
                element.render(spriteBatch);
                if (element.isFinished()) {
                    it.remove();
                }
            }
        }
    }

    public void renderShapes(ShapeRenderer shapeRenderer) {
        synchronized (hudObjects) {
            Iterator<GLHudElement> it = hudObjects.iterator();
            while (it.hasNext()) {
                GLHudElement element = it.next();
                element.renderShapes(shapeRenderer);
            }
        }
    }

    /**
     * The actual implementation of the rendering of the scene.
     */
    private void displayImpl(ModelBatch modelBatch, Environment environment, PerspectiveCamera cam) {
//
//        if (renderWireframe) {
//            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
//            gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
//            gl.glLineWidth(1f);
//        }
//        else {
//            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
//            gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
//        }
//

        // now render the scene:

//        if (renderReflections && !renderWireframe) {
//            // render reflections:
//            // mirror along x/y-plane
//            cam.combined.scale(1f, 1f, -1f);
//            cam.update();
//
//            // render objects first pass
////            gl.glDisable(GL.GL_CULL_FACE);
//            for(GLRenderable renderable : objects) {
//                renderable.render(modelBatch, environment, true);
//            }
//
//            // revert to normal
////            gl.glEnable(GL.GL_CULL_FACE);
//            cam.combined.scale(1f, 1f, -1f);
//            cam.update();
//        }

        // render all objects
        for(GLRenderable renderable : objects) {
            renderable.render(modelBatch, environment, false);
        }
    }


    /**
     * Setup the scene lights.
     *
     */
    private void setupLight(Environment environment) {
        environment.clear();

        // setup lighting...
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.3f, 0.3f, 0.3f, -1f, -0.8f, -0.2f));

        environment.add(topLight);
        environment.add(camLight);

        // third light from puck position
        Vector2D puckPosition = Game.getPuck().getPosition();
        puckLight.position.set(
                (float)puckPosition.getX(),
                (float)puckPosition.getY(),
                3.5f);
        short playerIdx = Game.getPuck().getLastHitPlayerIndex();
        if (playerIdx >= 0) {
            float[] color = Game.getPlayer(playerIdx).getColor();
            puckLight.color.set(color[0], color[1], color[2]+0.2f, 1f);
        }

        environment.add(puckLight);
    }

        /**
         * Setup the camera view.
         *
         */
        private void setupCamera (PerspectiveCamera cam){

            cam.viewportWidth = dispWidth;
            cam.viewportHeight = dispHeight;

            cam.position.set(camPos[0], camPos[1], camPos[2]);
            cam.up.set(0f, 0f, 1f);
            cam.lookAt(0, 0, 0);
            cam.near = 1f;
            cam.far = 300f;

            // rotate view following the players disk...
//            if (followPlayer >= 0) {
//                // get player position into tmp
//                tmp.reset(Game.getPlayer(followPlayer).getControlledDisk().getPosition());
//                // map to his reachable area in [0..1] range
//                Rectangle area = Game.getPlayingField().getReachableArea(followPlayer);
//                Vector2D playerPosition = area.getMappedInverse(tmp);
//                float rotX = (float) (playerPosition.getY() - 0.5) * -5f;
//                float rotZ = (float) (playerPosition.getX() - 0.5) * -5f;
//                // use the right-axis of the area to rotate on y-movement
//                Vector2D rightAxis = area.getRight();
//
//                cam.rotate(rotX, (float) rightAxis.getX(), (float) rightAxis.getY(), 0.0f);
//                cam.rotate(rotZ, 0.0f, 0.0f, 1.0f);
//            }

            cam.update();
        }



    public void dispose() {
        for (GLRenderable renderable : objects) {
            renderable.dispose();
        }
        for (GLHudElement hudElement : hudObjects) {
            hudElement.dispose();
        }
        GLBitmapText.disposeFont();
    }


    public GLMenu getMenu() {
        return menu;
    }


    public TextInput getInput() {
        return input;
    }
}