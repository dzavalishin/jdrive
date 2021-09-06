package game.util;

import javax.sound.sampled.Clip;

public class RandomSoundClip {

	
	private Clip[] clips;

	public RandomSoundClip(int nSounds, String fileNameStem) 
	{
		loadSounds( nSounds, fileNameStem );
	}
	
	private void loadSounds( int nSounds, String fileNameStem )
	{
		clips = new Clip[nSounds];
		
		
		for( int i = 0; i < nSounds; i++ )
		{
			String filename = fileNameStem + Integer.toString( i+1 ) + ".wav";
					
			clips[i] = 	ShortSounds.loadClip( ShortSounds.class.getResource(filename));	;			
		}
		
	}
	
	public void playRandomSound()
	{
		int ni = (int) (Math.random() * clips.length);
		
		ni %= clips.length; // Shouldn't happen, but... :)
		
		ShortSounds.playClip( clips[ni] );
	}
	
	
}