package de.steffens.airhockey.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglGraphics;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import de.steffens.airhockey.AirhockeyGame;
import de.steffens.airhockey.GameConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        Preferences prefs = new LwjglPreferences(GameConfiguration.PREFS_NAME, config.preferencesDirectory);
        GameConfiguration gameConfig = new GameConfiguration(prefs);


        config.samples = 8;
        config.resizable = true;
        config.width = gameConfig.getWidth();
        config.height = gameConfig.getHeight();
        config.fullscreen = gameConfig.isFullScreen();

        new LwjglApplication(new AirhockeyGame(), config);
	}
}
