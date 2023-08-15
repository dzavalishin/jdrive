package com.dzavalishin.util;

import java.util.Arrays;
import java.util.Iterator;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.dzavalishin.game.Engine;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.Rail;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.Vehicle;
import com.dzavalishin.ids.EngineID;
import com.dzavalishin.struct.Point;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.xui.MusicGui;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

public class Sound {
	//private static final int SAMPLES_PER_XFER = 32;
	private static final int SAMPLES_PER_XFER = 16;
	private static final int SOUND_SLOT = 31;
	private static final int PANNING_LEVELS = 16;
	private static int _file_count;
	private static FileEntry[] _files;
	private static Mixer _mixer;
	private static boolean stopSound = false;
	//private static int effect_vol = 127;


	static final Snd trainSfx[] = {
			Snd.SND_04_TRAIN,
			Snd.SND_0A_TRAIN_HORN,
			Snd.SND_0A_TRAIN_HORN
	};

	public static void TrainPlayLeaveStationSound(final Vehicle v)
	{

		EngineID engtype = v.getEngine_type();

		switch (Engine.GetEngine(engtype).getRailtype()) {
		case Rail.RAILTYPE_RAIL:
			v.SndPlayVehicleFx(trainSfx[Engine.RailVehInfo(engtype.id).engclass]);
			break;

		case Rail.RAILTYPE_MONO:
			v.SndPlayVehicleFx(Snd.SND_47_MAGLEV_2);
			break;

		case Rail.RAILTYPE_MAGLEV:
			v.SndPlayVehicleFx(Snd.SND_41_MAGLEV);
			break;
		}

	}


	public static void MxInitialize(int freq) {
		_mixer = new Mixer(freq);
		
		
		Thread mixerThread = new Thread() 
		{
			@Override
		    public void run() {
		        try {
		            Thread.sleep(1); // Don't kill CPU in any case
		            soundPump();		            
		        } catch(InterruptedException v) {
		        	//mixerThread.interrupt();
		        	interrupt();
		        }
		    }  
		};

		mixerThread.setDaemon(true);
		mixerThread.setName("Sfx Mixer");
		mixerThread.start();		
	}

	static class FileEntry {
		int file_offset;
		int file_size;
		int rate;
		int bits_per_sample;
		int channels;
		byte [] data;
	}


	static class MixerChannel {
		// Mixer
		Mixer mx;
		boolean active;

		// pointer to allocated buffer memory
		byte [] memory;

		// current position in memory
		int pos;
		int frac_pos;
		int frac_speed;
		int samples_left;

		// Mixing volume
		int volume_left;
		int volume_right;

		int flags;
		private int bps; // bits per sample

		void setChannelVolume(int left, int right)
		{
			volume_left = left;
			volume_right = right;
		}


		void activateChannel()
		{
			active = true;
		}

		void setChannelRawSrc( FileEntry fe, int flags )
		{
			int size = fe.file_size;
			int rate = fe.rate;
			bps = fe.bits_per_sample;
			memory = fe.data;
			this.flags = flags;
			frac_pos = 0;
			pos = 0;

			frac_speed = (rate << 16) / mx.play_rate;

			// adjust the magnitude to prevent overflow
			while(0 != (size & 0xFFFF0000)) {
				size >>= 1;
			rate = (rate >> 1) + 1;
			}

			samples_left = size * mx.play_rate / rate;
		}

		void closeChannel()
		{
			active = false;
			memory = null;
		}

		// TODO still does not work with 16 bit samples :(
		void mixInt8ToInt16(int [] buffer, int samples)
		{			
			int bufferPos = 0;

			if (samples > samples_left) samples = samples_left;
			samples_left -= samples;
			assert samples > 0;

			Pixel b = new Pixel( memory, pos );

			boolean x2 = bps == 16;
			if(x2) samples /= 2;
			
			if (frac_speed == 0x10000) {
				// Special case when frac_speed is 0x10000
				do {
					if(x2) b.inc();
					buffer[bufferPos+0] += b.r(0) * volume_left >> 8;
					buffer[bufferPos+1] += b.r(0) * volume_right >> 8;
					b.inc();
					bufferPos += 2;
				} while (--samples > 0);
			} else {
				do {
					buffer[bufferPos+0] += b.r(x2? 1:0) * volume_left >> 8;
					buffer[bufferPos+1] += b.r(x2? 1:0) * volume_right >> 8;
					bufferPos += 2;
					frac_pos += frac_speed;
					b.madd( frac_pos >> 16 );
					if(x2) b.madd( frac_pos >> 16 );
					frac_pos &= 0xffff;
				} while (--samples > 0);
			}

			//sc.pos = b - sc.memory;
			pos = b.getDisplacement();
		}

	}

	static class Mixer 
	{
		int play_rate;
		MixerChannel [] channels = new MixerChannel[8];

		public Mixer(int freq) {
			play_rate = freq;
			for(int i = 0; i < channels.length; i++ )
				channels[i] = new MixerChannel();
		}

		MixerChannel allocateChannel()
		{
			for (MixerChannel mc : channels)
				if (mc.memory == null) {
					mc.active = false;
					mc.mx = this;
					return mc;
				}
			return null;
		}

		void mixSamples(int [] buffer, int samples)
		{
			// Clear the buffer
			Arrays.fill(buffer, (byte)0);

			// Mix each channel
			for(MixerChannel mc : channels) 
			{
				if (mc.active) {
					mc.mixInt8ToInt16(buffer, samples);
					//Global.debug("mix ch %x", mc.hashCode());
					if (mc.samples_left == 0) mc.closeChannel();
				}
			}
		}


	}


	public static void SoundInitialize(String filename) 
	{		
		int count;
		int i;

		FileIO.FioOpenFile(SOUND_SLOT, filename);
		count = FileIO.FioReadDword();
		
		boolean newFormat = BitOps.HASBIT(count, 31);
		count &= ~0x80000000;
		
		count /= 8; 
		
		FileEntry [] fea = new FileEntry[count];

		_file_count = count;
		_files = fea;

		FileIO.FioSeekTo(0, FileIO.SEEK_SET);

		for (i = 0; i != count; i++) {
			fea[i] = new FileEntry();
			fea[i].file_offset = FileIO.FioReadDword() & ~0x80000000;
			fea[i].file_size = FileIO.FioReadDword();
		}

		for (i = 0; i != count; i++) {
			//char name[255];
			FileEntry fe = fea[i];

			FileIO.FioSeekTo(fe.file_offset, FileIO.SEEK_SET);

			// Check for special case, see else case
			byte[] nameBytes = FileIO.FioReadBlock(FileIO.FioReadByte()); // Read the name of the sound
			String name = BitOps.stringFromBytes(nameBytes, 0, nameBytes.length);

			if(newFormat || !name.equals("Corrupt sound")) {
				FileIO.FioSeekTo(12, FileIO.SEEK_CUR); // Skip past RIFF header

				// Read riff tags
				for (;;) {
					int tag = FileIO.FioReadDword();
					int size = FileIO.FioReadDword();

					if (check4chars(tag, ' ', 't', 'm', 'f')) {
						FileIO.FioReadWord(); // wFormatTag
						fe.channels = FileIO.FioReadWord(); // wChannels
						FileIO.FioReadDword();   // samples per second
						fe.rate = 11025; // seems like all samples should be played at this rate.
						FileIO.FioReadDword();   // avg bytes per second
						FileIO.FioReadWord();    // alignment
						fe.bits_per_sample = FileIO.FioReadByte(); // bits per sample
						FileIO.FioSeekTo(size - (2 + 2 + 4 + 4 + 2 + 1L), FileIO.SEEK_CUR);
					} else if (check4chars(tag, 'a', 't', 'a', 'd')) {
						fe.file_size = size;
						fe.file_offset = (int)FileIO.FioGetPos() | (SOUND_SLOT << 24);
						break;
					} else {
						fe.file_size = 0;
						break;
					}
				}
			} else {
				/*
				 * Special case for the jackhammer sound
				 * (name in sample.cat is "Corrupt sound")
				 * It's no RIFF file, but raw PCM data
				 */
				fe.channels = 1;
				fe.rate = 11025;
				fe.bits_per_sample = 8;
				fe.file_offset = (int) (FileIO.FioGetPos() | (SOUND_SLOT << 24));
			}
		}
	}

	private static boolean check4chars(int tag, char ... cs ) 
	{
		if( ((tag >> 0) & 0xFF) != cs[3]) return false;		
		if( ((tag >> 8) & 0xFF) != cs[2]) return false;		
		if( ((tag >>16) & 0xFF) != cs[1]) return false;		
		if( ((tag >>24) & 0xFF) != cs[0]) return false;		

		return true;
	}


	static boolean SetBankSource(MixerChannel mc, int bank)
	{
		FileEntry fe;
		int i;

		if (bank >= _file_count) return false;
		fe = _files[bank];

		if (fe.file_size == 0) return false;

		if(fe.data == null)
		{

			byte [] mem = new byte[fe.file_size];

			FileIO.FioSeekToFile(fe.file_offset);
			mem = FileIO.FioReadBlock(fe.file_size);

			if(fe.bits_per_sample == 8)
			{
				for (i = 0; i != fe.file_size; i++)
					mem[i] += -128; // Convert unsigned sound data to signed
			}
			
			fe.data = mem;
		}

		assert( fe.channels == 1 && fe.file_size != 0 && fe.rate != 0);
		assert fe.bits_per_sample == 8 || fe.bits_per_sample == 16;
		
		mc.setChannelRawSrc(fe, 0);

		return true;
	}


	private static void StartSound(int sound, int panning, int volume)
	{
		int left_vol, right_vol;

		//Global.debug("start snd %d", sound);
		
		if (volume == 0) return;
		MixerChannel mc = _mixer.allocateChannel();
		if (mc == null) return;

		if (!SetBankSource(mc,sound)) return;

		panning = BitOps.clamp(panning, -PANNING_LEVELS, PANNING_LEVELS);
		left_vol = (volume * PANNING_LEVELS) - (volume * panning);
		right_vol = (volume * PANNING_LEVELS) + (volume * panning);
		mc.setChannelVolume(left_vol * 128 / PANNING_LEVELS, right_vol * 128 / PANNING_LEVELS);
		mc.activateChannel();
	}


	static final int _vol_factor_by_zoom[] = {255, 190, 134};

	static final int _sound_base_vol[] = {
			128,  90, 128, 128, 128, 128, 128, 128,
			128,  90,  90, 128, 128, 128, 128, 128,
			128, 128, 128,  80, 128, 128, 128, 128,
			128, 128, 128, 128, 128, 128, 128, 128,
			128, 128,  90,  90,  90, 128,  90, 128,
			128,  90, 128, 128, 128,  90, 128, 128,
			128, 128, 128, 128,  90, 128, 128, 128,
			128,  90, 128, 128, 128, 128, 128, 128,
			128, 128,  90,  90,  90, 128, 128, 128,
			90,
	};

	static final int _sound_idx[] = {
			2,  3,  4,  5,  6,  7,  8,  9,
			10, 11, 12, 13, 14, 15, 16, 17,
			18, 19, 20, 21, 22, 23, 24, 25,
			26, 27, 28, 29, 30, 31, 32, 33,
			34, 35, 36, 37, 38, 39, 40,  0,
			1, 41, 42, 43, 44, 45, 46, 47,
			48, 49, 50, 51, 52, 53, 54, 55,
			56, 57, 58, 59, 60, 61, 62, 63,
			64, 65, 66, 67, 68, 69, 70, 71,
			72,
	};

	public static void SndPlayScreenCoordFx(/*SoundFx*/ int  sound, int x, int y)
	{
		if (MusicGui.getEffectVolume() == 0) return;

		Iterator<Window> ii = Window.getIterator();
		while( ii.hasNext() )
		{
			Window w = ii.next();
			final ViewPort vp = w.getViewport();

			if (vp != null &&
					BitOps.IS_INSIDE_1D(x, vp.getVirtual_left(), vp.getVirtual_width()) &&
					BitOps.IS_INSIDE_1D(y, vp.getVirtual_top(), vp.getVirtual_height())) {
				int left = (x - vp.getVirtual_left());

				StartSound(
						_sound_idx[sound],
						left / (vp.getVirtual_width() / ((PANNING_LEVELS << 1) + 1)) - PANNING_LEVELS,
						(_sound_base_vol[sound] * MusicGui.getEffectVolume() * _vol_factor_by_zoom[vp.getZoom()]) >> 15
						);
				return;
			}
		}

	}

	public static void SndPlayTileFx(Snd snd, TileIndex tile) {
		SndPlayTileFx(snd.ordinal(), tile);
	}

	public static void SndPlayTileFx(/*SoundFx*/ int  sound, TileIndex tile)
	{
		/* emits sound from center (+ 8) of the tile */
		int x = tile.TileX() * 16 + 8;
		int y = tile.TileY() * 16 + 8;
		Point pt = Point.RemapCoords(x, y, Landscape.GetSlopeZ(x, y));
		SndPlayScreenCoordFx(sound, pt.x, pt.y);
	}

	public static void SndPlayFx(Snd sound) {
		SndPlayFx(sound.ordinal());
	}

	static void SndPlayFx(/*SoundFx*/ int  sound)
	{
		StartSound(
				_sound_idx[sound],
				0,
				(_sound_base_vol[sound] * MusicGui.getEffectVolume()) >> 7
				);
	}



	public static void soundPump() 
	{
		SourceDataLine soundLine = null;

		// Set up an audio input stream piped from the mixer
		try {
			AudioFormat audioFormat = new AudioFormat(_mixer.play_rate, 16, 2, true, false);			
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			soundLine = (SourceDataLine) AudioSystem.getLine(info);
			soundLine.open(audioFormat);
			//Global.debug("Sound buffer size %d", soundLine.getBufferSize() ); // shows 22048
			soundLine.start();

			int[] intBuffer = new int[SAMPLES_PER_XFER];
			byte[] byteBuffer = new byte[SAMPLES_PER_XFER*2];

			while((!stopSound) && (!Global._exit_game))
			{
				_mixer.mixSamples(intBuffer, intBuffer.length/2); // stereo!

				for(int i = 0; i < SAMPLES_PER_XFER; i++)
				{
					byteBuffer[i*2+0] = (byte) intBuffer[i];
					byteBuffer[i*2+1] = (byte) (intBuffer[i] >> 8);
				}
				
				// Writes audio data to the mixer via this source data line.
				soundLine.write(byteBuffer, 0, SAMPLES_PER_XFER*2);
				
			}
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		} finally {
			if(soundLine != null)
			{
				soundLine.drain();
				soundLine.close();
			}
		}
	}


	public static void stop() {
		stopSound  = true;
		
	}


}


