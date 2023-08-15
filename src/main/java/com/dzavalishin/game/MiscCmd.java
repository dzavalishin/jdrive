package com.dzavalishin.game;

import java.util.Iterator;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.xui.SettingsGui;
import com.dzavalishin.xui.Window;

public class MiscCmd {

	/** Change the player's face.
	 * @param x,y unused
	 * @param p1 unused
	 * @param p2 face bitmasked
	 */
	static int CmdSetPlayerFace(int x, int y, int flags, int p1, int p2)
	{
		if(0 != (flags & Cmd.DC_EXEC)) {
			Player.GetCurrentPlayer().face = p2;
			Hal.MarkWholeScreenDirty();
		}
		return 0;
	}

	/** Change the player's company-colour
	 * @param x,y unused
	 * @param p1 unused
	 * @param p2 new colour for vehicles, property, etc.
	 */
	static int CmdSetPlayerColor(int x, int y, int flags, int p1, int p2)
	{
		Player p;
		byte colour;

		if (p2 >= 16) return Cmd.CMD_ERROR; // max 16 colours
		colour = (byte) p2;

		p = Player.GetCurrentPlayer();

		/* Ensure no two companies have the same colour */
		//FOR_ALL_PLAYERS(pp)
		Iterator<Player> ii = Player.getIterator();
		while(ii.hasNext())
		{
			Player pp = ii.next();
			if (pp.is_active && pp != p && pp.player_color == colour)
				return Cmd.CMD_ERROR;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			Global.gs._player_colors[PlayerID.getCurrent().id] = colour;
			p.player_color = colour;
			Hal.MarkWholeScreenDirty();
		}
		return 0;
	}

	/** Increase the loan of your company.
	 * @param x,y unused
	 * @param p1 unused
	 * @param p2 when set, loans the maximum amount in one go (press CTRL)
	 */
	static int CmdIncreaseLoan(int x, int y, int flags, int p1, int p2)
	{
		Player p;

		p = Player.GetCurrentPlayer();

		if (p.current_loan >= Global.gs._economy.getMax_loan()) {
			Global.SetDParam(0, Global.gs._economy.getMax_loan());
			return Cmd.return_cmd_error(Str.STR_702B_MAXIMUM_PERMITTED_LOAN);
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			/* Loan the maximum amount or not? */
			final int v = (PlayerID.getCurrent().IS_HUMAN_PLAYER() || Global._patches.ainew_active) ? 10000 : 50000;
			int loan = (p2 != 0) ? Global.gs._economy.getMax_loan() - p.current_loan : v;

			p.money64 += loan;
			p.current_loan += loan;
			//p.UpdatePlayerMoney32();
			p.InvalidatePlayerWindows();
		}

		return 0;
	}

	/** Decrease the loan of your company.
	 * @param x,y unused
	 * @param p1 unused
	 * @param p2 when set, pays back the maximum loan permitting money (press CTRL)
	 */
	static int CmdDecreaseLoan(int x, int y, int flags, int p1, int p2)
	{
		Player p;
		long loan;

		p = Player.GetCurrentPlayer();

		if (p.current_loan == 0) return Cmd.return_cmd_error(Str.STR_702D_LOAN_ALREADY_REPAYED);

		loan = p.current_loan;

		/* p2 is true while CTRL is pressed (repay all possible loan, or max money you have)
		 * Repay any loan in chunks of 10.000 pounds */
		if (p2 != 0) {
			loan = Math.min(loan, p.getMoney());
			loan = Math.max(loan, 10000);
			loan -= loan % 10000;
		} else {
			loan = Math.min(loan, (PlayerID.getCurrent().IS_HUMAN_PLAYER() || Global._patches.ainew_active) ? 10000 : 50000);
		}

		if (p.getMoney() < loan) {
			Global.SetDParam(0, (int)loan); // TODO long -> int
			return Cmd.return_cmd_error(Str.STR_702E_REQUIRED);
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			p.money64 -= loan;
			p.current_loan -= loan;
			//p.UpdatePlayerMoney32();
			p.InvalidatePlayerWindows();
		}
		return 0;
	}

	/** Change the name of the company.
	 * @param x,y unused
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdChangeCompanyName(int x, int y, int flags, int p1, int p2)
	{
		StringID str;
		Player p;

		if (Global._cmd_text == null) return Cmd.CMD_ERROR;

		str = Global.AllocateNameUnique(Global._cmd_text, 4);
		if (str == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			p = Player.GetCurrentPlayer();
			Global.DeleteName(p.name_1);
			p.name_1 = str.id;
			Hal.MarkWholeScreenDirty();
		} else
			Global.DeleteName(str);

		return 0;
	}

	/** Change the name of the president.
	 * @param x,y unused
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdChangePresidentName(int x, int y, int flags, int p1, int p2)
	{
		StringID str;
		Player p;

		if (Global._cmd_text == null) return Cmd.CMD_ERROR;

		str = Global.AllocateNameUnique(Global._cmd_text, 4);
		if (str == null) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			p = Player.GetCurrentPlayer();
			Global.DeleteName(p.president_name_1);
			p.president_name_1 = str.id;

			if (p.name_1 == Str.STR_SV_UNNAMED) {
				Global._cmd_text = String.format("%s Transport", Global._cmd_text);
				Cmd.DoCommandByTile(new TileIndex(0), 0, 0, Cmd.DC_EXEC, Cmd.CMD_CHANGE_COMPANY_NAME);
			}
			Hal.MarkWholeScreenDirty();
		} else
			Global.DeleteName(str);

		return 0;
	}

	/** Pause/Unpause the game (server-only).
	 * Increase or decrease the pause counter. If the counter is zero,
	 * the game is unpaused. A counter is used instead of a booleanean value
	 * to have more control over the game when saving/loading, etc.
	 * @param x,y unused
	 * @param p1 0 = decrease pause counter; 1 = increase pause counter
	 * @param p2 unused
	 */
	static int CmdPause(int x, int y, int flags, int p1, int p2)
	{
		if(0 != (flags & Cmd.DC_EXEC) ) {
			Global._pause += (p1 == 1) ? 1 : -1;
			if (Global._pause == -1) Global._pause = 0;
			Window.InvalidateWindow(Window.WC_STATUS_BAR, 0);
			Window.InvalidateWindow(Window.WC_MAIN_TOOLBAR, 0);
		}
		return 0;
	}

	/** Change the financial flow of your company.
	 * This is normally only enabled in offline mode, but if there is a debug
	 * build, you can cheat (to test).
	 * @param x,y unused
	 * @param p1 the amount of money to receive (if negative), or spend (if positive)
	 * @param p2 unused
	 */
	static int CmdMoneyCheat(int x, int y, int flags, int p1, int p2)
	{
	/*#ifndef _DEBUG
		if (_networking) return Cmd.CMD_ERROR;
	#endif*/
		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);
		return p1;
	}

	/** Transfer funds (money) from one player to another.
	 * To prevent abuse	in multiplayer games you can only send money to other
	 * players if you have paid off your loan (either explicitely, or implicitely
	 * given the fact that you have more money than loan).
	 * @param x,y unused
	 * @param p1 the amount of money to transfer; max 20.000.000
	 * @param p2 the player to transfer the money to
	 */
	static int CmdGiveMoney(int x, int y, int flags, int p1, int p2)
	{
		final Player p = Player.GetCurrentPlayer();
		int amount = Math.min(p1, 20000000);

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_OTHER);

		/* You can only transfer funds that is in excess of your loan */
		if (p.money64 - p.current_loan < amount || amount <= 0) return Cmd.CMD_ERROR;
		if (!Global._networking || p2 >= Global.MAX_PLAYERS) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			/* Add money to player */
			PlayerID old_cp = PlayerID.getCurrent();
			PlayerID.setCurrent( PlayerID.get( p2 ) );
			Player.SubtractMoneyFromPlayer(-amount);
			PlayerID.setCurrent(old_cp);
		}

		/* Subtract money from local-player */
		return amount;
	}

	/** Change difficulty level/settings (server-only).
	 * We cannot really check for valid values of p2 (too much work mostly); stored
	 * in file 'settings_gui.c' _game_setting_info[]; we'll just trust the server it knows
	 * what to do and does this correctly
	 * @param x,y unused
	 * @param p1 the difficulty setting being changed. If it is -1, the difficulty level
	 *           itself is changed. The new value is inside p2
	 * @param p2 new value for a difficulty setting or difficulty level
	 */
	static int CmdChangeDifficultyLevel(int x, int y, int flags, int p1, int p2)
	{
		if (p1 != (int)-1L && (p1 >= Global.GAME_DIFFICULTY_NUM || p1 < 0)) return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			if (p1 != (int)-1L) {
				// ((int*)&GameOptions._opt_ptr.diff)[p1] = p2;
				GameOptions._opt_ptr.diff.setAsInt(p1, p2);
				GameOptions._opt_ptr.diff_level = 3; // custom difficulty level
			} else
				GameOptions._opt_ptr.diff_level = (byte) p2;

			/* If we are a network-client, update the difficult setting (if it is open).
			 * Use this instead of just dirtying the window because we need to load in
			 * the new difficulty settings */
			if (Global._networking && !Global._network_server && Window.FindWindowById(Window.WC_GAME_OPTIONS, 0) != null)
				SettingsGui.ShowGameDifficulty();
		}
		return 0;
	}

}
