/* $Id: sound.c 3131 2005-11-04 10:02:50Z peter1138 $ */

#include "stdafx.h"
#include "openttd.h"
#include "functions.h"
#include "map.h"
#include "mixer.h"
#include "sound.h"
#include "vehicle.h"
#include "window.h"
#include "viewport.h"
#include "fileio.h"

typedef struct FileEntry {
	uint32 file_offset;
	uint32 file_size;
	uint16 rate;
	uint8 bits_per_sample;
	uint8 channels;
} FileEntry;

static uint _file_count;
static FileEntry* _files;

#define SOUND_SLOT 31
// Number of levels of panning per side
#define PANNING_LEVELS 16


static void OpenBankFile(const char *filename)
{
	FileEntry* fe;
	uint count;
	uint i;

	FioOpenFile(SOUND_SLOT, filename);
	count = FioReadDword() / 8;
	fe = calloc(sizeof(*fe), count);

	if (fe == NULL) {
		_file_count = 0;
		_files = NULL;
		return;
	}

	_file_count = count;
	_files = fe;

	FioSeekTo(0, SEEK_SET);

	for (i = 0; i != count; i++) {
		fe[i].file_offset = FioReadDword();
		fe[i].file_size = FioReadDword();
	}

	for (i = 0; i != count; i++, fe++) {
		char name[255];

		FioSeekTo(fe->file_offset, SEEK_SET);

		// Check for special case, see else case
		FioReadBlock(name, FioReadByte()); // Read the name of the sound
		if (strcmp(name, "Corrupt sound") != 0) {
			FioSeekTo(12, SEEK_CUR); // Skip past RIFF header

			// Read riff tags
			for (;;) {
				uint32 tag = FioReadDword();
				uint32 size = FioReadDword();

				if (tag == ' tmf') {
					FioReadWord(); // wFormatTag
					fe->channels = FioReadWord(); // wChannels
					FioReadDword();   // samples per second
					fe->rate = 11025; // seems like all samples should be played at this rate.
					FioReadDword();   // avg bytes per second
					FioReadWord();    // alignment
					fe->bits_per_sample = FioReadByte(); // bits per sample
					FioSeekTo(size - (2 + 2 + 4 + 4 + 2 + 1), SEEK_CUR);
				} else if (tag == 'atad') {
					fe->file_size = size;
					fe->file_offset = FioGetPos() | (SOUND_SLOT << 24);
					break;
				} else {
					fe->file_size = 0;
					break;
				}
			}
		} else {
			/*
			 * Special case for the jackhammer sound
			 * (name in sample.cat is "Corrupt sound")
			 * It's no RIFF file, but raw PCM data
			 */
			fe->channels = 1;
			fe->rate = 11025;
			fe->bits_per_sample = 8;
			fe->file_offset = FioGetPos() | (SOUND_SLOT << 24);
		}
	}
}

static bool SetBankSource(MixerChannel *mc, uint bank)
{
	FileEntry* fe;
	int8* mem;
	uint i;

	if (bank >= _file_count) return false;
	fe = &_files[bank];

	if (fe->file_size == 0) return false;

	mem = malloc(fe->file_size);
	if (mem == NULL) return false;

	FioSeekToFile(fe->file_offset);
	FioReadBlock(mem, fe->file_size);

	for (i = 0; i != fe->file_size; i++)
		mem[i] += -128; // Convert unsigned sound data to signed

	assert(fe->bits_per_sample == 8 && fe->channels == 1 && fe->file_size != 0 && fe->rate != 0);

	MxSetChannelRawSrc(mc, mem, fe->file_size, fe->rate, MX_AUTOFREE);

	return true;
}

bool SoundInitialize(const char *filename)
{
	OpenBankFile(filename);
	return true;
}

// Low level sound player
static void StartSound(uint sound, int panning, uint volume)
{
	MixerChannel* mc;
	uint left_vol, right_vol;

	if (volume == 0) return;
	mc = MxAllocateChannel(_mixer);
	if (mc == NULL) return;
	if (!SetBankSource(mc, sound)) return;

	panning = clamp(panning, -PANNING_LEVELS, PANNING_LEVELS);
	left_vol = (volume * PANNING_LEVELS) - (volume * panning);
	right_vol = (volume * PANNING_LEVELS) + (volume * panning);
	MxSetChannelVolume(mc, left_vol * 128 / PANNING_LEVELS, right_vol * 128 / PANNING_LEVELS);
	MxActivateChannel(mc);
}


static const byte _vol_factor_by_zoom[] = {255, 190, 134};

static const byte _sound_base_vol[] = {
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

static const byte _sound_idx[] = {
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

static void SndPlayScreenCoordFx(SoundFx sound, int x, int y)
{
	const Window* w;

	if (msf.effect_vol == 0) return;

	for (w = _windows; w != _last_window; w++) {
		const ViewPort* vp = w->viewport;

		if (vp != NULL &&
				IS_INSIDE_1D(x, vp->virtual_left, vp->virtual_width) &&
				IS_INSIDE_1D(y, vp->virtual_top, vp->virtual_height)) {
			int left = (x - vp->virtual_left);

			StartSound(
				_sound_idx[sound],
				left / (vp->virtual_width / ((PANNING_LEVELS << 1) + 1)) - PANNING_LEVELS,
				(_sound_base_vol[sound] * msf.effect_vol * _vol_factor_by_zoom[vp->zoom]) >> 15
			);
			return;
		}
	}

}

void SndPlayTileFx(SoundFx sound, TileIndex tile)
{
	/* emits sound from center (+ 8) of the tile */
	int x = TileX(tile) * 16 + 8;
	int y = TileY(tile) * 16 + 8;
	Point pt = RemapCoords(x, y, GetSlopeZ(x, y));
	SndPlayScreenCoordFx(sound, pt.x, pt.y);
}

void SndPlayVehicleFx(SoundFx sound, const Vehicle *v)
{
	SndPlayScreenCoordFx(sound,
		(v->left_coord + v->right_coord) / 2,
		(v->top_coord + v->bottom_coord) / 2
	);
}

void SndPlayFx(SoundFx sound)
{
	StartSound(
		_sound_idx[sound],
		0,
		(_sound_base_vol[sound] * msf.effect_vol) >> 7
	);
}
