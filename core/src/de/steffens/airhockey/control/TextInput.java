/*
 * Created on 19.02.2011
 */
package de.steffens.airhockey.control;

import de.steffens.airhockey.view.GLMenu;

/**
 * Class used for text input.
 *
 * @author Johannes Scheerer
 */
public interface TextInput {

    public void enable(GLMenu.MenuItem item, TextInputListener l);

    public void setValue(String value);

    public boolean hasInteger();

    public int getInteger();

    public String getString();

    public boolean isEnabled();

    /**
     * Handle pressed key.
     *
     * @param keycode
     * @return <code>true</code>, if the key press was handled here
     */
    public boolean keyPressed(int keycode);

    public boolean keyTyped(char c);


    public static interface TextInputListener {
        public void update(String s);

        public void finished(boolean cancelled);
    }
}
