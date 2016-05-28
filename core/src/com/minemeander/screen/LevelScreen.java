package com.minemeander.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.minemeander.Constant;
import com.minemeander.Controller;
import com.minemeander.Level;
import com.minemeander.objects.Jack;

public class LevelScreen extends AbstractScreen{
	
	private Level level;
	public static SpriteBatch spriteBatch = new SpriteBatch();
	private long lastKeyTime = System.currentTimeMillis();
	private float timer = 0f;	
	private Box2DDebugRenderer box2dDebugRenderer;
	private boolean pause;	
	public static final float PPM = 100;
	
	OrthographicCamera camera;
	Viewport viewport;
	public static Controller controller;	
	Jack jack;
	
	public LevelScreen(int worldId) {
		level = new Level(this, worldId);		
		viewport = new FitViewport(900/PPM, 600/PPM, camera);
		controller = new Controller();
		fadeIn();
	}
		
	public void handleInput(){
		if(controller.isRightPressed()){
			//System.out.printf("Pressed Right Button\n");
			//jack.getRightThrust();
		}
		else if(controller.isLeftPressed()){
			//System.out.printf("Pressed Left Button\n");
			//jack.getLeftThrust();
		}
		if(controller.isUpPressed()){
			//System.out.printf("Pressed Up Button\n");
			//jack.getRightThrust();
		}
		else if(controller.isDownPressed()){
			//System.out.printf("Pressed Down Button\n");
			//jack.getLeftThrust();
		}
	}
	
	public void update(float dt){
		handleInput();
		//camera.update();
	}
	
	public void reset(){
		fadeOut();
	}

	
	@Override
	protected void onFadeOutTermination() {
		level.objectManager.reset();
		level.camera.focusOnJack(level.objectManager.getJack());
		controller = new Controller();
		fadeIn();
	}
	
	@Override
	public void render(float delta) {
		update(Gdx.graphics.getDeltaTime());
		if (Gdx.input.isKeyPressed(Input.Keys.F1) && (System.currentTimeMillis()-lastKeyTime)>100) {
			level.debugMode = !level.debugMode;			
			lastKeyTime=System.currentTimeMillis();
		}
		
		float deltaTime = pause ? 0 : Gdx.graphics.getDeltaTime();
		timer += deltaTime;
		level.step(deltaTime, 8, 3);
		
		level.camera.update(level.objectManager.getJack());
		
		// Render map
		level.tiledMapRenderer.setView(level.camera.parrallax);
		level.tiledMapRenderer.render(Constant.PARALLAX_LAYERS);
		level.tiledMapRenderer.setView(level.camera.front);
		level.tiledMapRenderer.render(Constant.BACKGROUND_LAYERS);
		
		// Render sprites
		spriteBatch.getProjectionMatrix().set(level.camera.front.combined);
		spriteBatch.begin();
		level.objectManager.draw(spriteBatch, timer);		
		spriteBatch.end();
		
		// Render HUD		
		level.hud.draw(level, spriteBatch, timer);
		
		// Render Controller
		controller.draw();
		
		// Display info
		if (level.debugMode) {			
			if (box2dDebugRenderer == null) {
				box2dDebugRenderer = new Box2DDebugRenderer();
				box2dDebugRenderer.setDrawJoints(true);
				box2dDebugRenderer.setDrawBodies(true);
				box2dDebugRenderer.setDrawInactiveBodies(true);
			}
		
			box2dDebugRenderer.render(level.physicalWorld, level.camera.front.combined);
			spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			spriteBatch.begin();
			level.font.draw(spriteBatch, getStatusString(), 20, 460);
			spriteBatch.end();			
			if (level.debugMode) {
				level.objectManager.drawDebugInfo();
				}				
		}
		// Select and Render the level
		super.renderCurtain();
	}
	
	private String getStatusString() {
		Jack jack = level.objectManager.getJack();
		return jack.toString();
	}

	public void pause() {
		this.pause = true;
	}
	
}
