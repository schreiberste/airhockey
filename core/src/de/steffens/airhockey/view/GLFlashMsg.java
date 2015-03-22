/**
 * Created on 20.02.15.
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import de.steffens.airhockey.model.Console;
import de.steffens.airhockey.model.Game;

/**
 * The GL viewer for the console.
 *
 * @author Steffen Schreiber
 */
public class GLFlashMsg extends GLHudElement {

    private final long nanosToShow;
    private final long nanosToFade;
    private final long startTime;
    GLBitmapText text;


    public GLFlashMsg(String msg, long nanosToShow, long nanosToFade) {
        this.text = new GLBitmapText(msg, 6 * GLDisplay.ORTHO_HEIGHT / 10);
        this.text.scale = 2f;
        this.text.setColor(1f, 1f, 1f);
        this.nanosToShow = nanosToShow;
        this.nanosToFade = nanosToFade;
        this.startTime = Game.getSimulation().getCurrentTime();
    }


    @Override
    public void render(SpriteBatch spriteBatch) {
        long time = Game.getSimulation().getCurrentTime() - startTime;
        if (time < nanosToShow) {
            // just show
            text.render(spriteBatch);
        }
        else if (time < (nanosToShow + nanosToFade)) {
            float fraction = (float)(time - nanosToShow) / (float)nanosToFade;
            text.setAlpha(1.0f - fraction);
            text.scale = 1.5f + 5f * fraction;
            text.y_pos = (6f + 3f *fraction) * GLDisplay.ORTHO_HEIGHT / 10;
            text.render(spriteBatch);
        }
        else {
            // the message ended, remove from display
            setFinished(true);
        }
    }
}
