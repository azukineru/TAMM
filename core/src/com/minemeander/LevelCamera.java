package com.minemeander;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.minemeander.objects.Jack;

public class LevelCamera {
	public OrthographicCamera front;
	public OrthographicCamera parrallax;
	
	private int viewPortWidthInMeters;
	private int viewPortHeightInMeters;
	private float zoom;
	private int levelHeightInMeters;
	private int levelWidthInMeters;
	public Body cameraBody;
	
	public LevelCamera(Level level, float initialX, float initialY) {
		TiledMapTileLayer layer = (TiledMapTileLayer)level.tiledMap.getLayers().get(0);
		
		this.viewPortWidthInMeters = (int) ((Gdx.graphics.getWidth() / 32) * Constant.METERS_PER_TILE);
		this.viewPortHeightInMeters = (int) ((Gdx.graphics.getHeight() / 32) * Constant.METERS_PER_TILE);
		this.levelWidthInMeters = layer.getWidth() * Constant.METERS_PER_TILE;
		this.levelHeightInMeters = layer.getHeight() * Constant.METERS_PER_TILE;
		this.zoom = Constant.ZOOM_FACTOR;
		this.front = new OrthographicCamera(viewPortWidthInMeters, viewPortHeightInMeters);
		this.parrallax = new OrthographicCamera(viewPortWidthInMeters, viewPortHeightInMeters);
		
		this.front.position.x = initialX;
		this.front.position.y = initialY;
		
		this.front.zoom = zoom;
		this.parrallax.zoom = zoom;
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		bodyDef.position.x = front.position.x;
		bodyDef.position.y = front.position.y;				
		cameraBody = level.physicalWorld.createBody(bodyDef);		
	}
	
	public void update(Jack jack) {
		front.update();
		parrallax.update();		
		smoothTrackJack(jack);
	}
	
	Vector3 jackScreenPosition = new Vector3();
	
	public void focusOnJack(Jack jack) {			
		Vector2 jackWorldPosition = jack.body.getPosition();
		this.front.position.x = jackWorldPosition.x;
		this.front.position.y = jackWorldPosition.y;
		cameraBody.setTransform(jackWorldPosition.x, jackWorldPosition.y, 0);		
		cameraBody.setLinearVelocity(0,0);
	}
	
private void smoothTrackJack(Jack jack) {		
		
		if (jack.isDead()) {
			return;
		}
		
		Vector2 jackWorldPosition = jack.body.getPosition();		
		jackScreenPosition.x = jackWorldPosition.x;
		jackScreenPosition.y = jackWorldPosition.y;
		front.project(jackScreenPosition);
		
		Vector2 cameraBodyPosition = cameraBody.getPosition();		
				
		int halfViewPortWidth = viewPortWidthInMeters / 2;
		int halfViewPortHeight = viewPortHeightInMeters / 2;
		
		Vector2 camVector = jackWorldPosition.sub(front.position.x, front.position.y).scl(2);
				
		if (cameraBodyPosition.x < halfViewPortWidth * zoom) {					
			cameraBodyPosition.x = halfViewPortWidth * zoom;				
			if (camVector.x < 0) {
				camVector.x = 0;
			}
		}
		if (cameraBodyPosition.x > levelWidthInMeters - halfViewPortWidth * zoom) {				
			cameraBodyPosition.x = levelWidthInMeters - halfViewPortWidth * zoom;			
			if (camVector.x > 0) {
				camVector.x = 0;
			}
		}
		if (cameraBodyPosition.y < halfViewPortHeight* zoom) {			
			cameraBodyPosition.y = halfViewPortHeight* zoom;			
			if (camVector.y < 0) {
				camVector.y = 0;
			}
		}
		if (cameraBodyPosition.y > levelHeightInMeters - halfViewPortHeight* zoom) {
			cameraBodyPosition.y = levelHeightInMeters - halfViewPortHeight* zoom;
			if (camVector.y > 0) {
				camVector.y = 0;
			}
		}
		
		front.position.x = cameraBodyPosition.x;
		front.position.y = cameraBodyPosition.y;
	
		cameraBody.setLinearVelocity(camVector);
			
		parrallax.position.x = front.position.x / 2 + 20;
		parrallax.position.y = front.position.y / 2 + 8;						
	}
	
}
