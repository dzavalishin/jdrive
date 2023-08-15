package com.dzavalishin.ids;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.dzavalishin.game.Global;
import com.dzavalishin.game.Player;
import com.dzavalishin.enums.Owner;

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
	
	/** Get 'OWNER_NONE' user */
	public static PlayerID getWater() 
	{
		return get(Owner.OWNER_WATER);
	}
	
	/** Get 'OWNER_SPECTATOR' user */
	public static PlayerID getSpectator() {
		return get(Owner.OWNER_SPECTATOR);
	}

	/** Get Global.gs._local_player */
	public static PlayerID getLocal() {
		return Global.gs._local_player;
	}

	
	public boolean IS_HUMAN_PLAYER()
	{
		return !Player.GetPlayer(this).isAi();
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
		//return equals( Global.gs._current_player ); 
		return equals( getCurrent() ); 
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

	/**
	 * 
	 * @return true if not a regular player, but NONE/WATER/TOWN/etc
	 */
	public boolean isSpecial() {
		// TODO validate
		return  id >= Global.MAX_PLAYERS;
	}
	
	
	// -------------------------------------------------------------------
	// Delegates
	// -------------------------------------------------------------------

	public static PlayerID getCurrent() { return Global.gs.getCurrentPlayer(); }
	public static void setCurrent(PlayerID p) 
	{  
		Global.gs.setCurrentPlayer( p ); 
	}

	/** Set current player to no one */
	public static void setCurrentToNone() {	setCurrent(getNone()); }


	
}
