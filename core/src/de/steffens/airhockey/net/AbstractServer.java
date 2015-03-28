package de.steffens.airhockey.net;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.control.RemotePlayer;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.PlayingField;
import de.steffens.airhockey.model.Simulation;
import de.steffens.airhockey.view.GLMenu;

/**
 * Server waiting for connections and registering them for updates without specifying the actual
 * protocol to use. Sub classes can implement their means of transport.
 *
 * @author Johannes Scheerer
 */
public abstract class AbstractServer {

    public static final String TCP = "TCP/IP";
    public static final String UDP = "UDP/IP";

    private boolean playersReady = false;
    private final Object MONITOR = new Object();
    private GameConfiguration gameCfg;
    private Set<RemotePlayer> remotePlayers = Collections.synchronizedSet(new HashSet<RemotePlayer>());

    public static AbstractServer createServer(String type) {
        if (TCP.equals(type)) {
            return new TcpServer();
        }
        throw new UnsupportedOperationException("Server type not supported: " + type);
    }

    public void start(final GameConfiguration config, final Simulation simulation) {
        gameCfg = config;

        // this is called in the render-thread, but we have to wait for
        // all remote players to be connected, so start a new thread
        new Thread(new Runnable() {

            @Override
            public void run() {
                // open connection and wait for remote players
                acceptConnections(config.getPort(), config.getRemotePlayers(),
                        config.isDedicatedServer() ? 0 : 1);

                // all players connected, start simulation
                System.out.println("[" + Thread.currentThread().getName()+"]: players connected, starting simulation...");
                Game.start();
            }
        }, "Server Startup Thread").start();

    }

    /**
     * Accept listeners and send them updates.
     *
     * @param port the network port to listen to
     * @param numRemotePlayers the number of remote players to wait for
     * @param firstRemotePlayer the player index of the first remote player
     */
    public void acceptConnections(int port, final int numRemotePlayers,
                                         final int firstRemotePlayer) {
        if (!Game.isServer()) {
            return;
        }
        // temporarily enable the console
        Game.getConsole().setVisible(true);
        playersReady = false;
        try {
            acceptPlayerConnections(port, numRemotePlayers, firstRemotePlayer);
            if (numRemotePlayers > 0) {
                synchronized (MONITOR) {
                    while (!playersReady) {
                        try {
                            MONITOR.wait();
                        } catch (InterruptedException e) {
                            // Nothing to do here.
                        }
                    }
                }
            }

            // broadcast player data  to all clients and send the "Start" signal
            System.out.println("[" + Thread.currentThread().getName()+"]: broadcast player data");
            for (RemotePlayer remotePlayer : remotePlayers) {
                for(short playerIndex=0; playerIndex < Game.getPlayerCount(); playerIndex++) {
                    sendNewPlayerData(remotePlayer, playerIndex, Game.getPlayer(playerIndex));
                }
                sendStartGame(remotePlayer);
            }

        } catch (IOException e) {
            System.err.println("Creating server failed.");
            e.printStackTrace();
        }
    }

    protected abstract void acceptPlayerConnections(int port, int numRemotePlayers,
                                                    int firstRemotePlayer) throws IOException;
    protected abstract void sendNewPlayerData(RemotePlayer remotePlayer, int index, Player player) throws IOException;
    protected abstract void sendStartGame(RemotePlayer remotePlayer) throws IOException;
    protected abstract void sendInitialGameData(RemotePlayer remotePlayer, GameConfiguration gameCfg,
                                                PlayingField field, Disk puck, int playerCount,
                                                Disk[] playerDisks) throws IOException;

    protected GLMenu.MenuItem getWaitMenuItem(int numRemotePlayers) {
        GLMenu menu = Game.getDisplay().getMenu();
        menu.enable();
        menu.disableBack();
        menu.addMenu();
        menu.add(new GLMenu.MenuItem(""));
        menu.add(new GLMenu.MenuItem("Server running..."));
        GLMenu.MenuItem menuItem = new GLMenu.MenuItem(getWaitString(numRemotePlayers, 0));
        menu.add(menuItem);
        menu.add(new GLMenu.MenuItem(""));
        menu.add(new GLMenu.MenuItem("Exit", null, new GLMenu.MenuAction() {
            @Override
            public void run(GLMenu.MenuItem item, int code) {
                Game.exit();
            }
        }, true));
        menu.update();
        return menuItem;
    }

    protected String getWaitString(int numRemotePlayers, int players) {
        return "Awaiting " + (numRemotePlayers - players) + " remote Players";
    }

    protected void sendPlayerDataToOtherPlayers(int playerIndex, Player player) throws IOException {
        for (RemotePlayer remotePlayer : remotePlayers) {
            sendNewPlayerData(remotePlayer, playerIndex, player);
        }
    }

    protected void addRemotePlayer(RemotePlayer player) {
        remotePlayers.add(player);
    }

    protected void removeRemotePlayer(RemotePlayer player) {
        remotePlayers.remove(player);
    }

    protected void remotePlayerConnected(RemotePlayer player) throws IOException {
        Disk[] playerDisks = new Disk[Game.getPlayerCount()];
        for (int i = 0; i < Game.getPlayerCount(); i++) {
            playerDisks[i] = Game.getPlayer(i).getControlledDisk();
        }
        sendInitialGameData(player, gameCfg, Game.getPlayingField(), Game.getPuck(),
                Game.getPlayerCount(), playerDisks);
    }

    protected void checkForGameStart(int players, int numRemotePlayers) {
        if (players == numRemotePlayers && !playersReady) {
            GLMenu menu = Game.getDisplay().getMenu();
            menu.enableBack();
            menu.selectBack();
            menu.disable();
            System.out.println("[" + Thread.currentThread().getName()+"]: ready");
            Game.getConsole().addLine("Ready!", false);
            Game.getConsole().setVisible(gameCfg.showConsole());
            // signal the server startup thread that all players connected
            // and simulation my run now
            synchronized (MONITOR) {
                playersReady = true;
                MONITOR.notifyAll();
            }
        }
    }
}
