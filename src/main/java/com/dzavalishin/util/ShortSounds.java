package com.dzavalishin.util;



import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import com.dzavalishin.game.Global;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.struct.Point;
import com.dzavalishin.xui.MusicGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

/**
 * 
 * TODO Only new sounds volume is controlled, old ones won't change.
 * TODO Make list of all started sounds, control volume and stop them on request
 * TODO Keep own volume/pan modifiers and reapply on volume change
 * TODO Track viewport scroll and control pan accordingly
 * 
 * @author dz
 *
 */


public class ShortSounds 
{

	private static boolean enabled = true;

	private static SingleSoundClip blipClip;
	private static Clip teleportHumClip;

	private static RandomSoundClip randomMotorSound;
	private static RandomSoundClip randomFarmSound;
	private static RandomSoundClip randomSawMillSound;

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
		} catch (Throwable e) {
			Global.error(e);;
		}

		return null;
	}


	public static Clip loadClip( String fn )
	{
		//return loadClip(ShortSounds.class.getResource(fn));
		try {
			return loadClip(new URL("file", null, fn));
		} catch (MalformedURLException e) {
			Global.error(e);
			return null;
		}
	}

	static public void preload()
	{
		//blipClip = loadClip( ShortSounds.class.getResource("resources/sounds/blip.wav"));
		blipClip = new SingleSoundClip( "resources/sounds/blip.wav" );

		randomFarmSound = new RandomSoundClip("resources/sounds/", "farm-");
		randomMotorSound = new RandomSoundClip("resources/sounds/", "motor-");
		randomSawMillSound = new RandomSoundClip("resources/sounds/", "sawmill-");

		//teleportHumClip.setLoopPoints(16000, 50000);
		//teleportHumClip.setLoopPoints(23782, 50000);
		//teleportHumVolume = (FloatControl) teleportHumClip.getControl(FloatControl.Type.MASTER_GAIN);//Type.MASTER_GAIN);
		//loopForever(teleportHumClip);		
		//setTeleportVolume(0);
	}

	static void playClip(Clip c)
	{
		if( !ShortSounds.enabled || c == null ) return;

		//FloatControl volc = (FloatControl) c.getControl(Type.VOLUME);
		//volc.setValue(MusicGui.getEffectVolume()/128.0f); // TODO bring all vol to 0..1.0 rande and pan to -1..0..1
		
		ISoundClip.setVolume(c, MusicGui.getEffectVolume()/128.0f);
		ISoundClip.setPan(c, 0);
		
		c.stop();
		c.setFramePosition(0);
		c.start();
	}

	public static void playClip(Clip c, int vol, int pan) 
	{
		if( !ShortSounds.enabled || c == null ) return;

		c.stop();

		ISoundClip.setPan(c, pan * 1.0f / ISoundClip.PANNING_LEVELS);
		ISoundClip.setVolume(c, vol/128.0f);
		
		c.setFramePosition(0);
		c.start();
	}
	
	private static void loopForever(Clip c)
	{
		c.loop(Clip.LOOP_CONTINUOUSLY);
	}


	public static void playBlipSound()		{ blipClip.play();;	}

	public static void playMotorSound()		{ randomMotorSound.play(); } // playClip(explosionClip); }
	public static void playFarmSound()    	{ randomFarmSound.play(); }
	public static void playSawMillSound()	{ randomSawMillSound.play(); }

	/**
	 * 
	 * @param volume 0...1
	 */
	public static void setTeleportVolume(double volume)
	{
		//if( !ShortSounds.enabled ) volume = 0;
		//teleportHumVolume.setValue((float) -40);
		ISoundClip.setVolume(teleportHumVolume, (float) volume); // redo with other setvolume method
	}

	/**
	 * Called on level end, stop what is possible to stop.
	 */
	public static void stop() { // TODO call me
		setTeleportVolume(0);		
	}

	public static void SndPlayTileFx(ISoundClip sound, TileIndex tile)
	{
		/* emits sound from center (+ 8) of the tile */
		int x = tile.TileX() * 16 + 8;
		int y = tile.TileY() * 16 + 8;
		Point pt = Point.RemapCoords(x, y, Landscape.GetSlopeZ(x, y));
		SndPlayScreenCoordFx(sound, pt);
	}

	
	private static final double _vol_factor_by_zoom[] = {1.0, 190.0/255, 134.0/255};
	
	public static void SndPlayScreenCoordFx(ISoundClip sound, Point p)
	{
		int vol = MusicGui.getEffectVolume();
		if (vol == 0) return;
		

		Iterator<Window> ii = Window.getIterator();
		while( ii.hasNext() )
		{
			Window w = ii.next();
			final ViewPort vp = w.getViewport();

			if (vp != null && p.isInside(vp) )
			{
				int left = (p.x - vp.getVirtual_left());
				int pan = left / (vp.getVirtual_width() / ((ISoundClip.PANNING_LEVELS*2) + 1)) - ISoundClip.PANNING_LEVELS;

				sound.play((int) (vol * _vol_factor_by_zoom[vp.getZoom()]), pan);
				
				return;
			}
		}

	}



}