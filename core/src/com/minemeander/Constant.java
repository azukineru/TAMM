package com.minemeander;

public class Constant {
	public static final String LADDER_ZONE = "1";
	public static final String SOLID_ZONE = "1";
	public static final String HAZARD_ZONE = "2";

	public static float ZOOM_FACTOR = 1;
	
	public static int METERS_PER_TILE = 2;	
	public static int TILE_SIZE_IN_PIXELS = 32;
	public static int METERS_TO_PIXELS_RATIO = TILE_SIZE_IN_PIXELS / METERS_PER_TILE; // pixels per meters
		
	public static int[] PARALLAX_LAYERS = { 0 };
	public static int[] BACKGROUND_LAYERS = { 1, 2 };
	public static int LADDER_LAYER = 2;
	public static int PLATFORM_LAYER = 1;
	public static int PARRALAX_LAYER = 0;
	public static int SPRITE_LAYER = 3;
	public static int[] SPRITE_LAYERS = { 3 };
}
