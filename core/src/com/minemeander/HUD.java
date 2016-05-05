package com.minemeander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.minemeander.objects.Jack;

public class HUD {
	
	ShapeRenderer shapeRenderer = new ShapeRenderer();
	
	public void draw(Level level, SpriteBatch spriteBatch, float timer){
		
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.begin();
		
		Jack jack = level.objectManager.getJack();
		int life = jack.getMojo();
		
		for(int i=0; i<life; i++){
			TextureRegion keyFrame = Art.heartAnimation.getKeyFrame(timer, true);
			int x = Gdx.graphics.getWidth()-220+64*i;
			int y = Gdx.graphics.getHeight()-70;
			spriteBatch.draw(keyFrame, x, y, 0, 0, 32, 32, 2f, 2f, 0f);
			
		}
		
		Art.bitmapFont.draw(spriteBatch, String.format("SCORE: %08d", 
			jack.getScore()),
			20, Gdx.graphics.getHeight()-20);
		Art.bitmapFont.draw(spriteBatch, String.format("AVATAR LIFE: %d", 
				jack.getLife()),
				20, Gdx.graphics.getHeight()-40);
		spriteBatch.end();
	}
}
