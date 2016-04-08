package com.minemeander.engine.tiles;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;

public enum WorldTile {
	GROUND, 
	DIRT, 
	HALF_DIRT, 
	PLATFORM_LEFT, 
	PLATFORM, 
	PLATFORM_RIGHT, 
	LADDER, 
	LADDER_TOP, 
	ROAD_SIGN_RIGHT, 
	BACK_LIGHT1, 
	BACK_DARK1, 
	SPIKE, 
	ROAD_SIGN_LEFT, 
	BACK_LIGHT2, 
	BACK_DARK2, 
	BACK_NORTH_WEST, 
	BACK_NORTH_EAST, 
	BACK_SOUTH_WEST, 
	BACK_SOUTH_EAST;
	
	public TiledMapTile fromTileSet(TiledMapTileSet set) {
		for (TiledMapTile tiledMapTile : set) {
			if (name().equals(tiledMapTile.getProperties().get("id"))) {
				return tiledMapTile;
			}
		}
		return null;
	}
	
	public Cell toCell(TiledMapTileSet tileSet) {
		Cell cell = new Cell();
		cell.setTile(fromTileSet(tileSet));
		return cell;
	}
	
	public static WorldTile fromCell(Cell cell){
		if(cell == null)
			return null;
		WorldTile[] values = values();
		for(WorldTile mmTile : values){
			if(mmTile.name().equals(cell.getTile().getProperties().get("id"))){
				return mmTile;
			}
		}
		return null;
	}
	
}
