package com.dzavalishin.game;

import com.dzavalishin.struct.Pair;
import com.dzavalishin.xui.ViewPort;
import com.dzavalishin.xui.Window;

class Subsidy {
	int cargo_type;
	int age;
	int from;
	int to;
	public static Subsidy[] _subsidies = new Subsidy[Global.MAX_PLAYERS];

	public Subsidy() 
	{
		markInvalid();
	}

	boolean appliesTo(Station s_from, Station s_to, int req_cargo_type)
	{
		return cargo_type == req_cargo_type &&
				age >= 12 &&
				from == s_from.index &&
				to == s_to.index;		
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Subsidy) {
			Subsidy ss = (Subsidy) obj;
			return this != ss &&
					ss.from == from &&
					ss.to == to &&
					ss.cargo_type == cargo_type &&
					ss.age == age;			
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return cargo_type+age+(from<<1)+(to<<2);
	}
	
	public boolean equalsExAge(Subsidy ss) 
	{
		return this != ss &&
				ss.from == from &&
				ss.to == to &&
				ss.cargo_type == cargo_type;			
	}

	public void markInvalid() {
		cargo_type = AcceptedCargo.CT_INVALID;		
	}


	public TileIndex getFromXy()
	{
		//		int offs = from;

		/*
		if (age >= 12) {
			return Station.GetStation(offs).getXy();
		} else if (cargo_type == AcceptedCargo.CT_PASSENGERS 
				|| cargo_type == AcceptedCargo.CT_MAIL) {
			return Town.GetTown(offs).getXy();
		} else {
			return Industry.GetIndustry(offs).xy;

		}*/
		return isFromTown() ? 
				Town.GetTown(from).getXy() :
					Industry.GetIndustry(from).xy;
	}

	public TileIndex getToXy() {
		/*int offs = to;
		if (age >= 12) {
			return Station.GetStation(offs).getXy();
		} else if (cargo_type == AcceptedCargo.CT_PASSENGERS 
				|| cargo_type == AcceptedCargo.CT_MAIL 
				|| cargo_type == AcceptedCargo.CT_GOODS 
				|| cargo_type == AcceptedCargo.CT_FOOD) {
			return Town.GetTown(offs).getXy();
		} else {
			return Industry.GetIndustry(offs).xy;
		}*/

		return isToTown() ? 
				Town.GetTown(to).getXy() :
					Industry.GetIndustry(to).xy;
	}

	public boolean isValid() {
		return cargo_type != AcceptedCargo.CT_INVALID;
	}

	public boolean isFromTown()
	{
		return age < 12 && 
				(cargo_type == AcceptedCargo.CT_PASSENGERS 
				|| cargo_type == AcceptedCargo.CT_MAIL);
	}

	public boolean isToTown()
	{
		return age < 12 && 
				(cargo_type == AcceptedCargo.CT_PASSENGERS 
				|| cargo_type == AcceptedCargo.CT_MAIL
				|| cargo_type == AcceptedCargo.CT_GOODS 
				|| cargo_type == AcceptedCargo.CT_FOOD);
	}


	public void handleClick()
	{

		TileIndex xy;
		// determine from coordinate for subsidy and try to scroll to it
		/*
		int offs = s.from;

		if (s.age >= 12) {
			xy = Station.GetStation(offs).getXy();
		} else if (s.cargo_type == AcceptedCargo.CT_PASSENGERS || s.cargo_type == AcceptedCargo.CT_MAIL) {
			xy = Town.GetTown(offs).getXy();
		} else {
			xy = Industry.GetIndustry(offs).xy;

		}*/
		xy = getFromXy();
		if (!ViewPort.ScrollMainWindowToTile(xy)) {
			// otherwise determine to coordinate for subsidy and scroll to it 
			xy = getToXy();
			/*offs = s.to;
			if (s.age >= 12) {
				xy = Station.GetStation(offs).getXy();
			} else if (s.cargo_type == AcceptedCargo.CT_PASSENGERS || s.cargo_type == AcceptedCargo.CT_MAIL || s.cargo_type == AcceptedCargo.CT_GOODS || s.cargo_type == AcceptedCargo.CT_FOOD) {
				xy = Town.GetTown(offs).getXy();
			} else {
				xy = Industry.GetIndustry(offs).xy;
			}*/
			ViewPort.ScrollMainWindowToTile(xy);
		}

	}


	public Pair SetupSubsidyDecodeParam(boolean mode)
	{
		TileIndex tile;
		TileIndex tile2;
		Pair tp = new Pair();

		/* if mode is false, use the singular form */
		Global.SetDParam(0, Global._cargoc.names_s[cargo_type] + (mode ? 0 : 32));

		if (age < 12) {
			/*
			if (s.cargo_type != AcceptedCargo.CT_PASSENGERS && s.cargo_type != AcceptedCargo.CT_MAIL) {
				Global.SetDParam(1, Str.STR_INDUSTRY);
				Global.SetDParam(2, s.from);
				tile = Industry.GetIndustry(s.from).xy;

				if (s.cargo_type != AcceptedCargo.CT_GOODS && s.cargo_type != AcceptedCargo.CT_FOOD) {
					Global.SetDParam(4, Str.STR_INDUSTRY);
					Global.SetDParam(5, s.to);
					tile2 = Industry.GetIndustry(s.to).xy;
				} else {
					Global.SetDParam(4, Str.STR_TOWN);
					Global.SetDParam(5, s.to);
					tile2 = Town.GetTown(s.to).getXy();
				}
			} else {
				Global.SetDParam(1, Str.STR_TOWN);
				Global.SetDParam(2, s.from);
				tile = Town.GetTown(s.from).getXy();

				Global.SetDParam(4, Str.STR_TOWN);
				Global.SetDParam(5, s.to);
				tile2 = Town.GetTown(s.to).getXy();
			}*/

			Global.SetDParam(1, isFromTown() ? Str.STR_TOWN : Str.STR_INDUSTRY);
			Global.SetDParam(2, from);
			tile = getFromXy();

			Global.SetDParam(4, isToTown() ? Str.STR_TOWN : Str.STR_INDUSTRY);
			Global.SetDParam(5, to);
			tile2 = getToXy();


		} else {
			Global.SetDParam(1, from);
			tile = Station.GetStation(from).getXy();

			Global.SetDParam(2, to);
			tile2 = Station.GetStation(to).getXy();
		}

		tp.a = tile.tile;
		tp.b = tile2.tile;

		return tp;
	}


	static boolean CheckSubsidyDuplicate(Subsidy s)
	{
		for (Subsidy ss : _subsidies) {
			/*if (s != ss &&
					ss.from == s.from &&
					ss.to == s.to &&
					ss.cargo_type == s.cargo_type)*/
			if (ss.equalsExAge(s)) {
				s.markInvalid();
				return true;
			}
		}
		return false;
	}

	static void DeleteSubsidyWithIndustry(int index)
	{
		for(int i = 0; i < Subsidy._subsidies.length; i++) 
		{
			Subsidy s = Subsidy._subsidies[i];
			if (s.isValid() && s.age < 12 &&
					s.cargo_type != AcceptedCargo.CT_PASSENGERS && s.cargo_type != AcceptedCargo.CT_MAIL &&
					(index == s.from || (s.cargo_type!=AcceptedCargo.CT_GOODS && s.cargo_type!=AcceptedCargo.CT_FOOD && index==s.to))) {
				s.markInvalid();
			}
		}
	}

	static void DeleteSubsidyWithStation(int index)
	{
		boolean dirty = false;

		for(int i = 0; i < Subsidy._subsidies.length; i++) 
		{
			Subsidy s = Subsidy._subsidies[i];
			if (s.isValid() && s.age >= 12 &&
					(s.from == index || s.to == index)) {
				//s.cargo_type = AcceptedCargo.CT_INVALID;
				s.markInvalid();
				dirty = true;
			}
		}

		if (dirty)
			Window.InvalidateWindow(Window.WC_SUBSIDIES_LIST, 0);
	}

	public boolean updateAge() 
	{
		if (!isValid())
			return false;

		boolean modified = false;

		Pair pair;
		if (age == 12-1) 
		{
			pair = SetupSubsidyDecodeParam(true);
			NewsItem.AddNewsItem(Str.STR_202E_OFFER_OF_SUBSIDY_EXPIRED, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, NewsItem.NF_TILE, NewsItem.NT_SUBSIDIES, 0), pair.a, pair.b);
			markInvalid();
			modified = true;
		} else if (age == 2*12-1) {
			Station st = Station.GetStation(to);
			if (st.owner.isLocalPlayer()) 
			{
				pair = SetupSubsidyDecodeParam(true);
				NewsItem.AddNewsItem(Str.STR_202F_SUBSIDY_WITHDRAWN_SERVICE, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, NewsItem.NF_TILE, NewsItem.NT_SUBSIDIES, 0), pair.a, pair.b);
			}
			markInvalid();
			modified = true;
		} else {
			age++;
		}

		return modified;
	}


}