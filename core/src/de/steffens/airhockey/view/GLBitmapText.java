package de.steffens.airhockey.view;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

public class GLBitmapText extends GLHudElement {

    /** Approximate height of the unscaled font. */
    public static final int FONT_HEIGHT = 70;

    private static final String ASSETS_PREFIX = System.getProperty("de.steffens.airhockey.view.assets", "");
    private static BitmapFont font;

    public String text;
    public float x_pos = 0;
    public float width = GLDisplay.ORTHO_WIDTH;
    public float y_pos = 0;
    public float scale = 1.0f;
    private float[] color;
    private int alignment = Align.left;

    private float[] borderColor = null;


    /**
     * Creates a new centered text at the given y-position.
     *
     * @param text the text to display
     * @param y the y-position
     */
    public GLBitmapText(String text, int y) {
        this(text, 0, y);
        alignment = Align.center;
    }

    public static void disposeFont() {
        font.dispose();
        font = null;
    }

    private void initFont() {
        color = new float[] {0.6f, 0.6f, 1f, 1f};
        if (font != null) {
            return;
        }
        // load font texture with enabled mipmaps
        String fontName = "FreeSans41";
        Texture texture = new Texture(
            Gdx.files.internal(ASSETS_PREFIX + "img/fonts/" + fontName + ".png"), true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);

        font= new BitmapFont(
            Gdx.files.internal(ASSETS_PREFIX + "img/fonts/" + fontName + ".fnt"),
            new TextureRegion(texture), false);

    }

    /**
     * Creates a new left-aligned text at the given xy-position.
     *
     * @param text the text to display
     * @param x the x-position
     * @param y the y-position
     */
    public GLBitmapText(String text, int x, int y) {
        initFont();
        this.text = text;
        this.x_pos = x;
        this.y_pos = y;
    }

    /**
     * Creates a new centered text at the given xy-position.
     * The text will be centered between x start and end position
     *
     * @param text the text to display
     * @param xStart the x-position start
     * @param xEnd the x-position end
     * @param y the y-position
     */
    public GLBitmapText(String text, float xStart, float xEnd, float y) {
        initFont();
        this.text = text;
        this.x_pos = xStart;
        this.width = xEnd - xStart;
        this.y_pos = y;
        alignment = Align.center;
    }

    /**
     * Returns the cap height, which is the distance from the top of most uppercase 
     * characters to the baseline. Since the drawing position is the cap height of
     * the first line, the cap height can be used to get the location of the baseline.
     * 
     * @return the cap height
     */
    public float getCapHeight() {
        return font.getCapHeight();
    }


    public void render(SpriteBatch spriteBatch) {
        if (text.isEmpty() || (color[3] < 0.01f)) {
            return;
        }
        font.setColor(color[0], color[1], color[2], color[3]);
        if (scale != font.getScaleX()) {
            font.getData().setScale(scale);
        }
        font.draw(spriteBatch, text, x_pos, y_pos, width, alignment, true);
        if (1 != font.getScaleX()) {
            font.getData().setScale(1f);
        }
    }

    /**
     * Render any shapes associated with this object.
     * This will be called before render().
     * @param shapeRenderer
     */
    public void renderShapes(ShapeRenderer shapeRenderer) {
        if (borderColor == null) {
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(borderColor[0], borderColor[1], borderColor[2], borderColor[3] * 0.2f);
        shapeRenderer.rect(x_pos, y_pos - font.getLineHeight() + font.getDescent(), width, font.getLineHeight());
        shapeRenderer.setColor(borderColor[0], borderColor[1], borderColor[2], borderColor[3]);
        shapeRenderer.rect(x_pos, y_pos - font.getLineHeight() + font.getDescent(), width, -font.getDescent() * 0.5f);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0f, 0f, 0f, 1f);
        shapeRenderer.rect(x_pos, y_pos - font.getLineHeight() + font.getDescent(), width, font.getLineHeight());
        shapeRenderer.end();
    }


    public void setColor(float red, float green, float blue) {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
    }


    public void setAlpha(float alpha) {
        color[3] = alpha;
    }


    /**
     * Set the color for the border, or <code>null</code> for no border.
     * @param color
     */
    public void setBorderColor(float[] color) {
        if (color == null) {
            this.borderColor = null;
        }
        else {
            if (borderColor == null) {
                borderColor = new float[4];
            }
            borderColor[3] = 1f;
            System.arraycopy(color, 0, borderColor, 0, color.length);
        }
    }
}
