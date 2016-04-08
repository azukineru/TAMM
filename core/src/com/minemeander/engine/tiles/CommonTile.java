package com.minemeander.engine.tiles;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;

public enum CommonTile {
	FLOWER,
	SPIDER,
	ZOMBIE,
	ESKIMO,
	JACK,
	EXIT,
	X_MARK,
	BOULDER,
	BIG_BLUE_JEWEL,
	BLUE_JEWEL;

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
	
	public static CommonTile fromCell(Cell cell) {
		if (cell == null) 
			return null;
		CommonTile[] values = values();
		for (CommonTile mmTile : values) {
			if (mmTile.name().equals(cell.getTile().getProperties().get("id"))) {
				return mmTile;
			}
		}
		return null;
	}
}
