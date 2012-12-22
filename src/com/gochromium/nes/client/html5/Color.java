package com.gochromium.nes.client.html5;

public class Color {

	public static final Color BLACK = new Color("#000000");
	
	private String color;
	private int r;
	private int g;
	private int b;
	
	public Color(String color) {
		this.color = color;
        this.r = Integer.valueOf( color.substring( 1, 3 ), 16 );
        this.g = Integer.valueOf( color.substring( 3, 5 ), 16 );
        this.b = Integer.valueOf( color.substring( 5, 7 ), 16 );
	}
	
	public Color(int r, int g, int b) {
		this.color = "rgb("+r+","+g+","+b+")";
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public int getRed() {
		return r;
	}
	
	public int getGreen() {
		return g;
	}
	
	public int getBlue() {
		return b;
	}
	
	@Override
	public String toString() {
		return color;
	}
	
	
}
