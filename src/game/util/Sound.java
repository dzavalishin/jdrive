package game.util;

import java.util.Arrays;

import game.TileIndex;
import game.Vehicle;
import game.tables.Snd;

public class Sound {
	private static final int SOUND_SLOT = 31;
	private static final int PANNING_LEVELS = 16;
	private static int _file_count;
	private static FileEntry[] _files;
	private static Mixer _mixer;

	public static void TrainPlayLeaveStationSound(final Vehicle  v)
	{
		/*
		EngineID engtype = v.getEngine_type();

		switch (Engine.GetEngine(engtype).getRailtype()) {
		case RAILTYPE_RAIL:
			//SndPlayVehicleFx(sfx[RailVehInfo(engtype).engclass], v);
			break;

		case RAILTYPE_MONO:
			SndPlayVehicleFx(SND_47_MAGLEV_2, v);
			break;

		case RAILTYPE_MAGLEV:
			//SndPlayVehicleFx(SND_41_MAGLEV, v);
			break;
		}
		 */
	}

	public static void SndPlayTileFx(Snd snd, TileIndex tile) {


	}

	public static void MxInitialize(int freq) {
		_mixer = new Mixer(freq);
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

		void MxSetChannelVolume(int left, int right)
		{
			volume_left = left;
			volume_right = right;
		}


		void MxActivateChannel()
		{
			active = true;
		}

		void MxSetChannelRawSrc( byte [] mem, int size, int rate, int flags )
		{
			memory = mem;
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
		
		void MxCloseChannel()
		{
			active = false;
			memory = null;
		}
		
		void mix_int8_to_int16(int [] buffer, int samples)
		{			
			int bufferPos = 0;

			if (samples > samples_left) samples = samples_left;
			samples_left -= samples;
			assert samples > 0;

			Pixel b = new Pixel( memory, pos );

			if (frac_speed == 0x10000) {
				// Special case when frac_speed is 0x10000
				do {
					buffer[bufferPos+0] += b.r(0) * volume_left >> 8;
					buffer[bufferPos+1] += b.r(0) * volume_right >> 8;
					b.inc();
					bufferPos += 2;
				} while (--samples > 0);
			} else {
				do {
					buffer[bufferPos+0] += b.r(0) * volume_left >> 8;
					buffer[bufferPos+1] += b.r(0) * volume_right >> 8;
					bufferPos += 2;
					frac_pos += frac_speed;
					b.madd( frac_pos >> 16 );
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

		MixerChannel MxAllocateChannel()
		{
			for (MixerChannel mc : channels)
				if (mc.memory == null) {
					mc.active = false;
					mc.mx = this;
					return mc;
				}
			return null;
		}
		
		void MxMixSamples(int [] buffer, int samples)
		{
			// Clear the buffer
			//memset(buffer, 0, sizeof(int16) * 2 * samples);
			Arrays.fill(buffer, (byte)0);

			// Mix each channel
			for(MixerChannel mc : channels) 
			{
				if (mc.active) {
					mc.mix_int8_to_int16(buffer, samples);
					if (mc.samples_left == 0) mc.MxCloseChannel();
				}
			}
		}
		
		
	}
	
	
	public static void SoundInitialize(String filename) 
	{		
		int count;
		int i;

		FileIO.FioOpenFile(SOUND_SLOT, filename);
		count = FileIO.FioReadDword() / 8;
		FileEntry [] fea = new FileEntry[count];

		_file_count = count;
		_files = fea;

		FileIO.FioSeekTo(0, FileIO.SEEK_SET);

		for (i = 0; i != count; i++) {
			fea[i] = new FileEntry();
			fea[i].file_offset = FileIO.FioReadDword();
			fea[i].file_size = FileIO.FioReadDword();
		}

		for (i = 0; i != count; i++) {
			//char name[255];
			FileEntry fe = fea[i];

			FileIO.FioSeekTo(fe.file_offset, FileIO.SEEK_SET);

			// Check for special case, see else case
			byte[] nameBytes = FileIO.FioReadBlock(FileIO.FioReadByte()); // Read the name of the sound
			String name = BitOps.stringFromBytes(nameBytes, 0, nameBytes.length);
			
			if(!name.equals("Corrupt sound")) {
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
						FileIO.FioSeekTo(size - (2 + 2 + 4 + 4 + 2 + 1), FileIO.SEEK_CUR);
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
		if( ((tag >> 0) & 0xFF) != cs[0]) return false;		
		if( ((tag >> 8) & 0xFF) != cs[0]) return false;		
		if( ((tag >>16) & 0xFF) != cs[0]) return false;		
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

			for (i = 0; i != fe.file_size; i++)
				mem[i] += -128; // Convert unsigned sound data to signed
			
			fe.data = mem;
		}
		
		assert(fe.bits_per_sample == 8 && fe.channels == 1 && fe.file_size != 0 && fe.rate != 0);

		mc.MxSetChannelRawSrc(fe.data, fe.file_size, fe.rate, 0);

		return true;
	}

	
	static void StartSound(int sound, int panning, int volume)
	{
		int left_vol, right_vol;

		if (volume == 0) return;
		MixerChannel mc = _mixer.MxAllocateChannel();
		if (mc == null) return;
		
		if (!SetBankSource(mc,sound)) return;

		panning = BitOps.clamp(panning, -PANNING_LEVELS, PANNING_LEVELS);
		left_vol = (volume * PANNING_LEVELS) - (volume * panning);
		right_vol = (volume * PANNING_LEVELS) + (volume * panning);
		mc.MxSetChannelVolume(left_vol * 128 / PANNING_LEVELS, right_vol * 128 / PANNING_LEVELS);
		mc.MxActivateChannel();
	}

}
