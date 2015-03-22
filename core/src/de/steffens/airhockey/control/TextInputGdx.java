/**
 * Created on 15.02.15.
 */
package de.steffens.airhockey.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import de.steffens.airhockey.view.GLMenu;

/**
 * Text input implementation that uses libGdx text input.
 * This will probably be a dialog asking for input.
 */
public class TextInputGdx implements TextInput {

    private String input = "";
    private boolean active;


    @Override
    public void enable(GLMenu.MenuItem item, final TextInputListener l) {
        active = true;
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                input = text;
                l.finished(false);
                active = false;
            }


            @Override
            public void canceled() {
                input = "";
                l.finished(true);
                active = false;
            }
        },
            item.getLabel(),
            "",
            item.getValue()
        );
    }


    @Override
    public void setValue(String value) {
        input = value;
    }


    @Override
    public boolean hasInteger() {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }


    @Override
    public int getInteger() {
        try {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }


    @Override
    public String getString() {
        return input;
    }


    @Override
    public boolean isEnabled() {
        return active;
    }


    @Override
    public boolean keyPressed(int keycode) {
        return false;
    }


    @Override
    public boolean keyTyped(char c) {
        return false;
    }
}
