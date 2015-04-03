/* 
 * Created on 28.09.2010
 */

package de.steffens.airhockey.model;

import com.badlogic.gdx.Gdx;

import de.steffens.airhockey.AirhockeyGame;
import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.view.GLDisplay;

/**
 * Global state and objects of the game.
 * This class holds references that are used everywhere in the game, like the 
 * simulation and the playing field.
 * 
 * @author Steffen Schreiber
 */
public class Game {

    private static PlayingField field;
    private static Simulation simulation;
    private static Player[] players;
    private static int[] score;
    private static Disk puck;
    private static GLDisplay display = null;
    private static GameConfiguration config = null;
    private static AirhockeyGame main = null;
    private static Console console = new Console();
    private static long gameTimeoutMs = -1;


    public static void setPlayingField(PlayingField field) {
        Game.field = field;
    }
    
    public static PlayingField getPlayingField() {
        return field;
    }
    
    public static void setSimulation(Simulation simulation) {
        Game.simulation = simulation;
    }
    
    public static Simulation getSimulation() {
        return simulation;
    }
    
    public static void setPlayers(Player[] players) {
        Game.players = players;
        Game.score = new int[players.length];
    }
    
    public static Player getPlayer(int playerIndex) {
        return players[playerIndex];
    }
    
    public static int getPlayerCount() {
    	return players.length;
    }

    public static int[] getScore() {
        return score;
    }

    public static boolean isGameOver() {
        if (System.currentTimeMillis() > gameTimeoutMs) {
            return true;
        }
        for (int i : score) {
            if (i >= config.getMaximumScore()) {
                return true;
            }
        }
        return false;
    }

    public static long getMaximumGameLengthMs() { return config.getMaximumGameTimeMin() * 60 * 1000; }

    public static void setPuck(Disk puck) {
        Game.puck = puck;
    }

    public static Disk getPuck() {
        return puck;
    }

    public static boolean isFullscreen() {
        return config.isFullScreen();
    }

    public static int getTargetFPS() {
        return config.getFramesPerSecond();
    }

	public static boolean isBreakout() {
	    return config.isBreakout();
    }

	public static boolean isClient() {
	    return config.getPort() > 0 && config.getServerAddress() != null;
    }

	public static boolean isServer() {
	    return config.getPort() > 0 && config.getServerAddress() == null;
    }
	
	public static void setDisplay(GLDisplay display) {
	    Game.display = display;
    }

	public static GLDisplay getDisplay() {
	    return display;
    }


    public static Console getConsole() {
        return console;
    }

    public static void setConfiguration(GameConfiguration config) {
	    Game.config = config;
    }

	public static boolean isDemo() {
		return config.isDemo();
	}

	public static boolean showCursor() {
		return config.showCursor();
	}


    public static void setMain(AirhockeyGame main) {
        Game.main = main;
    }


    public static void createGame(GameConfiguration gameConfig) {
        getDisplay().dispose();
        getSimulation().stop();

        main.createGame(gameConfig);
    }


    public static void start() {
        gameTimeoutMs = System.currentTimeMillis() + config.getMaximumGameTimeMin() * 60 * 1000;
        field.resetState(true);
        simulation.start();
    }


    public static void exit() {
        Gdx.app.exit();
    }

}
