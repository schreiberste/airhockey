/*
 * Created on 19.02.2011
 */
package de.steffens.airhockey.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

import de.steffens.airhockey.model.Game;

/**
 * Class implementing the menu of the game.
 * 
 * @author Johannes Scheerer
 */
public class GLMenu extends GLHudElement {

    private static final int menuStartX = Math.round(GLDisplay.ORTHO_WIDTH * 0.12f);
    private static final int menuEntryX = Math.round(GLDisplay.ORTHO_WIDTH * 0.15f);
    private static final int sizeY = Math.round(GLDisplay.ORTHO_HEIGHT * 0.9f);
    private static final int offsetY = Math.round(GLDisplay.ORTHO_HEIGHT / 10f);
    private static final int menuStartY = sizeY - offsetY;
    private static final int menuHeight = sizeY - 2 * offsetY;
    private static final int menuWidth = GLDisplay.ORTHO_WIDTH - (2 * menuStartX);

    private final ShapeRenderer shapeRenderer;

    private boolean active = true;
    private GLBitmapText[] itemViewers;
    private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
    private ArrayList<Menu> previousMenus = new ArrayList<Menu>();
    private int selection = 0;
    private boolean disableBack = false;

    private MenuItem mouseDragItem = null;


    public void add(MenuItem item) {
        items.add(item);
        item.setMenu(this);
    }

    public GLMenu() {
        shapeRenderer = new ShapeRenderer();

    }

    public void addMenu() {
        previousMenus.add(new Menu());
        items = new ArrayList<MenuItem>();
    }


    /**
     * Render any shapes associated with this object.
     * This will be called before render().
     * @param shapeRenderer
     */
    public void renderShapes(ShapeRenderer shapeRenderer) {
        if (!active) {
            return;
        }
        // height of one menu entry line
        int entryHeight = menuHeight / itemViewers.length;

        boolean hasSelection = selection >= 0 && selection < items.size()
            && items.get(selection).action != null;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.4f);
        shapeRenderer.rect(menuStartX, offsetY, menuWidth, menuHeight);
        if (hasSelection) {
            // draw selected menu entry background...
            shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 0.4f);
            int entryY = menuStartY - selection * entryHeight;
            shapeRenderer.rect(menuStartX, entryY - entryHeight, menuWidth, entryHeight);
        }
        // draw special menu entries
        for (int idx = 0; idx < items.size(); idx++) {
            MenuItem item = items.get(idx);
            if (item instanceof ColorMenuItem) {
                ColorMenuItem colItem = (ColorMenuItem) item;
                colItem.render(menuWidth, entryHeight, idx, selection == idx, shapeRenderer);
            }
            if (item instanceof ScaleMenuItem) {
                ScaleMenuItem scaleItem = (ScaleMenuItem) item;
                scaleItem.render(menuWidth, entryHeight, idx, selection == idx, shapeRenderer);
            }
        }
        shapeRenderer.end();
        // draw the same background as an outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 1f, 0.15f);
        shapeRenderer.rect(menuStartX, offsetY, menuWidth, menuHeight);
        if (hasSelection) {
            shapeRenderer.setColor(0.5f, 0.5f, 0.7f, 0.8f);
            int entryY = menuStartY - selection * entryHeight;
            shapeRenderer.rect(menuStartX, entryY - entryHeight, menuWidth, entryHeight);
        }
        shapeRenderer.end();
    }


    @Override
    public void render(SpriteBatch spriteBatch) {
        if (!active || itemViewers == null) {
            return;
        }

        for (int i = 0; i < itemViewers.length; i++) {
            if (i == selection && items.get(i).action != null) {
                itemViewers[i].setAlpha(1);
                itemViewers[i].scale = 1.1f;
            } else {
                itemViewers[i].setAlpha(0.6f);
                itemViewers[i].scale = 1.0f;
            }
            itemViewers[i].render(spriteBatch);
        }
    }

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void enableBack() {
        disableBack = false;
    }

    public void disableBack() {
        disableBack = true;
    }


    /**
     * Handle touch or mouse click events.
     *
     * @param displayX position x
     * @param displayY position y
     * @param down button/finger was down
     * @param moved either just mouse move or drag
     */
    public void handleTouch(int displayX, int displayY, boolean down, boolean moved) {
        float x = getOrthoX(displayX);
        float y = getOrthoY(displayY);

        int itemIndex = getItemAt(x, y);
        if (moved) {
            if (down) {
                // handle dragging...
                if (mouseDragItem == null) {
                    if (itemIndex >= 0) {
                        mouseDragItem = items.get(itemIndex);
                    }
                }
                mouseDragItem.handleDrag(x,y);
            }
            else {
                // moving the mouse, let the selection follow
                if (itemIndex >= 0) {
                    selectIndex(itemIndex);
                }
            }
        }
        else {
            if (down) {
                // button/finger just went down. just set selection
                if (itemIndex >= 0) {
                    selectIndex(itemIndex);
                }
            }
            else {

                // button/finger went up,
                if (mouseDragItem != null) {
                    // finished dragging
                    mouseDragItem = null;
                }
                else {
                    // perform action, if any
                    if (itemIndex >= 0) {
                        select();
                    }
                }
            }
        }
    }


    /**
     * Handle mouse scroll.
     * @param amount
     */
    public void handleScroll(int amount) {
        if (selection < 0 || selection >= items.size()) {
            return;
        }
        MenuAction action = items.get(selection).action;
        if (action != null) {
            action.run(items.get(selection), amount > 0 ? -1 : 1 );
        }
    }

    private float getOrthoX(int displayX) {
        // calculate relative position in ortho
        GLDisplay display = Game.getDisplay();
        return displayX * GLDisplay.ORTHO_WIDTH / (float) display.dispWidth;
    }

    private float getOrthoY(int displayY) {
        // calculate relative position in ortho
        GLDisplay display = Game.getDisplay();
        return GLDisplay.ORTHO_HEIGHT - (displayY * GLDisplay.ORTHO_HEIGHT / (float) display.dispHeight);
    }

    private int getItemAt(float xPos, float yPos) {
        if (!active) {
            return -1;
        }

        if (xPos < menuStartX || xPos > GLDisplay.ORTHO_WIDTH - menuStartX) {
            return -1;
        }

        // height of one menu entry line
        int entryHeight = menuHeight / itemViewers.length;

        int index = (int)Math.floor((menuStartY - yPos) / (float)entryHeight);
        if (index >= 0 && index < items.size()) {
            return index;
        }
        return -1;
    }

    public void selectUp() {
        changeSelection(-1);
    }

    public void selectDown() {
        changeSelection(1);
    }

    private void selectIndex(int index) {
        selection = (index + itemViewers.length) % itemViewers.length;
    }

    private void changeSelection(int offset) {
        selection = (selection + itemViewers.length + offset) % itemViewers.length;
    }

    public void select() {
        MenuAction action = items.get(selection).action;
        if (action != null) {
            action.run(items.get(selection), 0);
        }
    }

    public void selectPlus() {
        MenuAction action = items.get(selection).action;
        if (action != null) {
            action.run(items.get(selection), +1);
        }
    }

    public void selectMinus() {
        MenuAction action = items.get(selection).action;
        if (action != null) {
            action.run(items.get(selection), -1);
        }
    }

    public void selectBack() {
        if (disableBack) {
            return;
        }
        if (previousMenus.size() > 0) {
            Menu last = previousMenus.remove(previousMenus.size() - 1);
            items = last.items;
            selection = last.selection;
            itemViewers = last.entries;
        } else {
            active = !active;
        }
    }

    @Override
    public void update() {
        GLBitmapText[] tmpEntries = new GLBitmapText[items.size()];
        int entryHeight = menuHeight / tmpEntries.length;

        int spacing = (entryHeight - GLBitmapText.FONT_HEIGHT) / 2;
        for (int i = 0; i < tmpEntries.length; i++) {
            int entryOffset = menuStartY - i * entryHeight;
            int entryY = entryOffset - spacing + 2;
            if (items.get(i).isCentered()) {
                tmpEntries[i] = new GLBitmapText("", entryY);
            } else {
                tmpEntries[i] = new GLBitmapText("", menuEntryX, entryY);
            }
            tmpEntries[i].setColor(1.0f, 0.95f, 0.7f);
            items.get(i).setViewer(tmpEntries[i]);
        }
        selection = 0;
        itemViewers = tmpEntries;
    }


    public static class MenuItem {
        private String label;
        private String value;
        private boolean typing;
        protected GLBitmapText viewer;
        protected boolean centered;
        protected MenuAction action;
        protected GLMenu menu;


        /**
         * Create a new menu item without action. This item will be displayed centered.
         * 
         * @param label the item label
         */
        public MenuItem(String label) {
            this(label, null, null, true);
        }

        public MenuItem(String label, MenuAction menuAction) {
            this(label, null, menuAction, false);
        }

        public MenuItem(String label, String value, MenuAction menuAction) {
            this(label, value, menuAction, false);
        }

        public MenuItem(String label, String value, MenuAction menuAction, boolean centered) {
            this.label = label;
            this.value = value;
            this.action = menuAction;
            this.centered = centered;
        }

        public void updateLabel(String newLabel) {
            label = newLabel;
            updateViewer();
        }

        public void updateValue(String newValue, boolean typing) {
            value = newValue;
            this.typing = typing;
            updateViewer();
        }

        private void updateViewer() {
            viewer.text = (value == null) ? label : label + ": " + value;
            if (typing) {
                viewer.setColor(0f, 1f, 0f);
                viewer.text += "_";
            } else {
                viewer.setColor(1.0f, 0.95f, 0.7f);
            }
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public void setViewer(GLBitmapText viewer) {
            this.viewer = viewer;
            updateViewer();
        }

        public boolean isCentered() {
            return centered;
        }

        public void setMenu(GLMenu menu) {
            this.menu = menu;
        }

        public GLMenu getMenu() {
            return menu;
        }


        public void handleDrag(float x, float y) {
            // nothing to do for text items
        }
    }

    /**
     * Menu item for color chooser.
     */
    public abstract static class ColorMenuItem extends MenuItem {

        private float[] color;


        public ColorMenuItem(String label, float[] color) {
            this(label, color, true);
        }

        private ColorMenuItem(String label, float[] color, boolean createAction) {
            super(label);
            centered = false;
            this.color = color;
            if (createAction) {
                action = new MenuAction() {
                    @Override
                    public void run(MenuItem item, int code) {
                        showColorMenu();
                    }
                };
            }
        }

        public float[] getColor() {
            return color;
        }

        private void showColorMenu() {
            menu.addMenu();
            menu.add(new ColorMenuItem(getLabel(), color, false) {
                @Override
                public void finished(float[] color) {}
            });
            menu.add(new ScaleMenuItem("Red", 0, color));
            menu.add(new ScaleMenuItem("Green", 1, color));
            menu.add(new ScaleMenuItem("Blue", 2, color));
            menu.add(new MenuItem("Back", new MenuAction() {
                @Override
                public void run(MenuItem item, int code) {
                    finished(color);
                    menu.selectBack();
                }
            }));
            menu.update();
        }

        public abstract void finished(float[] color);


        public void render(int menuWidth, int entryHeight, int idx, boolean focus, ShapeRenderer shapeRenderer) {
            float itemSpacing = entryHeight * 0.2f;
            float itemHeight = entryHeight - 2 * itemSpacing;
            float barWidth = menuWidth * 0.38f;
            float barStart = menuWidth * 0.6f;
            float alpha = (focus) ? 1f : (action == null) ? 1f : 0.5f;
            shapeRenderer.setColor(color[0], color[1], color[2], alpha);
            int entryY = menuStartY - idx * entryHeight;
            shapeRenderer.rect(menuStartX + barStart,
                entryY - itemHeight - itemSpacing, barWidth, itemHeight);
        }
    }


    private static class ScaleMenuItem extends MenuItem {

        private int colIdx;
        private float[] color;
        private float barStartX;
        private float barStartY;
        private float barWidth;
        private float barHeight;


        public ScaleMenuItem(String colLabel, final int colIdx, final float[] color) {
            super(colLabel, getValue(color[colIdx]), new MenuAction() {
                @Override
                public void run(MenuItem item, int code) {
                    if (code > 0) {
                        color[colIdx] = Math.min(1f, color[colIdx] + 0.05f);
                    }
                    if (code < 0) {
                        color[colIdx] = Math.max(0f, color[colIdx] - 0.05f);
                    }
                    item.updateValue(getValue(color[colIdx]), false);
                }
            });
            this.colIdx = colIdx;
            this.color = color;
        }

        private static String getValue(float val) {
            return String.valueOf((float) Math.round(100f * val) / 100f);
        }

        public void handleDrag(float x, float y) {
            float width = barWidth * 0.95f;  // bar - marker
            float pos = Math.max(x, barStartX);
            pos = Math.min(pos, barStartX + width);
            color[colIdx] = (pos - barStartX) / width;
            updateValue(getValue(color[colIdx]), false);
        }

        private float getPercentage() {
            return color[colIdx];
        }

        public void render(int menuWidth, int entryHeight, int idx, boolean focus, ShapeRenderer shapeRenderer) {
            float itemSpacing = entryHeight * 0.2f;
            int entryY = menuStartY - idx * entryHeight;
            barHeight = entryHeight - 2 * itemSpacing;
            barWidth = menuWidth * 0.58f;
            barStartX = menuStartX + menuWidth * 0.4f;
            barStartY = entryY - barHeight - itemSpacing;
            float alpha = (focus) ? 1f : (action == null) ? 1f : 0.5f;
            // draw dark scale background
            shapeRenderer.setColor(0f, 0f, 0f, alpha);
            shapeRenderer.rect(barStartX, barStartY, barWidth, barHeight);
            // draw scale marker
            float markerWidth = barWidth * 0.05f;
            float pos = barStartX + (barWidth - markerWidth) * getPercentage();
            shapeRenderer.setColor(0.7f, 0.7f, 0.7f, alpha);
            shapeRenderer.rect(pos, barStartY, markerWidth, barHeight);

        }
    }

    public static interface MenuAction {
        /**
         * Perform the menu action.
         * The code is -1 for MINUS selection (or previous item), +1 for PLUS selection
         * (or next item) or 0 for ENTER selection.
         *
         * @param item the menu item
         * @param code the selection code
         */
        public void run(MenuItem item, int code);
    }

    private class Menu {
        private final GLBitmapText[] entries;
        private final ArrayList<MenuItem> items;
        private final int selection;

        private Menu() {
            this.entries = GLMenu.this.itemViewers;
            this.items = GLMenu.this.items;
            this.selection = GLMenu.this.selection;
        }
    }
}
