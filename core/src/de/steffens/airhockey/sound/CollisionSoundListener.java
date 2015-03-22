package de.steffens.airhockey.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import de.steffens.airhockey.model.CollisionListener;
import de.steffens.airhockey.model.MovingObject;

/**
 * A collision listener that produces sound effects. 
 * 
 * @author Steffen Schreiber
 */
public class CollisionSoundListener implements CollisionListener {

    private static Sound wallSound = Gdx.audio.newSound(Gdx.files.internal("sfx/wall001.mp3"));
    private static Sound puckSound = Gdx.audio.newSound(Gdx.files.internal("sfx/puck001.mp3"));

    /**
     * @see de.steffens.airhockey.model.CollisionListener#collisionOccurred(de.steffens.airhockey.model.CollisionListener.DiskWallCollision)
     */
    @Override
    public void collisionOccurred(DiskWallCollision e) {
        // a disk-wall-collision, play the wall collision sound
        // TODO: modify sound position and volume depending on the collision event
        float strength = (float)(e.velocity.getValue() / MovingObject.MAX_VELOCITY_VALUE);
        wallSound.play(0.5f * strength);
    }

    /**
     * @see de.steffens.airhockey.model.CollisionListener#collisionOccurred(de.steffens.airhockey.model.CollisionListener.DiskDiskCollision)
     */
    @Override
    public void collisionOccurred(DiskDiskCollision e) {
        // a disk-disk-collision, play the disk collision sound
        // TODO: modify sound position and volume depending on the collision event
        float strength = (float)(e.velocity.getValue() / MovingObject.MAX_VELOCITY_VALUE);
        puckSound.play(0.5f * strength);
    }
}
