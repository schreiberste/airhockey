/*
 * Created on 19.02.2011
 */
package de.steffens.airhockey.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;

import de.steffens.airhockey.GameConfiguration;
import de.steffens.airhockey.control.TextInput;
import de.steffens.airhockey.control.TextInput.TextInputListener;
import de.steffens.airhockey.view.GLMenu;
import de.steffens.airhockey.view.GLMenu.MenuAction;
import de.steffens.airhockey.view.GLMenu.MenuItem;
import de.steffens.airhockey.view.GLMenu.ColorMenuItem;

/**
 * Helper class for creating the game menu.
 *
 * @author Johannes Scheerer
 */
public class GameMenu {
    private static final String ADDRESS = "Server Address";
    private static final String PORT = "Server Port";
    private static final String NUMBER_OF_PLAYERS = "Number of Players";
    private static final String NUMBER_OF_REMOTE_PLAYERS = "Number of Remote Players";

    private static boolean videoModeChanged = false;
    private static final DisplayMode[] vidModes = Gdx.graphics.getDisplayModes();
    private static int currentVidMode = -1;
    private static int nearestVidMode = -1;


    private static String getAddress() {
        String address = GameConfiguration.getConfig().getServerAddress();
        if (address != null) {
            return address;
        }
        return "not specified";
    }


    private static String getResolution() {
        return GameConfiguration.getConfig().getWidth() + " x "
            + GameConfiguration.getConfig().getHeight();
    }


    private static String getPort() {
        int port = GameConfiguration.getConfig().getPort();
        if (port > 0) {
            return String.valueOf(port);
        }
        return "none";
    }


    private static String getFirstPlayer() {
        if (GameConfiguration.getConfig().isHumanPlayer()) {
            return "Human";
        }
        return "Computer";
    }


    private static String getPlayers() {
        int players = GameConfiguration.getConfig().getNumberOfPlayers();
        return String.valueOf(players);
    }


    private static String getRemotePlayers() {
        int remotePlayers = GameConfiguration.getConfig().getRemotePlayers();
        if (remotePlayers > 0) {
            return String.valueOf(remotePlayers);
        }
        return "none";
    }


    private static String getFullscreen() {
        if (GameConfiguration.getConfig().isFullScreen()) {
            return "enabled";
        }
        return "disabled";
    }


    private static String getBreakout() {
        if (GameConfiguration.getConfig().isBreakout()) {
            return "enabled";
        }
        return "disabled";
    }


    private static String getMaximumScore() {
        return Integer.toString(GameConfiguration.getConfig().getMaximumScore());
    }


    private static void createNewGame(final GameConfiguration config) {

        // This is called in the AWT event dispatch thread.
        // Creating a server in this thread would block painting,
        // therefore start the game in a new thread.
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                new GlAirHockey(config);
//            }
//        }).start();

        Game.createGame(config);
    }


    public static void create(final GLMenu menu, final TextInput input) {
        final MenuItem back = new MenuItem("Back", new MenuAction() {
            @Override
            public void run(MenuItem item, int code) {
                menu.selectBack();
            }
        });

        final InputMenuAction portInput = new InputMenuAction(input) {
            final int port = GameConfiguration.getConfig().getPort();
            @Override
            String finished(MenuItem item, boolean cancelled) {
                if (!cancelled && input.hasInteger()) {
                    int newPort = input.getInteger();
                    if (newPort > Character.MAX_VALUE) {
                        GameConfiguration.setPort(port);
                    } else {
                        GameConfiguration.setPort(newPort);
                    }
                }
                return getPort();
            }
        };

        final MenuAction playersInput = new InputMenuAction(input) {
            final int players = GameConfiguration.getConfig().getNumberOfPlayers();
            @Override
            String finished(MenuItem item, boolean cancelled) {
                if (!cancelled && input.hasInteger()) {
                    int newPlayers = input.getInteger();
                    if (newPlayers > 16) {
                        GameConfiguration.setNumberOfPlayers(players);
                    } else {
                        GameConfiguration.setNumberOfPlayers(newPlayers);
                    }
                }
                return getPlayers();
            }
        };

        final MenuAction remotePlayersInput = new InputMenuAction(input) {
            final int remotePlayers = GameConfiguration.getConfig().getRemotePlayers();
            @Override
            String finished(MenuItem item, boolean cancelled) {
                if (!cancelled && input.hasInteger()) {
                    int newRemotePlayers = input.getInteger();
                    if (newRemotePlayers > 16) {
                        GameConfiguration.setRemotePlayers(remotePlayers);
                    } else {
                        GameConfiguration.setRemotePlayers(newRemotePlayers);
                    }
                }
                return getRemotePlayers();
            }
        };

        final MenuAction maximumScoreInput = new InputMenuAction(input) {
            final int maximumScore = GameConfiguration.getConfig().getMaximumScore();
            @Override
            String finished(MenuItem item, boolean cancelled) {
                if (!cancelled && input.hasInteger()) {
                    int newMaximumScore = input.getInteger();
                    if (newMaximumScore <= 0 || newMaximumScore >= 100) {
                        GameConfiguration.setMaximumScore(maximumScore);
                    } else {
                        GameConfiguration.setMaximumScore(newMaximumScore);
                    }
                }
                return getMaximumScore();
            }
        };

        // Add actual menu items.
        menu.add(new MenuItem("Start Single Player Game", new MenuAction() {
            @Override
            public void run(MenuItem item, int code) {
                createNewGame(GameConfiguration.createSinglePlayerConfig());
            }
        }));
        menu.add(new MenuItem("Start Multiplayer Game", new MenuAction() {
            @Override
            public void run(MenuItem item, int code) {
                menu.addMenu();
                menu.add(new MenuItem(PORT, getPort(), portInput));
                menu.add(new MenuItem(NUMBER_OF_PLAYERS, getPlayers(), playersInput));
                menu.add(new MenuItem(NUMBER_OF_REMOTE_PLAYERS, getRemotePlayers(), remotePlayersInput));
                menu.add(new MenuItem("Start Game", new MenuAction() {
                    @Override
                    public void run(MenuItem item, int code) {
                        createNewGame(GameConfiguration.createServerConfig());
                    }
                }));
                menu.add(back);
                menu.update();
            }
        }));
        menu.add(new MenuItem("Join Multiplayer Game", new MenuAction() {
            @Override
            public void run(MenuItem item, int code) {
                menu.addMenu();
                menu.add(new MenuItem(ADDRESS, getAddress(), new InputMenuAction(input) {
                    final String address = GameConfiguration.getConfig().getServerAddress();
                    @Override
                    String finished(MenuItem item, boolean cancelled) {
                        if (!cancelled) {
                            String newAddress = input.getString();
                            if (newAddress.trim().length() == 0 || newAddress.matches(".*[:/\\\\].*")) {
                                GameConfiguration.setServerAddress(address);
                            } else {
                                GameConfiguration.setServerAddress(newAddress);
                            }
                        }
                        return getAddress();
                    }
                }));
                menu.add(new MenuItem(PORT, getPort(), portInput));
                menu.add(new MenuItem("Start Game", new MenuAction() {
                    @Override
                    public void run(MenuItem item, int code) {
                        if (GameConfiguration.getConfig().getPort() > 0
                            && GameConfiguration.getConfig().getServerAddress() != null) {
                            createNewGame(GameConfiguration.createClientConfig());
                        }
                    }
                }));
                menu.add(back);
                menu.update();
            }
        }));
        menu.add(new MenuItem("Settings", new MenuAction() {
            @Override
            public void run(MenuItem item, int code) {
                menu.addMenu();
                menu.add(new MenuItem("Players", new MenuAction() {
                    @Override
                    public void run(MenuItem item, int code) {
                        menu.addMenu();
                        menu.add(new MenuItem("First Player", getFirstPlayer(), new MenuAction() {
                            @Override
                            public void run(MenuItem item, int code) {
                                boolean humanPlayer = GameConfiguration.getConfig().isHumanPlayer();
                                GameConfiguration.setHumanPlayer(!humanPlayer);
                                item.updateValue(getFirstPlayer(), false);
                            }
                        }));
                        menu.add(new MenuItem("Player Name", GameConfiguration.getConfig().getPlayerName(), new InputMenuAction(input) {
                            @Override
                            String finished(MenuItem item, boolean cancelled) {
                                if (!cancelled) {
                                    String newName = input.getString().trim();
                                    if (!newName.isEmpty()) {
                                        GameConfiguration.setPlayerName(newName);
                                    }
                                }
                                return GameConfiguration.getConfig().getPlayerName();
                            }
                        }));
                        menu.add(new ColorMenuItem("Player Color", GameConfiguration.getConfig().getPlayerColor()) {
                            @Override
                            public void finished(float[] color) {
                                GameConfiguration.getConfig().setPlayerColor(color);
                            }
                        });
                        menu.add(back);
                        menu.update();
                    }
                }));


                if (Gdx.app.getType() == Application.ApplicationType.Desktop) {

                    int currentWidth = GameConfiguration.getConfig().getWidth();
                    int currentHeight = GameConfiguration.getConfig().getHeight();
                    int nearestErr = Integer.MAX_VALUE;
                    for (int i=0; i<vidModes.length; i++) {
                        if (vidModes[i].width == currentWidth && vidModes[i].height == currentHeight) {
                            currentVidMode = i;
                            nearestVidMode = i;
                            break;
                        }
                        else {
                            int err = Math.abs(currentWidth - vidModes[i].width);
                            if (err < nearestErr) {
                                nearestVidMode = i;
                                nearestErr = err;
                            }
                        }
                    }
                    menu.add(new MenuItem("Graphics", new MenuAction() {
                        @Override
                        public void run(MenuItem item, int code) {
                            menu.addMenu();
                            final MenuItem resolutionMenu = new MenuItem("Resolution", getResolution(), new MenuAction() {
                                @Override
                                public void run(MenuItem item, int code) {
                                    if (currentVidMode == -1) {
                                        // we seem to be in a resized window. choose nearest resolution
                                        currentVidMode = nearestVidMode;
                                    }
                                    else if (code >= 0) {
                                        // choose next resolution in list
                                        currentVidMode = (currentVidMode + 1) % vidModes.length;
                                    }
                                    else {
                                        // choose previous resolution in list
                                        currentVidMode--;
                                        if (currentVidMode < 0) {
                                            currentVidMode = vidModes.length-1;
                                        }
                                    }
                                    GameConfiguration.getConfig().setResolution(
                                        vidModes[currentVidMode].width, vidModes[currentVidMode].height);
                                    item.updateValue(getResolution(), false);
                                    videoModeChanged = true;
                                }
                            });
                            menu.add(resolutionMenu);

                            menu.add(new MenuItem("Fullscreen", getFullscreen(), new MenuAction() {
                                @Override
                                public void run(MenuItem item, int code) {
                                    boolean fullScreen = !GameConfiguration.getConfig().isFullScreen();
                                    GameConfiguration.setFullScreen(fullScreen);
                                    item.updateValue(getFullscreen(), false);
                                    videoModeChanged = true;
                                    // when fullscreen is selected, make sure the resolution is
                                    // set to some available mode
                                    if (fullScreen) {
                                        if (currentVidMode == -1) {
                                            currentVidMode = nearestVidMode;
                                            GameConfiguration.getConfig().setResolution(
                                                vidModes[currentVidMode].width, vidModes[currentVidMode].height);
                                            resolutionMenu.updateValue(getResolution(), false);
                                        }
                                    }
                                }
                            }));
//                          menu.add(new MenuItem("Mouse Cursor", getCursor(), new MenuAction() {
//                              @Override
//                              public void run(MenuItem item) {
//                                  boolean showCursor = GameConfiguration.getConfig().showCursor();
//                                  GameConfiguration.setShowCursor(!showCursor);
//                                  item.updateValue(getCursor());
//                              }
//                          }));
                            menu.add(back);
                            menu.update();
                        }
                    }));

                }
                menu.add(new MenuItem("Game", new MenuAction() {
                    @Override
                    public void run(MenuItem item, int code) {
                        menu.addMenu();
                        menu.add(new MenuItem("Breakout", getBreakout(), new MenuAction() {
                            @Override
                            public void run(MenuItem item, int code) {
                                boolean breakout = GameConfiguration.getConfig().isBreakout();
                                GameConfiguration.setBreakout(!breakout);
                                item.updateValue(getBreakout(), false);
                            }
                        }));
                        menu.add(new MenuItem("Maximum Score", getMaximumScore(), maximumScoreInput));
                        menu.add(back);
                        menu.update();
                    }
                }));

                menu.add(new MenuItem("Back", new MenuAction() {
                    @Override
                    public void run(MenuItem item, int code) {
                        // check for video setting changes...
                        if (videoModeChanged) {
                            if (Gdx.graphics.supportsDisplayModeChange()) {
                                GameConfiguration config = GameConfiguration.getConfig();
                                Gdx.graphics.setDisplayMode(
                                    config.getWidth(), config.getHeight(), config.isFullScreen());
                            }
                            videoModeChanged = false;
                        }
                        menu.selectBack();
                    }
                }));

                menu.update();
            }
        }));
        menu.add(new MenuItem("Exit", new MenuAction() {
            @Override
            public void run(MenuItem item, int code) {
                Game.exit();
            }
        }));
        menu.update();
    }
    
    /**
     * Menu action that provides text input for the menu item.
     */
    private static abstract class InputMenuAction implements MenuAction {
        private final TextInput textInput;

        public InputMenuAction(final TextInput input) {
            this.textInput = input;
        }

        @Override
        public void run(final MenuItem item, int code) {

            if (code != 0) {
                // PLUS or MINUS selection.
                // Check if current value is an int...
                String currentValue = item.getValue();
                try {
                    int val = Integer.parseInt(currentValue);
                    if (code < 0) {
                        val--;
                    }
                    else {
                        val++;
                    }
                    item.updateValue(String.valueOf(val), false);
                    textInput.setValue(item.getValue());
                    String value = InputMenuAction.this.finished(item, false);
                    item.updateValue(value, false);
                    return;
                }
                catch (NumberFormatException e) {
                    // nothing to do
                }
            }

            // normal selection, use text input to change the value
            textInput.enable(item, new TextInputListener() {
                @Override
                public void update(String s) {
                    item.updateValue(s, true);
                }

                @Override
                public void finished(boolean cancelled) {
                    String value = InputMenuAction.this.finished(item, cancelled);
                    item.updateValue(value, false);
                }
            });
        }

        /**
         * Called when user input is finished on the given menu item.
         * @param item the menu item
         * @param cancelled input was cancelled
         * @return the new value to show for the menu item
         */
        abstract String finished(final MenuItem item, boolean cancelled);
    }
}
