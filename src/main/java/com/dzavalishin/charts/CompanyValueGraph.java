package com.dzavalishin.charts;

import java.util.Iterator;

import com.dzavalishin.game.Player;
import org.jfree.data.category.DefaultCategoryDataset;

import com.dzavalishin.util.Strings;

public class CompanyValueGraph extends AbstractChart
{
	private static final long serialVersionUID = 1L;

	public CompanyValueGraph() {
		super(
				"Company value" ,
				"Company value vs time",
				"Days", "Money"
				);
	}

	@Override
	protected DefaultCategoryDataset createDataset( ) 
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

		/*
			for(int i=0; i!=AcceptedCargo.NUM_CARGO; i++) 
			{
				int iName = Global._cargoc.names_s[i];		
				String sName = Global.GetString(iName);

				//TODO gd.colors[i] = (byte) _cargo_legend_colors[i];

				for(int j=0; j!=20; j++) {
					long cost = (long)Economy.GetTransportedGoodsIncome(10, 20, j*6+6, i);
					dataset.addValue( cost , sName , Integer.toString(j*6+6) );
				}
			}*/


		Iterator<Player> ii = Player.getIterator();
		while(ii.hasNext())
		{
			Player p = ii.next();
			String sName = Strings.GetString(p.getName_1()) + " " + Strings.GetString(p.getName_2());

			if (p.isActive()) {
				//gd.colors[numd] = (byte) Global._color_list[p.getColor()].window_color_bgb;
				for(int j=20; --j >= 0;) 
				{
					//gd.cost[numd][i] 
					long value = (j >= p.num_valid_stat_ent) ? 0 : (long)p.old_economy[j].company_value;
					dataset.addValue( value , sName , Integer.toString(j) );

					//i++;
				}
			}
			//numd++;
		}

		return dataset;
	}

	public static void showChart() {
		CompanyValueGraph chart = new CompanyValueGraph();
		chart.display();
	}


	//public static void main( String[ ] args ) {		showChart();	}

}


