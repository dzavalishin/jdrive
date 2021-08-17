/* $Id: null_s.c 2704 2005-07-25 07:16:10Z tron $ */

#include "../stdafx.h"
#include "../openttd.h"
#include "null_s.h"

static const char *NullSoundStart(const char * const *parm) { return NULL; }
static void NullSoundStop(void) {}

const HalSoundDriver _null_sound_driver = {
	NullSoundStart,
	NullSoundStop,
};
