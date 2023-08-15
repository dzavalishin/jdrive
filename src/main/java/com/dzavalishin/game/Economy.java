package com.dzavalishin.game;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import com.dzavalishin.ai.Ai;
import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StationID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.ids.UnitID;
import com.dzavalishin.ids.VehicleID;
import com.dzavalishin.net.Net;
import com.dzavalishin.net.NetServer;
import com.dzavalishin.net.NetworkClientInfo;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.struct.FoundRoute;
import com.dzavalishin.struct.GoodsEntry;
import com.dzavalishin.struct.Pair;
import com.dzavalishin.struct.PlayerEconomyEntry;
import com.dzavalishin.tables.EconomeTables;
import com.dzavalishin.tables.Snd;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.util.Prices;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.MiscGui;
import com.dzavalishin.xui.PlayerGui;
import com.dzavalishin.xui.VehicleGui;
import com.dzavalishin.xui.Window;

public class Economy extends EconomeTables implements Serializable
{
	private static final long serialVersionUID = 2L;
	
	// Maximum possible loan
	private int max_loan;
	private double max_loan_unround;
	// Economy fluctuation status
	private int fluct;
	// Interest
	private int interest_rate;
	private int infl_amount;
	private int infl_amount_pr;




	public static final long[][] _score_part = new long [Global.MAX_PLAYERS][NUM_SCORE];


	//int _score_part[MAX_PLAYERS][NUM_SCORE];

	static void UpdatePlayerHouse(Player p, int score)
	{
		int val;
		TileIndex tile = p.location_of_house;

		if (tile == null)
			return;

		val = 128;
		if( score >= 170)
		{
			val+= 4;
			if( score >= 350)
			{
				val+= 4;
				if( score >= 520)
				{
					val+= 4;
					if( score > 720)
						val+= 4;
				}
			}
		}

		/* house is already big enough */
		if (val <= tile.getMap().m5)
			return;

		tile.iadd(0, 0).getMap().m5 =   val;
		tile.iadd(0, 1).getMap().m5 = ++val;
		tile.iadd(1, 0).getMap().m5 = ++val;
		tile.iadd(1, 1).getMap().m5 = ++val;

		tile.iadd(0, 0).MarkTileDirtyByTile();
		tile.iadd(0, 1).MarkTileDirtyByTile();
		tile.iadd(1, 0).MarkTileDirtyByTile();
		tile.iadd(1, 1).MarkTileDirtyByTile();
	}

	public static long CalculateCompanyValue(final Player p)
	{
		PlayerID owner = p.index;
		long value;

		{
			int [] num = {0};

			Station.forEach( st ->
			{
				if( st.isValid() && st.owner.equals(owner) ) {
					int facil = st.facilities;
					do num[0] += (facil&1); while ((facil >>= 1) > 0);
				}
			});

			value = (long) (num[0] * Global._price.station_value * 25L);
		}

		{
			Iterator<Vehicle> ii = Vehicle.getIterator();
			while(ii.hasNext())
			{
				Vehicle v = ii.next();
				if (!v.owner.equals(owner))
					continue;
				if (v.type == Vehicle.VEH_Train ||
						v.type == Vehicle.VEH_Road ||
						(v.type == Vehicle.VEH_Aircraft && v.subtype<=2) ||
						v.type == Vehicle.VEH_Ship) {
					value += v.value * 3L >> 1;
				}
			}
		}

		value += p.money64 - p.current_loan; // add real money value

		return Long.max(value, 1);
	}

	// if update is set to true, the economy is updated with this score
	//  (also the house is updated, should only be true in the on-tick event)
	public static int UpdateCompanyRatingAndValue(Player p, boolean update)
	{
		int owner = p.index.id;
		int score = 0;

		//memset(_score_part[owner], 0, sizeof(_score_part[owner]));
		_score_part[owner] = new long[NUM_SCORE];

		/* Count vehicles */
		{
			int min_profit = _score_info[SCORE_MIN_PROFIT].needed;
			int num = 0;

			Iterator<Vehicle> ii = Vehicle.getIterator();
			while(ii.hasNext())
			{
				Vehicle v = ii.next();
				if (v.owner.id != owner)
					continue;
				if ((v.type == Vehicle.VEH_Train && v.IsFrontEngine()) ||
						v.type == Vehicle.VEH_Road ||
						(v.type == Vehicle.VEH_Aircraft && v.subtype<=2) ||
						v.type == Vehicle.VEH_Ship) {
					num++;
					if (v.age > 730) {
						if (min_profit > v.profit_last_year)
							min_profit = v.profit_last_year;
					}
				}
			}

			_score_part[owner][SCORE_VEHICLES] = num;
			if (min_profit > 0)
				_score_part[owner][SCORE_MIN_PROFIT] = min_profit;
		}

		/* Count stations */
		{
			int [] num = { 0 };

			Station.forEach( st ->
			{
				if (st.getXy() != null && st.owner.id == owner) {
					int facil = st.facilities;
					do { num[0] += facil&1; } while (0 != (facil>>=1) );
				}
			});
			_score_part[owner][SCORE_STATIONS] = num[0];
		}

		/* Generate statistics depending on recent income statistics */
		{
			PlayerEconomyEntry pee;
			int numec;
			int min_income;
			int max_income;

			numec = Math.min(p.num_valid_stat_ent, 12);
			if (numec != 0) {
				min_income = 0x7FFFFFFF;
				max_income = 0;
				//pee = p.old_economy;
				int peei = 0;
				do {
					pee  = p.old_economy[peei++];
					min_income = Integer.min(min_income, pee.income + pee.expenses);
					max_income = Integer.max(max_income, pee.income + pee.expenses);
					//++pee;
				} while (--numec > 0);

				if (min_income > 0)
					_score_part[owner][SCORE_MIN_INCOME] = min_income;

				_score_part[owner][SCORE_MAX_INCOME] = max_income;
			}
		}

		/* Generate score depending on amount of transported cargo */
		{
			PlayerEconomyEntry pee;
			int numec;
			int total_delivered;

			numec = Math.min(p.num_valid_stat_ent, 4);
			if (numec != 0) {
				//pee = p.old_economy;
				int peei = 0;
				total_delivered = 0;
				do {
					pee  = p.old_economy[peei++];
					total_delivered += pee.delivered_cargo;
					//++pee;
				} while (--numec > 0);

				_score_part[owner][SCORE_DELIVERED] = total_delivered;
			}
		}

		/* Generate score for variety of cargo */
		{
			int cargo = p.cargo_types;
			int num = 0;
			do num += cargo&1; while ((cargo>>=1) != 0);
			_score_part[owner][SCORE_CARGO] = num;
			if (update)
				p.cargo_types = 0;
		}

		/* Generate score for player money */
		{
			long money = p.getMoney();
			if (money > 0) {
				_score_part[owner][SCORE_MONEY] = money;
			}
		}

		/* Generate score for loan */
		{
			_score_part[owner][SCORE_LOAN] = _score_info[SCORE_LOAN].needed - (long)p.current_loan;
		}

		// Now we calculate the score for each item..
		{
			int i;
			int total_score = 0;
			long s;
			score = 0;
			for (i=0;i<NUM_SCORE;i++) {
				// Skip the total
				if (i == SCORE_TOTAL) continue;
				// Check the score
				s = (_score_part[owner][i] >= _score_info[i].needed) ?
						_score_info[i].score :
							((_score_part[owner][i] * _score_info[i].score) / _score_info[i].needed);
				if (s < 0) s = 0;
				score += s;
				total_score += _score_info[i].score;
			}

			_score_part[owner][SCORE_TOTAL] = score;

			// We always want the score scaled to SCORE_MAX (1000)
			if (total_score != SCORE_MAX)
				score = score * SCORE_MAX / total_score;
		}

		if (update) {
			p.old_economy[0].performance_history = score;
			UpdatePlayerHouse(p, score);
			p.old_economy[0].company_value = CalculateCompanyValue(p);
		}

		Window.InvalidateWindow(Window.WC_PERFORMANCE_DETAIL, 0);
		return score;
	}

	// use Owner.OWNER_SPECTATOR as new_player to delete the player.
	static void ChangeOwnershipOfPlayerItems(PlayerID old_player, PlayerID new_player)
	{
		PlayerID old = PlayerID.getCurrent();
		PlayerID.setCurrent(old_player);

		if (new_player.isSpectator()) {

			for (int i = 0; i < Subsidy._subsidies.length; i++) 
			{
				Subsidy s = Subsidy._subsidies[i];

				if (s.isValid() && s.age >= 12) {
					if (Station.GetStation(s.to).owner.equals(old_player))
						s.markInvalid();
				}
			}
		}

		/* Take care of rating in towns */
		{
			if (!new_player.isSpectator()) {

				Town.forEach( (t) ->
				{
					/* If a player takes over, give the ratings to that player. */
					if (t.IsValidTown() && BitOps.HASBIT(t.have_ratings, old_player.id)) {
						if (BitOps.HASBIT(t.have_ratings, new_player.id)) {
							// use max of the two ratings.
							t.ratings[new_player.id] = Math.max(t.ratings[new_player.id], t.ratings[old_player.id]);
						} else {
							t.have_ratings = BitOps.RETSETBIT(t.have_ratings, new_player.id);
							t.ratings[new_player.id] = t.ratings[old_player.id];
						}
					}

					/* Reset ratings for the town */
					if (t.IsValidTown()) {
						t.ratings[old_player.id] = 500;
						t.have_ratings = BitOps.RETCLRBIT(t.have_ratings, old_player.id);
					}
				});
			}
		}

		{
			int num_train = 0;
			int num_road = 0;
			int num_ship = 0;
			int num_aircraft = 0;

			// Determine Ids for the new vehicles
			Iterator<Vehicle> ii = Vehicle.getIterator();
			while(ii.hasNext())
			{
				Vehicle v = ii.next();
				if (v.owner.equals(new_player)) {
					switch (v.type) {
					case Vehicle.VEH_Train:
						if (v.IsFrontEngine()) num_train++;
						break;
					case Vehicle.VEH_Road:
						num_road++;
						break;
					case Vehicle.VEH_Ship:
						num_ship++;
						break;
					case Vehicle.VEH_Aircraft:
						if (v.subtype <= 2) num_aircraft++;
						break;
					default: break;
					}
				}
			}

			Iterator<Vehicle> vii = Vehicle.getIterator();
			while(vii.hasNext())
			{
				Vehicle v = vii.next();
				if (v.owner.equals(old_player) && BitOps.IS_INT_INSIDE(v.type, Vehicle.VEH_Train, Vehicle.VEH_Aircraft+1) ) 
				{
					if (new_player.isSpectator()) {
						Window.DeleteWindowById(Window.WC_VEHICLE_VIEW, v.index);
						Window.DeleteWindowById(Window.WC_VEHICLE_DETAILS, v.index);
						Window.DeleteWindowById(Window.WC_VEHICLE_ORDERS, v.index);
						Vehicle.DeleteVehicle(v);
					} else {
						v.owner = new_player;
						if (v.type == Vehicle.VEH_Train && v.IsFrontEngine())
							v.unitnumber = UnitID.get( ++num_train );
						else if (v.type == Vehicle.VEH_Road)
							v.unitnumber = UnitID.get( ++num_road );
						else if (v.type == Vehicle.VEH_Ship)
							v.unitnumber = UnitID.get( ++num_ship );
						else if (v.type == Vehicle.VEH_Aircraft && v.subtype <= 2)
							v.unitnumber = UnitID.get( ++num_aircraft );
					}
				}
			}
		}

		// Change ownership of tiles
		{
			MutableTileIndex tile = new MutableTileIndex( TileIndex.get(0) );
			do {
				Landscape.ChangeTileOwner( tile, old_player.id, new_player.id);
			} while (tile.madd(1).tile < Global.MapSize());
		}

		// Change color of existing windows
		if (new_player.id != Owner.OWNER_SPECTATOR) {
			Iterator<Window> it = Window.getIterator();
			while(it.hasNext())
			{
				Window w = it.next();
				if (w.caption_color == old_player.id)
					w.caption_color = (byte) new_player.id;
			}
		}

		{
			int i;

			/* Check for shares */
			for( Player p : Global.gs._players )
			{
				for (i = 0; i < 4; i++) {
					/* 'Sell' the share if this player has any */
					if (p.share_owners[i] == null || p.share_owners[i].equals(PlayerID.getCurrent()))
						p.share_owners[i] = PlayerID.get( Owner.OWNER_SPECTATOR );
				}
			}

			Player p = PlayerID.getCurrent().GetPlayer();
			/* Sell all the shares that people have on this company */
			for (i = 0; i < 4; i++)
				p.share_owners[i] = PlayerID.get( Owner.OWNER_SPECTATOR );
		}

		PlayerID.setCurrent(old);

		Hal.MarkWholeScreenDirty();
	}

	static void PlayersCheckBankrupt(Player p)
	{
		PlayerID owner;
		long val;

		// If the player has money again, it does not go bankrupt
		if (p.getMoney() >= 0) {
			p.quarters_of_bankrupcy = 0;
			return;
		}

		p.quarters_of_bankrupcy++;

		owner = p.index;

		switch (p.quarters_of_bankrupcy) {
		case 2:
			NewsItem.AddNewsItem( new StringID(owner.id + 16),
					NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_COMPANY_INFO, NewsItem.DNC_BANKRUPCY),0,0);
			break;
		case 3: {
			/* XXX - In multiplayer, should we ask other players if it wants to take
			          over when it is a human company? -- TrueLight */
			if (owner.IS_HUMAN_PLAYER()) {
				NewsItem.AddNewsItem( new StringID(owner.id + 16),
						NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_COMPANY_INFO, NewsItem.DNC_BANKRUPCY),0,0);
				break;
			}

			// Check if the company has any value.. if not, declare it bankrupt
			//  right now
			val = CalculateCompanyValue(p);
			if (val > 0) {
				p.bankrupt_value = (int) val;
				p.bankrupt_asked = (byte) (1 << owner.id); // Don't ask the owner
				p.bankrupt_timeout = 0;
				break;
			}
			// Else, fall thru to case 4...
		}
		case 4: {
			// Close everything the owner has open
			Player.DeletePlayerWindows(owner);

			//			Show bankrupt news
			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);
			NewsItem.AddNewsItem( new StringID(owner.id + 16*3), NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_COMPANY_INFO, NewsItem.DNC_BANKRUPCY),0,0);

			// If the player is human, and it is no network play, leave the player playing
			if (owner.IS_HUMAN_PLAYER() && !Global._networking) {
				p.bankrupt_asked = (byte) 255;
				p.bankrupt_timeout = 0x456;
			} else {
					if (owner.IS_HUMAN_PLAYER() && Global._network_server) {
						// If we are the server, make sure it is clear that his player is no
						//  longer with us!
						//NetworkClientInfo ci;
						//NetworkClientState cs;
						// * Find all clients that were in control of this company * /
						//FOR_ALL_CLIENTS(cs) 
						//for(NetworkClientState cs : Net.getClients())
						Net.FOR_ALL_CLIENTS(cs ->
						{
							NetworkClientInfo ci = cs.getCi(); // DEREF_CLIENT_INFO(cs);
							if ((ci.client_playas-1) == owner.id) {
								ci.client_playas = Owner.OWNER_SPECTATOR;
								// Send the new info to all the clients
								try {
									NetServer.NetworkUpdateClientInfo(Net._network_own_client_index);
								} catch (IOException e) {
									// e.printStackTrace();
									Global.error(e);
								}
							}
						});
					}
					// Make sure the player no longer controls the company
					if (owner.IS_HUMAN_PLAYER() && owner.equals(Global.gs._local_player)) {
						// Switch the player to spectator..
						Global.gs._local_player = Owner.OWNER_SPECTATOR_ID;
					}

				// Convert everything the player owns to NO_OWNER
				p.money64 = Player.INITIAL_MONEY;
				ChangeOwnershipOfPlayerItems(owner, PlayerID.get(Owner.OWNER_SPECTATOR));
				// Register the player as not-active
				p.is_active = false;

				if (!owner.IS_HUMAN_PLAYER() && (!Global._networking || Global._network_server) && Ai._ai.enabled)
					Ai.AI_PlayerDied(owner);
				if (owner.IS_HUMAN_PLAYER() && owner.isLocalPlayer() && Ai._ai.network_client)
					Ai.AI_PlayerDied(owner);
			}
		}
		}
	}

	static void DrawNewsBankrupcy(Window w)
	{
		Player p;

		NewsItem.DrawNewsBorder(w);

		p = Player.GetPlayer(w.as_news_d().ni.getString_id().id & 15);
		Player.DrawPlayerFace(p.face, p.player_color, 2, 23);
		Gfx.GfxFillRect(3, 23, 3+91, 23+118, 0x323 | Sprite.USE_COLORTABLE);

		Global.SetDParam(0, p.president_name_1);
		Global.SetDParam(1, p.president_name_2);

		Gfx.DrawStringMultiCenter(49, 148, Str.STR_7058_PRESIDENT, 94);

		switch(w.as_news_d().ni.getString_id().id >> 4) {
		case 1:
			Gfx.DrawStringCentered(w.getWidth()>>1, 1, Str.STR_7056_TRANSPORT_COMPANY_IN_TROUBLE, 0);

			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);

			Gfx.DrawStringMultiCenter(
					((w.getWidth() - 101) >> 1) + 98,
					90,
					Str.STR_7057_WILL_BE_SOLD_OFF_OR_DECLARED,
					w.getWidth() - 101);
			break;

		case 2: {
			int price;

			Gfx.DrawStringCentered(w.getWidth()>>1, 1, Str.STR_7059_TRANSPORT_COMPANY_MERGER, 0);
			Global.COPY_IN_DPARAM(0,w.as_news_d().ni.params, 2);
			Global.SetDParam(2, p.name_1);
			Global.SetDParam(3, p.name_2);
			price = w.as_news_d().ni.params[2];
			Global.SetDParam(4, price);
			Gfx.DrawStringMultiCenter(
					((w.getWidth() - 101) >> 1) + 98,
					90,
					price==0 ? Str.STR_707F_HAS_BEEN_TAKEN_OVER_BY : Str.STR_705A_HAS_BEEN_SOLD_TO_FOR,
							w.getWidth() - 101);
			break;
		}

		case 3:
			Gfx.DrawStringCentered(w.getWidth()>>1, 1, Str.STR_705C_BANKRUPT, 0);
			Global.COPY_IN_DPARAM(0,w.as_news_d().ni.params, 2);
			Gfx.DrawStringMultiCenter(
					((w.getWidth() - 101) >> 1) + 98,
					90,
					Str.STR_705D_HAS_BEEN_CLOSED_DOWN_BY,
					w.getWidth() - 101);
			break;

		case 4:
			Gfx.DrawStringCentered(w.getWidth()>>1, 1, Str.STR_705E_NEW_TRANSPORT_COMPANY_LAUNCHED, 0);
			Global.SetDParam(0, p.name_1);
			Global.SetDParam(1, p.name_2);
			Global.COPY_IN_DPARAM(2,w.as_news_d().ni.params, 2);
			Gfx.DrawStringMultiCenter(
					((w.getWidth() - 101) >> 1) + 98,
					90,
					Str.STR_705F_STARTS_CONSTRUCTION_NEAR,
					w.getWidth() - 101);
			break;

		default:
			assert false;
		}
	}

	static int GetNewsStringBankrupcy(final NewsItem ni)
	{
		final Player p = PlayerID.get(ni.getString_id().id & 0xF).GetPlayer();

		switch (ni.getString_id().id >> 4) {
		case 1:
			Global.SetDParam(0, Str.STR_7056_TRANSPORT_COMPANY_IN_TROUBLE);
			Global.SetDParam(1, Str.STR_7057_WILL_BE_SOLD_OFF_OR_DECLARED);
			Global.SetDParam(2, p.name_1);
			Global.SetDParam(3, p.name_2);
			return Str.STR_02B6;
		case 2:
			Global.SetDParam(0, Str.STR_7059_TRANSPORT_COMPANY_MERGER);
			Global.SetDParam(1, Str.STR_705A_HAS_BEEN_SOLD_TO_FOR);
			Global.COPY_IN_DPARAM(2,ni.params, 2);
			Global.SetDParam(4, p.name_1);
			Global.SetDParam(5, p.name_2);
			
			//Global.COPY_IN_DPARAM(6,ni.params + 2, 1);
			System.arraycopy(ni.params, 2, Global._decode_parameters, 6, 1 );
			
			
			return Str.STR_02B6;
		case 3:
			Global.SetDParam(0, Str.STR_705C_BANKRUPT);
			Global.SetDParam(1, Str.STR_705D_HAS_BEEN_CLOSED_DOWN_BY);
			Global.COPY_IN_DPARAM(2,ni.params, 2);
			return Str.STR_02B6;
		case 4:
			Global.SetDParam(0, Str.STR_705E_NEW_TRANSPORT_COMPANY_LAUNCHED);
			Global.SetDParam(1, Str.STR_705F_STARTS_CONSTRUCTION_NEAR);
			Global.SetDParam(2, p.name_1);
			Global.SetDParam(3, p.name_2);
			Global.COPY_IN_DPARAM(4,ni.params, 2);
			return Str.STR_02B6;
		default:
			assert false;
		}

		/* useless, but avoids compiler warning this way */
		return 0;
	}

	static void PlayersGenStatistics()
	{
		Station.forEachValid( st ->
		{
				PlayerID.setCurrent(st.owner); 
				Player.SET_EXPENSES_TYPE(Player.EXPENSES_PROPERTY);
				Player.SubtractMoneyFromPlayer((int) (Global._price.station_value/2));
		});
		
		PlayerID.setCurrentToNone(); // [dz] clean up

		if (!BitOps.HASBIT(1<<0|1<<3|1<<6|1<<9, Global.get_cur_month()))
			return;

		Player.forEach( (p) ->
		{
			if (p.is_active) {
				//
				// Looks like it was a C hack - using the fact that p.old_economy and p.cur_economy are 
				// one next to other
				// 

				//memmove(&p.old_economy, &p.cur_economy, sizeof(p.old_economy));
				//memset(&p.cur_economy, 0, sizeof(p.cur_economy));

				System.arraycopy( p.old_economy, 0, p.old_economy, 1, p.old_economy.length-1 );
				p.old_economy[0] = p.cur_economy;
				p.cur_economy = new PlayerEconomyEntry();

				if (p.num_valid_stat_ent != 24)
					p.num_valid_stat_ent++;

				UpdateCompanyRatingAndValue(p, true);
				PlayersCheckBankrupt(p);

				if (p.block_preview != 0)
					p.block_preview--;
			}
		});

		Window.InvalidateWindow(Window.WC_INCOME_GRAPH, 0);
		Window.InvalidateWindow(Window.WC_OPERATING_PROFIT, 0);
		Window.InvalidateWindow(Window.WC_DELIVERED_CARGO, 0);
		Window.InvalidateWindow(Window.WC_PERFORMANCE_HISTORY, 0);
		Window.InvalidateWindow(Window.WC_COMPANY_VALUE, 0);
		Window.InvalidateWindow(Window.WC_COMPANY_LEAGUE, 0);
	}


	/*
	static void AddSingleInflation(int *value, int *frac, int amt)
	{
		long tmp;
		int low;
		tmp = BIGMULS(*value, amt);
	 *frac = (int)(low = (int)tmp + *frac);
	 *value += (int)(tmp >> 16) + (low >> 16);
	}
	 */
	void AddInflation()
	{

		int i;
		int inf = infl_amount * 54;
		double infd = 1.0 + ((inf+0.0) / (1<<16));

		for (i = 0; i != Prices.NUM_PRICES; i++) {
			//AddSingleInflation((int*)&_price + i, _price_frac + i, inf);
			double p = Global._price.getPrice(i);
			Global._price.setPrice(i, p*infd);
		}

		//max_loan_unround += BIGMULUS(max_loan_unround, inf, 16);
		max_loan_unround *= infd; 

		if (max_loan + 50000 <= max_loan_unround)
			max_loan += 50000;

		inf = infl_amount_pr * 54;
		infd = 1.0 + (inf+0.0 / (1<<16));
		for (i = 0; i != AcceptedCargo.NUM_CARGO; i++) {
			/*AddSingleInflation(
				(int*)_cargo_payment_rates + i,
				_cargo_payment_rates_frac + i,
				inf
			);*/
			
			Global._cargo_payment_rates[i] *= infd;
		}

		Window.InvalidateWindowClasses(Window.WC_BUILD_VEHICLE);
		Window.InvalidateWindowClasses(Window.WC_REPLACE_VEHICLE);
		Window.InvalidateWindowClasses(Window.WC_VEHICLE_DETAILS);
		Window.InvalidateWindow(Window.WC_PAYMENT_RATES, 0);

	}

	void PlayersPayInterest()
	{		
		int interest = interest_rate * 54;

		Iterator<Player> ii = Player.getIterator();
		while(ii.hasNext())
		{
			final Player p = ii.next();
			if (!p.is_active) continue;

			PlayerID.setCurrent(p.index);
			Player.SET_EXPENSES_TYPE(Player.EXPENSES_LOAN_INT);

			Player.SubtractMoneyFromPlayer(BitOps.BIGMULUS(p.current_loan, interest, 16));

			Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);
			//Player.SubtractMoneyFromPlayer(Global._price.station_value >> 2);
			Player.SubtractMoneyFromPlayer((int) (Global._price.station_value/4));
		}
		
		PlayerID.setCurrentToNone(); // [dz] or else it can be null?
	}

	void HandleEconomyFluctuations()
	{
		if (GameOptions._opt.diff.economy == 0) return;

		if (--fluct == 0) {
			fluct = -BitOps.GB(Hal.Random(), 0, 2);
			NewsItem.AddNewsItem(Str.STR_7073_WORLD_RECESSION_FINANCIAL, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL,0,NewsItem.NT_ECONOMY,0), 0, 0);
		} else if (fluct == -12) {
			fluct = BitOps.GB(Hal.Random(), 0, 8) + 312;
			NewsItem.AddNewsItem(Str.STR_7074_RECESSION_OVER_UPTURN_IN, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL,0,NewsItem.NT_ECONOMY,0), 0, 0);
		}
	}


	static final byte[] price_base_multiplier = new byte[Global.NUM_PRICES];

	/**
	 * Reset changes to the price base multipliers.
	 */
	public static void ResetPriceBaseMultipliers()
	{
		int i;

		// 8 means no multiplier.
		for (i = 0; i < price_base_multiplier.length; i++)
			price_base_multiplier[i] = 8;
	}

	/**
	 * Change a price base by the given factor.
	 * The price base is altered by factors of two, with an offset of 8.
	 * NewBaseCost = OldBaseCost * 2^(n-8)
	 * @param price Index of price base to change.
	 * @param factor Amount to change by.
	 */
	static void SetPriceBaseMultiplier(int price, byte factor)
	{
		assert(price < Global.NUM_PRICES);
		price_base_multiplier[price] = factor;
	}

	public void StartupEconomy()
	{
		int i;

		//assert(Global._price.length == Global.NUM_PRICES);

		for(i=0; i!= Prices.NUM_PRICES; i++) 
		{
			int price = _price_base[i];
			if (_price_category[i] != 0) {
				int mod = _price_category[i] == 1 ? GameOptions._opt.diff.vehicle_costs : GameOptions._opt.diff.construction_cost;
				if (mod < 1) {
					price = price * 3 >> 2;
				} else if (mod > 1) {
					price = price * 9 >> 3;
				}
			}
			if (price_base_multiplier[i] > 8) {
				price <<= price_base_multiplier[i] - 8;
			} else {
				price >>= 8 - price_base_multiplier[i];
			}
			//((int*)&Global._price)[i] = price;
			Global._price.setPrice(i, price);
			//Global._price_frac[i] = 0;
		}

		interest_rate = GameOptions._opt.diff.initial_interest;
		infl_amount = GameOptions._opt.diff.initial_interest;
		infl_amount_pr = Math.max(0, GameOptions._opt.diff.initial_interest - 1);
		max_loan_unround = max_loan = GameOptions._opt.diff.max_loan * 1000;
		fluct = BitOps.GB(Hal.Random(), 0, 8) + 168;
	}



	private static FoundRoute FindSubsidyPassengerRoute()
	{
		Town from,to;
		FoundRoute fr = new FoundRoute();


		fr.distance = -1;

		fr.from = from = Town.getRandomTown();//Town.GetTown(Hal.RandomRange(Town._total_towns));
		if (from.getXy() == null || from.population < 400)
			return null;

		fr.to = to = Town.getRandomTown();//Town.GetTown(Hal.RandomRange(Town._total_towns));
		if (from==to || to.getXy() == null || to.population < 400 || to.pct_pass_transported > 42)
			return null;

		fr.distance = Map.DistanceManhattan(from.getXy(), to.getXy());
		
		return fr;
	}

	static FoundRoute FindSubsidyCargoRoute()
	{
		FoundRoute fr = new FoundRoute();
		Industry i;
		int trans, total;
		int cargo;

		fr.distance = -1;

		fr.from = i = Industry.GetIndustry(Hal.RandomRange(Industry._total_industries));
		if (!i.isValid())
			return null;

		// Randomize cargo type
		if ( 0 != (Hal.Random()&1) && i.produced_cargo[1] != AcceptedCargo.CT_INVALID) {
			cargo = i.produced_cargo[1];
			trans = i.pct_transported[1];
			total = i.total_production[1];
		} else {
			cargo = i.produced_cargo[0];
			trans = i.pct_transported[0];
			total = i.total_production[0];
		}

		// Quit if no production in this industry
		//  or if the cargo type is passengers
		//  or if the pct transported is already large enough
		if (total == 0 || trans > 42 || cargo == AcceptedCargo.CT_INVALID || cargo == AcceptedCargo.CT_PASSENGERS)
			return null;

		fr.cargo = cargo;

		if (cargo == AcceptedCargo.CT_GOODS || cargo == AcceptedCargo.CT_FOOD) {
			// The destination is a town
			Town t = Town.getRandomTown(); //GetTown(Hal.RandomRange(Town._total_towns));

			// Only want big towns
			if (t.getXy() == null || t.population < 900)
				return null;
			fr.distance = Map.DistanceManhattan(i.xy, t.getXy());
			fr.to = t;
		} else {
			// The destination is an industry
			Industry i2 = Industry.GetIndustry(Hal.RandomRange(Industry._total_industries));

			// The industry must accept the cargo
			if (i == i2 || !i2.isValid() ||
					(cargo != i2.accepts_cargo[0] &&
					cargo != i2.accepts_cargo[1] &&
					cargo != i2.accepts_cargo[2]))
				return null;
			fr.distance = Map.DistanceManhattan(i.xy, i2.xy);
			fr.to = i2;
		}
		
		return fr;
	}

	static void SubsidyMonthlyHandler()
	{
		Pair pair;
		int n;
		boolean modified = false;

		for(Subsidy s : Subsidy._subsidies ) 
			if( s.updateAge() ) modified = true;
		
		// 25% chance to go on
		if (BitOps.CHANCE16(1,4)) 
		{
			// Find a free slot
			//s = _subsidies;
			int sp = 0;
			while (Subsidy._subsidies[sp].isValid()) 
			{
				if (++sp >= Subsidy._subsidies.length)
				{
					//goto no_add;
					if (modified)
						Window.InvalidateWindow(Window.WC_SUBSIDIES_LIST, 0);
					return;
				}
			}

			Subsidy s = Subsidy._subsidies[sp];

			n = 1000;
			do {
				FoundRoute fr = FindSubsidyPassengerRoute();
				if(fr == null) continue;
				
				if (fr.distance <= 70 && (fr.to instanceof Town) ) {
					s.cargo_type = AcceptedCargo.CT_PASSENGERS;
					s.from = ((Town)fr.from).index;
					s.to = ((Town)fr.to).index;

					//goto add_subsidy;
					if (!Subsidy.CheckSubsidyDuplicate(s)) {
						s.age = 0;
						pair = s.SetupSubsidyDecodeParam(false);
						NewsItem.AddNewsItem(Str.STR_2030_SERVICE_SUBSIDY_OFFERED, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, NewsItem.NF_TILE, NewsItem.NT_SUBSIDIES, 0), pair.a, pair.b);
						modified = true;
						break;
					}
					continue;
				}
				fr = FindSubsidyCargoRoute();
				if (fr != null && fr.distance <= 70 && fr.distance > 0) {
					s.cargo_type = fr.cargo;
					s.from = ((Industry)fr.from).index;
					s.to = (fr.cargo == AcceptedCargo.CT_GOODS || fr.cargo == AcceptedCargo.CT_FOOD) ? ((Town)fr.to).index : ((Industry)fr.to).index;
					//add_subsidy:
					if (!Subsidy.CheckSubsidyDuplicate(s)) {
						s.age = 0;
						pair = s.SetupSubsidyDecodeParam(false);
						NewsItem.AddNewsItem(Str.STR_2030_SERVICE_SUBSIDY_OFFERED, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, NewsItem.NF_TILE, NewsItem.NT_SUBSIDIES, 0), pair.a, pair.b);
						modified = true;
						break;
					}
				}
			} while (n-- > 0);
		}
		//no_add:;
		if (modified)
			Window.InvalidateWindow(Window.WC_SUBSIDIES_LIST, 0);
	}

	/* TODO save
	static final SaveLoad _subsidies_desc[] = {
		SLE_VAR(Subsidy,cargo_type,		SLE_UINT8),
		SLE_VAR(Subsidy,age,					SLE_UINT8),
		SLE_CONDVAR(Subsidy,from,			SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVAR(Subsidy,from,			SLE_UINT16, 5, 255),
		SLE_CONDVAR(Subsidy,to,				SLE_FILE_U8 | SLE_VAR_U16, 0, 4),
		SLE_CONDVAR(Subsidy,to,				SLE_UINT16, 5, 255),
		SLE_END()
	};

	/*
	static void Save_SUBS()
	{
		int i;
		Subsidy s;

		for(i=0; i!=lengthof(_subsidies); i++) {
			s = &_subsidies[i];
			if (s.cargo_type != AcceptedCargo.CT_INVALID) {
				SlSetArrayIndex(i);
				SlObject(s, _subsidies_desc);
			}
		}
	}

	static void Load_SUBS()
	{
		int index;
		while ((index = SlIterateArray()) != -1)
			SlObject(&_subsidies[index], _subsidies_desc);
	} */

	public static int GetTransportedGoodsIncome(int num_pieces, int dist, int transit_days, int cargo_type)
	{
		int cargo = cargo_type;
		int f;

		/* zero the distance if it's the bank and very short transport. */
		if (GameOptions._opt.landscape == Landscape.LT_NORMAL && cargo == AcceptedCargo.CT_VALUABLES && dist < 10)
			dist = 0;

		f = 255;
		if (transit_days > Global._cargoc.transit_days_1[cargo]) {
			transit_days -= Global._cargoc.transit_days_1[cargo];
			f -= transit_days;

			if (transit_days > Global._cargoc.transit_days_2[cargo]) {
				transit_days -= Global._cargoc.transit_days_2[cargo];

				if (f < transit_days)
					f = 0;
				else
					f -= transit_days;
			}
		}
		if (f < 31) f = 31;

		return BitOps.BIGMULSS(dist * f * num_pieces, (int) Global._cargo_payment_rates[cargo], 21);
	}

	static void DeliverGoodsToIndustry(TileIndex xy, int cargo_type, int num_pieces)
	{
		Industry [] best = { null };
		int [] u = { Global._patches.station_spread + 8 };

		/* Check if there's an industry close to the station that accepts
		 * the cargo */
		Industry.forEach( (ind) ->
		{
			int t;
			if (ind.isValid() && ind.acceptsCargo(cargo_type) 
					&& !ind.producesCargo(cargo_type)
					 &&	(t = Map.DistanceManhattan(ind.xy, xy)) < 2 * u[0]) 
			{
				u[0] = t;
				best[0] = ind;
			}
		});

		/* Found one? */
		if (best[0] != null) {
			//best[0].was_cargo_delivered = true;
			//best[0].cargo_waiting[0] = Math.min(best[0].cargo_waiting[0] + num_pieces, 0xFFFF);
			best[0].acceptCargo(cargo_type, num_pieces);
		}
	}

	static boolean CheckSubsidised(Station from, Station to, int cargo_type)
	{
		//Subsidy s;
		TileIndex xy;
		Pair pair;
		Player p;

		// check if there is an already existing subsidy that applies to us
		for( Subsidy s : Subsidy._subsidies )
			/*if (s.cargo_type == cargo_type &&	s.age >= 12 && s.from == from.index && s.to == to.index)*/
			if( s.appliesTo(from,to,cargo_type))
				return true;

		/* check if there's a new subsidy that applies.. */
		for( Subsidy s : Subsidy._subsidies )
		{
			if (s.cargo_type == cargo_type && s.age < 12) 
			{

				/* Check distance from source */
				if (cargo_type == AcceptedCargo.CT_PASSENGERS || cargo_type == AcceptedCargo.CT_MAIL) {
					xy = Town.GetTown(s.from).getXy();
				} else {
					xy = (Industry.GetIndustry(s.from)).xy;
				}

				if (Map.DistanceMax(xy, from.getXy()) > 9)
					continue;

				/* Check distance from dest */
				if (cargo_type == AcceptedCargo.CT_PASSENGERS || cargo_type == AcceptedCargo.CT_MAIL || cargo_type == AcceptedCargo.CT_GOODS || cargo_type == AcceptedCargo.CT_FOOD) {
					xy = Town.GetTown(s.to).getXy();
				} else {
					xy = (Industry.GetIndustry(s.to)).xy;
				}

				if (Map.DistanceMax(xy, to.getXy()) > 9)
					continue;

				/* Found a subsidy, change the values to indicate that it's in use */
				s.age = 12;
				s.from = from.index;
				s.to = to.index;

				/* Add a news item */
				pair = s.SetupSubsidyDecodeParam(false);
				Global.InjectDParam(2);

				p = PlayerID.getCurrent().GetPlayer();
				Global.SetDParam(0, p.name_1);
				Global.SetDParam(1, p.name_2);
				NewsItem.AddNewsItem(
						Str.STR_2031_SERVICE_SUBSIDY_AWARDED + GameOptions._opt.diff.subsidy_multiplier,
						NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, NewsItem.NF_TILE, NewsItem.NT_SUBSIDIES, 0),
						pair.a, pair.b);

				Window.InvalidateWindow(Window.WC_SUBSIDIES_LIST, 0);
				return true;
			}
		}
		return false;
	}

	static int DeliverGoods(int num_pieces, int cargo_type, int source, int dest, int days_in_transit)
	{
		boolean subsidised;
		Station s_from, s_to;
		int profit;

		assert(num_pieces > 0);

		// Update player statistics
		{
			Player p = PlayerID.getCurrent().GetPlayer();
			p.cur_economy.delivered_cargo += num_pieces;
			p.cargo_types = BitOps.RETSETBIT(p.cargo_types, cargo_type);
		}

		// Get station pointers.
		s_from = Station.GetStation(source);
		s_to = Station.GetStation(dest);

		// Check if a subsidy applies.
		subsidised = CheckSubsidised(s_from, s_to, cargo_type);

		// Increase town's counter for some special goods types
		if (cargo_type == AcceptedCargo.CT_FOOD) s_to.town.new_act_food += num_pieces;
		if (cargo_type == AcceptedCargo.CT_WATER)  s_to.town.new_act_water += num_pieces;

		// Give the goods to the industry.
		DeliverGoodsToIndustry(s_to.getXy(), cargo_type, num_pieces);

		// Determine profit
		profit = GetTransportedGoodsIncome(num_pieces, Map.DistanceManhattan(s_from.getXy(), s_to.getXy()), days_in_transit, cargo_type);


		// Modify profit if a subsidy is in effect
		if (subsidised) {
			if (GameOptions._opt.diff.subsidy_multiplier < 1) {				/* 1.5x */
				profit += profit >> 1;
			} else if (GameOptions._opt.diff.subsidy_multiplier == 1) {		/* 2x */
				profit *= 2;
			} else if (GameOptions._opt.diff.subsidy_multiplier == 2) {		/* 3x */
				profit *= 3;
			} else {														/* 4x */
				profit *= 4;
			}
		}

		return profit;
	}

	/*
	 * Returns true if Vehicle v should wait loading because other vehicle is
	 * already loading the same cargo type
	 * v = vehicle to load, u = GetFirstInChain(v)
	 */
	static boolean LoadWait(final Vehicle v, final Vehicle u) {
		Vehicle w;
		boolean has_any_cargo = false;

		if(!u.getCurrent_order().hasFlag(Order.OF_FULL_LOAD)) return false;

		for (w = u; w != null; w = w.next) {
			if (w.cargo_count != 0) {
				if (v.getCargo_type() == w.getCargo_type() &&
						u.last_station_visited == w.getCargo_source())
					return false;
				has_any_cargo = true;
			}
		}

		Iterator<Vehicle> ii = Vehicle.getIterator();
		while(ii.hasNext())
		{
			Vehicle x = ii.next();

			if ((x.type != Vehicle.VEH_Train || x.IsFrontEngine()) && // for all locs
					u.last_station_visited == x.last_station_visited && // at the same station
					!x.isStopped() && // not stopped
					x.getCurrent_order().type == Order.OT_LOADING && // loading
					u != x) { // not itself
				boolean other_has_any_cargo = false;
				boolean has_space_for_same_type = false;
				boolean other_has_same_type = false;

				for (w = x; w != null; w = w.next) {
					if (w.cargo_count < w.getCargo_cap() && v.getCargo_type() == w.getCargo_type())
						has_space_for_same_type = true;

					if (w.cargo_count != 0) {
						if (v.getCargo_type() == w.getCargo_type() &&
								u.last_station_visited == w.getCargo_source())
							other_has_same_type = true;
						other_has_any_cargo = true;
					}
				}

				if (has_space_for_same_type) {
					if (other_has_same_type) return true;
					if (other_has_any_cargo && !has_any_cargo) return true;
				}
			}
		}

		return false;
	}

	static int LoadUnloadVehicle(Vehicle v)
	{
		StationID original_cargo_source = StationID.get( v.getCargo_source() );
		int profit = 0;
		int v_profit; //virtual profit for feeder systems
		int v_profit_total = 0;
		int unloading_time = 20;
		Vehicle u = v;
		int result = 0;
		int last_visited;
		Station st;
		int t;
		int count, cap;
		PlayerID old_player;
		boolean completely_empty = true;

		assert(v.getCurrent_order().type == Order.OT_LOADING);

		v.cur_speed = 0;

		old_player = PlayerID.getCurrent();
		PlayerID.setCurrent(v.owner);

		st = Station.GetStation(last_visited = v.last_station_visited);

		for (; v != null; v = v.next) {
			GoodsEntry ge;

			if (v.getCargo_cap() == 0) continue;

			//ge = &st.goods[v.cargo_type];
			ge = st.goods[v.getCargo_type()];

			/* unload? */
			if (v.cargo_count != 0) {
				if (v.getCargo_source() != last_visited 
						&& 0 != (ge.waiting_acceptance & 0x8000) 
						&& !u.getCurrent_order().hasFlag(Order.OF_TRANSFER)) {
					// deliver goods to the station
					st.time_since_unload = 0;

					unloading_time += v.cargo_count; /* TTDBUG: bug in original TTD */
					//<<<<<<< .mine
					//profit += DeliverGoods(v.cargo_count, v.cargo_type, v.cargo_source, last_visited, v.cargo_days);				

					//=======

					/* Aircraft planespeed patch: don't let profit get out of control because
					 * aircraft are delivering in very short time!
					 */
					if(v.type == Vehicle.VEH_Aircraft)
						profit += DeliverGoods(v.cargo_count, v.getCargo_type(), v.getCargo_source(), last_visited, v.cargo_days * Global._patches.aircraft_speed_coeff);
					else
						profit += DeliverGoods(v.cargo_count, v.getCargo_type(), v.getCargo_source(), last_visited, v.cargo_days);

					//>>>>>>> .theirs
					result |= 1;
					v.cargo_count = 0;
				} 
				else if(u.getCurrent_order().hasFlag(Order.OF_UNLOAD | Order.OF_TRANSFER)) 
				{
					/* unload goods and let it wait at the station */
					st.time_since_unload = 0;

					/* Aircraft planespeed patch: don't let profit get out of control because
					 * aircraft are delivering in very short time!
					 */
					if(v.type == Vehicle.VEH_Aircraft) {
						v_profit = GetTransportedGoodsIncome(
								v.cargo_count,
								Map.DistanceManhattan(Station.GetStation(v.getCargo_source()).getXy(), Station.GetStation(last_visited).getXy()),
								v.cargo_days * Global._patches.aircraft_speed_coeff,
								v.getCargo_type()) * 3 / 2;
					} else {
						v_profit = GetTransportedGoodsIncome(
								v.cargo_count,
								Map.DistanceManhattan(Station.GetStation(v.getCargo_source()).getXy(), Station.GetStation(last_visited).getXy()),
								v.cargo_days,
								v.getCargo_type()) * 3 / 2;
					}
					v_profit_total += v_profit;
					unloading_time += v.cargo_count;
					t = BitOps.GB(ge.waiting_acceptance, 0, 12);
					if (t == 0) {
						// No goods waiting at station
						ge.enroute_time = v.cargo_days;
						ge.enroute_from = v.getCargo_source();
					} else {
						// Goods already waiting at station. Set counters to the worst value.
						if (v.cargo_days >= ge.enroute_time)
							ge.enroute_time = v.cargo_days;
						if (last_visited != ge.enroute_from)
							ge.enroute_from = v.getCargo_source();
					}
					// Update amount of waiting cargo
					ge.waiting_acceptance = BitOps.RETSB(ge.waiting_acceptance, 0, 12, Math.min(v.cargo_count + t, 0xFFF));
					ge.feeder_profit += v_profit;
					u.profit_this_year += v_profit;
					result |= 2;
					v.cargo_count = 0;
				}

				if (v.cargo_count != 0)
					completely_empty = false;
			}

			/* don't pick up goods that we unloaded */
			if(0 != (u.getCurrent_order().flags & Order.OF_UNLOAD)) 
				continue;

			/* update stats */
			ge.days_since_pickup = 0;
			t = u.getMax_speed();
			if (u.type == Vehicle.VEH_Road) t >>=1;
		if (u.type == Vehicle.VEH_Train) t = u.rail.getCached_max_speed();

		// if last speed is 0, we treat that as if no vehicle has ever visited the station.
		ge.last_speed =  (Math.min(t, 255));
		ge.last_age =  (Global.get_cur_year() - v.getBuild_year());

		// If there's goods waiting at the station, and the vehicle
		//  has capacity for it, load it on the vehicle.
		count = BitOps.GB(ge.waiting_acceptance, 0, 12);
		if (count != 0 &&
				(cap = v.getCargo_cap() - v.cargo_count) != 0) {
			int cargoshare;
			int feeder_profit_share;

			if (v.cargo_count == 0)
				v.TriggerVehicle(Engine.VEHICLE_TRIGGER_NEW_CARGO);

			/* Skip loading this vehicle if another train/vehicle is already handling
			 * the same cargo type at this station */
			if (Global._patches.improved_load && LoadWait(v,u)) continue;

			/* TODO: Regarding this, when we do gradual loading, we
			 * should first unload all vehicles and then start
			 * loading them. Since this will cause
			 * VEHICLE_TRIGGER_EMPTY to be called at the time when
			 * the whole vehicle chain is really totally empty, the
			 * @completely_empty assignment can then be safely
			 * removed; that's how TTDPatch behaves too. --pasky */
			completely_empty = false;

			if (cap > count) cap = count;
			cargoshare = cap * 10000 / ge.waiting_acceptance;
			feeder_profit_share = ge.feeder_profit * cargoshare / 10000;
			v.cargo_count += cap;
			ge.waiting_acceptance -= cap;
			v.profit_this_year -= feeder_profit_share;
			ge.feeder_profit -= feeder_profit_share;
			unloading_time += cap;
			st.time_since_load = 0;

			// And record the source of the cargo, and the days in travel.
			v.cargo_source = st.index;	//changed this for feeder systems
			v.cargo_days = ge.enroute_time;
			result |= 2;
			st.last_vehicle = VehicleID.get( v.index );
		}
		}


		v = u;

		if (v_profit_total > 0)
			MiscGui.ShowFeederIncomeAnimation(v.getX_pos(), v.getY_pos(), v.z_pos, v_profit_total);

		if (v.type == Vehicle.VEH_Train) {
			// Each platform tile is worth 2 rail vehicles.
			int overhang = v.rail.cached_total_length - st.GetStationPlatforms(v.tile) * 16;
			if (overhang > 0) {
				unloading_time <<= 1;
				unloading_time += (overhang * unloading_time) / 8;
			}
		}

		v.load_unload_time_rem = unloading_time;

		if (completely_empty) {
			v.TriggerVehicle(Engine.VEHICLE_TRIGGER_EMPTY);
		}

		if (result != 0) {
			Window.InvalidateWindow(Window.WC_VEHICLE_DETAILS, v.index);

			if(0 != (result & 2))
				Window.InvalidateWindow(Window.WC_STATION_VIEW, last_visited);

			if (profit != 0) {

				if (Station.GetStation(last_visited).owner.isTown() 
						&& Station.GetStation(original_cargo_source.id).owner.isTown())

					v.MA_Tax(profit*2);

				else if (Station.GetStation(last_visited).owner.isTown() 
						|| Station.GetStation(original_cargo_source.id).owner.isTown())

					v.MA_Tax(profit);

				v.profit_this_year += profit;
				Player.SubtractMoneyFromPlayer(-profit);

				if (Player.IsLocalPlayer()) v.SndPlayVehicleFx(Snd.SND_14_CASHTILL);

				MiscGui.ShowCostOrIncomeAnimation(v.getX_pos(), v.getY_pos(), v.z_pos, -profit);
			}
		}

		PlayerID.setCurrent(old_player);
		return result;
	}

	public void PlayersMonthlyLoop()
	{
		PlayersGenStatistics();
		if (Global._patches.inflation.get() && Global.get_cur_year() < Global.MAX_YEAR_END)
			AddInflation();
		PlayersPayInterest();
		// Reset the _current_player flag
		PlayerID.setCurrentToNone();
		HandleEconomyFluctuations();
		SubsidyMonthlyHandler();
	}

	static void DoAcquireCompany(Player p)
	{
		Player owner;
		int i,pi;
		long value;

		Global.SetDParam(0, p.name_1);
		Global.SetDParam(1, p.name_2);
		Global.SetDParam(2, p.getBankrupt_value());
		NewsItem.AddNewsItem( new StringID(PlayerID.getCurrent().id + 16*2), NewsItem.NEWS_FLAGS(NewsItem.NM_CALLBACK, 0, NewsItem.NT_COMPANY_INFO, NewsItem.DNC_BANKRUPCY),0,0);

		// original code does this a little bit differently
		pi = p.index.id;
		ChangeOwnershipOfPlayerItems(PlayerID.get(pi), PlayerID.getCurrent());

		if (p.getBankrupt_value() == 0) {
			owner = PlayerID.getCurrent().GetPlayer();
			owner.current_loan += p.current_loan;
		}

		value = CalculateCompanyValue(p) >> 2;
		for(i=0; i!=4; i++) {
			if (p.share_owners[i].id != Owner.OWNER_SPECTATOR) {
				owner = p.share_owners[i].GetPlayer();
				owner.money64 += value;
				owner.getYearly_expenses()[0][Player.EXPENSES_OTHER] += value;
				//owner.UpdatePlayerMoney32();
			}
		}

		p.is_active = false;

		Player.DeletePlayerWindows( PlayerID.get(pi) );
		VehicleGui.RebuildVehicleLists();	//Updates the open windows to add the newly acquired vehicles to the lists
	}

	//extern int GetAmountOwnedBy(Player p, byte owner);

	/** Acquire shares in an opposing company.
	 * @param x,y unused
	 * @param p1 player to buy the shares from
	 * @param p2 unused
	 */
	public static int CmdBuyShareInCompany(int x, int y, int flags, int p1, int p2)
	{
		// Check if buying shares is allowed (protection against modified clients 
		if (p1 >= Global.MAX_PLAYERS || !Global._patches.allow_shares) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);
		Player p = PlayerID.get(p1).GetPlayer();

		// Protect new companies from hostile takeovers 
		if (Global.get_cur_year() - p.inaugurated_year < 6) return Cmd.return_cmd_error(Str.STR_7080_PROTECTED);

		// Those lines are here for network-protection (clients can be slow) 
		if (PlayerGui.GetAmountOwnedBy(p, PlayerID.getSpectator() ) == 0) return 0;

		// We can not buy out a real player (temporarily). TODO: well, enable it obviously 
		if (PlayerGui.GetAmountOwnedBy(p, PlayerID.getSpectator() ) == 1 && !p.isAi()) return 0;

		long cost = CalculateCompanyValue(p) >> 2;
		if(0 != (flags & Cmd.DC_EXEC) ) {
			PlayerID [] bp = p.share_owners;
			int b = 0;
			int i;

			while(!bp[b].equals(Owner.OWNER_SPECTATOR)) 
				b++; // share owners is guaranteed to contain at least one Owner.OWNER_SPECTATOR 
		 
			bp[b] = PlayerID.getCurrent(); // _current_player;

			for (i = 0; p.share_owners[i].isCurrentPlayer();) {
				if (++i == 4) {
					p.bankrupt_value = 0;
					DoAcquireCompany(p);
					break;
				}
			}
			Window.InvalidateWindow(Window.WC_COMPANY, (int)p1);
		}
		return (int) cost; // TODO long truncated
	}

	/** Sell shares in an opposing company.
	 * @param x,y unused
	 * @param p1 player to sell the shares from
	 * @param p2 unused
	 */
	public static int CmdSellShareInCompany(int x, int y, int flags, int p1, int p2)
	{
		// Check if buying shares is allowed (protection against modified clients 
		if (p1 >= Global.MAX_PLAYERS || !Global._patches.allow_shares) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);
		Player p = PlayerID.get(p1).GetPlayer();

		// Those lines are here for network-protection (clients can be slow) 
		if (PlayerGui.GetAmountOwnedBy(p, PlayerID.getCurrent()) == 0) return 0;

		// adjust it a little to make it less profitable to sell and buy 
		long cost = CalculateCompanyValue(p) >> 2;
		cost = -(cost - (cost >> 7));

		if(0 != (flags & Cmd.DC_EXEC)) {
			PlayerID [] bp = p.share_owners;
			int b = 0;
			while (!bp[b].isCurrentPlayer()) 
				b++; // share owners is guaranteed to contain player 
			bp[b] = PlayerID.getSpectator();
			Window.InvalidateWindow(Window.WC_COMPANY, (int)p1);
		}
		return (int) cost; // TODO long truncated
	}

	/** Buy up another company.
	 * When a competing company is gone bankrupt you get the chance to purchase
	 * that company.
	 * <br>
	 * TODO currently this only works for AI players
	 *
	 * @param x,y unused
	 * @param p1 player/company to buy up
	 * @param p2 unused
	 */
	public static int CmdBuyCompany(int x, int y, int flags, int p1, int p2)
	{
		Player p;

		/* Disable takeovers in multiplayer games */
		if (p1 >= Global.MAX_PLAYERS || Global._networking) return Cmd.CMD_ERROR;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);
		p = PlayerID.get(p1).GetPlayer();

		if (!p.isAi()) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			DoAcquireCompany(p);
		}
		return p.getBankrupt_value();
	}
	/*
	// Prices
	static void SaveLoad_PRIC()
	{
		SlArray(&_price, NUM_PRICES, SLE_INT32); // TODO save
	}

	// Cargo payment rates
	static void SaveLoad_CAPR()
	{
		SlArray(&_cargo_payment_rates, NUM_CARGO, SLE_INT32); // TODO save
	}


	// Economy variables
	static void SaveLoad_ECMY()
	{
		SlObject(&Global._economy, _economy_desc); // TODO save?
	}

	 */

	public int getMax_loan() { return max_loan; }
	public int getFluct() { return fluct; }
}
