/**
 * Created on 20.02.15.
 */
package de.steffens.airhockey.net;

/**
 * The list of defined message types.
 */
public interface MSG {

    int PLAYING_FIELD = 0;

    int SIMULATION_UPDATE = 1;

    int COLLISION_DISK_WALL = 2;

    int COLLISION_DISK_DISK = 3;

    int CONSOLE_CLEAR = 4;

    int CONSOLE_LINE = 5;

    int NEW_PLAYER_DATA = 6;
}
