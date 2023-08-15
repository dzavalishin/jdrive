package com.dzavalishin.util;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

import com.dzavalishin.game.Global;

public class Music 
{
	private static Sequencer sequencer = null;
	private static int volume;

	public static void set_volume(int new_vol) {
		Music.volume = new_vol;

		if(sequencer == null)
			return;

		/* Does not work
		if (sequencer instanceof Synthesizer) {
			Synthesizer synthesizer = (Synthesizer) sequencer;
			MidiChannel[] channels = synthesizer.getChannels();

			// gain is a value between 0 and 1 (loudest)
			double gain = volume / 127D;
			for (int i = 0; i < channels.length; i++) {
				channels[i].controlChange(7, (int) (gain * 127.0));
			}
		} */
	}





	public static void stop_song() 
	{
		if(sequencer == null)
			return;

		// Close the MidiDevice & free resources
		sequencer.stop();
		sequencer.close();
	}

	public static boolean is_song_playing() {
		return sequencer.isRunning();
	}

	public static void play_song(String filename) 
	{
		//Global.debug("playing music %s", filename);
		File midiFile = new File(filename);

		if(!midiFile.exists() || midiFile.isDirectory() || !midiFile.canRead()) {
			Global.error("failed to play music %s", filename);
			return;
		}

		// Play once
		try {
			if(sequencer == null) sequencer = MidiSystem.getSequencer();

			if(sequencer == null)
			{
				Global.error("failed to get MIDI seqencer");
				return;
			}

			if(sequencer.isRunning()) {
				// Close the MidiDevice & free resources
				sequencer.stop();
				sequencer.close();
			}			

			sequencer.setSequence(MidiSystem.getSequence(midiFile));
			sequencer.open();
			sequencer.start();
		} catch(MidiUnavailableException mue) {
			Global.error("Midi device unavailable");
		} catch(InvalidMidiDataException imde) {
			Global.error("Invalid Midi data");
		} catch(IOException ioe) {
			Global.error("I/O Error");
		} 

	}  


}
