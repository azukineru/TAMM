package com.minemeander.engine.tiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.GridPoint2;
import com.sun.imageio.stream.CloseableDisposerRecord;

public class MMLevelLayout implements Iterable<Room>{
	
	public int hRooms, vRooms;
	public int roomWidthInTiles, roomHeightInTiles;
	
	public Room[][] rooms;
	public List<Room> filledRoomsList = new ArrayList<Room>();
	
	public int width;
	public int height;
	public int startRoomX;
	public int startRoomY;
	public int endRoomX;
	public int endRoomY;
	public static boolean failure;
	public static int cursorX, cursorY;
	public static int tempcursorX, tempcursorY;
	public static int arrcursorX[], arrcursorY[];
	public static int counter;
		
	public MMLevelLayout(int hRooms, int vRooms,
			int roomWidthInTiles, int roomHeightInTiles,
			int startRoomX, int startRoomY, int endRoomX, int endRoomY) {
		this.hRooms = hRooms;
		this.vRooms = vRooms;
		this.roomWidthInTiles = roomWidthInTiles;
		this.roomHeightInTiles = roomHeightInTiles;
		this.rooms = new Room[hRooms][vRooms];
		this.width = roomWidthInTiles * hRooms;
		this.height = roomHeightInTiles * vRooms;
		this.startRoomX = startRoomX;
		this.startRoomY = startRoomY;
		this.endRoomX = endRoomX;
		this.endRoomY = endRoomY;
	}
	
	@Override
	public String toString(){
		return "MMLevelLayout [hRooms=" + hRooms + ", vRooms=" + vRooms + ", roomWidth=" + roomWidthInTiles + ", roomHeight=" + roomHeightInTiles + ", rooms=" + Arrays.toString(rooms)
				+ ", width=" + width + ", height=" + height + ", startRoomY=" + startRoomY + ", startRoomX=" + startRoomX + ", endRoomY=" + endRoomY + ", endRoomX=" + endRoomX
				+ "]";
	}
	
	public void addFilledRoom(int x, int y){
		Room room = new Room(y*hRooms+x, new GridPoint2(x,y), x*roomWidthInTiles, y*roomHeightInTiles );
		rooms[x][y] = room;
		filledRoomsList.add(room);
	}
	
	public void addRoom(int x, int y, boolean topWall, boolean bottomWall,
		boolean leftWall, boolean rightWall, boolean ground, Orientation orientation){
			
		RoomType roomType = RoomType.PROCEDURAL;
		if (x == startRoomX && y == startRoomY) {
			roomType = RoomType.START;		
		}
		else if (x == endRoomX && y == endRoomY) {
			roomType = RoomType.END;		
		}
		Room room = new Room(y*hRooms+x, new GridPoint2(x, y),
			topWall, bottomWall, leftWall, rightWall, 
			x*roomWidthInTiles, 
			y*roomHeightInTiles, ground, roomType,
			orientation);
		rooms[x][y] = room;		
	}
	
	public static enum Direction {
		NORTH, SOUTH, EAST, WEST
	}
	
	public static class Orientation {
		public Direction previous;
		public Direction current;
		public Orientation(Direction previous, Direction current) {
			super();
			this.previous = previous;
			this.current = current;
		}
			
		@Override
		public String toString() {
			return "Orientation [previous=" + previous + ", current=" + current + "]";
		}		
	}
	
	public static List<Direction> directionList = Arrays.asList(Direction.values());
	
	public static Orientation[][] directions = new Orientation[64][64];
	
	public static ArrayList<Direction> possibleDirections = new ArrayList<MMLevelLayout.Direction>(10);
	
	
	public static boolean searchingPath(int nbRooms, int cursorX, int cursorY, Direction lastDirection){


		if( nbRooms == 0 ){
			failure = false;
			//System.out.printf("Saat selesai recursion cursorX=%d ,cursorY=%d\n", cursorX, cursorY);
			tempcursorX = cursorX; tempcursorY = cursorY;
			return true;
		}
		int size, index2;
		double random;
		Direction newDirection;
		switch(lastDirection){
			case NORTH:
				cursorY++;break;
			case SOUTH:
				cursorY--;break;
			case EAST:
				cursorX++;break;
			case WEST:
				cursorX--;break;
		}
		
		possibleDirections.clear();
		possibleDirections.addAll(directionList);
		if(cursorX == 30 || directions[cursorX-1][cursorY] != null){
			possibleDirections.remove(Direction.WEST);
		}
		if(cursorY == 35 || directions[cursorX][cursorY+1] != null){
			possibleDirections.remove(Direction.NORTH);
		}
		if(cursorY == 30 || directions[cursorX][cursorY-1] != null){
			possibleDirections.remove(Direction.SOUTH);
		}
		if(cursorX == 35 || directions[cursorX+1][cursorY] != null){
			possibleDirections.remove(Direction.EAST);
		}
		
		System.out.print(lastDirection);
		System.out.printf(". cursorX: %d, cursorY: %d when counter %d\n", cursorX, cursorY, counter);
		
		
		size = possibleDirections.size();
		if(size == 0){
			failure = true;
			return false;
		}
		
		random = Math.random();
		index2 = (int)(random*size);
		newDirection = possibleDirections.get(index2);
		directions[cursorX][cursorY] = new Orientation(lastDirection, newDirection); 
		lastDirection = newDirection;

		counter++;
		searchingPath(nbRooms-1, cursorX, cursorY, lastDirection);
		
		return false;
	}
	
	
	
	public static MMLevelLayout random(int nbRooms) {
		
		//1. Random path search
			
		
		int directionGridCenter = directions.length/2;
		//ArrayList<Direction> possibleDirections = new ArrayList<MMLevelLayout.Direction>(10);

		do{
			counter = 1;
			failure = false;
			clearDirectionGrid();
			Direction lastDirection = Direction.EAST;
			cursorX = directionGridCenter;
			cursorY = directionGridCenter;
			directions[directionGridCenter][directionGridCenter] = new Orientation(null, Direction.EAST);
			
			System.out.print(lastDirection);
			System.out.printf(". cursorX: %d, cursorY: %d when counter %d\n", cursorX, cursorY, counter);
			
			
			searchingPath(nbRooms-1, cursorX, cursorY, lastDirection);
			
		}
		while(failure);
		
		cursorX = tempcursorX; cursorY = tempcursorY;
		
		//2. Determine the bound
		int bbTopX = 0, bbTopY = 0, bbBottomX = Integer.MAX_VALUE, bbBottomY = Integer.MAX_VALUE;
		for (int x = 0; x < directions.length; x++) {
			for (int y = 0; y < directions[x].length; y++) {
				if (directions[x][y] != null) {
					if (x < bbBottomX) {
						bbBottomX = x;
					}
					if (y < bbBottomY) {
						bbBottomY = y;
					}
					if (x > bbTopX) {
						bbTopX = x;
					}
					if (y > bbTopY) {
						bbTopY = y;
					}
				}
			}
		}
		//System.out.printf("bbBottomX: %d, bbBottomY: %d\n", bbBottomX, bbBottomY);
		
		//3. Generate Layout
		int hRooms = bbTopX-bbBottomX + 1;
		int vRooms = bbTopY-bbBottomY + 1;
		MMLevelLayout mmLevelLayout = new MMLevelLayout(hRooms, vRooms, 20, 16,
				directionGridCenter-bbBottomX,
				directionGridCenter-bbBottomY,
				cursorX-bbBottomX,
				cursorY-bbBottomY);
		
		for(int x=0; x<hRooms; x++){
			for(int y=0; y<vRooms; y++){
				Orientation orientation = directions[x+bbBottomX][y+bbBottomY];
				
				if(orientation != null) {					
					boolean topWall = true;
					boolean bottomWall = true;
					boolean leftWall = true;
					boolean rightWall = true;
					boolean ground = false;					
					if (orientation.current == Direction.NORTH || orientation.previous == Direction.SOUTH) {
						topWall = false;						
					}
					if (orientation.current == Direction.SOUTH || orientation.previous == Direction.NORTH) {
						bottomWall = false;
						ground = false;
					}
					if (orientation.current == Direction.EAST || orientation.previous == Direction.WEST) {
						rightWall = false;
					}
					if (orientation.current == Direction.WEST || orientation.previous == Direction.EAST) {
						leftWall = false;
					}
					mmLevelLayout.addRoom(x, y, topWall, bottomWall, leftWall, rightWall, ground, orientation);
				}
				else{
					mmLevelLayout.addFilledRoom(x, y);
				}
			}
		}
		
		mmLevelLayout.dump();
		return mmLevelLayout;
	}

	private static void clearDirectionGrid() {
		for (int x = 0; x < directions.length; x++) {
			for (int y = 0; y < directions[x].length; y++) {
				directions[x][y] = null;
			}
		}
	}
	
	private void dump() {
		System.out.println("start ="+startRoomX + "," + startRoomY + " end ="+endRoomX + "," + endRoomY);
		
		for(int y=vRooms-1; y >= 0; y--) {
			for(int x=0; x < hRooms; x++) {
				Room room = rooms[x][y];
				if (room.roomType == RoomType.FILLED) {
					System.out.print("[@@@@]");
				} else {					
					System.out.print('['+
							(room.topWall?"T":" ") +
							(room.bottomWall?"B":" ") +
							(room.leftWall?"L":" ") +
							(room.rightWall?"R":" ") + ']'								
						);
				}	
			}
			System.out.println();
		}
		for(int y=vRooms-1; y >= 0; y--) {
			for(int x=0; x < hRooms; x++) {
				Room room = rooms[x][y];				
				System.out.print(String.format("%02d", room.id) + ":" + room.roomType.name().substring(0, 3) + " ");
			}
			System.out.println();
		}
	}
	
	//main
	public static void main(String[] args){
		MMLevelLayout mmLevelLayout = MMLevelLayout.random(8);
		System.out.println(mmLevelLayout);
		
		for(Room room : mmLevelLayout.filledRoomsList){
			System.out.println(room);
		}
	}
	
	@Override
	public Iterator<Room> iterator() {
		return new Iterator<Room>() {
			private int x = MMLevelLayout.this.startRoomX;
			private int y = MMLevelLayout.this.startRoomY;
			private boolean reachedEnd = false;
			
			@Override
			public boolean hasNext() {
				return !reachedEnd;
			}
			
			@Override
			public Room next() {
				Room room = MMLevelLayout.this.rooms[x][y];
				if(room.roomType != RoomType.END){
					switch(room.orientation.current){
						case EAST:
							x++;break;
						case NORTH:
							y++;break;
						case SOUTH:
							y--;break;
						case WEST:
							x--;break;
						default:
							break;
					}
				}
				if(room.roomType == RoomType.END){
					reachedEnd = true;
				}
				return room;
			}
			
			@Override
			public void remove(){
				
			}
		};
	}
	
	public GridPoint2 randomPositionInRoom(Room room, Random rng) {
		int x = rng.nextInt(roomWidthInTiles - 4);
		int y = rng.nextInt(roomHeightInTiles - 4);
		return new GridPoint2(room.offsetX + 2 + x, room.offsetY + 2 + y);
	}
	
	public Room nextRoom(Room room) {
		if (room.roomType == RoomType.END) {
			return null;
		}
		switch(room.orientation.current) {
		case EAST:
			return rooms[room.gridPosition.x+1][room.gridPosition.y];
		case NORTH:
			return rooms[room.gridPosition.x][room.gridPosition.y+1];
		case SOUTH:
			return rooms[room.gridPosition.x][room.gridPosition.y-1];
		case WEST:
			return rooms[room.gridPosition.x-1][room.gridPosition.y];
		default:
			return null;		
		}
	}	

}
