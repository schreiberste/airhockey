/*
 * Created on 12.02.2011
 */
package de.steffens.airhockey.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.model.CollisionListener;
import de.steffens.airhockey.model.Game;

/**
 * Client waiting for server updates and updating local data structures.
 * 
 * @author Johannes Scheerer
 */
public class Client {

    private static Socket socket;
    private static DataInputStream is;
    private static DataOutputStream os;
    private static int playerIndex = -1;

    /**
     * Connects to the address and port given in the game config.
     * 
     * @throws IOException if an error occurs during connecting
     */
    public static void connect(GameConfiguration config) throws IOException {
        socket = new Socket(config.getServerAddress(), config.getPort());
        socket.setTcpNoDelay(true);
        System.out.println("Client: connect...");
        is = new DataInputStream(socket.getInputStream());
        playerIndex = is.readInt();
        System.out.println("Client: player " + playerIndex);
        if (playerIndex == -1) {
            // only viewer...
        }
        else {
            System.out.println("Client: sending data " + config.getPlayerName());
            os = new DataOutputStream(socket.getOutputStream());
            Player.writeData(os, config);
        }
    }

    public static void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        int msg = getIs().readInt();
                        switch (msg) {
                            case MSG.SIMULATION_UPDATE:
                                readSimulationUpdate();
                                break;
                            case MSG.COLLISION_DISK_DISK:
                                Game.getSimulation().notifyCollisionListeners(
                                    CollisionListener.DiskDiskCollision.read(getIs()));
                                break;
                            case MSG.COLLISION_DISK_WALL:
                                Game.getSimulation().notifyCollisionListeners(
                                    CollisionListener.DiskWallCollision.read(getIs()));
                                break;
                            case MSG.CONSOLE_CLEAR:
                                Game.getConsole().clear();
                                break;
                            case MSG.CONSOLE_LINE:
                                String consoleMsg = getIs().readUTF();
                                boolean sticky = getIs().readBoolean();
                                Game.getConsole().addLine(consoleMsg, sticky);
                                System.out.println("Client: console line=" + consoleMsg + " ["+sticky+"]");
                                break;
                            case MSG.NEW_PLAYER_DATA:
                                readNewPlayerData();
                                break;

                            default:
                                getIs().close();
                                throw new IOException("Illegal server message type " + msg);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving update: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, "ClientThread");
        t.setDaemon(true);
        t.start();
    }

    private static void readSimulationUpdate() throws IOException {
        Game.getSimulation().readSimulationUpdate(getIs());
        int players = getIs().readInt();
        if (players != Game.getPlayerCount()) {
            System.err.println("Client: got wrong number of players: " + players);
            assert false;
        }
        int[] score = Game.getScore();
        for (int i = 0; i < players; i++) {
            score[i] = getIs().readInt();
        }
    }


    private static void readNewPlayerData() throws IOException {
        int newPlayerIndex = getIs().readInt();
        Game.getPlayer(newPlayerIndex).readData(getIs());
    }


    public static DataInputStream getIs() {
        return is;
    }

    public static DataOutputStream getOs() {
        return os;
    }

    public static int getPlayer() {
        return playerIndex;
    }
}
