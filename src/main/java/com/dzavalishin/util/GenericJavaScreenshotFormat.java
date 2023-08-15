package com.dzavalishin.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.dzavalishin.game.Global;
import com.dzavalishin.game.MainWindow;

/**
 * 
 * Uses super.extension to determine file format.
 * 
 * See ImageIO.write(image, __extension__, outputfile);
 * 
 * @author dz
 *
 */
public abstract class GenericJavaScreenshotFormat extends ScreenshotFormat 
{
	private BufferedImage image;


	protected GenericJavaScreenshotFormat(String name, String extension) {
		super(name, extension);
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
	
	/* TODO fails - in Russian locale file name is in Russian, but encoding is wrong
	// TODO Cyrillic only. Can we support other languages? Locale?
	public String convert(String s) throws UnsupportedEncodingException {
	    //final byte[] bytes = s.getBytes("Windows-1251");
		//return new String(bytes, "UTF-8");

		final byte[] bytes = s.getBytes("UTF-8");
		return new String(bytes, "Windows-1251");
	} */
	
	private boolean writeFile(String name) {
		
		/*try {
			name = convert(name);
		} catch (UnsupportedEncodingException e1) {
			// e1.printStackTrace();
			Global.error(e1);
		}*/
		
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
		
	}*/

}
