/*
 * Created on 01.11.2010
 *
 */
package de.steffens.airhockey.control;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.vector.Vector2D;

/**
 * Processor for input (mouse, touch) that controls a human player
 *
 * @author Steffen Schreiber
 */
public class Mouse extends InputAdapter {

    /**
     * Size of the filter history list.
     */
    public final static int FILTER_HISTORY_SIZE = 10;

    // singleton instance
    private static final Mouse instance = new Mouse();

    private static final boolean isOSX = System.getProperty("os.name").contains("OS X");

    private static MousePosition[] lastMousePositions;
    private static int lastMousePositionIndex;
    private PerspectiveCamera cam;


    private static MousePosition[] createLastMousePositions() {
        MousePosition[] result = new MousePosition[FILTER_HISTORY_SIZE];
        for (int i = 0; i < result.length; i++) {
            result[i] = new MousePosition(0, 0, 0);
        }
        return result;
    }


    // the xy-plane used to calculate intersections with the pick ray
    final Plane xyPlane = new Plane(new Vector3(0, 0, 1), 0);
    final Vector3 intersection = new Vector3();


    /**
     * Set the world camera used for unprojecting from screen to world
     * coordinates.
     *
     * @param cam
     */
    public void setCam(PerspectiveCamera cam) {
        this.cam = cam;
    }


    /**
     * Small class to hold a mouse position at a given time.
     */
    public static class MousePosition {
        double x;
        double y;
        long time;

        /**
         * @param newX
         * @param newY
         * @param newTime
         */
        public MousePosition(double newX, double newY, long newTime) {
            x = newX;
            y = newY;
            time = newTime;
        }

        private void set(double newX, double newY, long newTime) {
            x = newX;
            y = newY;
            time = newTime;
        }
    }

    /**
     * Private constructor to force singleton.
     */
    private Mouse() {
        lastMousePositions = createLastMousePositions();
        lastMousePositionIndex = 0;
        lastMousePositions[lastMousePositionIndex].set(0.5, 0.1, 0);
    }

    /**
     * @return the singleton instance.
     */
    public static Mouse getInstance() {
        return instance;
    }


    /**
     * Get the relative mouse position extrapolated for the given time.
     * This may use some filtering of the last mouse positions to get a smooth
     * approximation of the mouse position at the given time. The requested
     * time should be shortly after the last mouse event...
     *
     * @param time the time to get a mouse position for
     * @return the position
     */
    public static MousePosition getFilteredMousePosition(long time) {
        // TODO: implement filtering.
        // for the moment, we just return the last mouse position
        synchronized (lastMousePositions) {
            return lastMousePositions[lastMousePositionIndex];
        }
    }

    /**
     * The handler for touch movement events.
     * This translates window coordinates to relative coordinates and stores
     * the new mouse position.
     */
    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        long newTime = Game.getSimulation().getCurrentTime();

        // calculate world coordinates at the xy-plane
        Ray pickRay = cam.getPickRay(x, y);
        Intersector.intersectRayPlane(pickRay, xyPlane, intersection);

        // add the new mouse position to the position history for filtering
        synchronized (lastMousePositions) {
            lastMousePositionIndex = (lastMousePositionIndex + 1) % lastMousePositions.length;
            lastMousePositions[lastMousePositionIndex].set(intersection.x, intersection.y, newTime);
        }
        // mouse event is handled.
        return true;
    }


    /**
     * The handler for mouse movement events.
     * This translates window coordinates to relative coordinates and stores
     * the new mouse position.
     */
    @Override
    public boolean mouseMoved(int x, int y) {
        long newTime = Game.getSimulation().getCurrentTime();

        // calculate world coordinates at the xy-plane
        Ray pickRay = cam.getPickRay(x, y);
        Intersector.intersectRayPlane(pickRay, xyPlane, intersection);

        // add the new mouse position to the position history for filtering
        synchronized (lastMousePositions) {
            lastMousePositionIndex = (lastMousePositionIndex + 1) % lastMousePositions.length;
            lastMousePositions[lastMousePositionIndex].set(intersection.x, intersection.y, newTime);
        }
        // mouse event is handled.
        return true;
    }


    public static void setMousePosition(Vector2D position) {
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            Vector3 screenPos = instance.cam.project(
                new Vector3((float) position.getX(), (float) position.getY(), 0f));

            // workaround for bug in gdx / lwjgl:
            // setting mouse position is not consistent across desktops...
            if (isOSX) {
                Gdx.input.setCursorPosition(Math.round(screenPos.x), Math.round(screenPos.y));
            }
            else {
                Gdx.input.setCursorPosition(Math.round(screenPos.x),
                    Gdx.graphics.getHeight() - Math.round(screenPos.y));
            }
        }
    }
}
