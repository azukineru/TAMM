package com.minemeander.objects;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.minemeander.Art;
import com.minemeander.Level;

public class Jewel extends GameObject implements Collectable{

	private JewelType type;
	
	public Jewel(int id, Level level, float x, float y, JewelType type) {
		super(id,level, x, y, 16, 16, CollisionCategory.COLLECTABLE, false);
		this.type = type;
	}
	
	@Override
	public int getScoreValue() {
		return type.getScoreValue();
	}

	@Override
	public TextureRegion getTextureRegion(float tick) {
		body.applyLinearImpulse(0, -1f, body.getPosition().x, body.getPosition().y, true);
		
		switch(type) {
			case BLUE: return Art.blueJewelAnimation.getKeyFrame(tick, true);
			
//			default: return Art.blueJewelAnimation.getKeyFrame(tick, true);
		}
		return null;
	}
	
}
