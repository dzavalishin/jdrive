package com.dzavalishin.game;

import com.dzavalishin.util.YearMonthDay;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.Widget;
import com.dzavalishin.xui.Window;
import com.dzavalishin.xui.WindowDesc;
import com.dzavalishin.xui.WindowEvent;

public class Subsidies {


	/*
	private static void doHandleClick( Subsidy s )
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

		}* /
		xy = s.getFromXy();
		if (!ViewPort.ScrollMainWindowToTile(xy)) {
			// otherwise determine to coordinate for subsidy and scroll to it 
			xy = s.getToXy();
			/*offs = s.to;
			if (s.age >= 12) {
				xy = Station.GetStation(offs).getXy();
			} else if (s.cargo_type == AcceptedCargo.CT_PASSENGERS || s.cargo_type == AcceptedCargo.CT_MAIL || s.cargo_type == AcceptedCargo.CT_GOODS || s.cargo_type == AcceptedCargo.CT_FOOD) {
				xy = Town.GetTown(offs).getXy();
			} else {
				xy = Industry.GetIndustry(offs).xy;
			}* /
			ViewPort.ScrollMainWindowToTile(xy);
		}

	}*/

	private static void HandleSubsidyClick(int y)
	{
		//final Subsidy  s;
		int num;
		//int offs;

		if (y < 0) return;

		num = 0;
		//for (s = _subsidies; s != endof(_subsidies); s++)
		for( Subsidy s : Subsidy._subsidies )
		{
			if (s.isValid() && s.age < 12) {
				y -= 10;
				if (y < 0) 
				{
					//goto handle_click;
					s.handleClick();
					return;
				}
				num++;
			}
		}

		if (num == 0) {
			y -= 10;
			if (y < 0) return;
		}

		y -= 11;
		if (y < 0) return;

		//for (s = _subsidies; s != endof(_subsidies); s++) 
		for( Subsidy s : Subsidy._subsidies )
		{
			if (s.cargo_type != AcceptedCargo.CT_INVALID && s.age >= 12) {
				y -= 10;
				if (y < 0) {
					//goto handle_click;
					s.handleClick();
					return;
				}
			}
		}
		

		/*
		handle_click:

			// determine from coordinate for subsidy and try to scroll to it 
			offs = s.from;
		if (s.age >= 12) {
			xy = Station.GetStation(offs).xy;
		} else if (s.cargo_type == AcceptedCargo.CT_PASSENGERS || s.cargo_type == AcceptedCargo.CT_MAIL) {
			xy = GetTown(offs).xy;
		} else {
			xy = GetIndustry(offs).xy;

		}
		if (!ScrollMainWindowToTile(xy)) {
			// otherwise determine to coordinate for subsidy and scroll to it 
			offs = s.to;
			if (s.age >= 12) {
				xy = Station.GetStation(offs).xy;
			} else if (s.cargo_type == AcceptedCargo.CT_PASSENGERS || s.cargo_type == AcceptedCargo.CT_MAIL || s.cargo_type == AcceptedCargo.CT_GOODS || s.cargo_type == AcceptedCargo.CT_FOOD) {
				xy = GetTown(offs).xy;
			} else {
				xy = GetIndustry(offs).xy;
			}
			ViewPort.ScrollMainWindowToTile(xy);
		}
		 */
	}

	static void DrawSubsidiesWindow(final Window  w)
	{
		//final Subsidy  s;
		int num;
		int x;
		int y;

		w.DrawWindowWidgets();

		YearMonthDay ymd = new YearMonthDay(Global.get_date());
		//YearMonthDay.ConvertDayToYMD(ymd, Global._date);

		y = 15;
		x = 1;
		Gfx.DrawString(x, y, Str.STR_2026_SUBSIDIES_ON_OFFER_FOR, 0);
		y += 10;
		num = 0;

		//for (s = _subsidies; s != endof(_subsidies); s++) 
		for( Subsidy s : Subsidy._subsidies )
		{
			if (s.isValid() && s.age < 12) {
				int x2;

				s.SetupSubsidyDecodeParam(true);
				x2 = Gfx.DrawString(x + 2, y, Str.STR_2027_FROM_TO, 0);

				Global.SetDParam(0, Global.get_date() - ymd.day + 384 - s.age * 32);
				Gfx.DrawString(x2, y, Str.STR_2028_BY, 0);
				y += 10;
				num++;
			}
		}

		if (num == 0) {
			Gfx.DrawString(x + 2, y, Str.STR_202A_NONE, 0);
			y += 10;
		}

		Gfx.DrawString(x, y + 1, Str.STR_202B_SERVICES_ALREADY_SUBSIDISED, 0);
		y += 10;
		num = 0;

		//for (s = _subsidies; s != endof(_subsidies); s++) 
		for( Subsidy s : Subsidy._subsidies )
		{
			if (s.isValid() && s.age >= 12) {
				final Player  p;
				int xt;

				s.SetupSubsidyDecodeParam(true);

				p = Player.GetPlayer(Station.GetStation(s.to).owner);
				Global.SetDParam(3, p.name_1);
				Global.SetDParam(4, p.name_2);

				xt = Gfx.DrawString(x + 2, y, Str.STR_202C_FROM_TO, 0);

				Global.SetDParam(0, Global.get_date() - ymd.day + 768 - s.age * 32);
				Gfx.DrawString(xt, y, Str.STR_202D_UNTIL, 0);
				y += 10;
				num++;
			}
		}

		if (num == 0) Gfx.DrawString(x + 2, y, Str.STR_202A_NONE, 0);
	}

	static void SubsidiesListWndProc(Window w, WindowEvent e)
	{
		switch (e.event) {
		case WE_PAINT: DrawSubsidiesWindow(w); break;

		case WE_CLICK:
			switch (e.widget) {
			case 2: HandleSubsidyClick(e.pt.y - 25); break;
			}
			break;
		default:
			break;
		}
	}

	static final Widget _subsidies_list_widgets[] = {
			new Widget(   Window.WWT_CLOSEBOX,   Window.RESIZE_NONE,    13,     0,    10,     0,    13, Str.STR_00C5, Str.STR_018B_CLOSE_WINDOW),
			new Widget(    Window.WWT_CAPTION,   Window.RESIZE_NONE,    13,    11,   629,     0,    13, Str.STR_2025_SUBSIDIES, Str.STR_018C_WINDOW_TITLE_DRAG_THIS),
			new Widget(      Window.WWT_PANEL,   Window.RESIZE_NONE,    13,     0,   629,    14,   126, 0x0, Str.STR_01FD_CLICK_ON_SERVICE_TO_CENTER),
	};

	static final WindowDesc _subsidies_list_desc = new WindowDesc(
			-1, -1, 630, 127,
			Window.WC_SUBSIDIES_LIST,0,
			WindowDesc.WDF_STD_TOOLTIPS | WindowDesc.WDF_STD_BTN | WindowDesc.WDF_DEF_WIDGET,
			_subsidies_list_widgets,
			Subsidies::SubsidiesListWndProc
			);


	public static void ShowSubsidiesList()
	{
		Window.AllocateWindowDescFront(_subsidies_list_desc, 0);
	}

}
