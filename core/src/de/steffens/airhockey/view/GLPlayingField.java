/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.view;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

import java.util.ArrayList;
import java.util.List;

import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.PlayingField;
import de.steffens.airhockey.model.Rectangle;
import de.steffens.airhockey.model.Wall;

/**
 * The OpenGL Renderer for playing fields.
 * 
 * @author Steffen Schreiber
 */
public class GLPlayingField extends GLRenderable {

    /** The playing field model to render */
    private final PlayingField model;

    /**
     * Creates a new renderer for the given playing field model.
     *
     * @param model the playing field model.
     */
    public GLPlayingField(PlayingField model) {
        this.model = model;
    }


    @Override
    public void render(ModelBatch modelBatch, Environment environment, boolean reflection) {

        // 1. the floor
        for (Rectangle rect : model.getRectangles()) {
            GLRenderable viewer = (GLRenderable) rect.getViewer();
            if (viewer == null) {
                viewer = new GLRectangle(rect);
                rect.setViewer(viewer);
            }
            viewer.render(modelBatch, environment, reflection);
        }

        // 1 b) For debugging: the reachable area, if the puck is inside...
//        for (int plIndex = 0; plIndex < model.getNrOfPlayers(); plIndex++) {
//            Rectangle area = model.getReachableArea(plIndex);
//            if (area.isInside(Game.getPuck().getPosition())) {
//                GLRenderable viewer = (GLRenderable) area.getViewer();
//                if (viewer == null) {
//                    viewer = new GLRectangle(area);
//                    area.setViewer(viewer);
//                }
//                viewer.render(modelBatch, environment, reflection);
//            }
//        }

        // 2. render the walls
        for (Wall wall : getWalls()) {
            GLRenderable viewer = (GLRenderable) wall.getViewer();
            if (viewer == null) {
                viewer = new GLWall(wall);
                wall.setViewer(viewer);
            }
            viewer.render(modelBatch, environment, reflection);
        }
        // 3. render the wall corners
        for (Disk corner : model.getCorners()) {
            GLRenderable viewer = (GLRenderable) corner.getViewer();
            if (viewer == null) {
                viewer = new GLDisk(corner);
                corner.setViewer(viewer);
            }
            viewer.render(modelBatch, environment, reflection);
        }
    }

    private List<Wall> getWalls() {
    	if (Game.getTargetFPS() > 0) {
    		return new ArrayList<Wall>(model.getWalls());
    	}
    	return model.getWalls();
    }
}
