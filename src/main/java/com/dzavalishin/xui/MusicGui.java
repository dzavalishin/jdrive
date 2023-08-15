package com.dzavalishin.xui;

import java.util.Arrays;

import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.WindowEvents;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Hal;
import com.dzavalishin.game.Str;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Music;
import com.dzavalishin.util.Strings;

public class MusicGui
{
	static MusicFileSettings msf = new MusicFileSettings();

	static byte _music_wnd_cursong;
	static boolean _song_is_active;
	static byte [] _cur_playlist = new byte[33];

	static final int NUM_SONGS_AVAILABLE = 22;


	static byte _playlist_all[] = {
			1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 0
	};

	static byte _playlist_old_style[] = {
			1, 8, 2, 9, 14, 15, 19, 13, 0
	};

	static byte _playlist_new_style[] = {
			6, 11, 10, 17, 21, 18, 5, 0
	};

	static byte _playlist_ezy_street[] = {
			12, 7, 16, 3, 20, 4, 0
	};

	static final byte [][] _playlists = {
			_playlist_all,
			_playlist_old_style,
			_playlist_new_style,
			_playlist_ezy_street,
			msf.custom_1,
			msf.custom_2,
	};

	// Map the order of the song names to the numbers of the midi filenames
	static final byte midi_idx[] = {
			0, // Tycoon DELUXE Theme
			2, // Easy Driver
			3, // Little Red Diesel
			17, // Cruise Control
			7, // Don't Walk!
			9, // Fell Apart On Me
			4, // City Groove
			19, // Funk Central
			6, // Stoke It
			12, // Road Hog
			5, // Aliens Ate My Railway
			1, // Snarl Up
			18, // Stroll On
			10, // Can't Get There From Here
			8, // Sawyer's Tune
			13, // Hold That Train!
			21, // Movin' On
			15, // Goss Groove
			16, // Small Town
			14, // Broomer's Oil Rag
			20, // Jammit
			11  // Hard Drivin'
	};


	static void SkipToPrevSong()
	{
		byte [] b = _cur_playlist;
		//byte [] p = b;
		byte t;

		// empty playlist
		if (b[0] == 0) return;

		// find the end
		int pos = 0;
		do pos++; while (b[pos] != 0);

		// and copy the bytes
		t = b[--pos]; //*--p;
		while (pos > 0) {
			pos--;
			b[pos+1] = b[pos+0];
		}
		b[0] = t;

		_song_is_active = false;
	}

	static void SkipToNextSong()
	{
		byte [] b = _cur_playlist;
		byte t;

		t = b[0];
		
		int pos = 0;
		if (t != 0) {
			while (b[pos+1] != 0) {
				b[pos+0] = b[pos+1];
				pos++;
			}
			b[pos] = t;
		}
		
		_song_is_active = false;
	}

	static void MusicVolumeChanged(int new_vol)
	{
		Music.set_volume(new_vol);
	}

	static void DoPlaySong()
	{
		String filename = String.format("%sgm_tt%02d.gm", Global._path.gm_dir, midi_idx[_music_wnd_cursong - 1]);
		Music.play_song(filename);
	}

	static void DoStopMusic()
	{
		Music.stop_song();
	}

	static void SelectSongToPlay()
	{
		int i = 0;

		//memset(_cur_playlist, 0, sizeof(_cur_playlist));
		Arrays.fill(_cur_playlist, (byte)0);
		
		do {
			_cur_playlist[i] = _playlists[msf.playlist][i];
		} while (_playlists[msf.playlist][i++] != 0 && i < _cur_playlist.length - 1);

		if (msf.shuffle) {
			i = 500;
			do {
				int r = Hal.InteractiveRandom();
				//byte *a = &_cur_playlist[BitOps.GB(r, 0, 5)];
				//byte *b = &_cur_playlist[BitOps.GB(r, 8, 5)];
				int a = BitOps.GB(r, 0, 5);
				int b = BitOps.GB(r, 8, 5);

				if (_cur_playlist[a] != 0 && _cur_playlist[b] != 0) {
					byte t = _cur_playlist[a];
					_cur_playlist[a] = _cur_playlist[b];
					_cur_playlist[b] = t;
				}
			} while (--i > 0);
		}
	}

	static void StopMusic()
	{
		_music_wnd_cursong = 0;
		DoStopMusic();
		_song_is_active = false;
		Window.InvalidateWindowWidget(Window.WC_MUSIC_WINDOW, 0, 9);
	}

	static void PlayPlaylistSong()
	{
		if (_cur_playlist[0] == 0) {
			SelectSongToPlay();
			if (_cur_playlist[0] == 0) return;
		}
		_music_wnd_cursong = _cur_playlist[0];
		DoPlaySong();
		_song_is_active = true;

		Window.InvalidateWindowWidget(Window.WC_MUSIC_WINDOW, 0, 9);
	}

	static void ResetMusic()
	{
		_music_wnd_cursong = 1;
		DoPlaySong();
	}

	public static void MusicLoop()
	{
		if (!msf.btn_down && _song_is_active) {
			StopMusic();
		} else if (msf.btn_down && !_song_is_active) {
			PlayPlaylistSong();
		}

		if (_song_is_active == false) return;

		if (!Music.is_song_playing()) {
			if (Global._game_mode != GameModes.GM_MENU) {
				StopMusic();
				SkipToNextSong();
				PlayPlaylistSong();
			} else {
				ResetMusic();
			}
		}
	}

	static void MusicTrackSelectionWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			//final byte [] p;
			int i;
			int y;

			w.disabled_state = (msf.playlist  <= 3) ? (1 << 11) : 0;
			w.click_state |= 0x18;
			w.DrawWindowWidgets();

			Gfx.GfxFillRect(3, 23, 3+177,23+191,0);
			Gfx.GfxFillRect(251, 23, 251+177,23+191,0);

			Gfx.DrawStringCentered(92, 15, Str.STR_01EE_TRACK_INDEX, 0);

			Global.SetDParam(0, Str.STR_01D5_ALL + msf.playlist);
			Gfx.DrawStringCentered(340, 15, Str.STR_01EF_PROGRAM, 0);

			for (i = 1; i <= NUM_SONGS_AVAILABLE; i++) {
				Global.SetDParam(0, i);
				Global.SetDParam(2, i);
				Global.SetDParam(1, Strings.SPECSTR_SONGNAME);
				Gfx.DrawString(4, 23+(i-1)*6, (i < 10) ? Str.STR_01EC_0 : Str.STR_01ED, 0);
			}

			for (i = 0; i != 6; i++) {
				Gfx.DrawStringCentered(216, 45 + i * 8, Str.STR_01D5_ALL + i, (i == msf.playlist) ? 0xC : 0x10);
			}

			Gfx.DrawStringCentered(216, 45+8*6+16, Str.STR_01F0_CLEAR, 0);
			Gfx.DrawStringCentered(216, 45+8*6+16*2, Str.STR_01F1_SAVE, 0);

			y = 23;
			//for (p = _playlists[msf.playlist], i = 0; (i = p[0]) != 0; p++)
			byte[] p = _playlists[msf.playlist];
			int ip = 0;
			for ( i = 0; (i = p[ip]) != 0; ip++) 
			{
				Global.SetDParam(0, i);
				Global.SetDParam(1, Strings.SPECSTR_SONGNAME);
				Global.SetDParam(2, i);
				Gfx.DrawString(252, y, (i < 10) ? Str.STR_01EC_0 : Str.STR_01ED, 0);
				y += 6;
			}
			break;
		}

		case WE_CLICK:
			switch (e.widget) {
			case 3: { /* add to playlist */
				int y = (e.pt.y - 23) / 6;
				int i;
				byte []p;

				if (msf.playlist < 4) return;
				if (!BitOps.IS_INT_INSIDE(y, 0, NUM_SONGS_AVAILABLE)) return;

				p = _playlists[msf.playlist];
				for (i = 0; i != 32; i++) {
					if (p[i] == 0) {
						p[i] = (byte) (y + 1);
						p[i + 1] = 0;
						w.SetWindowDirty();
						SelectSongToPlay();
						break;
					}
				}

			} break;
			case 11: /* clear */
				_playlists[msf.playlist][0] = 0;
				w.SetWindowDirty();
				StopMusic();
				SelectSongToPlay();
				break;
			case 12: /* save */
				Hal.ShowInfo("MusicTrackSelectionWndProc:save not implemented\n");
				break;
			case 5: case 6: case 7: case 8: case 9: case 10: /* set playlist */
				msf.playlist = e.widget - 5;
				w.SetWindowDirty();
				Window.InvalidateWindow(Window.WC_MUSIC_WINDOW, 0);
				StopMusic();
				SelectSongToPlay();
				break;
			}
			break;
		default:
			break;
		}
	}

	static final Widget _music_track_selection_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   431,     0,    13, Str.STR_01EB_MUSIC_PROGRAM_SELECTION, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   431,    14,   217, 0x0,			Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     2,   181,    22,   215, 0x0,			Str.STR_01FA_CLICK_ON_MUSIC_TRACK_TO),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   250,   429,    22,   215, 0x0,			Str.STR_01F2_CURRENT_PROGRAM_OF_MUSIC),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,    44,    51, 0x0,			Str.STR_01F3_SELECT_ALL_TRACKS_PROGRAM),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,    52,    59, 0x0,			Str.STR_01F4_SELECT_OLD_STYLE_MUSIC),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,    60,    67, 0x0,			Str.STR_01F5_SELECT_NEW_STYLE_MUSIC),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,    68,    75, 0x0,			Str.STR_0330_SELECT_EZY_STREET_STYLE),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,    76,    83, 0x0,			Str.STR_01F6_SELECT_CUSTOM_1_USER_DEFINED),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,    84,    91, 0x0,			Str.STR_01F7_SELECT_CUSTOM_2_USER_DEFINED),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,   108,   115, 0x0,			Str.STR_01F8_CLEAR_CURRENT_PROGRAM_CUSTOM1),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   186,   245,   124,   131, 0x0,			Str.STR_01F9_SAVE_MUSIC_SETTINGS),

	};

	static final WindowDesc _music_track_selection_desc = new WindowDesc(
			104, 131, 432, 218,
			Window.WC_MUSIC_TRACK_SELECTION,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_music_track_selection_widgets,
			MusicGui::MusicTrackSelectionWndProc
	);

	static void ShowMusicTrackSelection()
	{
		Window.AllocateWindowDescFront(_music_track_selection_desc, 0);
	}

	static void MusicWindowWndProc(Window w, WindowEvent e)
	{
		switch(e.event) {
		case WE_PAINT: {
			int i;
			//StringID 
			int str;

			w.click_state |= 0x280;
			w.DrawWindowWidgets();

			Gfx.GfxFillRect(187, 16, 200, 33, 0);

			for (i = 0; i != 8; i++) {
				int color = 0xD0;
				if (i > 4) {
					color = 0xBF;
					if (i > 6) {
						color = 0xB8;
					}
				}
				Gfx.GfxFillRect(187, 33 - i * 2, 200, 33 - i * 2, color);
			}

			Gfx.GfxFillRect(60, 46, 239, 52, 0);

			if (!_song_is_active || _music_wnd_cursong == 0) {
				str = Str.STR_01E3;
			} else {
				Global.SetDParam(0, _music_wnd_cursong);
				str = (_music_wnd_cursong < 10) ? Str.STR_01E4_0 : Str.STR_01E5;
			}
			Gfx.DrawString(62, 46, str, 0);

			str = Str.STR_01E6;
			if (_song_is_active && _music_wnd_cursong != 0) {
				str = Str.STR_01E7;
				Global.SetDParam(0, Strings.SPECSTR_SONGNAME);
				Global.SetDParam(1, _music_wnd_cursong);
			}
			Gfx.DrawStringCentered(155, 46, str, 0);


			Gfx.DrawString(60, 38, Str.STR_01E8_TRACK_XTITLE, 0);

			for (i = 0; i != 6; i++) {
				Gfx.DrawStringCentered(25 + i * 50, 59, Str.STR_01D5_ALL + i, msf.playlist == i ? 0xC : 0x10);
			}

			Gfx.DrawStringCentered(31, 43, Str.STR_01E9_SHUFFLE, (msf.shuffle ? 0xC : 0x10));
			Gfx.DrawStringCentered(269, 43, Str.STR_01EA_PROGRAM, 0);
			Gfx.DrawStringCentered(141, 15, Str.STR_01DB_MUSIC_VOLUME, 0);
			Gfx.DrawStringCentered(141, 29, Str.STR_01DD_MIN_MAX, 0);
			Gfx.DrawStringCentered(247, 15, Str.STR_01DC_EFFECTS_VOLUME, 0);
			Gfx.DrawStringCentered(247, 29, Str.STR_01DD_MIN_MAX, 0);

			Gfx.DrawFrameRect(108, 23, 174, 26, 14, Window.FR_LOWERED);
			Gfx.DrawFrameRect(214, 23, 280, 26, 14, Window.FR_LOWERED);

			Gfx.DrawFrameRect(108 + (msf.music_vol>>1),
					22,
					111 + (msf.music_vol>>1),
					28,
					14,
					0);

			Gfx.DrawFrameRect(214 + (msf.effect_vol>>1),
					22,
					217 + (msf.effect_vol>>1),
					28,
					14,
					0);
		} break;

		case WE_CLICK:
			switch(e.widget) {
			case 2: // skip to prev
				if (!_song_is_active)
					return;
				SkipToPrevSong();
				break;
			case 3: // skip to next
				if (!_song_is_active)
					return;
				SkipToNextSong();
				break;
			case 4: // stop playing
				msf.btn_down = false;
				break;
			case 5: // start playing
				msf.btn_down = true;
				break;
			case 6:{ // volume sliders
				//byte *vol;
				int x = e.pt.x - 88;

				if (x < 0)
					return;

				//vol = &msf.music_vol;
				boolean effectVol = false;
				if (x >= 106) {
					//vol = &msf.effect_vol;
					effectVol = true;
					x -= 106;
				}
				int old_vol = effectVol ? msf.effect_vol : msf.music_vol;
				
				int new_vol = Math.min(Math.max(x-21,0)*2,127);
				if (new_vol != old_vol) {
					
					if(effectVol) msf.effect_vol = new_vol; 
					else msf.music_vol = new_vol;
					
					if(!effectVol)
						MusicVolumeChanged(new_vol);
					w.SetWindowDirty();
				}

				Window._left_button_clicked = false;
			} break;
			case 10: //toggle shuffle
				msf.shuffle ^= true;
				StopMusic();
				SelectSongToPlay();
				break;
			case 11: //show track selection
				ShowMusicTrackSelection();
				break;
			case 12: case 13: case 14: case 15: case 16: case 17: // playlist
				msf.playlist = e.widget - 12;
				w.SetWindowDirty();
				Window.InvalidateWindow(Window.WC_MUSIC_TRACK_SELECTION, 0);
				StopMusic();
				SelectSongToPlay();
				break;
			}
			break;

		case WE_MOUSELOOP:
			Window.InvalidateWindowWidget(Window.WC_MUSIC_WINDOW, 0, 7);
			break;
			
		default:
			break;
		}

	}

	static final Widget _music_window_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    14,     0,    10,     0,    13, Str.STR_00C5,	Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    14,    11,   299,     0,    13, Str.STR_01D2_JAZZ_JUKEBOX, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,     0,    21,    14,    35, 0x2C5,			Str.STR_01DE_SKIP_TO_PREVIOUS_TRACK),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,    22,    43,    14,    35, 0x2C6,			Str.STR_01DF_SKIP_TO_NEXT_TRACK_IN_SELECTION),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,    44,    65,    14,    35, 0x2C7,			Str.STR_01E0_STOP_PLAYING_MUSIC),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,    66,    87,    14,    35, 0x2C8,			Str.STR_01E1_START_PLAYING_MUSIC),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    88,   299,    14,    35, 0x0,				Str.STR_01E2_DRAG_SLIDERS_TO_SET_MUSIC),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,   186,   201,    15,    34, 0x0,				Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,     0,   299,    36,    57, 0x0,				Str.STR_NULL),
			new Widget(     Window.WWT_IMGBTN,   Window.RESIZE_NONE,    14,    59,   240,    45,    53, 0x0,				Str.STR_NULL),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,     6,    55,    42,    49, 0x0,				Str.STR_01FB_TOGGLE_PROGRAM_SHUFFLE),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   244,   293,    42,    49, 0x0,				Str.STR_01FC_SHOW_MUSIC_TRACK_SELECTION),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,     0,    49,    58,    65, 0x0,				Str.STR_01F3_SELECT_ALL_TRACKS_PROGRAM),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,    50,    99,    58,    65, 0x0,				Str.STR_01F4_SELECT_OLD_STYLE_MUSIC),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   100,   149,    58,    65, 0x0,				Str.STR_01F5_SELECT_NEW_STYLE_MUSIC),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   150,   199,    58,    65, 0x0,				Str.STR_0330_SELECT_EZY_STREET_STYLE),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   200,   249,    58,    65, 0x0,				Str.STR_01F6_SELECT_CUSTOM_1_USER_DEFINED),
			new Widget( Window.WWT_PUSHIMGBTN,   Window.RESIZE_NONE,    14,   250,   299,    58,    65, 0x0,				Str.STR_01F7_SELECT_CUSTOM_2_USER_DEFINED),
	};

	static final WindowDesc _music_window_desc = new WindowDesc(
			0, 22, 300, 66,
			Window.WC_MUSIC_WINDOW,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET | WindowDesc.WDF_UNCLICK_BUTTONS,
			_music_window_widgets,
			MusicGui::MusicWindowWndProc
	);

	static void ShowMusicWindow()
	{
		Window.AllocateWindowDescFront(_music_window_desc, 0);
	}

	
	static class MusicFileSettings {
		int playlist;
		int music_vol = 100;
		int effect_vol = 100;
		byte [] custom_1 = new byte[33];
		byte [] custom_2 = new byte[33];
		boolean btn_down;
		boolean shuffle;
		char [] extmidi = new char[80];
	}


	public static int getEffectVolume() {
		return msf.effect_vol;
	}

	
	
}
