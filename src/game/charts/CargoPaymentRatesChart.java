package game.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import game.AcceptedCargo;
import game.Economy;
import game.Global;

public class CargoPaymentRatesChart extends ApplicationFrame
{

	public CargoPaymentRatesChart( String applicationTitle , String chartTitle ) {
		super(applicationTitle);
		JFreeChart lineChart = ChartFactory.createLineChart(
				chartTitle,
				"Years","Number of Schools",
				createDataset(),
				PlotOrientation.VERTICAL,
				true,true,false);

		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		setContentPane( chartPanel );
	}

	private DefaultCategoryDataset createDataset( ) 
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset( );

		/*
	      dataset.addValue( 15 , "schools" , "1970" );
	      dataset.addValue( 30 , "schools" , "1980" );
	      dataset.addValue( 60 , "schools" ,  "1990" );
	      dataset.addValue( 120 , "schools" , "2000" );
	      dataset.addValue( 240 , "schools" , "2010" );
	      dataset.addValue( 300 , "schools" , "2014" );
		 */
		for(int i=0; i!=AcceptedCargo.NUM_CARGO; i++) 
		{
			int iName = Global._cargoc.names_s[i];		
			//String sName = Global.GetString(iName);
			String sName = "Cargo "+i;
			//gd.colors[i] = (byte) _cargo_legend_colors[i];
			for(int j=0; j!=20; j++) {
				long cost = (long)Economy.GetTransportedGoodsIncome(10, 20, j*6+6,i);
				dataset.addValue( cost , sName , Integer.toString(j) );
			}
		}


		return dataset;
	}

	public static void showChart() {
		CargoPaymentRatesChart chart = new CargoPaymentRatesChart(
				"Cargo payment rates" ,
				"Money paid vs days to deliver");

		chart.pack( );
		RefineryUtilities.centerFrameOnScreen( chart );
		chart.setVisible( true );
	}

	public static void main( String[ ] args ) {
		showChart();
	}

}
