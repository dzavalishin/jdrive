package com.dzavalishin.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class WTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Test Frame");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setSize(1280, 1024);

		Dimension maximumSize = new Dimension(2560, 850);
		frame.setMaximumSize(maximumSize);

		//frame.setIconImages(icons);

		JLabel l = new JLabel("Привет");
		l.setForeground(Color.decode("#A000A0"));

		JMenuBar bar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem mi1 = new JMenuItem("Exit");
		JMenuItem mi2 = new JMenuItem("More!");

		fileMenu.add(mi2);
		fileMenu.add(mi1);

		bar.add(fileMenu);

		frame.setJMenuBar(bar);
		
		FlowLayout fl = new FlowLayout();
		frame.setLayout( fl );
		
		//frame.add(l);
		frame.setVisible(true);

		mi1.addActionListener( e -> System.exit(0) ); 
		
		mi2.addActionListener( e -> {
			JLabel l1 = new JLabel("Привет1");
			l1.setForeground(Color.BLACK);
			frame.add(l1);
			frame.validate();
		});
		
	}

}
