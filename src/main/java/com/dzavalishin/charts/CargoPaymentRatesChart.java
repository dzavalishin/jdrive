package com.dzavalishin.charts;

import com.dzavalishin.game.AcceptedCargo;
import com.dzavalishin.game.Economy;
import com.dzavalishin.game.Global;
import org.jfree.data.category.DefaultCategoryDataset;

import com.dzavalishin.util.Strings;

public class CargoPaymentRatesChart extends AbstractChart//JFrame// ApplicationFrame
{
	private static final long serialVersionUID = 1L;

	public CargoPaymentRatesChart() {
		super(
				"Cargo payment rates" ,
				"Money paid vs days to deliver",
				"Days", "Money"
				);
	}

	@Override
	protected DefaultCategoryDataset createDataset( ) 
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

		for(int i = 0; i!= AcceptedCargo.NUM_CARGO; i++)
		{
			int iName = Global._cargoc.names_s[i];
			String sName = Strings.GetString(iName);

			//TODO gd.colors[i] = (byte) _cargo_legend_colors[i];
			
			for(int j=0; j!=20; j++) {
				long cost = Economy.GetTransportedGoodsIncome(10, 20, j*6+6, i);
				dataset.addValue( cost , sName , Integer.toString(j*6+6) );
			}
		}


		return dataset;
	}

	public static void showChart() {
		CargoPaymentRatesChart chart = new CargoPaymentRatesChart();
		chart.display();
	}


	//public static void main( String[ ] args ) {		showChart();	}

}
