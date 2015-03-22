/**
 * Created on 20.02.15.
 */
package de.steffens.airhockey.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Console output visible on screen in the game
 *
 * @author Steffen Schreiber
 */
public class Console extends VisualObject {

    private final ArrayList<ConsoleListener> consoleListeners = new ArrayList<ConsoleListener>();
    private ArrayList<String> lines = new ArrayList<String>();
    private boolean sticky;
    private boolean visible;


    public boolean isSticky() {
        return sticky;
    }


    public void clear() {
        lines.clear();
        updateViewer();
        synchronized (consoleListeners) {
            for (ConsoleListener cl : consoleListeners) {
                cl.clear();
            }
        }
    }

    public void addLine(String line, boolean sticky) {
        this.sticky = sticky;
        lines.add(line);
        updateViewer();
        synchronized (consoleListeners) {
            for (ConsoleListener cl : consoleListeners) {
                cl.addLine(line, sticky);
            }
        }
    }

    public List<String> getLines() {
        return lines;
    }


    /**
     * Add a new listener for simulation updates.
     *
     * @param listener the new simulation update listener.
     */
    public void addConsoleListener(ConsoleListener listener) {
        synchronized (consoleListeners) {
            consoleListeners.add(listener);
        }
    }

    /**
     * Remove a Console update listener from the list of registered
     * Console update listeners.
     *
     * @param listener the listener to remove
     */
    public void removeConsoleListener(ConsoleListener listener) {
        synchronized (consoleListeners) {
            consoleListeners.remove(listener);
        }
    }


    public boolean isVisible() {
        return visible;
    }


    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            updateViewer();
        }
    }


    /**
     * Interface used to propagate console updates.
     */
    public interface ConsoleListener {
        /**
         * Called when the console was cleared.
         */
        public void clear();

        /**
         * Called when a line was added to the console
         * @param line
         */
        public void addLine(String line, boolean sticky);
    }

}
