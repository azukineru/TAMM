package com.minemeander;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class Art {

private static final int TILESIZE = 32;
	
	public static Sound startSound;
	public static Sound coinSound;
	public static Sound hurtSound;
	public static Sound jumpSound;
	
	// Jack
	public static Animation walkingRightAnimation;
	public static Animation walkingLeftAnimation;
	public static Animation climbingAnimation;
	
	// Zombie
	public static Animation zombieWalkingRightAnimation;
	public static Animation zombieWalkingLeftAnimation;
	public static Animation zombieClimbAnimation;
	public static Animation eskimoWalkingLeftAnimation;
	public static Animation eskimoWalkingRightAnimation;
	public static Animation eskimoClimbAnimation;
	
	// Spider
	public static Animation spiderAnimation;
	
	// Flower 
	public static Animation flowerIdleAnimation;
	
	// Bonus
	public static Animation blueJewelAnimation;
	public static Animation bigBlueJewelAnimation;
	
	// HUD
	public static Animation heartAnimation;

	public static TextureRegion heartStaticTexture;
	
	public static BitmapFont bitmapFont;

	// Menu
	public static AtlasRegion jackBenoitLogo;

	
			
	public static void load(TextureAtlas atlas) {	
		// Menu
		jackBenoitLogo = atlas.findRegion("logo");		
		
		bitmapFont = new BitmapFont(Gdx.files.getFileHandle("data/output/font/kromasky20.fnt", FileType.Internal), 
				Gdx.files.getFileHandle("data/output/font/kromasky20.png", FileType.Internal), false);
		//Art.bitmapFont.scale(1.1f);
		
		TextureRegion[] heartAnimationTextures = atlas.findRegion("heart").split(TILESIZE, TILESIZE)[0];		
		heartAnimation = new Animation(0.2f, heartAnimationTextures);
		heartStaticTexture = heartAnimationTextures[0];
		
		// Jack
		TextureRegion[] walkingTextures = atlas.findRegion("walk").split(TILESIZE, TILESIZE)[0];		
		walkingRightAnimation = new Animation(0.1f, walkingTextures);
		
		TextureRegion[] flippedWalkingTextures = atlas.findRegion("walk").split(TILESIZE, TILESIZE)[0];
		for (TextureRegion textureRegion : flippedWalkingTextures) {
			textureRegion.flip(true, false);
		}		
		walkingLeftAnimation = new Animation(0.1f, flippedWalkingTextures);
				
		TextureRegion[] climbTextures = atlas.findRegion("Climb").split(TILESIZE, TILESIZE)[0];
		climbingAnimation = new Animation(0.1f, climbTextures);
		
		// Flower
		TextureRegion[] flowerIdleTextures = atlas.findRegion("flower-idle").split(TILESIZE, TILESIZE)[0];
		flowerIdleAnimation = new Animation(0.2f, flowerIdleTextures);
		
		loadZombie(atlas);
		loadEskimo(atlas);
		
		TextureRegion[] spiderTextures = atlas.findRegion("spider0").split(TILESIZE, 16)[0];
		System.out.println(spiderTextures.length);
		spiderAnimation = new Animation(0.2f, spiderTextures);
				
		// Goodies
		TextureRegion[] blueJewelTextures = atlas.findRegion("crystal-qubodup-ccby3-16-blue").split(16, 16)[0];		
		blueJewelAnimation = new Animation(0.1f, blueJewelTextures);
		TextureRegion[] bigBlueJewelTextures = atlas.findRegion("crystal-qubodup-ccby3-32-blue").split(32, 32)[0];		
		bigBlueJewelAnimation = new Animation(0.1f, bigBlueJewelTextures);
		
		
		coinSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/output/sound/coin2.wav", FileType.Internal));
		hurtSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/output/sound/hurt.wav", FileType.Internal));
		jumpSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/output/sound/jump.wav", FileType.Internal));
		startSound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/output/sound/start.mp3", FileType.Internal));
	}



	private static void loadZombie(TextureAtlas atlas) {
		TextureRegion[] zombieWalkingTextures = atlas.findRegion("zombie_walk").split(TILESIZE, TILESIZE)[0];		
		zombieWalkingRightAnimation = new Animation(0.2f, zombieWalkingTextures);

		TextureRegion[] zombieFlippedWalkingTextures = atlas.findRegion("zombie_walk").split(TILESIZE, TILESIZE)[0];
		for (TextureRegion textureRegion : zombieFlippedWalkingTextures) {
			textureRegion.flip(true, false);
		}		
		zombieWalkingLeftAnimation = new Animation(0.2f, zombieFlippedWalkingTextures);
		TextureRegion[] zombieClimbTextures = atlas.findRegion("zombie_climb").split(TILESIZE, TILESIZE)[0];
		zombieClimbAnimation = new Animation(0.2f, zombieClimbTextures);
	}
	
	private static void loadEskimo(TextureAtlas atlas) {
		TextureRegion[] zombieWalkingTextures = atlas.findRegion("eskimo_walk").split(TILESIZE, TILESIZE)[0];		
		eskimoWalkingRightAnimation = new Animation(0.2f, zombieWalkingTextures);

		TextureRegion[] zombieFlippedWalkingTextures = atlas.findRegion("eskimo_walk").split(TILESIZE, TILESIZE)[0];
		for (TextureRegion textureRegion : zombieFlippedWalkingTextures) {
			textureRegion.flip(true, false);
		}		
		eskimoWalkingLeftAnimation = new Animation(0.2f, zombieFlippedWalkingTextures);
		TextureRegion[] zombieClimbTextures = atlas.findRegion("eskimo_climb").split(TILESIZE, TILESIZE)[0];
		eskimoClimbAnimation = new Animation(0.2f, zombieClimbTextures);
	}
	
}
