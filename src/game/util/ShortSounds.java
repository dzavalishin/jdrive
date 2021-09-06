package game.util;



import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ShortSounds {

	private static boolean enabled = true;
	
	private static Clip blipClip;
	private static Clip teleportHumClip;
	
	private static RandomSoundClip randomMotorSound;
	private static RandomSoundClip randomFarmSound;
	
	private static  FloatControl teleportHumVolume;


	static public void setEnabled( boolean enabled )
	{
		ShortSounds.enabled = enabled;
		startStop();
	}

	public static void toggleEnabled() {
		ShortSounds.enabled = !ShortSounds.enabled;
		startStop();
	}

	private static void startStop() {
		if( ShortSounds.enabled )
			loopForever(teleportHumClip);
		else
			teleportHumClip.stop();
		
	}

	public static boolean isEnabled() { return ShortSounds.enabled; }


	
	public static Clip loadClip(URL url)
	{
		try {
			Clip clip = AudioSystem.getClip();
			// getAudioInputStream() also accepts a File or InputStream
			AudioInputStream ais = AudioSystem.getAudioInputStream(url);
			clip.open(ais);
			return clip;
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static Clip loadClip( String fn )
	{
		return loadClip(ShortSounds.class.getResource(fn));
	}
	
	static public void preload()
	{
		blipClip = loadClip( ShortSounds.class.getResource("/sounds/blip.wav"));
	
		randomFarmSound = new RandomSoundClip(3, "/sounds/farm-");
		randomMotorSound = new RandomSoundClip(2, "/sounds/motor-");
		
		//teleportHumClip.setLoopPoints(16000, 50000);
		//teleportHumClip.setLoopPoints(23782, 50000);
		//teleportHumVolume = (FloatControl) teleportHumClip.getControl(FloatControl.Type.MASTER_GAIN);//Type.MASTER_GAIN);
		//loopForever(teleportHumClip);		
		//setTeleportVolume(0);
	}

	static void playClip(Clip c)
	{
		if( !ShortSounds.enabled ) return;
		
		c.stop();
		c.setFramePosition(0);
		c.start();
	}

	private static void loopForever(Clip c)
	{
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	

	public static void playMotorSound()		{ randomMotorSound.playRandomSound(); } // playClip(explosionClip); }
	public static void playBlipSound()		{ playClip(blipClip);	}
	public static void playFarmSound()    	{ randomFarmSound.playRandomSound(); }

	
	private static void setVolume(FloatControl fc, float x) {
		if (x<0) x = 0;
		if (x>1) x = 1;
		float min = fc.getMinimum();
		float max = fc.getMaximum();
		fc.setValue((max-min)*x+min);
	}
	
	/**
	 * 
	 * @param volume 0...1
	 */
	public static void setTeleportVolume(double volume)
	{
		//if( !ShortSounds.enabled ) volume = 0;
		//teleportHumVolume.setValue((float) -40);
		setVolume(teleportHumVolume, (float) volume);
	}

	/**
	 * Called on level end, stop what is possible to stop.
	 */
	public static void stop() {
		setTeleportVolume(0);		
	}


}