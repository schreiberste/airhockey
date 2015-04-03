/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.control.RemotePlayer;
import de.steffens.airhockey.model.Console;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.Simulation;
import de.steffens.airhockey.view.GLMenu;
import de.steffens.airhockey.view.GLMenu.MenuAction;
import de.steffens.airhockey.view.GLMenu.MenuItem;

/**
 * Server waiting for connections and registering them for updates.
 *
 * @author Johannes Scheerer
 */
public class Server {

    private static boolean playersReady = false;
    private static final Object MONITOR = new Object();
    private static GameConfiguration gameCfg;
    private static HashSet<RemotePlayer> remotePlayers = new HashSet<RemotePlayer>();


    private static String getWaitString(int numRemotePlayers, int players) {
        return "Awaiting " + (numRemotePlayers - players) + " remote Players";
    }

    public static void start(final GameConfiguration config, final Simulation simulation) {
        gameCfg = config;

        // this is called in the render-thread, but we have to wait for
        // all remote players to be connected, so start a new thread
        new Thread(new Runnable() {

            @Override
            public void run() {
                // open connection and wait for remote players
                Server.acceptConnections(config.getPort(), config.getRemotePlayers(),
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
    public static void acceptConnections(int port, final int numRemotePlayers,
            final int firstRemotePlayer) {
        if (!Game.isServer()) {
            return;
        }
        // temporarily enable the console
        Game.getConsole().setVisible(true);
        playersReady = false;
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(getWaitString(numRemotePlayers, 0));
                    MenuItem menuItem = null;
                    if (numRemotePlayers > 0) {
                        GLMenu menu = Game.getDisplay().getMenu();
                        menu.enable();
                        menu.disableBack();
                        menu.addMenu();
                        menu.add(new MenuItem(""));
                        menu.add(new MenuItem("Server running..."));
                        menuItem = new MenuItem(getWaitString(numRemotePlayers, 0));
                        menu.add(menuItem);
                        menu.add(new MenuItem(""));
                        menu.add(new MenuItem("Exit", null, new MenuAction() {
                            @Override
                            public void run(MenuItem item, int code) {
                                Game.exit();
                            }
                        }, true));
                        menu.update();
                    }
                    acceptLoop(numRemotePlayers, firstRemotePlayer, serverSocket, menuItem);
                }

            }, "ServerThread");
            t.setDaemon(true);
            t.start();
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
                DataOutputStream out = remotePlayer.getConnection();
                for(short playerIndex=0; playerIndex < Game.getPlayerCount(); playerIndex++) {
                    out.writeInt(MSG.NEW_PLAYER_DATA);
                    out.writeInt(playerIndex);
                    Game.getPlayer(playerIndex).writeData(out);
                }
                out.writeInt(MSG.START_GAME);
            }

        } catch (IOException e) {
            System.err.println("Creating server socket failed.");
            e.printStackTrace();
        }
    }


    /**
     * Start the server accept loop.
     * 
     * @param numRemotePlayers the number of remote players to wait for
     * @param firstRemotePlayer the first remote player index
     * @param serverSocket the server socket
     * @param menuItem the menu item used to update server messages
     */
    private static void acceptLoop(final int numRemotePlayers, final int firstRemotePlayer,
            final ServerSocket serverSocket, MenuItem menuItem) {

        int players = 0;
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                final DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                if (players < numRemotePlayers) {
                    System.out.println("Server: Remote Player connected.");
                    final int playerIndex = players + firstRemotePlayer;
                    final DataInputStream is = new DataInputStream(socket.getInputStream());
                    // handshake: send the player index
                    System.out.println("Server: Sending player index " + playerIndex);
                    os.writeInt(playerIndex);
                    // receive the player name
                    System.out.println("Server: waiting for player data...");
                    final RemotePlayer player = (RemotePlayer) Game.getPlayer(playerIndex);
                    player.readData(is);
                    Game.getConsole().addLine("Player " + player.getName() + " connected.", true);

                    // broadcast new player to the other players
                    for (RemotePlayer remotePlayer : remotePlayers) {
                        DataOutputStream out = remotePlayer.getConnection();
                        out.writeInt(MSG.NEW_PLAYER_DATA);
                        out.writeInt(playerIndex);
                        player.writeData(out);
                    }
                    // register the new player
                    player.setConnection(os);
                    remotePlayers.add(player);

                    // start thread to handle client input
                    // this is just for position updates for now
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    player.setMouse(is.readDouble(), is.readDouble());
                                }
                            } catch (IOException e) {
                                System.err.println("Error while reading from client " + playerIndex + 
                                        " (Player " +player.getName() + ").");
                                e.printStackTrace();
                                // disconnect the player
                                remotePlayers.remove(player);
                            }
                        }
                    }, "ClientListener " + playerIndex);
                    t.start();

                    players++;
                    menuItem.updateLabel(getWaitString(numRemotePlayers, players));
                } else {
                    System.out.println("Remote Viewer connected.");
                    Game.getConsole().addLine("Remote Viewer connected.", true);
                    // No player, just a viewer.
                    os.writeInt(-1);
                }

                // Write initial data.
                gameCfg.write(os);
                Game.getPlayingField().write(os);
                Game.getPuck().write(os);
                os.writeInt(Game.getPlayerCount());
                for (int i = 0; i < Game.getPlayerCount(); i++) {
                    Game.getPlayer(i).getControlledDisk().write(os);
                }
                os.flush();

                // Register listeners that forward events to the client
                ServerSimulationForward simListener = new ServerSimulationForward(os);
                Game.getSimulation().addSimulationListener(simListener);
                Game.getSimulation().addCollisionListener(simListener);
                Game.getConsole().addConsoleListener(new Console.ConsoleListener() {
                    private boolean error = false;

                    @Override
                    public void clear() {
                        if (error) {
                            return;
                        }
                        try {
                            os.writeInt(MSG.CONSOLE_CLEAR);
                            os.flush();
                        } catch (IOException e) {
                            System.err.println("Error sending console clear.");
                            e.printStackTrace();
                            Game.getConsole().removeConsoleListener(this);
                            error = true;
                        }
                    }

                    @Override
                    public void addLine(String line, boolean sticky) {
                        if (error) {
                            return;
                        }
                        try {
                            os.writeInt(MSG.CONSOLE_LINE);
                            os.writeUTF(line);
                            os.writeBoolean(sticky);
                            os.flush();
                        } catch (IOException e) {
                            System.err.println("Error sending console line.");
                            e.printStackTrace();
                            Game.getConsole().removeConsoleListener(this);
                            error = true;
                        }
                    }
                });

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
            } catch (IOException e) {
                System.err.println("Connection error.");
                e.printStackTrace();
            }
        }
    }

}
