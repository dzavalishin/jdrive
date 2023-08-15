package com.dzavalishin.tables;

public class BubbleMovement {
	public final int x;
	public final int y;
	public final int z;
	public final int image;

	public BubbleMovement(int x, int y, int z, int image ) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.image = image;
	}

	//#define ME(i) { i, 4, 0, 0 }

	public BubbleMovement(int x) {
		this.x = x;
		this.y = 4;
		this.z = 0;
		this.image = 0;
	}

}




