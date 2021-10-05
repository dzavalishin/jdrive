package game.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import game.Global;
import game.MainWindow;

public class PngScreenshotFormat extends ScreenshotFormat 
{
	private BufferedImage image;

	protected PngScreenshotFormat() {
		super("png", "png");
	}

	@Override
	public boolean proc(String name, ScreenshotCallback getter, Object userData, int w, int h, int i,
			Colour[] curPalette) {
	
		makeImage(w, h, curPalette);
		
		/*
		// use by default 64k temp memory
		int maxlines = BitOps.clamp(65536 / w, 16, 128);
		int maxlines = h;

		// now generate the bitmap bits
		byte[] bufd = new byte[w * maxlines]; // by default generate 128 lines at a time.		
		Pixel buff = new Pixel(bufd); 
		
		int y = 0;
		do {
			// determine # lines to write
			int n = Integer.min(h - y, maxlines);

			// render the pixels into the buffer
			getter.get(userData, buff, y, w, n);
			y += n;

			// write them to png
			for (i = 0; i != n; i++)
				writeRow(buff, i * w);
			
			
		} while (y != h);
		*/
		
		// now generate the bitmap bits
		byte[] bufd = new byte[w * h]; // by default generate 128 lines at a time.		
		Pixel buff = new Pixel(bufd); 
		getter.get(userData, buff, 0, w, h);
		image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(bufd, bufd.length), new java.awt.Point(0,0) ) );
		
		return writeFile(name);
	}

	void makeImage(int width, int height, Colour[] curPalette)
	{
		IndexColorModel icm =  MainWindow.makePalette(curPalette);
		image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);

		//image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferByte(screen, screen.length), new java.awt.Point(0,0) ) );

	}
	
	private boolean writeFile(String name) {
		File outputfile = new File(name);
		try {
			ImageIO.write(image, extension, outputfile);
		} catch (IOException e) {
			// e.printStackTrace();
			Global.error(e);
			return false;
		}		
		return true;
	}

	/*private void writeRow(Pixel buff, int i) {
		// TODO Auto-generated method stub
		
	}*/

}
