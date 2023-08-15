package com.dzavalishin.charts;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

public abstract class AbstractChart extends JFrame 
{
	private static final long serialVersionUID = 1L;

	abstract DefaultCategoryDataset createDataset(); 
	
	protected AbstractChart( 
			String applicationTitle , String chartTitle,
			String legendX, String legendY
			) 
	{
		super(applicationTitle);
		JFreeChart lineChart = ChartFactory.createLineChart(
				chartTitle,
				legendX, legendY,
				createDataset(),
				PlotOrientation.VERTICAL,
				true,true,false);

		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 660 , 367 ) );
		setContentPane( chartPanel );
	}

	protected void display() {
		pack();
		RefineryUtilities.centerFrameOnScreen( this );
		setVisible( true );
	}
	
}
