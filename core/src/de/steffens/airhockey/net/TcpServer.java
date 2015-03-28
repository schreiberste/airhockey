package de.steffens.airhockey.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.control.RemotePlayer;
import de.steffens.airhockey.model.Console;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.PlayingField;
import de.steffens.airhockey.view.GLMenu;

/**
 * A server using tcp/ip for communication with its clients.
 *
 * @author Johannes Scheerer
 */
public class TcpServer extends AbstractServer {
    @Override
    protected void acceptPlayerConnections(int port, final int numRemotePlayers,
                                           final int firstRemotePlayer) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(getWaitString(numRemotePlayers, 0));
                GLMenu.MenuItem menuItem = null;
                if (numRemotePlayers > 0) {
                    menuItem = getWaitMenuItem(numRemotePlayers);
                }
                acceptLoop(numRemotePlayers, firstRemotePlayer, serverSocket, menuItem);
            }

        }, "ServerThread");
        t.setDaemon(true);
        t.start();
    }

    @Override
    protected void sendNewPlayerData(RemotePlayer remotePlayer, int index, Player player) throws IOException {
        DataOutputStream out = remotePlayer.getConnection();
        out.writeInt(MSG.NEW_PLAYER_DATA);
        out.writeInt(index);
        player.writeData(out);
    }

    @Override
    protected void sendStartGame(RemotePlayer remotePlayer) throws IOException {
        DataOutputStream out = remotePlayer.getConnection();
        out.writeInt(MSG.START_GAME);
    }

    @Override
    protected void sendInitialGameData(RemotePlayer remotePlayer, GameConfiguration gameCfg, PlayingField field, Disk puck, int playerCount, Disk[] playerDisks) throws IOException {
        DataOutputStream os = remotePlayer.getConnection();
        gameCfg.write(os);
        field.write(os);
        puck.write(os);
        os.writeInt(playerCount);
        for (int i = 0; i < playerDisks.length; i++) {
            playerDisks[i].write(os);
        }
        os.flush();
    }

    /**
     * Start the server accept loop.
     *
     * @param numRemotePlayers the number of remote players to wait for
     * @param firstRemotePlayer the first remote player index
     * @param serverSocket the server socket
     * @param menuItem the menu item used to update server messages
     */
    private void acceptLoop(final int numRemotePlayers, final int firstRemotePlayer,
                            final ServerSocket serverSocket, GLMenu.MenuItem menuItem) {

        int players = 0;
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                final DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                final RemotePlayer player;
                if (players < numRemotePlayers) {
                    System.out.println("Server: Remote Player connected.");
                    final int playerIndex = players + firstRemotePlayer;
                    final DataInputStream is = new DataInputStream(socket.getInputStream());
                    // handshake: send the player index
                    System.out.println("Server: Sending player index " + playerIndex);
                    os.writeInt(playerIndex);
                    // receive the player name
                    System.out.println("Server: waiting for player data...");
                    player = (RemotePlayer) Game.getPlayer(playerIndex);
                    player.readData(is);
                    Game.getConsole().addLine("Player " + player.getName() + " connected.", true);

                    // broadcast new player to the other players
                    sendPlayerDataToOtherPlayers(playerIndex, player);
                    // register the new player
                    player.setConnection(os);
                    addRemotePlayer(player);

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
                                removeRemotePlayer(player);
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
                    player = new RemotePlayer(-1, null, null);
                    player.setConnection(os);
                }

                // Write initial data.
                remotePlayerConnected(player);

                // Register listeners that forward events to the client
                TcpServerSimulationForward simListener = new TcpServerSimulationForward(os);
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

                checkForGameStart(players, numRemotePlayers);
            } catch (IOException e) {
                System.err.println("Connection error.");
                e.printStackTrace();
            }
        }
    }
}
