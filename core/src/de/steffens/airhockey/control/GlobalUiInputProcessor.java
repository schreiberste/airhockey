/*
 * Created on 01.11.2010
 *
 */
package de.steffens.airhockey.control;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;


import de.steffens.airhockey.model.Collision;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.view.GLMenu;

/**
 * Processor for input (mouse, touch) that controls the global UI
 * like menu etc.
 *
 * @author Steffen Schreiber
 */
public class GlobalUiInputProcessor extends InputAdapter {

    // singleton instance
    private static final GlobalUiInputProcessor instance = new GlobalUiInputProcessor();

    private int dragStartX = -1;
    private int dragStartY = -1;
    private boolean dragging = false;

    /**
     * Private constructor to force singleton.
     */
    private GlobalUiInputProcessor() {
    }


    /**
     * @return the singleton instance.
     */
    public static GlobalUiInputProcessor getInstance() {
        return instance;
    }


    @Override
    public boolean keyDown(int keycode) {
        GLMenu menu = Game.getDisplay().getMenu();
        TextInput input = Game.getDisplay().getInput();

        // first handle menu input
        if (input.isEnabled()) {
            if (input.keyPressed(keycode)) {
                return true;
            };
        }

        switch (keycode) {
            case Input.Keys.SPACE:
                // toggle simulation pause
                Game.getSimulation().togglePause();
                break;

            // switch camera keys...
            case Input.Keys.NUM_1:
                Game.getDisplay().followPlayer(0);
                break;
            case Input.Keys.NUM_2:
                Game.getDisplay().followPlayer(1);
                break;
            case Input.Keys.NUM_3:
                Game.getDisplay().followPlayer(2);
                break;
            case Input.Keys.NUM_4:
                Game.getDisplay().followPlayer(3);
                break;
            case Input.Keys.NUM_5:
                Game.getDisplay().followPlayer(4);
                break;
            case Input.Keys.NUM_6:
                Game.getDisplay().followPlayer(5);
                break;
            case Input.Keys.NUM_0:
                // switch camera to a side-view
                Game.getDisplay().followPlayer(-1);
                break;

            case Input.Keys.PLUS:
                if (menu.isActive()) {
                    menu.selectPlus();
                }
                else {
                    Collision.IMPULSE_LOSS += 0.02;
                    System.out.println("Impulse loss = " + Collision.IMPULSE_LOSS);
                }
                break;

            case Input.Keys.MINUS:
                if (menu.isActive()) {
                    menu.selectMinus();
                }
                else {
                    Collision.IMPULSE_LOSS -= 0.02;
                    System.out.println("Impulse loss = " + Collision.IMPULSE_LOSS);
                }
                break;

            // menu keys
            case Input.Keys.UP:
                if (menu.isActive()) {
                    menu.selectUp();
                }
                break;
            case Input.Keys.DOWN:
                if (menu.isActive()) {
                    menu.selectDown();
                }
                break;
            case Input.Keys.LEFT:
                if (menu.isActive()) {
                    menu.selectMinus();
                }
                break;
            case Input.Keys.RIGHT:
                if (menu.isActive()) {
                    menu.selectPlus();
                }
                break;
            case Input.Keys.ENTER:
                if (menu.isActive()) {
                    menu.select();
                }
                break;
            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
                menu.selectBack();
                break;

            default:
                // ignore, key was not handled here!
                return false;
        }
        // key was handled here
        return true;
    }


    @Override
    public boolean keyUp(int keycode) {
        return false;
    }


    @Override
    public boolean keyTyped(char character) {
        TextInput input = Game.getDisplay().getInput();

        if (input.isEnabled()) {
            input.keyTyped(character);
            return true;
        }
        return false;
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        dragStartX = screenX;
        dragStartY = screenY;
        GLMenu menu = Game.getDisplay().getMenu();
        if (menu.isActive()) {
            menu.handleTouch(screenX, screenY, true, false);
            return true;
        }
        return false;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        dragging = false;
        GLMenu menu = Game.getDisplay().getMenu();
        if (menu.isActive()) {
            menu.handleTouch(screenX, screenY, false, false);
            return true;
        }
        return false;
    }



    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        int dx = screenX - dragStartX;
        int dy = screenY - dragStartY;
        if (dragging || Math.sqrt(dx*dx + dy*dy) > Game.getDisplay().dispWidth * 0.01) {
            dragging = true;
            GLMenu menu = Game.getDisplay().getMenu();
            if (menu.isActive()) {
                menu.handleTouch(screenX, screenY, true, true);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        GLMenu menu = Game.getDisplay().getMenu();
        if (menu.isActive()) {
            menu.handleTouch(screenX, screenY, false, true);
            return true;
        }
        return false;
    }


    @Override
    public boolean scrolled(int amount) {
        GLMenu menu = Game.getDisplay().getMenu();
        if (menu.isActive()) {
            menu.handleScroll(amount);
            return true;
        }
        return false;
    }
}
