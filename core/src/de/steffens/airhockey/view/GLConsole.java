/**
 * Created on 20.02.15.
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import de.steffens.airhockey.model.Console;

/**
 * The GL viewer for the console.
 *
 * @author Steffen Schreiber
 */
public class GLConsole extends GLHudElement {

    private int maxLinesToShow = 4;
    private final Console console;
    private float alpha = 4.0f;
    GLBitmapText text;


    public GLConsole(Console console) {
        this.console = console;
        this.console.setViewer(this);
        this.text = new GLBitmapText("", 10, GLDisplay.ORTHO_HEIGHT - 10);
        this.text.scale = 0.35f;
    }


    @Override
    public void render(SpriteBatch spriteBatch) {
        // fade out....
        alpha = console.isSticky() ? 4f : alpha * 0.985f;
        text.setAlpha(alpha > 1f ? 1f : alpha < 0f ? 0f : alpha);
        text.render(spriteBatch);
    }


    @Override
    public void update() {
        if (console.isVisible()) {
            alpha = 4.0f;
            List<String> lines = console.getLines();
            int start = Math.max(0, lines.size() - maxLinesToShow);
            StringBuilder result = new StringBuilder();

            for (int i=start; i<lines.size(); i++) {
                if (i != start) {
                    result.append("\n");
                }
                result.append(lines.get(i));
            }

            text.text = result.toString();
        }
    }
}
