/*
 * Created on 19.02.2011
 */
package de.steffens.airhockey.control;

import com.badlogic.gdx.Input;

import java.awt.event.KeyEvent;

import de.steffens.airhockey.view.GLMenu;

/**
 * Class used for text input.
 *
 * @author Johannes Scheerer
 */
public class TextInputKeyboard implements TextInput {

    private StringBuilder sb = new StringBuilder();
    private boolean active = false;
    private boolean aborted = false;
    private TextInput.TextInputListener listener;
    private GLMenu.MenuItem item;


    @Override
    public void enable(GLMenu.MenuItem item, TextInput.TextInputListener l) {
        assert !active && listener == null;
        sb.setLength(0);
        listener = l;
        active = true;
        aborted = false;
        this.item = item;
        listener.update(sb.toString());
    }


    @Override
    public void setValue(String value) {
        sb.setLength(0);
        sb.append(value);
    }


    public boolean hasInteger() {
        try {
            Integer.parseInt(sb.toString());
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }


    public int getInteger() {
        try {
            return Integer.parseInt(sb.toString());
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }


    public String getString() {
        return sb.toString();
    }


    public boolean isEnabled() {
        return active;
    }


    public boolean keyPressed(int keycode) {
        assert active;

        // normally, when the text input is active we hanlde all key
        // presses here.
        boolean keyPressHandled = true;

        switch (keycode) {
            case Input.Keys.ESCAPE:
                aborted = true;
                active = false;
                break;
            case Input.Keys.UP:
            case Input.Keys.DOWN:
                active = false;
                // leave unhandled so the menu navigation will work
                keyPressHandled = false;
                break;
            case Input.Keys.ENTER:
                active = false;
                break;
            case Input.Keys.FORWARD_DEL:
            case Input.Keys.BACKSPACE:
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                    listener.update(sb.toString());
                }
                break;
            default:
                break;
        }
        if (!active) {
            listener.finished(aborted);
            listener = null;
            item = null;
        }
        return keyPressHandled;
    }


    public boolean keyTyped(char c) {
        if (isPrintableChar(c)) {
            sb.append(c);
            System.out.println("Input buffer= " + sb);
            listener.update(sb.toString());
            return true;
        }
        return false;
    }


    private boolean isPrintableChar( char c ) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
            c != KeyEvent.CHAR_UNDEFINED &&
            block != null &&
            block != Character.UnicodeBlock.SPECIALS;
    }

    public static interface TextInputListener {
        public void update(String s);

        public void finished(boolean cancelled);
    }
}
