/*
 * Created on 19.02.2011
 */
package de.steffens.airhockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helper class for game configuration settings.
 * 
 * @author Johannes Scheerer
 */
public class GameConfiguration {

    public static final String PREFS_NAME = "airhockey";

    private static GameConfiguration config = null;

    // local configuration
    private String serverAddress;
    private int port = 50000;
    private int remotePlayers = -1;
    private String playerName = "Player";
    private float[] playerColor = {0.6f, 0.6f, 0.8f};
    private boolean humanPlayer = true;
    private boolean fullScreen = false;
    private int width = 1024;
    private int height = 576;
    private int fps = 60;
    private boolean showCursor = false;

    // server side configuration
    private int numPlayers = 5;
    private boolean breakout = false;
    private boolean showConsole = true;
    private int maximumScore = 10;
    private int maximumGameTimeMin = 10;

    /**
     * Create a new game configuration initialized with default values
     * from the default preferences store.
     */
    public GameConfiguration() {
        readPreferences(Gdx.app.getPreferences(PREFS_NAME));
    }

    /**
     * Create a new game configuration initialized with default values
     * from the given preferences store.
     */
    public GameConfiguration(Preferences prefs) {
        readPreferences(prefs);
    }


    public void readPreferences(Preferences prefs) {
        serverAddress = prefs.getString("serverAddress", serverAddress);
        port = prefs.getInteger("port", port);
        remotePlayers = prefs.getInteger("remotePlayers", remotePlayers);
        playerName = prefs.getString("playerName", playerName);
        playerColor = new float[] {
            prefs.getFloat("playerColorR", 0.6f),
            prefs.getFloat("playerColorG", 0.6f),
            prefs.getFloat("playerColorB", 0.8f),
        };
        humanPlayer = prefs.getBoolean("humanPlayer", humanPlayer);
        fullScreen = prefs.getBoolean("fullScreen", fullScreen);
        width = prefs.getInteger("width", width);
        height = prefs.getInteger("height", height);
        fps = prefs.getInteger("fps", fps);
        showCursor = prefs.getBoolean("showCursor", showCursor);
        numPlayers = prefs.getInteger("numPlayers", numPlayers);
        breakout = prefs.getBoolean("breakout", breakout);
        showConsole = prefs.getBoolean("showConsole", showConsole);
        maximumScore = prefs.getInteger("maximumScore", maximumScore);
        maximumGameTimeMin = prefs.getInteger("maximumGameTime", maximumGameTimeMin);
    }

    public void write(DataOutputStream os) throws IOException {
        os.writeInt(numPlayers);
        os.writeBoolean(breakout);
        os.writeBoolean(showConsole);
    }

    public void read(DataInputStream is) throws IOException {
        numPlayers = is.readInt();
        breakout = is.readBoolean();
        showConsole = is.readBoolean();
    }

    public static GameConfiguration getConfig() {
        if (config == null) {
            config = new GameConfiguration();
        }
        return config;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getPort() {
        return port;
    }

    public int getNumberOfPlayers() {
        return numPlayers;
    }

    public String getPlayerName() {
        return playerName;
    }


    public float[] getPlayerColor() {
        return playerColor;
    }


    public int getRemotePlayers() {
        return remotePlayers;
    }

    public boolean isHumanPlayer() {
        return humanPlayer;
    }

    public boolean isBreakout() {
        return breakout;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }


    public int getHeight() {
        return height;
    }


    public int getWidth() {
        return width;
    }

    public int getMaximumScore() { return maximumScore; }

    public int getMaximumGameTimeMin() { return maximumGameTimeMin; }

    public int getFramesPerSecond() {
        return fps;
    }

    public boolean showCursor() {
        return showCursor;
    }

    public boolean showConsole() {
        return showConsole;
    }

    public static void setServerAddress(String serverAddress) {
        getConfig().serverAddress = serverAddress;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString("serverAddress", serverAddress);
        prefs.flush();
    }

    public static void setPort(int port) {
        getConfig().port = port;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("port", port);
        prefs.flush();
    }

    public static void setNumberOfPlayers(int numPlayers) {
        getConfig().numPlayers = numPlayers;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("numPlayers", numPlayers);
        prefs.flush();
    }

    public static void setRemotePlayers(int remotePlayers) {
        getConfig().remotePlayers = remotePlayers;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("remotePlayers", remotePlayers);
        prefs.flush();
    }

    public static void setPlayerName(String newName) {
        getConfig().playerName = newName;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString("playerName", newName);
        prefs.flush();
    }

    public static void setPlayerColor(float[] color) {
        getConfig().playerColor = color;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putFloat("playerColorR", color[0]);
        prefs.putFloat("playerColorG", color[1]);
        prefs.putFloat("playerColorB", color[2]);
        prefs.flush();
    }

    public static void setHumanPlayer(boolean humanPlayer) {
        getConfig().humanPlayer = humanPlayer;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putBoolean("humanPlayer", humanPlayer);
        prefs.flush();
    }

    public static void setBreakout(boolean breakout) {
        getConfig().breakout = breakout;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putBoolean("breakout", breakout);
        prefs.flush();
    }

    public static void setFullScreen(boolean fullScreen) {
        getConfig().fullScreen = fullScreen;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putBoolean("fullScreen", fullScreen);
        prefs.flush();
    }

    public static void setResolution(int width, int height) {
        getConfig().width = width;
        getConfig().height = height;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("width", width);
        prefs.putInteger("height", height);
        prefs.flush();
    }

    public static void setFramesPerSecond(int fps) {
        getConfig().fps = fps;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("fps", fps);
        prefs.flush();
    }

    public static void setShowCursor(boolean showCursor) {
        getConfig().showCursor = showCursor;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putBoolean("showCursor", showCursor);
        prefs.flush();
    }

    public static void setMaximumScore(int score) {
        getConfig().maximumScore = score;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("maximumScore", score);
        prefs.flush();
    }

    public static void setMaximumGameTime(int gameTime) {
        getConfig().maximumGameTimeMin = gameTime;
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("maximumGameTime", gameTime);
        prefs.flush();
    }

    public static GameConfiguration create(String[] args) {
        GameConfiguration config = new GameConfiguration();
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            if ("-full".equals(argument)) {
                config.fullScreen = true;
            } else if ("-fps".equals(argument) && i + 1 < args.length) {
                i++;
                try {
                    config.fps = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse target frame rate: " + args[i]);
                }
            } else if ("-nobreakout".equals(argument)) {
                config.breakout = false;
            } else if ("-server".equals(argument) && i + 1 < args.length) {
                i++;
                try {
                    config.port = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse server port: " + args[i]);
                }
            } else if ("-connect".equals(argument) && i + 1 < args.length) {
                i++;
                String[] addressAndPort = args[i].split(":", 2);
                if (addressAndPort.length != 2) {
                    System.err.println("Illegal server address (" + args[i]
                            + "), use \"<server address>:<port>\".");
                    continue;
                }
                try {
                    config.serverAddress = addressAndPort[0];
                    config.port = Integer.parseInt(addressAndPort[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse server port: " + args[i]);
                }
            } else if ("-wait".equals(argument) && i + 1 < args.length) {
                i++;
                try {
                    config.remotePlayers = Integer.parseInt(args[i]);
                    if (config.getPort() <= 0 || config.serverAddress != null) {
                        System.err.println("Illegal option \"-wait " + args[i]
                                + "\", specify \"-server\" beforehand.");
                        config.remotePlayers = -1;
                        continue;
                    }
                    if (config.remotePlayers >= config.numPlayers) {
                        System.err.println("Illegal option \"-wait " + args[i]
                                + "\", specify appropriate number for \"-player\" beforehand.");
                        config.remotePlayers = -1;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse the number of remote players: " + args[i]);
                }
            } else if ("-player".equals(argument) && i + 1 < args.length) {
                i++;
                try {
                    config.numPlayers = Integer.parseInt(args[i]);
                } catch (NumberFormatException e) {
                    System.err.println("Unable to parse the number of players: " + args[i]);
                }
            } else if ("-nohuman".equals(argument)) {
                config.humanPlayer = false;
            } else if ("-showCursor".equals(argument)) {
                config.showCursor = true;
            }
        }
        return config;
    }

    private GameConfiguration copy() {
        GameConfiguration result = new GameConfiguration();
        result.breakout = breakout;
        result.fps = fps;
        result.fullScreen = fullScreen;
        result.humanPlayer = humanPlayer;
        result.numPlayers = numPlayers;
        result.port = port;
        result.remotePlayers = remotePlayers;
        result.serverAddress = serverAddress;
        result.showCursor = showCursor;
        result.playerName = playerName;
        result.maximumScore = maximumScore;
        result.maximumGameTimeMin = maximumGameTimeMin;
        System.arraycopy(playerColor, 0, result.playerColor, 0, 3);
        return result;
    }

    public static GameConfiguration createSinglePlayerConfig() {
        GameConfiguration result = getConfig().copy();
        result.port = -1;
        result.remotePlayers = -1;
        result.serverAddress = null;
        result.numPlayers = 2;
        result.showConsole = false;
        return result;
    }

    public static GameConfiguration createServerConfig() {
        GameConfiguration result = getConfig().copy();
        result.serverAddress = null;
        return result;
    }

    public static GameConfiguration createClientConfig() {
        GameConfiguration result = getConfig().copy();
        result.showConsole = true;
        return result;
    }

    public static GameConfiguration createDemoConfig() {
        GameConfiguration result = getConfig().copy();
        result.humanPlayer = false;
        result.numPlayers = 3;
        result.port = -1;
        result.showConsole = false;
        result.breakout = false;
        return result;
    }

}
