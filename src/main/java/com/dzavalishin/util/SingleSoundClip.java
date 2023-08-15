package com.dzavalishin.util;

import javax.sound.sampled.Clip;

public class SingleSoundClip implements ISoundClip 
{
	private Clip me;

	public SingleSoundClip(String fn) {
		me = ShortSounds.loadClip( fn );
	}	

	@Override
	public void play()
	{
		ShortSounds.playClip( me );
	}

	@Override
	public void play(int vol, int pan) 
	{
		ShortSounds.playClip( me, vol, pan);
	}

}
