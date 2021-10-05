package game.struct;

import game.Global;
import game.Player;
import game.Str;
import game.util.Strings;

public class HighScore 
{
	public String	company = "";
	public String	title = ""; 
	public int 		score = 0;

	public void initFromPlayer(Player p, int score) 
	{
		Global.SetDParam(0, p.getPresident_name_1());
		Global.SetDParam(1, p.getPresident_name_2());
		Global.SetDParam(2, p.getName_1());
		Global.SetDParam(3, p.getName_2());
		company = Strings.GetString(Str.STR_HIGHSCORE_NAME); // get manager/company name string

		this.score = score; //p.old_economy[0].performance_history;
		title = Strings.GetString( Player.EndGameGetPerformanceTitleFromValue(score) );
	}
}

