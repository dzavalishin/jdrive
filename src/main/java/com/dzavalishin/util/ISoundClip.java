package com.dzavalishin.util;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;

public interface ISoundClip 
{
	int PANNING_LEVELS = 32;
	
	void play();
	
	/**
	 * Play with given volume and pan.
	 * 
	 * @param vol
	 * @param pan In -PANNING_LEVELS...0...PANNING_LEVELS
	 */
	void play(int vol, int pan);

	static void setVolume(FloatControl fc, float x) {
		if (x<0) x = 0;
		if (x>1) x = 1;
		float min = fc.getMinimum();
		float max = fc.getMaximum();
		fc.setValue((max-min)*x+min);
	}
	
	/**
	 * 
	 * @param c Clip to set
	 * @param vol Volume in 0...1.0 range
	 */
	static void setVolume(Clip c, float vol) {
		FloatControl fc = (FloatControl) c.getControl(Type.MASTER_GAIN);
		//setVolume(fc, 20f * (float) Math.log10(vol));
		fc.setValue(20f * (float) Math.log10(vol));
	}	

	// TODO on some systems pan is not available. Return back to own mixer?
	static void setPan(Clip c, float x) 
	{
		try 
		{
		FloatControl fc = (FloatControl) c.getControl(Type.PAN);
		fc.setValue(x);
		}
		catch(Throwable e)
		{
			// Ignore
		}
	}	

}
