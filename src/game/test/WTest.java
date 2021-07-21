package game.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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


		mi1.addActionListener( new ActionListener() 
			{
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);				
			}

		});

		mi2.addActionListener( new ActionListener() 
		{
		@Override
		public void actionPerformed(ActionEvent e) {
			JLabel l = new JLabel("Привет1");
			l.setForeground(Color.BLACK);
			frame.add(l);
			frame.validate();
			//frame.invalidate();
			//frame.repaint();
			//frame.setVisible(false);
			//frame.setVisible(true);


		}

	});
		
	}

}
