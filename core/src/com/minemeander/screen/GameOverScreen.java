package com.minemeander.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.minemeander.Art;

public class GameOverScreen extends AbstractScreen{
SpriteBatch spriteBatch = new SpriteBatch();
	
	public GameOverScreen() {
		fadeIn();
	}
	
	@Override
	public void render(float delta) {
		
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		spriteBatch.begin();
								
		Art.bitmapFont.draw(spriteBatch, "GAME OVER", Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);				
		spriteBatch.end();
		
		super.renderCurtain();
	}
	
	@Override
	protected void onFadeInTermination() {
		fadeOut();
	}
	
	@Override
	protected void onFadeOutTermination() {
		transitionTo(new MainMenu());
	}
}
