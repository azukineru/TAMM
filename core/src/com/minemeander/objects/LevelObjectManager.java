package com.minemeander.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

import static com.minemeander.Constant.METERS_PER_TILE;
import com.minemeander.engine.tiles.CommonTile;
import com.minemeander.Constant;
import com.minemeander.Level;

public class LevelObjectManager {
	
	private GameObject[] list = new GameObject[5000];
	private int listSize = 0;
	private int idGenerator = 0;
	private Level level;
	private int jackId = -1;
	
	public LevelObjectManager(Level level) {	
		this.level = level;					
	}
	
	public void add(GameObject gameObject) {
		if (gameObject instanceof Jack) {
			this.jackId = gameObject.id;
		}
		listSize++;
		list[gameObject.id] = gameObject;
	}
	
	public GameObject get(int id) {
		return list[id];
	}
	
	public Jack getJack(){
		return (Jack) get(jackId);
	}
	
	public void draw(SpriteBatch spriteBatch, float tick) {	
		for (int i = 0; i < listSize; i++) {
			GameObject gameObject = list[i];
			if (gameObject != null)
				gameObject.render(spriteBatch, tick);
		}		
	}
	
	public void drawDebugInfo() {		
		for (int i = 0; i < listSize; i++) {
			GameObject gameObject = list[i];
			if (gameObject != null)
				gameObject.renderDebugInfo();
		}		
	}	
	
	public Level getWorld() {
		return level;
	}

	public int size() {
		return listSize;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		for (GameObject gameObject : list) {
			buffer.append(""+i+";"+gameObject + "\n");
			i++;
		}
		return buffer.toString();
	}

	public void reset() {
		listSize = 0;
		idGenerator = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i] != null && !list[i].userData.hidden) {
				list[i].destroy();
				list[i] = null;
			}
		}
		jackId = -1;
		populateLevel();
	}
	
	public void populateLevel() {
		TiledMapTileLayer spriteLayer = (TiledMapTileLayer)level.tiledMap.getLayers().get(Constant.SPRITE_LAYER);
			
		for (int y = spriteLayer.getHeight() - 1; y >= 0; y--) {							
			for (int x = 0; x < spriteLayer.getWidth(); x++) {
				Cell cell = spriteLayer.getCell(x, y);			
				if (cell == null) continue;
				
				float xPosition, yPosition;
				
				switch(CommonTile.fromCell(cell)) {
				case JACK: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE+1;
					add(new Jack(idGenerator++, level, xPosition, yPosition));
					break;
				}
				case SPIDER: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE-0.2f;
					add(new Spider(idGenerator++, level, xPosition, yPosition));
					break;
				}
				case ZOMBIE: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE+1;
					add(new Zombie(idGenerator++, level, xPosition, yPosition));
					break;
				}
				case ESKIMO: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE+1;
					add(new Eskimo(idGenerator++, level, xPosition, yPosition));
					break;
				}
				case FLOWER: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE+1;
					add(new Flower(idGenerator++, level, xPosition, yPosition));
					break;
				}
				case BLUE_JEWEL: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE+1;
					add(new Jewel(idGenerator++, level, xPosition, yPosition, JewelType.BLUE));
					break;
				}
				case BIG_BLUE_JEWEL: {
					xPosition = x*METERS_PER_TILE+1;
					yPosition = y*METERS_PER_TILE+1;
					add(new BigJewel(idGenerator++, level, xPosition, yPosition, JewelType.BLUE));
					break;
				}
				
				default: break;
				}
			
			}
		}	
	}

	
}
