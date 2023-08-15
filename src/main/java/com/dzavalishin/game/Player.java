package com.dzavalishin.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.dzavalishin.ai.Ai;
import com.dzavalishin.ai.PlayerAiNew;
import com.dzavalishin.ids.EngineID;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetServer;
import com.dzavalishin.net.NetworkClientInfo;
import com.dzavalishin.enums.GameModes;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.struct.HighScore;
import com.dzavalishin.struct.PlayerEconomyEntry;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Sound;
import com.dzavalishin.util.Strings;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.PlayerGui;
import com.dzavalishin.xui.Window;


public class Player implements Serializable 
{


	private static final long serialVersionUID = 1L;

	int name_2;
	int name_1;

	int president_name_1;
	int president_name_2;

	public int face;

	//int player_money;
	int current_loan;
	long money64; // internal 64-bit version of the money. the 32-bit field will be clamped to plus minus 2 billion

	int player_color;
	int player_money_fraction;
	public int avail_railtypes;
	int block_preview;
	PlayerID index;

	int cargo_types; // which cargo types were transported the last year 

	TileIndex location_of_house;
	TileIndex last_build_coordinate;

	public PlayerID share_owners[];

	int inaugurated_year;
	public int num_valid_stat_ent;

	int quarters_of_bankrupcy;
	int bankrupt_asked; // which players were asked about buying it?
	int bankrupt_timeout;
	int bankrupt_value;

	boolean is_active;
	private boolean is_ai;
	//  PlayerAI ai = new PlayerAI();
	public PlayerAiNew ainew = new PlayerAiNew();

	final long [][] yearly_expenses = new long[3][13];

	public PlayerEconomyEntry cur_economy;
	public PlayerEconomyEntry old_economy[];
	//EngineID engine_replacement[];
	int engine_replacement[];
	boolean engine_renew;
	boolean renew_keep_length;
	int engine_renew_months;
	long engine_renew_money;


	public Player()
	{
		clear();
	}

	private void clear() {
		is_active = false;

		share_owners = new PlayerID[4];
		old_economy = new PlayerEconomyEntry[24];
		for(int i = 0; i < old_economy.length; i++ )
			old_economy[i] = new PlayerEconomyEntry();

		engine_replacement = new int[Global.TOTAL_NUM_ENGINES];


		inaugurated_year = num_valid_stat_ent = quarters_of_bankrupcy = bankrupt_asked =
				player_color = player_money_fraction = avail_railtypes = block_preview = 0;

		name_2 = name_1 = president_name_1 = current_loan = bankrupt_timeout =
				bankrupt_value = cargo_types = engine_renew_months = 0;

		money64 = engine_renew_money = president_name_2 = face = 0;

		is_ai = engine_renew = renew_keep_length = false;

		cur_economy = new PlayerEconomyEntry();
	}


	public boolean isActive() { return is_active; }
	public boolean isAi() { return is_ai; }
	public PlayerID getIndex() { return index; }
	public long getMoney() { return money64; }



	public static final long INITIAL_MONEY = 100000000;

	public static void SET_EXPENSES_TYPE(int x) { Global.gs._yearly_expenses_type = x; }

	public static final int EXPENSES_CONSTRUCTION = 0;
	public static final int EXPENSES_NEW_VEHICLES = 1;
	public static final int EXPENSES_TRAIN_RUN = 2;
	public static final int EXPENSES_ROADVEH_RUN = 3;
	public static final int EXPENSES_AIRCRAFT_RUN = 4;
	public static final int EXPENSES_SHIP_RUN = 5;
	public static final int EXPENSES_PROPERTY = 6;
	public static final int EXPENSES_TRAIN_INC = 7;
	public static final int EXPENSES_ROADVEH_INC = 8;
	public static final int EXPENSES_AIRCRAFT_INC = 9;
	public static final int EXPENSES_SHIP_INC = 10;
	public static final int EXPENSES_LOAN_INT = 11;
	public static final int EXPENSES_OTHER = 12;





	public static Player GetPlayer(PlayerID i)
	{
		//assert(i.id < Global.gs._players.length);
		i.assertValid();
		return Global.gs._players[i.id];
	}

	public static Player GetPlayer(int i)
	{
		//assert(i < Global.gs._players.length);
		PlayerID.assertValid(i);
		return Global.gs._players[i];
	}

	public static Player GetCurrentPlayer() {
		return GetPlayer(PlayerID.getCurrent());
	}

	static boolean IsLocalPlayer()
	{
		return Global.gs._local_player.id == Global.gs.getCurrentPlayer().id;
	}



	//static final SpriteID cheeks_table[] = {
	static final int cheeks_table[] = {
			0x325, 0x326,
			0x390, 0x3B0,
	};

	//static final SpriteID mouth_table[3] = {
	static final int mouth_table[] = {
			0x34C, 0x34D, 0x34F
	};

	public static void DrawPlayerFace(int face, int color, int x, int y)
	{
		int flag = 0;

		if ( face < 0)
			flag |= 1;
		if ((((((face >> 7) ^ face) >> 7) ^ face) & 0x8080000) == 0x8000000)
			flag |= 2;

		// draw the gradient 
		Gfx.DrawSprite((color + 0x307) << Sprite.PALETTE_SPRITE_START | Sprite.PALETTE_MODIFIER_COLOR | Sprite.SPR_GRADIENT, x, y);

		// draw the cheeks 
		Gfx.DrawSprite(cheeks_table[flag&3], x, y);

		// draw the chin 
		// FIXME: real code uses -2 in zoomlevel 1 
		{
			int val = BitOps.GB(face, 4, 2);
			if (0 == (flag & 2)) {
				Gfx.DrawSprite(0x327 + (0 != (flag&1)?0:val), x, y);
			} else {
				Gfx.DrawSprite((0 != (flag&1)?0x3B1:0x391) + (val>>1), x, y);
			}
		}
		// draw the eyes 
		{
			int val1 = BitOps.GB(face,  6, 4);
			int val2 = BitOps.GB(face, 20, 3);
			int high = 0x314 << Sprite.PALETTE_SPRITE_START;

			if (val2 >= 6) {
				high = 0x30F << Sprite.PALETTE_SPRITE_START;
				if (val2 != 6)
					high = 0x30D << Sprite.PALETTE_SPRITE_START;
			}

			if (0 == (flag & 2)) {
				if (0 == (flag & 1)) {
					Gfx.DrawSprite(high+((val1 * 12 >> 4) + (0x32B | Sprite.PALETTE_MODIFIER_COLOR)), x, y);
				} else {
					Gfx.DrawSprite(high+(val1 + (0x337 | Sprite.PALETTE_MODIFIER_COLOR)), x, y);
				}
			} else {
				if ( 0 == (flag & 1)) {
					Gfx.DrawSprite(high+((val1 * 11 >> 4) + (0x39A | Sprite.PALETTE_MODIFIER_COLOR)), x, y);
				} else {
					Gfx.DrawSprite(high+(val1 + (0x3B8 | Sprite.PALETTE_MODIFIER_COLOR)), x, y);
				}
			}
		}

		// draw the mouth 
		do {
			int val = BitOps.GB(face, 10, 6);
			int val2;

			if (0 == (flag&1)) {
				val2 = ((val&0xF) * 15 >> 4);

				if (val2 < 3) {
					Gfx.DrawSprite(( 0 != (flag&2) ? 0x397 : 0x367) + val2, x, y);
					// skip the rest 
					//goto skip_mouth;
					break;
				}

				val2 -= 3;
				if( 0 != (flag & 2)) {
					if (val2 > 8) val2 = 0;
					val2 += 0x3A5 - 0x35B;
				}
				Gfx.DrawSprite(val2 + 0x35B, x, y);
			} else if (0 == (flag&2)) {
				Gfx.DrawSprite(((val&0xF) * 10 >> 4) + 0x351, x, y);
			} else {
				Gfx.DrawSprite(((val&0xF) * 9 >> 4) + 0x3C8, x, y);
			}

			val >>= 3;

			if (0 == (flag&2)) {
				if (0 == (flag&1)) {
					Gfx.DrawSprite(0x349 + val, x, y);
				} else {
					Gfx.DrawSprite( mouth_table[(val*3>>3)], x, y);
				}
			} else {
				if (0 == (flag&1)) {
					Gfx.DrawSprite(0x393 + (val&3), x, y);
				} else {
					Gfx.DrawSprite(0x3B3 + (val*5>>3), x, y);
				}
			}

			//skip_mouth:;
		} while(false); // goto target


		// draw the hair 
		{
			int val = BitOps.GB(face, 16, 4);
			if( 0 != (flag & 2)) {
				if( 0 != (flag & 1)) {
					Gfx.DrawSprite(0x3D9 + (val * 5 >> 4), x, y);
				} else {
					Gfx.DrawSprite(0x3D4 + (val * 5 >> 4), x, y);
				}
			} else {
				if( 0 != (flag & 1)) {
					Gfx.DrawSprite(0x38B + (val * 5 >> 4), x, y);
				} else {
					Gfx.DrawSprite(0x382 + (val * 9 >> 4), x, y);
				}
			}
		}

		// draw the tie 
		{
			int val = BitOps.GB(face, 20, 8);

			if (0 == (flag&1)) {
				Gfx.DrawSprite(0x36B + (BitOps.GB(val, 0, 2) * 3 >> 2), x, y);
				Gfx.DrawSprite(0x36E + (BitOps.GB(val, 2, 2) * 4 >> 2), x, y);
				Gfx.DrawSprite(0x372 + (BitOps.GB(val, 4, 4) * 6 >> 4), x, y);
			} else {
				Gfx.DrawSprite(0x378 + (BitOps.GB(val, 0, 2) * 3 >> 2), x, y);
				Gfx.DrawSprite(0x37B + (BitOps.GB(val, 2, 2) * 4 >> 2), x, y);

				val >>= 4;
				if (val < 3) Gfx.DrawSprite((0 != (flag & 2) ? 0x3D1 : 0x37F) + val, x, y);
			}
		}

		/* draw the glasses */
		{
			int val = BitOps.GB(face, 28, 3);

			if (0 != (flag & 2)) {
				if (val <= 1) Gfx.DrawSprite(0x3AE + val, x, y);
			} else {
				if (val <= 1) Gfx.DrawSprite(0x347 + val, x, y);
			}
		}
	}

	public void InvalidatePlayerWindows()
	{
		PlayerID pid = index;

		if (pid.equals(Global.gs._local_player)) Window.InvalidateWindow(Window.WC_STATUS_BAR, 0);
		Window.InvalidateWindow(Window.WC_FINANCES, pid.id);
	}

	/** current one */
	public static boolean CheckPlayerHasMoney(int cost)
	{
		if (cost > 0) {
			PlayerID pid = PlayerID.getCurrent();
			if (pid.id < Global.MAX_PLAYERS && cost > GetPlayer(pid).money64) {
				Global.SetDParam(0, cost);
				Global._error_message = Str.STR_0003_NOT_ENOUGH_CASH_REQUIRES;
				return false;
			}
		}
		return true;
	}

	private void SubtractMoneyFromAnyPlayer(int cost)
	{
		money64 -= cost;
		//UpdatePlayerMoney32();

		yearly_expenses[0][Global.gs._yearly_expenses_type] += cost;

		if(0 != ( ( 1 << Global.gs._yearly_expenses_type ) & (1<<7|1<<8|1<<9|1<<10)) )
			cur_economy.income -= cost;
		else if(0 != (( 1 << Global.gs._yearly_expenses_type ) & (1<<2|1<<3|1<<4|1<<5|1<<6|1<<11)) )
			cur_economy.expenses -= cost;

		InvalidatePlayerWindows();
	}

	static void SubtractMoneyFromPlayer(int cost)
	{
		PlayerID pid = PlayerID.getCurrent();
		if (pid.id < Global.MAX_PLAYERS)
			GetPlayer(pid).SubtractMoneyFromAnyPlayer(cost);
	}

	public static void SubtractMoneyFromPlayerFract(PlayerID player, int cost)
	{
		Player p = GetPlayer(player);
		int m = p.player_money_fraction;
		p.player_money_fraction =  (m - cost);
		cost >>= 8;
		if (p.player_money_fraction > m)
			cost++;
		if (cost != 0)
			p.SubtractMoneyFromAnyPlayer(cost);
	}

	// the player_money field is kept as it is, but money64 contains the actual amount of money.
	/*public void UpdatePlayerMoney32()
	{
		if (money64 < -2000000000)
			player_money = -2000000000;
		else if (money64 > 2000000000)
			player_money = 2000000000;
		else
			player_money = (int)money64;
	}*/

	public static void GetNameOfOwner(PlayerID owner, TileIndex tile)
	{
		Global.SetDParam(2, owner.id);

		if (owner.id != Owner.OWNER_TOWN) {
			if (owner.id >= 8)
				Global.SetDParam(0, Str.STR_0150_SOMEONE);
			else {
				Player p = GetPlayer(owner);
				Global.SetDParam(0, p.name_1);
				Global.SetDParam(1, p.name_2);
			}
		} else {
			Town t = Town.ClosestTownFromTile(tile, -1);
			Global.SetDParam(0, Str.STR_TOWN);
			Global.SetDParam(1, t.index);
		}
	}


	static boolean CheckOwnership(PlayerID owner)
	{
		assert(owner.id <= Owner.OWNER_WATER);

		if (owner.equals(PlayerID.getCurrent()))
			return true;
		Global._error_message = Str.STR_013B_OWNED_BY;
		GetNameOfOwner(owner, new TileIndex(0) );
		return false;
	}

	static boolean CheckTileOwnership(TileIndex tile)
	{
		PlayerID owner = PlayerID.get( tile.GetTileOwner().id );

		assert(owner.id <= Owner.OWNER_WATER);

		if (owner.equals(PlayerID.getCurrent()))
			return true;
		Global._error_message = Str.STR_013B_OWNED_BY;

		// no need to get the name of the owner unless we're the local player (saves some time)
		if (IsLocalPlayer()) GetNameOfOwner(owner, tile);
		return false;
	}


	private boolean GenerateCompanyName_verify_name(StringID str, int strp)
	{
		// No player must have this name already
		Iterator<Player> ii = Player.getIterator();
		while(ii.hasNext())
		{
			Player pp = ii.next();
			if (pp.name_1 == str.id && pp.name_2 == strp)
				return false;
		}

		String buffer = Strings.GetString( str );
		if (buffer.length() >= 32 || Gfx.GetStringWidth(buffer) >= 150)
			return false;

		return true;
	}

	private void GenerateCompanyName_set_name(Player p, Town t, StringID str, int strp) {
		p.name_1 = str.id;
		p.name_2 = strp;

		Hal.MarkWholeScreenDirty();

		if (!p.index.IS_HUMAN_PLAYER()) {
			Global.SetDParam(0, t.index);
			NewsItem.AddNewsItem(p.index.id + (4 << 4), NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, NewsItem.NF_TILE, NewsItem.NT_COMPANY_INFO, NewsItem.DNC_BANKRUPCY), last_build_coordinate.tile, 0);
		}
	}

	private void GenerateCompanyName()
	{
		Player p = this;

		TileIndex tile;
		Town t;
		StringID str;
		//Player pp;
		int strp;
		//String buffer;

		if (name_1 != Str.STR_SV_UNNAMED)
			return;

		tile = last_build_coordinate;
		if (tile == null)
			return;

		t = Town.ClosestTownFromTile(tile, -1);

		if( t != null && BitOps.IS_INT_INSIDE(t.townnametype, Strings.SPECSTR_TOWNNAME_START, Strings.SPECSTR_TOWNNAME_LAST+1)) 
		{
			str = new StringID( t.townnametype - Strings.SPECSTR_TOWNNAME_START + Strings.SPECSTR_PLAYERNAME_START );
			strp = t.townnameparts;

			//verify_name:;

			if( GenerateCompanyName_verify_name(str, strp) )
			{
				GenerateCompanyName_set_name(p, t, str, strp);
				return;
			}
		}

		while(true)
		{
			if (president_name_1 == Strings.SPECSTR_PRESIDENT_NAME) {
				str = new StringID( Strings.SPECSTR_ANDCO_NAME );
				strp = president_name_2;
				GenerateCompanyName_set_name(p, t, str, strp);
				return;
			} else {
				str = new StringID( Strings.SPECSTR_ANDCO_NAME );
				strp = Hal.Random();
				if( GenerateCompanyName_verify_name(str, strp) )
				{
					GenerateCompanyName_set_name(p, t, str, strp);
					return;
				}
			}
		}

	}


	private static void COLOR_SWAP( byte colors[], int i, int j)  
	{ 
		byte t=colors[i];colors[i]=colors[j];colors[j]=t; 
	} 

	static final byte _color_sort[] = {2, 2, 3, 2, 3, 2, 3, 2, 3, 2, 2, 2, 3, 1, 1, 1};
	static final byte _color_similar_1[] = {8, 6, (byte)255, 12,  (byte)255, 0, 1, 1, 0, 13,  11,  10, 3,   9,  15, 14};
	static final byte _color_similar_2[] = {5, 7, (byte)255, (byte)255, (byte)255, 8, 7, 6, 5, 12, (byte)255, (byte)255, 9, (byte)255, (byte)255, (byte)255};

	private static byte GeneratePlayerColor()
	{
		byte [] colors = new byte [16];
		int pcolor, t2;
		int i,j,n;
		int r;
		//Player p;

		// Initialize array
		for(i=0; i!=16; i++)
			colors[i] = (byte) i;

		// And randomize it
		n = 100;
		do {
			r = Hal.Random();
			COLOR_SWAP( colors, BitOps.GB(r, 0, 4), BitOps.GB(r, 4, 4));
		} while (--n > 0);

		// Bubble sort it according to the values in table 1
		i = 16;
		do {
			for(j=0; j!=15; j++) {
				if (_color_sort[colors[j]] < _color_sort[colors[j+1]]) {
					COLOR_SWAP( colors, j,j+1);
				}
			}
		} while (--i > 0);

		// Move the colors that look similar to each player's color to the side
		//FOR_ALL_PLAYERS(p)
		for( Player p : Global.gs._players )
			if (p.is_active) {
				pcolor = p.player_color;
				for(i=0; i!=16; i++) if (colors[i] == pcolor) {
					colors[i] = (byte) 0xFF;

					t2 = 0xFF & _color_similar_1[pcolor];
					if (t2 == 0xFF) break;
					for(i=0; i!=15; i++) {
						if (colors[i] == t2) {
							do COLOR_SWAP( colors, i,i+1); while (++i != 15);
							break;
						}
					}

					t2 = 0xFF & _color_similar_2[pcolor];
					if (t2 == 0xFF) break;
					for(i=0; i!=15; i++) {
						if (colors[i] == t2) {
							do COLOR_SWAP( colors, i,i+1); while (++i != 15);
							break;
						}
					}
					break;
				}
			}

		// Return the first available color
		i = 0;
		for(;;) {
			if (colors[i] != 0xFF)
				return colors[i];
			i++;
		}
	}

	private void GeneratePresidentName() // Player p)
	{
		//Player pp;
		String buffer;

		for(;;) {
			//restart:;

			president_name_2 = Hal.Random();
			president_name_1 = Strings.SPECSTR_PRESIDENT_NAME;

			Global.SetDParam(0, president_name_2);
			buffer = Strings.GetString(president_name_1);
			if (buffer.length() >= 32 || Gfx.GetStringWidth(buffer) >= 94)
				continue;
			boolean restart = false;

			Iterator<Player> ii = Player.getIterator();
			while(ii.hasNext())
			{
				Player pp =  ii.next();
				if (pp.is_active && this != pp) {
					Global.SetDParam(0, pp.president_name_2);
					String buffer2 = Strings.GetString(pp.president_name_1);
					if(buffer2.equalsIgnoreCase(buffer))
					{
						restart = true;
						break;
					}
				}
			}

			if( restart ) continue;
			return;
		}
	}

	private static Player AllocatePlayer()
	{
		//Player p;
		// Find a free slot
		for( Player p : Global.gs._players ) {
			if (!p.is_active) {
				int i = p.index.id;
				//memset(p, 0, sizeof(Player));
				p.clear();
				p.index = PlayerID.get(i);
				return p;
			}
		}
		return null;
	}


	static Player DoStartupNewPlayer(boolean is_ai)
	{
		Player p;

		p = AllocatePlayer();
		if (p == null)
			return null;

		// Make a color
		p.player_color = GeneratePlayerColor();
		Global.gs._player_colors[p.index.id] = p.player_color;
		p.name_1 = Str.STR_SV_UNNAMED;
		p.is_active = true;

		p.money64 = /*p.player_money*/ p.current_loan = Integer.MAX_VALUE; // TODO return this 100000;

		p.is_ai = is_ai;
		// TODO p.ai.state = 5; /* AIS_WANT_NEW_ROUTE */
		p.share_owners[0] = p.share_owners[1] = p.share_owners[2] = p.share_owners[3] = 
				PlayerID.get( Owner.OWNER_SPECTATOR );

		p.avail_railtypes = GetPlayerRailtypes(p.index);
		p.inaugurated_year = Global.get_cur_year();
		p.face = Hal.Random();

		/* Engine renewal settings */
		p.InitialiseEngineReplacement();
		p.renew_keep_length = false;
		p.engine_renew = false;
		p.engine_renew_months = -6;
		p.engine_renew_money = 100000;

		p.GeneratePresidentName();

		Window.InvalidateWindow(Window.WC_GRAPH_LEGEND, 0);
		Window.InvalidateWindow(Window.WC_TOOLBAR_MENU, 0);
		Window.InvalidateWindow(Window.WC_CLIENT_LIST, 0);

		if (is_ai && (!Global._networking || Global._network_server) && Ai._ai.enabled)
			Ai.AI_StartNewAI(p.index);

		return p;
	}

	static void StartupPlayers()
	{
		// The AI starts like in the setting with +2 month max
		Global.gs._next_competitor_start = GameOptions._opt.diff.competitor_start_time * 90 * Global.DAY_TICKS + Hal.RandomRange(60 * Global.DAY_TICKS) + 1;
	}

	private static void MaybeStartNewPlayer()
	{
		int n;
		//Player p;

		// count number of competitors
		n = 0;
		for( Player p : Global.gs._players ) {
			if (p.isActive() && p.isAi())
				n++;
		}

		// when there's a lot of computers in game, the probability that a new one starts is lower
		if (n < GameOptions._opt.diff.max_no_competitors)
			if (n < (Global._network_server ? Hal.InteractiveRandomRange(GameOptions._opt.diff.max_no_competitors + 2) : Hal.RandomRange(GameOptions._opt.diff.max_no_competitors + 2)) )
				// Send a command to all clients to start  up a new AI. Works fine for Multiplayer and SinglePlayer 
				Cmd.DoCommandP(new TileIndex(0), 1, 0, null, Cmd.CMD_PLAYER_CTRL);

		// The next AI starts like the difficulty setting said, with +2 month max
		Global.gs._next_competitor_start = GameOptions._opt.diff.competitor_start_time * 90 * Global.DAY_TICKS + 1;
		Global.gs._next_competitor_start += Global._network_server ? Hal.InteractiveRandomRange(60 * Global.DAY_TICKS) : Hal.RandomRange(60 * Global.DAY_TICKS);
	}

	static void InitializePlayers()
	{
		int i;

		for(i = 0; i != Global.MAX_PLAYERS; i++)
		{
			Global.gs._players[i] = new Player();
			Global.gs._players[i].index=PlayerID.get(i);
		}
		Global.gs._cur_player_tick_index = 0;
	}

	static void OnTick_Players()
	{
		Player p;

		if (Global._game_mode == GameModes.GM_EDITOR)
			return;

		p = GetPlayer(Global.gs._cur_player_tick_index);
		Global.gs._cur_player_tick_index = (Global.gs._cur_player_tick_index + 1) % Global.MAX_PLAYERS;
		if (p.name_1 != 0) p.GenerateCompanyName();

		if (Ai.AI_AllowNewAI() && Global._game_mode != GameModes.GM_MENU && 0 == --Global.gs._next_competitor_start)
			MaybeStartNewPlayer();
	}

	// index is the next parameter in _decode_parameters to set up
	//static StringID GetPlayerNameString(PlayerID player, int index)
	public static int GetPlayerNameString(PlayerID player, int index)
	{
		if (player.IS_HUMAN_PLAYER() && player.id < Global.MAX_PLAYERS) {
			Global.SetDParam(index, player.id+1);
			return Str.STR_7002_PLAYER;
		}
		return Str.STR_EMPTY;
	}

	//extern void ShowPlayerFinances(int player);

	public static void PlayersYearlyLoop()
	{
		//Player p;

		// Copy statistics
		for( Player p : Global.gs._players ) 
		{
			if (p.is_active) 
			{
				//memmove(p.yearly_expenses[1], p.yearly_expenses[0], sizeof(p.yearly_expenses) - sizeof(p.yearly_expenses[0]));
				//memset(p.yearly_expenses[0], 0, sizeof(p.yearly_expenses[0]));

				System.arraycopy(
						p.yearly_expenses, 0, 
						p.yearly_expenses, 1, 
						p.yearly_expenses.length - 1);

				p.yearly_expenses[0] = new long[13]; // TODO 3? 

				Window.InvalidateWindow(Window.WC_FINANCES, p.index.id);
			}
		}

		if (Global._patches.show_finances.get() && Global.gs._local_player.id != Owner.OWNER_SPECTATOR) {
			PlayerGui.ShowPlayerFinances(Global.gs._local_player.id);
			Player p = GetPlayer(Global.gs._local_player);


			if (p.num_valid_stat_ent > 5 && p.old_economy[0].performance_history < p.old_economy[4].performance_history) {
				Sound.SndPlayFx(Snd.SND_01_BAD_YEAR);
			} else {
				Sound.SndPlayFx(Snd.SND_00_GOOD_YEAR);
			}
		}
	}

	static void DeletePlayerWindows(PlayerID pi)
	{
		Window.DeleteWindowById(Window.WC_COMPANY, pi.id);
		Window.DeleteWindowById(Window.WC_FINANCES, pi.id);
		Window.DeleteWindowById(Window.WC_STATION_LIST, pi.id);
		Window.DeleteWindowById(Window.WC_TRAINS_LIST,   (Station.INVALID_STATION << 16) | pi.id);
		Window.DeleteWindowById(Window.WC_ROADVEH_LIST,  (Station.INVALID_STATION << 16) | pi.id);
		Window.DeleteWindowById(Window.WC_SHIPS_LIST,    (Station.INVALID_STATION << 16) | pi.id);
		Window.DeleteWindowById(Window.WC_AIRCRAFT_LIST, (Station.INVALID_STATION << 16) | pi.id);
		Window.DeleteWindowById(Window.WC_BUY_COMPANY, pi.id);
	}

	static byte GetPlayerRailtypes(PlayerID p)
	{
		byte rt = 0;
		int i;

		for (i = 0; i != Global.TOTAL_NUM_ENGINES; i++) {
			final Engine e = Engine.GetEngine(i);

			//final RailVehicleInfo info = Engine.RailVehInfo(i);
			if (e.getType() == Vehicle.VEH_Train &&
					(e.isAvailableTo(p) || e.getIntro_date() <= Global.get_date()) &&
					!Engine.RailVehInfo(i).isWagon()) 
			{
				assert(e.getRailtype() < Rail.RAILTYPE_END);
				rt = BitOps.RETSETBIT(rt, e.getRailtype());
			}
		}

		return rt;
	}

	private static void DeletePlayerStuff(PlayerID pi)
	{
		Player p;

		DeletePlayerWindows(pi);
		p = GetPlayer(pi);
		Global.DeleteName(p.name_1);
		Global.DeleteName(p.president_name_1);
		p.name_1 = 0;
		p.president_name_1 = 0;
	}

	/** Change engine renewal parameters
	 * @param x,y unused
	 * @param p1 bits 0-3 command
	 * - p1 = 0 - change auto renew boolean
	 * - p1 = 1 - change auto renew months
	 * - p1 = 2 - change auto renew money
	 * - p1 = 3 - change auto renew array
	 * - p1 = 4 - change boolean, months & money all together
	 * - p1 = 5 - change renew_keep_length
	 * @param p2 value to set
	 * if p1 = 0, then:
	 * - p2 = enable engine renewal
	 * if p1 = 1, then:
	 * - p2 = months left before engine expires to replace it
	 * if p1 = 2, then
	 * - p2 = minimum amount of money available
	 * if p1 = 3, then:
	 * - p2 bits  0-15 = old engine type
	 * - p2 bits 16-31 = new engine type
	 * if p1 = 4, then:
	 * - p1 bit     15 = enable engine renewal
	 * - p1 bits 16-31 = months left before engine expires to replace it
	 * - p2 bits  0-31 = minimum amount of money available
	 * if p1 = 5, then
	 * - p2 = enable renew_keep_length
	 */
	static int CmdReplaceVehicle(int x, int y, int flags, int p1, int p2)
	{
		Player p;
		if (!(!PlayerID.getCurrent().isSpecial()))
			return Cmd.CMD_ERROR;

		p = GetCurrentPlayer(); // Player(Global.gs.getCurrentPlayer());
		switch (BitOps.GB(p1, 0, 3)) {
		case 0:
			if (p.engine_renew == ( 0 != BitOps.GB(p2, 0, 1)) )
				return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) {
				p.engine_renew = ( 0 != BitOps.GB(p2, 0, 1) );
				if (IsLocalPlayer()) {
					Global._patches.autorenew.set( p.engine_renew );
					Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
				}
			}
			break;
		case 1:
			if (p.engine_renew_months == p2)
				return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) {
				p.engine_renew_months = p2;
				if (IsLocalPlayer()) {
					Global._patches.autorenew_months = p.engine_renew_months;
					Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
				}
			}
			break;
		case 2:
			if (p.engine_renew_money == p2)
				return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) {
				p.engine_renew_money = p2;
				if (IsLocalPlayer()) {
					Global._patches.autorenew_money = p.engine_renew_money;
					Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
				}
			}
			break;
		case 3: {
			EngineID old_engine_type = EngineID.get( BitOps.GB(p2, 0, 16) );
			EngineID new_engine_type = EngineID.get( BitOps.GB(p2, 16, 16) );

			if (new_engine_type.id != Engine.INVALID_ENGINE) {
				/* First we make sure that it's a valid type the user requested
				 * check that it's an engine that is in the engine array */
				if(!Engine.IsEngineIndex(new_engine_type.id))
					return Cmd.CMD_ERROR;

				// check that the new vehicle type is the same as the original one
				if (Engine.GetEngine(old_engine_type).getType() != Engine.GetEngine(new_engine_type).getType())
					return Cmd.CMD_ERROR;

				// make sure that we do not replace a plane with a helicopter or vise versa
				if (Engine.GetEngine(new_engine_type).getType() == Vehicle.VEH_Aircraft 
						&& BitOps.HASBIT(Engine.AircraftVehInfo(old_engine_type.id).subtype, 0) != BitOps.HASBIT(Engine.AircraftVehInfo(new_engine_type.id).subtype, 0))
					return Cmd.CMD_ERROR;

				// make sure that the player can actually buy the new engine
				if (!Engine.GetEngine(new_engine_type).isAvailableToMe())
					return Cmd.CMD_ERROR;

				return p.AddEngineReplacement(old_engine_type, new_engine_type, flags);
			} else {
				return p.RemoveEngineReplacement(old_engine_type, flags);
			}
		}

		case 4:
			if(0 != (flags & Cmd.DC_EXEC)) {
				p.engine_renew = 0 != BitOps.GB(p1, 15, 1);
				p.engine_renew_months = BitOps.GB(p1, 16, 16);
				p.engine_renew_money = p2;

				if (IsLocalPlayer()) {
					Global._patches.autorenew.set( p.engine_renew );
					Global._patches.autorenew_months = p.engine_renew_months;
					Global._patches.autorenew_money = p.engine_renew_money;
					Window.InvalidateWindow(Window.WC_GAME_OPTIONS, 0);
				}
			}
			break;
		case 5:
			if (p.renew_keep_length == ( 0 != BitOps.GB(p2, 0, 1)))
				return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) {
				p.renew_keep_length = ( 0 != BitOps.GB(p2, 0, 1) );
				if (IsLocalPlayer()) {
					Window.InvalidateWindow(Window.WC_REPLACE_VEHICLE, Vehicle.VEH_Train);
				}
			}
			break;

		}
		return 0;
	}

	/** Control the players: add, delete, etc.
	 * @param x,y unused
	 * @param p1 various functionality
	 * - p1 = 0 - create a new player, Which player (network) it will be is in p2
	 * - p1 = 1 - create a new AI player
	 * - p1 = 2 - delete a player. Player is identified by p2
	 * - p1 = 3 - merge two companies together. Player to merge #1 with player #2. Identified by p2
	 * @param p2 various functionality, dictated by p1
	 * - p1 = 0 - ClientID of the newly created player
	 * - p1 = 2 - PlayerID of the that is getting deleted
	 * - p1 = 3 - #1 p2 = (bit  0-15) - player to merge (p2 & 0xFFFF)
	 *          - #2 p2 = (bit 16-31) - player to be merged into ((p2>>16)&0xFFFF)
	 *
	 * TODO In the case of p1=0, create new player, the clientID of the new player is in parameter
	 * p2. This parameter is passed in at function DEF_SERVER_RECEIVE_COMMAND(PACKET_CLIENT_COMMAND)
	 * on the server itself. First of all this is unbelievably ugly; second of all, well,
	 * it IS ugly! <b>Someone fix this up :)</b> So where to fix?@n
	 *
	 * arg - network_server.c:838 DEF_SERVER_RECEIVE_COMMAND(PACKET_CLIENT_COMMAND)@n
	 * arg - network_client.c:536 DEF_CLIENT_RECEIVE_COMMAND(PACKET_SERVER_MAP) from where the map has been received
	 */
	static int CmdPlayerCtrl(int x, int y, int flags, int p1, int p2)
	{
		if(0 != (flags & Cmd.DC_EXEC)) PlayerID.setCurrentToNone();

		switch (p1) {
		case 0: { // Create a new Player 
			Player p;
			//PlayerID pid = p2;
			int pid = p2;

			if (0 == (flags & Cmd.DC_EXEC) || pid >= Global.MAX_PLAYERS) return 0;

			p = DoStartupNewPlayer(false);


			if (Global._networking && !Global._network_server && Global.gs._local_player.isSpectator())
				// In case we are a client joining a server... 
				Window.DeleteWindowById(Window.WC_NETWORK_STATUS_WINDOW, 0);

			if (p != null) {
				if (Global.gs._local_player.isSpectator() && (!Ai._ai.network_client || Ai._ai.network_playas == Owner.OWNER_SPECTATOR)) {
					/* Check if we do not want to be a spectator in network */
					if (!Global._networking || (Global._network_server && !Global._network_dedicated) || Global._network_playas != Owner.OWNER_SPECTATOR || Ai._ai.network_client) {
						if (Ai._ai.network_client) {
							/* As ai-network-client, we have our own rulez (disable GUI and stuff) */
							Ai._ai.network_playas = (byte) p.index.id;
							Global.gs._local_player = PlayerID.get( Owner.OWNER_SPECTATOR );
							if (Ai._ai.network_playas != Owner.OWNER_SPECTATOR) {
								/* If we didn't join the game as a spectator, activate the AI */
								Ai.AI_StartNewAI(PlayerID.get( Ai._ai.network_playas ));
							}
						} else {
							Global.gs._local_player = p.index;
						}
						Hal.MarkWholeScreenDirty();
					}
				} else if (p.index.isLocalPlayer()) {
					Cmd.DoCommandP(TileIndex.get(0), ((Global._patches.autorenew.get() ? 1:0) << 15 ) | (Global._patches.autorenew_months << 16) | 4, (int)Global._patches.autorenew_money, null, Cmd.CMD_REPLACE_VEHICLE);
				}

				if (Global._network_server) {
					// * XXX - UGLY! p2 (pid) is mis-used to fetch the client-id, done at server-side
					//  * in network_server.c:838, function DEF_SERVER_RECEIVE_COMMAND(PACKET_CLIENT_COMMAND) 
					//NetworkClientInfo ci = _network_client_info[pid];
					NetworkClientInfo ci = Net.getClient(pid).getCi();
					ci.client_playas = p.index.id + 1;
					try {
						NetServer.NetworkUpdateClientInfo(ci.client_index);
					} catch (IOException e) {
						Global.error(e);
					}

					if (ci.client_playas != 0 && ci.client_playas <= Global.MAX_PLAYERS) {
						PlayerID player_backup = Global.gs._local_player;
						Net._network_player_info[p.index.id].months_empty = 0;

						// XXX - When a client joins, we automatically set its name to the
						// * player's name (for some reason). As it stands now only the server
						// * knows the client's name, so it needs to send out a "broadcast" to
						// * do this. To achieve this we send a network command. However, it
						// * uses _local_player to execute the command as.  To prevent abuse
						// * (eg. only yourself can change your name/company), we 'cheat' by
						// * impersonation _local_player as the server. Not the best solution;
						// * but it works.
						// * TODO: Perhaps this could be improved by when the client is ready
						// * with joining to let it send itself the command, and not the server?
						// * For example in network_client.c:534? 
						Global._cmd_text = ci.client_name;
						Global.gs._local_player = PlayerID.get( ci.client_playas - 1 );
						Net.NetworkSend_Command(null, 0, 0, Cmd.CMD_CHANGE_PRESIDENT_NAME, null);
						Global.gs._local_player = player_backup;
					}
				}
			} else if (Global._network_server) {
				// * XXX - UGLY! p2 (pid) is mis-used to fetch the client-id, done at server-side
				// * in network_server.c:838, function DEF_SERVER_RECEIVE_COMMAND(PACKET_CLIENT_COMMAND) 
				//NetworkClientInfo ci = Net._network_client_info[pid];
				//Net._clients.get(pid).ci
				NetworkClientInfo ci = Net.getClient(pid).getCi();
				ci.client_playas = Owner.OWNER_SPECTATOR;
				try {
					NetServer.NetworkUpdateClientInfo(ci.client_index);
				} catch (IOException e) {
					Global.error(e);
				}
			}
		} break;

		case 1: /* Make a new AI Player */
			if (0 == (flags & Cmd.DC_EXEC)) return 0;

			DoStartupNewPlayer(true);
			break;

		case 2: { /* Delete a Player */
			Player p;

			if (p2 >= Global.MAX_PLAYERS) return Cmd.CMD_ERROR;

			if (0 == (flags & Cmd.DC_EXEC)) return 0;

			p = GetPlayer(p2);

			/* Only allow removal of HUMAN companies */
			if (p.index.IS_HUMAN_PLAYER()) {
				/* Delete any open window of the company */
				DeletePlayerWindows(p.index);

				/* Show the bankrupt news */
				Global.SetDParam(0, p.name_1);
				Global.SetDParam(1, p.name_2);
				NewsItem.AddNewsItem( new StringID(p.index.id + 16*3), NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_COMPANY_INFO, NewsItem.DNC_BANKRUPCY),0,0);

				/* Remove the company */
				Economy.ChangeOwnershipOfPlayerItems(p.index, PlayerID.get(Owner.OWNER_SPECTATOR));
				p.money64 = 100000000; // XXX - wtf?
				p.is_active = false;
			}
		} break;

		case 3: { /* Merge a company (#1) into another company (#2), elimination company #1 */
			PlayerID pid_old = PlayerID.get( BitOps.GB(p2,  0, 16) );
			PlayerID pid_new = PlayerID.get( BitOps.GB(p2, 16, 16) );

			if (pid_old.id >= Global.MAX_PLAYERS || pid_new.id >= Global.MAX_PLAYERS) return Cmd.CMD_ERROR;

			if (0 == (flags & Cmd.DC_EXEC)) return Cmd.CMD_ERROR;

			Economy.ChangeOwnershipOfPlayerItems(pid_old, pid_new);
			DeletePlayerStuff(pid_old);
		} break;
		default: return Cmd.CMD_ERROR;
		}

		return 0;
	}

	//static final StringID _endgame_perf_titles[] = {
	static final int _endgame_perf_titles[] = {
			Str.STR_0213_BUSINESSMAN,
			Str.STR_0213_BUSINESSMAN,
			Str.STR_0213_BUSINESSMAN,
			Str.STR_0213_BUSINESSMAN,
			Str.STR_0213_BUSINESSMAN,
			Str.STR_0214_ENTREPRENEUR,
			Str.STR_0214_ENTREPRENEUR,
			Str.STR_0215_INDUSTRIALIST,
			Str.STR_0215_INDUSTRIALIST,
			Str.STR_0216_CAPITALIST,
			Str.STR_0216_CAPITALIST,
			Str.STR_0217_MAGNATE,
			Str.STR_0217_MAGNATE,
			Str.STR_0218_MOGUL,
			Str.STR_0218_MOGUL,
			Str.STR_0219_TYCOON_OF_THE_CENTURY,
	};


	//static StringID EndGameGetPerformanceTitleFromValue(int value)
	public static int EndGameGetPerformanceTitleFromValue(int value)
	{

		long lvalue = BitOps.minu(value, 1000) >>> 6;
					if (lvalue >= _endgame_perf_titles.length) 
						lvalue = _endgame_perf_titles.length - 1L;

					return _endgame_perf_titles[(int) lvalue];
	}


	// Save the highscore for the Player 
	public static int SaveHighScoreValue(final Player p)
	{

		HighScore[] hs = Global._highscore_table[GameOptions._opt.diff_level];
		int i;
		int score = p.old_economy[0].performance_history;

		// Exclude cheaters from the honour of being in the highscore table 
		if (Cheat.CheatHasBeenUsed())
			return -1;

		for (i = 0; i < hs.length; i++) 
		{
			// You are in the TOP5. Move all values one down and save us there 
			if (hs[i].score <= score) {
				//char buf[sizeof(hs[i].company)];

				// move all elements one down starting from the replaced one
				//memmove(&hs[i + 1], &hs[i], sizeof(HighScore) * (lengthof(_highscore_table[0]) - i - 1));
				System.arraycopy(hs, i, hs, i+1, hs.length - i - 1);

				/*
				Global.SetDParam(0, p.president_name_1);
				Global.SetDParam(1, p.president_name_2);
				Global.SetDParam(2, p.name_1);
				Global.SetDParam(3, p.name_2);
				String buf = Strings.GetString(Str.STR_HIGHSCORE_NAME); // get manager/company name string
				hs[i].company = buf;
				hs[i].score = score;
				hs[i].title = Strings.GetString(EndGameGetPerformanceTitleFromValue(score));
				 */
				//hs[i].score = score;
				hs[i].initFromPlayer(p,score);				


				return i;
			}
		}

		return -1; // too bad; we did not make it into the top5
	}


	//* Save the highscores in a network game when it has ended 
	//#define LAST_HS_ITEM lengthof(_highscore_table) - 1
	private static final int LAST_HS_ITEM = Global._highscore_table.length - 1;
	public static int SaveHighScoreValueNetwork()
	{
		//Player [] player_sort = new Player[Global.MAX_PLAYERS];
		//int count = 0;
		int player = -1;

		List<Player> player_sort = new ArrayList<>();

		/* Sort all active players with the highest score first 
		for( Player p : _players ) {
			if (p.isActive())
				player_sort[count++] = p;
		}*/

		Player.forEach( p -> { if (p.isActive()) player_sort.add(p); } );
		Collections.sort(player_sort, new PlayerHiScoreComparator());

		//* Copy over Top5 companies 
		//for (int i = 0; i < Global._highscore_table[LAST_HS_ITEM].length && i < count; i++) {
		//	Player p_cur = player_sort.get(i);
		int i = 0;
		for(Player p : player_sort)
		{
			HighScore hs = new HighScore();
			hs.initFromPlayer(p,p.old_economy[0].performance_history);

			Global._highscore_table[LAST_HS_ITEM][i] = hs;
			// get the ranking of the local player
			if (p.index.isLocalPlayer())
				player = i;

			i++;
			if( i >= Global._highscore_table[LAST_HS_ITEM].length )
				break;
		}

		// Add top5 players to highscore table 
		return player;
	}
	/* */




	public void InitialiseEngineReplacement()
	{
		//EngineID engine;
		int engine;

		for (engine = 0; engine < Global.TOTAL_NUM_ENGINES; engine++)
			engine_replacement[engine] = Engine.INVALID_ENGINE;
	}

	/**
	 * Retrieve the engine replacement for the given player and original engine type.
	 * @param p Player.
	 * @param engine Engine type.
	 * @return Assigned replacement engine.
	 */
	public EngineID EngineReplacement(EngineID engine)
	{
		return EngineID.get(engine_replacement[engine.id]);
	}

	/**
	 * Check if an engine has a replacement set up.
	 * @param p Player.
	 * @param engine Engine type.
	 * @return True if there is a replacement for the original engine type.
	 */
	public boolean EngineHasReplacement(EngineID engine)
	{
		return EngineReplacement(engine).id != Engine.INVALID_ENGINE;
	}

	/**
	 * Add an engine replacement for the player.
	 * @param p Player.
	 * @param old_engine The original engine type.
	 * @param new_engine The replacement engine type.
	 * @param flags The calling command flags.
	 * @return 0 on success, CMD_ERROR on failure.
	 */
	public int AddEngineReplacement(EngineID old_engine, EngineID new_engine, int flags)
	{
		if(0 != (flags & Cmd.DC_EXEC)) 
			engine_replacement[old_engine.id] = new_engine.id;
		return 0;
	}

	/**
	 * Remove an engine replacement for the player.
	 * @param p Player.
	 * @param engine The original engine type.
	 * @param flags The calling command flags.
	 * @return 0 on success, CMD_ERROR on failure.
	 */
	public int RemoveEngineReplacement(EngineID engine, int flags)
	{
		if(0 != (flags & Cmd.DC_EXEC)) engine_replacement[engine.id] = Engine.INVALID_ENGINE;
		return 0;
	}


	public static Iterator<Player> getIterator()
	{
		List<Player> list = Arrays.asList(Global.gs._players);
		return list.iterator();
	}

	public static void forEach( Consumer<Player> p )
	{
		Iterator<Player> i = getIterator();
		while(i.hasNext())
			p.accept(i.next());
	}

	/* Validate functions for rail building */
	static boolean ValParamRailtype(int rail) 
	{ 
		return BitOps.HASBIT(GetPlayer(PlayerID.getCurrent()).avail_railtypes, rail);
	}


	/** Finds out if a Player has a certain railtype available
	 */
	public boolean HasRailtypeAvail(int railtype) 
	{
		return BitOps.HASBIT(avail_railtypes, railtype);
	}


	/*
// Save/load of players
static final SaveLoad _player_desc[] = {
		SLE_VAR(Player,name_2,					SLE_UINT32),
		SLE_VAR(Player,name_1,					SLE_STRINGID),

		SLE_VAR(Player,president_name_1,SLE_UINT16),
		SLE_VAR(Player,president_name_2,SLE_UINT32),

		SLE_VAR(Player,face,						SLE_UINT32),

		// money was changed to a 64 bit field in savegame version 1.
		SLE_CONDVAR(Player,money64,			SLE_VAR_I64 | SLE_FILE_I32, 0, 0),
		SLE_CONDVAR(Player,money64,			SLE_INT64, 1, 255),

		SLE_VAR(Player,current_loan,		SLE_INT32),

		SLE_VAR(Player,player_color,		SLE_Ubyte),
		SLE_VAR(Player,player_money_fraction,SLE_Ubyte),
		SLE_VAR(Player,avail_railtypes,		SLE_Ubyte),
		SLE_VAR(Player,block_preview,		SLE_Ubyte),

		SLE_VAR(Player,cargo_types,			SLE_UINT16),
		SLE_CONDVAR(Player, location_of_house,     SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Player, location_of_house,     SLE_UINT32, 6, 255),
		SLE_CONDVAR(Player, last_build_coordinate, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(Player, last_build_coordinate, SLE_UINT32, 6, 255),
		SLE_VAR(Player,inaugurated_year,SLE_Ubyte),

		SLE_ARR(Player,share_owners,		SLE_Ubyte, 4),

		SLE_VAR(Player,num_valid_stat_ent,SLE_Ubyte),

		SLE_VAR(Player,quarters_of_bankrupcy,SLE_Ubyte),
		SLE_VAR(Player,bankrupt_asked,	SLE_Ubyte),
		SLE_VAR(Player,bankrupt_timeout,SLE_INT16),
		SLE_VAR(Player,bankrupt_value,	SLE_INT32),

		// yearly expenses was changed to 64-bit in savegame version 2.
		SLE_CONDARR(Player,yearly_expenses,	SLE_FILE_I32|SLE_VAR_I64, 3*13, 0, 1),
		SLE_CONDARR(Player,yearly_expenses,	SLE_INT64, 3*13, 2, 255),

		SLE_CONDVAR(Player,is_ai,			SLE_Ubyte, 2, 255),
		SLE_CONDVAR(Player,is_active,	SLE_Ubyte, 4, 255),

		// Engine renewal settings
		SLE_CONDARR(Player,engine_replacement,  SLE_UINT16, 256, 16, 255),
		SLE_CONDVAR(Player,engine_renew,         SLE_Ubyte,      16, 255),
		SLE_CONDVAR(Player,engine_renew_months,  SLE_INT16,      16, 255),
		SLE_CONDVAR(Player,engine_renew_money,  SLE_UINT32,      16, 255),
		SLE_CONDVAR(Player,renew_keep_length,    SLE_Ubyte,       2, 255),	// added with 16.1, but was blank since 2

		// reserve extra space in savegame here. (currently 63 bytes)
		SLE_CONDARR(NullStruct,null,SLE_FILE_U8  | SLE_VAR_NULL, 7, 2, 255),
		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_NULL, 7, 2, 255),

		SLE_END()
};

static final SaveLoad _player_economy_desc[] = {
		// these were changed to 64-bit in savegame format 2
		SLE_CONDVAR(PlayerEconomyEntry,income,							SLE_INT32, 0, 1),
		SLE_CONDVAR(PlayerEconomyEntry,expenses,						SLE_INT32, 0, 1),
		SLE_CONDVAR(PlayerEconomyEntry,company_value, SLE_FILE_I32 | SLE_VAR_I64, 0, 1),
		SLE_CONDVAR(PlayerEconomyEntry,income,	SLE_FILE_I64 | SLE_VAR_I32, 2, 255),
		SLE_CONDVAR(PlayerEconomyEntry,expenses,SLE_FILE_I64 | SLE_VAR_I32, 2, 255),
		SLE_CONDVAR(PlayerEconomyEntry,company_value, SLE_INT64, 2, 255),

		SLE_VAR(PlayerEconomyEntry,delivered_cargo,			SLE_INT32),
		SLE_VAR(PlayerEconomyEntry,performance_history,	SLE_INT32),

		SLE_END()
};

static final SaveLoad _player_ai_desc[] = {
		SLE_VAR(PlayerAI,state,							SLE_Ubyte),
		SLE_VAR(PlayerAI,tick,							SLE_Ubyte),
		SLE_CONDVAR(PlayerAI,state_counter, SLE_FILE_U16 | SLE_VAR_U32, 0, 12),
		SLE_CONDVAR(PlayerAI,state_counter, SLE_UINT32, 13, 255),
		SLE_VAR(PlayerAI,timeout_counter,		SLE_UINT16),

		SLE_VAR(PlayerAI,state_mode,				SLE_Ubyte),
		SLE_VAR(PlayerAI,banned_tile_count,	SLE_Ubyte),
		SLE_VAR(PlayerAI,railtype_to_use,		SLE_Ubyte),

		SLE_VAR(PlayerAI,cargo_type,				SLE_Ubyte),
		SLE_VAR(PlayerAI,num_wagons,				SLE_Ubyte),
		SLE_VAR(PlayerAI,build_kind,				SLE_Ubyte),
		SLE_VAR(PlayerAI,num_build_rec,			SLE_Ubyte),
		SLE_VAR(PlayerAI,num_loco_to_build,	SLE_Ubyte),
		SLE_VAR(PlayerAI,num_want_fullload,	SLE_Ubyte),

		SLE_VAR(PlayerAI,route_type_mask,		SLE_Ubyte),

		SLE_CONDVAR(PlayerAI, start_tile_a, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(PlayerAI, start_tile_a, SLE_UINT32, 6, 255),
		SLE_CONDVAR(PlayerAI, cur_tile_a,   SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(PlayerAI, cur_tile_a,   SLE_UINT32, 6, 255),
		SLE_VAR(PlayerAI,start_dir_a,				SLE_Ubyte),
		SLE_VAR(PlayerAI,cur_dir_a,					SLE_Ubyte),

		SLE_CONDVAR(PlayerAI, start_tile_b, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(PlayerAI, start_tile_b, SLE_UINT32, 6, 255),
		SLE_CONDVAR(PlayerAI, cur_tile_b,   SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(PlayerAI, cur_tile_b,   SLE_UINT32, 6, 255),
		SLE_VAR(PlayerAI,start_dir_b,				SLE_Ubyte),
		SLE_VAR(PlayerAI,cur_dir_b,					SLE_Ubyte),

		SLE_REF(PlayerAI,cur_veh,						REF_VEHICLE),

		SLE_ARR(PlayerAI,wagon_list,				SLE_UINT16, 9),
		SLE_ARR(PlayerAI,order_list_blocks,	SLE_Ubyte, 20),
		SLE_ARR(PlayerAI,banned_tiles,			SLE_UINT16, 16),

		SLE_CONDARR(NullStruct,null,SLE_FILE_U64 | SLE_VAR_NULL, 8, 2, 255),
		SLE_END()
};

static final SaveLoad _player_ai_build_rec_desc[] = {
		SLE_CONDVAR(AiBuildRec,spec_tile, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(AiBuildRec,spec_tile, SLE_UINT32, 6, 255),
		SLE_CONDVAR(AiBuildRec,use_tile,  SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
		SLE_CONDVAR(AiBuildRec,use_tile,  SLE_UINT32, 6, 255),
		SLE_VAR(AiBuildRec,rand_rng,			SLE_Ubyte),
		SLE_VAR(AiBuildRec,cur_building_rule,SLE_Ubyte),
		SLE_VAR(AiBuildRec,unk6,					SLE_Ubyte),
		SLE_VAR(AiBuildRec,unk7,					SLE_Ubyte),
		SLE_VAR(AiBuildRec,buildcmd_a,		SLE_Ubyte),
		SLE_VAR(AiBuildRec,buildcmd_b,		SLE_Ubyte),
		SLE_VAR(AiBuildRec,direction,			SLE_Ubyte),
		SLE_VAR(AiBuildRec,cargo,					SLE_Ubyte),
		SLE_END()
};
	 */

	/*
static void SaveLoad_PLYR(Player p) {
	int i;

	SlObject(p, _player_desc);

	// Write AI?
	if (!p.index.IS_HUMAN_PLAYER()) {
		SlObject(&p.ai, _player_ai_desc);
		for(i=0; i!=p.ai.num_build_rec; i++)
			SlObject(&p.ai.src + i, _player_ai_build_rec_desc);
	}

	// Write economy
	SlObject(&p.cur_economy, _player_economy_desc);

	// Write old economy entries.
	{
		PlayerEconomyEntry *pe;
		for(i=p.num_valid_stat_ent,pe=p.old_economy; i!=0; i--,pe++)
			SlObject(pe, _player_economy_desc);
	}
}

static void Save_PLYR()
{
	//Player p;
	for( Player p : _players ) {
		if (p.is_active) {
			SlSetArrayIndex(p.index);
			SlAutolength((AutolengthProc*)SaveLoad_PLYR, p);
		}
	}
}

static void Load_PLYR()
{
	int index;
	while ((index = SlIterateArray()) != -1) {
		Player p = GetPlayer(index);
		SaveLoad_PLYR(p);
		_player_colors[index] = p.player_color;
		UpdatePlayerMoney32(p);

		// This is needed so an AI is attached to a loaded AI 
		if (p.is_ai && (!_networking || _network_server) && _ai.enabled)
			AI_StartNewAI(p.index);
	}
}

final Chunk Handler _player_chunk_handlers[] = {
		{ 'PLYR', Save_PLYR, Load_PLYR, CH_ARRAY | CH_LAST},
};
	 */

	public static void loadGame(ObjectInputStream oin) {
		//Global.gs._players = (Player[]) oin.readObject();
		for(Player p : Global.gs._players)
		{
			Global.gs._player_colors[p.index.id] = p.player_color;
			//p.UpdatePlayerMoney32();

			// This is needed so an AI is attached to a loaded AI 
			if (p.isAi() && (!Global._networking || Global._network_server) && Ai._ai.enabled)
				Ai.AI_StartNewAI(p.index);
		}
	}

	public static void saveGame(ObjectOutputStream oos) {
		//oos.writeObject(Global.gs._players);		
	}

	public String generateFileName() {
		Global.SetDParam(0, name_1);
		Global.SetDParam(1, name_2);
		Global.SetDParam(2, Global.get_date());
		return Strings.GetString(Str.STR_4004);
	}

	public void DrawPlayerFace() {
		DrawPlayerFace(face, player_color, 2, 16);		
	}

	public int getColor() { return player_color; }

	public int getName_1() { return name_1; }
	public int getName_2() { return name_2; }

	public int getPresident_name_1() {		return president_name_1;	}
	public int getPresident_name_2() {		return president_name_2;	}

	public int getCurrent_loan() {		return current_loan;	}
	public TileIndex getLocation_of_house() {		return location_of_house;	}
	public int getInaugurated_year() {		return inaugurated_year;	}

	public int getBankrupt_value() {		return bankrupt_value;	}
	public long [][] getYearly_expenses() {		return yearly_expenses;	}
	public boolean isRenew_keep_length() {		return renew_keep_length;	}

	public void setMoney(long m) { money64 = m; }

	public boolean isEngine_renew() {
		return engine_renew;
	}

	public int getEngine_renew_months() {
		return engine_renew_months;
	}

	public long getEngine_renew_money() {
		return engine_renew_money;
	}


	/* Sort all players given their performance * /
	static int  HighScoreSorter(final void *a, final void *b)
	{
		final Player pa = *(final Player* final*)a;
		final Player pb = *(final Player* final*)b;

		return pb.old_economy[0].performance_history - pa.old_economy[0].performance_history;
	} */

	public static class PlayerHiScoreComparator implements Comparator<Player> {

		@Override
		public int compare(Player pa, Player pb) {
			return pb.old_economy[0].performance_history - pa.old_economy[0].performance_history;
		}


	}


}



