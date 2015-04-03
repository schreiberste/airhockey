package de.steffens.airhockey;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.io.IOException;
import java.util.List;

import de.steffens.airhockey.control.AIPlayer;
import de.steffens.airhockey.control.AiClientPlayer;
import de.steffens.airhockey.control.GlobalUiInputProcessor;
import de.steffens.airhockey.control.HumanClientPlayer;
import de.steffens.airhockey.control.HumanPlayer;
import de.steffens.airhockey.control.Mouse;
import de.steffens.airhockey.control.Player;
import de.steffens.airhockey.control.RemotePlayer;
import de.steffens.airhockey.model.AbstractPlayingFieldBase;
import de.steffens.airhockey.model.Disk;
import de.steffens.airhockey.model.Game;
import de.steffens.airhockey.model.PlayingField;
import de.steffens.airhockey.model.PlayingFieldNPlayers;
import de.steffens.airhockey.model.PlayingFieldTwoPlayers;
import de.steffens.airhockey.model.RemoteSimulation;
import de.steffens.airhockey.model.Simulation;
import de.steffens.airhockey.model.vector.Vector2D;
import de.steffens.airhockey.model.vector.VectorFactory;
import de.steffens.airhockey.net.Client;
import de.steffens.airhockey.net.Server;
import de.steffens.airhockey.sound.CollisionSoundListener;
import de.steffens.airhockey.view.GLDisplay;


public class AirhockeyGame extends ApplicationAdapter {

    private ModelBatch modelBatch;
    /** sprite batch to draw text **/
    private SpriteBatch spriteBatch;
    /** shape renderer **/
    private ShapeRenderer shapeRenderer;
    private final Matrix4 textViewMatrix = new Matrix4();

    public PerspectiveCamera cam;
    public Environment environment;

    GLDisplay display;

    @Override
    public void create() {
        System.out.println("#########################################");
        System.out.println("###  create() ");
        System.out.println("#########################################");

        VectorFactory.useMutableVector();
        String[] args = new String[0];
        GameConfiguration config = args.length == 0 ? GameConfiguration.createDemoConfig()
                : GameConfiguration.create(args);

        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // setup camera with field of view
        cam = new PerspectiveCamera(35f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Mouse.getInstance().setCam(cam);

        // The Z axis is pointing towards the viewer, so for the viewer
        // a positive Z value of the camera is moving the viewer back
        cam.position.set(0f, -10, 4f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // handle mouse / touch and keyboard input.
        InputMultiplexer multiplexer = new InputMultiplexer();
        // first process global events like menu etc.
        multiplexer.addProcessor(GlobalUiInputProcessor.getInstance());
        // next, handle human player input
        multiplexer.addProcessor(Mouse.getInstance());
        Gdx.input.setInputProcessor(multiplexer);
        // catch the Back key on Android
        Gdx.input.setCatchBackKey(true);

        createGame(config);
    }

    public void createGame(GameConfiguration config) {
        Game.setMain(this);
        Game.getConsole().clear();
        Game.getConsole().setVisible(config.showConsole());

        Game.setConfiguration(config);

        if (Game.isClient()) {
            System.out.println("Initializing client...");
            try {
                initClient(config);
            } catch (IOException e) {
                System.err.println("Error setting up client.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Initializing server...");
            initServer(config);
        }

    }

    @Override
    public void render() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        display.setSize(width, height);

        textViewMatrix.setToOrtho2D(0f, 0f, GLDisplay.ORTHO_WIDTH, GLDisplay.ORTHO_HEIGHT);

        Gdx.gl.glViewport(0, 0, width, height);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT
                | GL20.GL_DEPTH_BUFFER_BIT
                | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV
                        : 0));

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);

        display.display(modelBatch, environment, cam);

        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        spriteBatch.setProjectionMatrix(textViewMatrix);
        shapeRenderer.setProjectionMatrix(textViewMatrix);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        display.renderShapes(shapeRenderer);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        spriteBatch.begin();
        spriteBatch.enableBlending();
        display.display(spriteBatch);
        spriteBatch.end();

    }

    @Override
    public void dispose() {
        System.out.println("#########################################");
        System.out.println("###  dispose() ");
        System.out.println("#########################################");

        modelBatch.dispose();
        spriteBatch.dispose();
        display.dispose();
    }

    private void initClient(GameConfiguration config) throws IOException {
        Client.connect(config);

        // update the game configuration to match the server.
        config.read(Client.getIs());

        Simulation simulation = new RemoteSimulation();
        Game.setSimulation(simulation);

        PlayingField field = AbstractPlayingFieldBase.read(Client.getIs());
        Game.setPlayingField(field);

        // //////// the disk models used for the puck and the players. /////////

        // the puck disk
        Disk puckModel = Disk.read(Client.getIs());
        simulation.addDisk(puckModel);
        Game.setPuck(puckModel);


        // //////// the players and player disks   ////////////////////////////

        int playerCount = Client.getIs().readInt();
        Player[] players = new Player[playerCount];
        Disk[] disks = new Disk[playerCount];
        Game.setPlayers(players);

        for (short plIdx = 0; plIdx < playerCount; plIdx++) {
            disks[plIdx] = Disk.read(Client.getIs());
            simulation.addDisk(disks[plIdx]);
            if (plIdx == Client.getPlayer()) {
                if (config.isHumanPlayer()) {
                    players[plIdx] = new HumanClientPlayer(plIdx, disks[plIdx], puckModel);
                    players[plIdx].setName(config.getPlayerName());
                }
                else {
                    players[plIdx] = new AiClientPlayer(plIdx, disks[plIdx], puckModel);
                    players[plIdx].setName(config.getPlayerName() + " [AI]");
                }
                players[plIdx].setColor(config.getPlayerColor());
            } else {
                players[plIdx] = new Player(plIdx, disks[plIdx], puckModel) {
                    @Override
                    public void update(long newTime) {
                        // Nothing to do here.
                    }
                };
            }
        }

        // /////////// the display objects //////////////

        initDisplay(field, simulation, Client.getPlayer());


        // ///////////////// Sound //////////////////////

        // add collision listener for collision sounds
        simulation.addCollisionListener(new CollisionSoundListener());

        // ///////////////// Network //////////////////////

        Client.start();
    }

    private void initServer(GameConfiguration config) {
        Simulation simulation = new Simulation();
        Game.setSimulation(simulation);

        // //////// the playing field, use default field for 2 players //////////

        PlayingField field;
        if (config.getNumberOfPlayers() == 2) {
            field = new PlayingFieldTwoPlayers();
        } else {
            field = new PlayingFieldNPlayers(config.getNumberOfPlayers());
        }
        Game.setPlayingField(field);

        // //////// the disk model used for the puck /////////

        Disk puckModel = field.createPuckDisk();
        puckModel.setAcceleration(0.9995);
        puckModel.setMass(0.1);
        simulation.addDisk(puckModel);
        Game.setPuck(puckModel);

        // //////// the players and their disks //////////

        Player[] players = new Player[config.getNumberOfPlayers()];
        Disk[] disks = new Disk[config.getNumberOfPlayers()];

        int remotePlayerIdx = 1;
        int aiIdx = 1;
        for (short playerIdx = 0; playerIdx < config.getNumberOfPlayers(); playerIdx++) {

            // the player disk
            disks[playerIdx] = field.createPlayerDisk(playerIdx);
            disks[playerIdx].setLastHitPlayerIndex(playerIdx);
            Vector2D pos = field.getInitialPosition(playerIdx);
            disks[playerIdx].setPosition(pos.getX(), pos.getY());
            disks[playerIdx].setFixed();
            simulation.addDisk(disks[playerIdx]);

            // the player model
            if (playerIdx == 0) {
                if (config.isHumanPlayer()) {
                    players[playerIdx] = new HumanPlayer(playerIdx, disks[playerIdx], puckModel);
                    players[playerIdx].setName(config.getPlayerName());
                }
                else {
                    players[playerIdx] = new AIPlayer(playerIdx, disks[playerIdx], puckModel);
                    players[playerIdx].setName(config.getPlayerName() + " [AI]");
                }
                players[playerIdx].setColor(config.getPlayerColor());
            } else {
                if (remotePlayerIdx <= config.getRemotePlayers()) {
                    players[playerIdx] = new RemotePlayer(playerIdx, disks[playerIdx], puckModel);
                    players[playerIdx].setName("Network Player " + remotePlayerIdx++);
                } else {
                    players[playerIdx] = new AIPlayer(playerIdx, disks[playerIdx], puckModel);
                    players[playerIdx].setName("Blechtrottel " + aiIdx++);
                }
            }
            simulation.addPlayer(players[playerIdx]);
        }

        // all players created
        Game.setPlayers(players);

        // /////////// the display objects //////////////

        initDisplay(field, simulation, 0);

        // ////////// initial game state /////////////////

        // player begins
        puckModel.setPosition(field.getKickoffPosition(0));

        // ///////////////// Sound //////////////////////

        // add collision listener for collision sounds
        simulation.addCollisionListener(new CollisionSoundListener());

        // ///////// Start server and simulation ////////
        Server.start(config, simulation);
    }

    private void initDisplay(PlayingField field, final Simulation simulation, int playerIndex) {
        String addon = "";
        if (Game.isServer()) {
            addon = " (Server)";
        } else if (Game.isClient()) {
            addon = Client.getPlayer() >= 0 ? " (Client)" : " (Viewer)";
        }
        display = Game.getDisplay();
        if (display == null) {
            display = new GLDisplay("AirHockey" + addon, Game.getSimulation());
            Game.setDisplay(display);
        } else {
            display.init("AirHockey" + addon, Game.getSimulation());
        }

        // playing field renderer should be the first object for now.
        display.setPlayingField(field);
        display.followPlayer(playerIndex);

        // the puck and player disks
        List<Disk> disks = simulation.getDisks();
        for (Disk d : disks) {
            display.addObject(d);
        }
    }

}
