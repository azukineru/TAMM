package com.minemeander.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import com.minemeander.Art;
import com.minemeander.Level;

public class Flower extends GameObject {

	public Flower(int id, Level level, float x, float y) {
		super(id, level, x, y, 32, 32, CollisionCategory.ENEMY, 0.5f, 0.5f, true);		
	}
	
	@Override
	public TextureRegion getTextureRegion(float tick) {
		TextureRegion keyFrame = Art.flowerIdleAnimation.getKeyFrame(tick, true);
		return keyFrame;
	}

}
