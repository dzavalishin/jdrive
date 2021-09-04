package game.ids;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import game.Global;
import game.Player;
import game.enums.Owner;

public class PlayerID extends AbstractID implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private PlayerID(int i) {
		super(i);
	}

	public Player GetPlayer() {
		return Player.GetPlayer(id);
	}


	private static final Map<Integer,PlayerID> ids = new HashMap<>();
	
	public static PlayerID get(int player) 
	{
		/*PlayerID old = ids.get(player);
		if( old == null ) 
		{
			old = new PlayerID(player);
			ids.put(player, old);		
		}*/
		
		//PlayerID old = ids.computeIfAbsent(player, k -> new PlayerID(k));
		PlayerID old = ids.computeIfAbsent(player, PlayerID::new );		
		assert player == old.id;
		return old;
	}

	/** Get 'OWNER_NONE' user */
	public static PlayerID getNone() 
	{
		return get(Owner.OWNER_NONE);
	}
	
	

	public boolean IS_HUMAN_PLAYER()
	{
		return 0 == Player.GetPlayer(this).is_ai;
	}

	public boolean IS_INTERACTIVE_PLAYER()
	{
		return equals( Global.gs._local_player );
	}

	public boolean isLocalPlayer()
	{
		return equals( Global.gs._local_player );
	}

	public boolean isCurrentPlayer() 
	{ 
		return equals( Global.gs._current_player ); 
	}

	public boolean isSpectator()
	{
		return id == Owner.OWNER_SPECTATOR;
	}
	
	/** Owner is water */
	public boolean isWater()
	{
		return id == Owner.OWNER_WATER;
	}
		
	/** Owner is town */
	public boolean isTown()
	{
		return id == Owner.OWNER_TOWN;
	}	
	
	/** No owner */
	public boolean isNone()
	{
		return id == Owner.OWNER_NONE;
	}	
	
	/** Not OWNER_NONE */
	public boolean isNotNone()
	{
		return id != Owner.OWNER_NONE;
	}	
	
	/*
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlayerID) {
			PlayerID him = (PlayerID) obj;
			return him.id == id;
		}
		return false;
	}*/

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public void assertValid() 
	{
		assertValid(id);
	}

	public static void assertValid(int i) {
		if( i < 0 || i >= Global.gs._players.length )
			throw new IllegalArgumentException("Invalid PlayerID: " + i);		
	}

	/**
	 * Valid and not special value as NONE/WATER/TOWN/SPECTATOR
	 * @return true if player is human/ai
	 */
	public boolean isValid() {
		return id >= 0 && id < Global.MAX_PLAYERS;
	}

	
	
}
