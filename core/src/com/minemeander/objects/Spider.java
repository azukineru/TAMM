package com.minemeander.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import com.minemeander.Art;
import com.minemeander.Level;

public class Spider extends GameObject{

	private final static float SPIDER_SPEED = 3.0f;
	private final static float SPIDER_PURSUIT_DISTANCE = 3.0f;
	
	final static Vector2 jumpRightImpulseVector = new Vector2(SPIDER_SPEED * 10, SPIDER_SPEED * 10);
	final static Vector2 jumpLeftImpulseVector = new Vector2(-SPIDER_SPEED * 10, SPIDER_SPEED * 10);

	final static Vector2 superJumpRightImpulseVector = new Vector2(SPIDER_SPEED * 50, SPIDER_SPEED * 50);
	final static Vector2 superJumpLeftImpulseVector = new Vector2(-SPIDER_SPEED * 50, SPIDER_SPEED * 50);
	
	public Spider(int id, Level level, float x, float y) {
		super(id, level, x, y, 32, 16, CollisionCategory.ENEMY, 0.4f, 0.3f, true);		
	}
	
	private boolean free = false;
	
	@Override
	public TextureRegion getTextureRegion(float tick) {
		if (free) {
			body.setGravityScale(0.5f);
			if (!userData.flying) {
				if (Math.random() <= 0.5) {
					if (Math.random() <= 0.2) {
						superJumpRight();
					}
					else if (Math.random() <= 0.2) {					
						jumpRight();
					}
				}
				else {
					if (Math.random() <= 0.2) {
						superJumpLeft();
					}
					else if (Math.random() <= 0.2) {
						jumpLeft();
					}
				}
			}	
		}
		else {
			Vector2 avatarTile = level.objectManager.getJack().getTile();
			body.setGravityScale(0f);
						
			if (getTile().dst(avatarTile) < SPIDER_PURSUIT_DISTANCE) {
				free = true;				
			}
		}
				
		return computeAnimation(tick);
	}

	private TextureRegion computeAnimation(float tick) {
		return Art.spiderAnimation.getKeyFrame(tick, true);
	}

	public void jumpRight() {
		body.applyLinearImpulse(jumpRightImpulseVector, FORCE_APPLICATION_POINT, true);
	}
	
	public void jumpLeft() {
		body.applyLinearImpulse(jumpLeftImpulseVector, FORCE_APPLICATION_POINT, true);
	}
	
	public void superJumpRight() {
		body.applyLinearImpulse(superJumpRightImpulseVector, FORCE_APPLICATION_POINT, true);
	}
	
	public void superJumpLeft() {
		body.applyLinearImpulse(superJumpLeftImpulseVector, FORCE_APPLICATION_POINT, true);
	}
	
}
