package com.minemeander.engine.tiles;

import com.badlogic.gdx.math.GridPoint2;
import com.minemeander.engine.tiles.RoomType;
import com.minemeander.engine.tiles.MMLevelLayout.Orientation;

public class Room {
	public int id; 
	public boolean topWall, bottomWall, leftWall, rightWall;
	public int offsetX, offsetY;
	public boolean ground;
	public RoomType roomType;
	public Orientation orientation;
	public GridPoint2 gridPosition;
	
	public Room(int id, GridPoint2 gridPosition, boolean topWall, boolean bottomWall, boolean leftWall, boolean rightWall, int offsetX, int offsetY, boolean ground, RoomType roomType, Orientation orientation) {
		super();
		this.id = id;
		this.gridPosition = gridPosition;
		this.topWall = topWall;
		this.bottomWall = bottomWall;
		this.leftWall = leftWall;
		this.rightWall = rightWall;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.ground = ground;
		this.roomType = roomType;
		if (roomType != RoomType.END) {
			this.orientation = orientation;
		}
	}

	public Room(int id, GridPoint2 gridPosition, int offsetX, int offsetY) {
		this.id = id;
		this.gridPosition = gridPosition;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.roomType = RoomType.FILLED;
	}

	@Override
	public String toString() {
		return "Room [id=" + id + ", topWall=" + topWall + ", bottomWall=" + bottomWall + ", leftWall=" + leftWall + ", rightWall=" + rightWall + ", offsetX=" + offsetX
				+ ", offsetY=" + offsetY + ", ground=" + ground + ", roomType=" + roomType + ", orientation=" + orientation + "]";
	}
	
}
