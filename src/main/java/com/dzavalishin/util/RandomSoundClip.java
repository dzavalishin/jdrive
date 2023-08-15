package com.dzavalishin.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.Clip;

import com.dzavalishin.game.Global;

public class RandomSoundClip implements ISoundClip 
{

	
	private List<Clip> clips = new ArrayList<>();

	public RandomSoundClip(String folder, String fileNameStem) 
	{
		loadSounds( folder, fileNameStem );
	}
	
	private void loadSounds( String folder, String fileNameStem )
	{

		Path dir = Path.of(folder);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, fileNameStem+"*.wav")) 
		{
			for (Path entry: stream) {
				//System.out.println("rnd sound "+entry.getFileName() );
				
				File file= entry.toFile();
				//filename = String.format( "%s/%s", _fios_path, dirent.d_name);
				final String name = file.getName();

				//System.out.println("rnd sound "+name );

				if(file.isDirectory()) continue;

				Clip c; 
				c = ShortSounds.loadClip( file.getAbsolutePath() );
				// TODO from JAR
				//c = ShortSounds.loadClip( ShortSounds.class.getResource(file.getAbsolutePath()));	
				
				clips.add(c);

			}
		} catch (IOException x) {
			Global.error(x);
		}
		
		
	}
	

	private Clip getRandomSound() {
		int ni = (int) (Math.random() * clips.size());
		
		ni %= clips.size(); // Shouldn't happen, but... :)
		
		final Clip c = clips.get(ni);
		return c;
	}

	@Override
	public void play()
	{
		final Clip c = getRandomSound();
		ShortSounds.playClip( c );
	}

	@Override
	public void play(int vol, int pan) 
	{
		final Clip c = getRandomSound();
		ShortSounds.playClip( c, vol, pan);
	}
	
	
}