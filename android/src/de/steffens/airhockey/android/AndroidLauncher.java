package de.steffens.airhockey.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import de.steffens.airhockey.AirhockeyGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        // enable multisampling
        config.numSamples = 2;

        // increase color bit-depth
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;

        // disable unneeded stuff
        config.useAccelerometer = false;
        config.useCompass = false;


		initialize(new AirhockeyGame(), config);
	}
}
