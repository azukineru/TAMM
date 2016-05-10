package com.minemeander.objects;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import com.minemeander.Constant;
import com.minemeander.Level;
import com.minemeander.ai.PathNode;

public abstract class GameObject {
	public static final int NO_LADDER = 0;
	public static final int LADDER = 1;
	public static final int LADDER_BELOW = 2;
	public static final int NO_CLIFF = 0;
	
	public static int MAX_NODE_LIST_SIZE = 1000;
	public static Vector2 FORCE_APPLICATION_POINT = new Vector2(32, 32);
	
	public int id;
	public Body body;
	public Fixture fixture;	
	public Level level;
	public float width = 2f;
	public float height = 2f;
	public GameObjectData userData;
	
	public GameObject(int id, Level level, float x, float y, CollisionCategory category, boolean bullet) {				
		init(id, level, x, y, 32, 32, category, 0.2f, 0.5f, bullet);
	}

	public GameObject(int id, Level level, float x, float y, int pixelWidth, int pixelHeight, CollisionCategory category, boolean bullet) {
		init(id, level, x, y, pixelWidth, pixelHeight, category, 0.2f, 0.5f, bullet);
	}

	public GameObject(int id, Level level, float x, float y, int pixelWidth, int pixelHeight, CollisionCategory category, float widthRatio, float heightRatio, boolean bullet) {
		init(id, level, x, y, pixelWidth, pixelHeight, category, widthRatio, heightRatio, bullet);
	}
	
	private void init(int id, Level level, float x, float y, int pixelWidth, int pixelHeight, CollisionCategory category, float widthRatio, float heightRatio, boolean bullet) {
		this.id = id;
		this.width = Constant.METERS_PER_TILE * pixelWidth / 32f;
		this.height = Constant.METERS_PER_TILE * pixelHeight / 32f;
					
		BodyDef bodyDef = getBodyDef(x, y);
		body = level.physicalWorld.createBody(bodyDef);
		userData = new GameObjectData(id, category);
		
		body.setUserData(userData);		
		body.setBullet(bullet);		
		customizeBody(body);
				
		createFixtures(level, body, widthRatio, heightRatio);		
		this.level=level;
	}

	public void customizeBody(Body body) {		
	}

	protected void createFixtures(Level level, Body body, float widthRatio, float heightRatio) {
		PolygonShape polyShape = new PolygonShape();
		polyShape.setAsBox(width*widthRatio, height*heightRatio);				
		fixture = body.createFixture(polyShape, 5);
		fixture.setFriction(0f);
		polyShape.dispose();		
	}

	protected BodyDef getBodyDef(float x, float y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		bodyDef.gravityScale = 0.9f;	
		bodyDef.fixedRotation = true;
		bodyDef.linearDamping = 8f;
		
		return bodyDef;
	}

	public abstract TextureRegion getTextureRegion(float tick);

	public void renderDebugInfo() {
	}
	
	public void render(SpriteBatch spriteBatch, float tick) {
		GameObjectData userData = (GameObjectData)body.getUserData();
		if (userData!= null) {
			if (!userData.hidden) {			
				TextureRegion textureRegion = getTextureRegion(tick);
				if (textureRegion == null) {
					return;
				}
				Vector2 position = body.getPosition();
				if (this instanceof Jack) {
					float orientation = ((Jack)this).getOrientation();
					spriteBatch.draw(textureRegion, position.x-(width/2), position.y-(height/2), 1f, 1f, width, height, 1f, 1f, orientation);
				}
				else {
					spriteBatch.draw(textureRegion, position.x-(width/2), position.y-(height/2), width, height);
				}
			}
			else {
				destroy();
			}
		}
	}

	/**
	 * 0 : no ladder
	 * 1 : ladder + no ladder below
	 * 2 : no ladder + ladder below
	 * 3 : ladder + ladder below 
	 * 
	 * @param camera
	 * @return
	 */
	public int getLadderStatus() {
		Vector2 position = body.getPosition();		
		float y = position.y-1;
		
		Vector2 currentTile = getTile();
		int tileX = (int)currentTile.x;		
		int tileY = (int)currentTile.y;
		
		TiledMapTileLayer ladderLayer = (TiledMapTileLayer)level.tiledMap.getLayers().get(Constant.LADDER_LAYER);
		int layerWidth = ladderLayer.getWidth();
		int layerHeight = ladderLayer.getHeight();
		
		int ladderStatus = 0;
		if (tileY >= 0 && tileY < layerHeight && tileX >= 0 && tileX < layerWidth) {
			Cell cell = ladderLayer.getCell(tileX, tileY);
			if (cell!=null && "1".equals(cell.getTile().getProperties().get("ladder"))) {			
				ladderStatus+=LADDER;
			}
		}
		
		y = position.y-1.2f;
		tileY = (int)(y/Constant.METERS_PER_TILE);
		
		if (tileY < ladderLayer.getHeight()) {			
			Cell cell = ladderLayer.getCell(tileX, tileY);
			if (cell!=null && "1".equals(cell.getTile().getProperties().get("ladder"))) {		
				ladderStatus+=LADDER_BELOW;
			}
		}
		return ladderStatus;
	}
	
	public boolean isTopOfTheLadder() {						
		Vector2 currentTile = getTile();
		int tileX = (int)currentTile.x;		
		int tileY = (int)currentTile.y + 1;		

		TiledMapTileLayer ladderLayer = (TiledMapTileLayer)level.tiledMap.getLayers().get(Constant.LADDER_LAYER);	
		if (tileY < ladderLayer.getHeight()) { 						
			Cell cell = ladderLayer.getCell(tileX, tileY);
			if (cell!=null && "1".equals(cell.getTile().getProperties().get("ladder"))) {					
				return false;
			}
		}
		return true;
	}
	
	private Vector2 getTileVector = new Vector2();
	public Vector2 getTile() {
		Vector2 position = body.getPosition();	
		int tileX = (int) (position.x/Constant.METERS_PER_TILE);				
		int tileY = (int) (position.y/Constant.METERS_PER_TILE);		
		return getTileVector.set(tileX, tileY);		
	}
	
	public Vector2 findWayPointTile(ArrayList<PathNode> wayPointContainer, Vector2 targetPosition) {					
		return level.pathingTool.findWayPointTile(wayPointContainer, getTile(), targetPosition);
	}
	
	public Vector2 findRandomWayPointTile(ArrayList<PathNode> wayPointContainer, int steps) {					
		Vector2 wayPointTile = level.pathingTool.findRandomWayPointTile(wayPointContainer, getTile(), steps);
		return wayPointTile;
	}
	
	
	@Override
	public String toString() {	
		return "[" + String.valueOf(id) + "] " +userData.toString();
	}
	
	public void destroy() {
		level.physicalWorld.destroyBody(body);
	}	
	
}
