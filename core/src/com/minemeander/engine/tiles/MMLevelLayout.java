package com.minemeander.engine.tiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.text.ChangedCharSetException;

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
	public static int cursorXafterSuccess, cursorYafterSuccess;
	public static int tempcursorX, tempcursorY;	
	public static int counter;
	public static Boolean stateSearching;
	public static Direction tempDirectionWhenFail, newDirectionAfterFail;	
		
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
	
	public static class Stack{
		public ArrayList<String> tempLastDirection;
		
		public Stack()
		{
			tempLastDirection = new ArrayList<String>(10);
		}
		
		public boolean isEmpty()
		{
			return tempLastDirection.isEmpty();
		}
		
		public String pop()
		{
			return tempLastDirection.remove(tempLastDirection.size()-1);
		}
		
		public void push(String x)
		{
			tempLastDirection.add(x);
			System.out.printf("Item %s has been pushed.\n", x);
			//System.out.printf("Size of stack is %d\n", tempLastDirection.size());
		}
		
		public String arrayTop()
		{
			return(tempLastDirection.get(tempLastDirection.size()-1));
		}
	}
	
	public static class StackIntX{
		public ArrayList<Integer> tempLastCursorX;
		
		public StackIntX()
		{
			tempLastCursorX = new ArrayList<Integer>();
		}
		
		public boolean isEmpty()
		{
			return tempLastCursorX.isEmpty();
		}
		
		public Integer pop()
		{
			return tempLastCursorX.remove(tempLastCursorX.size()-1);
		}
		
		public void push(Integer x)
		{
			tempLastCursorX.add(x);
			System.out.printf("Item %d has been pushed\t", x);
			System.out.printf("Size of stack is %d\n", tempLastCursorX.size());
		}
		
		public Integer arrayTop()
		{
			return(tempLastCursorX.get(tempLastCursorX.size()-1));
		}
	}
	
	public static class StackIntY{
		public ArrayList<Integer> tempLastCursorY;
		
		public StackIntY()
		{
			tempLastCursorY = new ArrayList<Integer>();
		}
		
		public boolean isEmpty()
		{
			return tempLastCursorY.isEmpty();
		}
		
		public Integer pop()
		{
			return tempLastCursorY.remove(tempLastCursorY.size()-1);
		}
		
		public void push(Integer x)
		{
			tempLastCursorY.add(x);
			System.out.printf("Item %d has been pushed\t", x);
			System.out.printf("Size of stack is %d\n", tempLastCursorY.size());
		}
		
		public Integer arrayTop()
		{
			return(tempLastCursorY.get(tempLastCursorY.size()-1));
		}
	}
	
	public static List<Direction> directionList = Arrays.asList(Direction.values());
	
	public static Orientation[][] directions = new Orientation[64][64];
	
	public static ArrayList<Direction> possibleDirections = new ArrayList<MMLevelLayout.Direction>(10);
	
	//Stack
	public static Stack lastoDirections = new Stack();	
	public static StackIntX lastoCursorX = new StackIntX();
	public static StackIntY lastoCursorY = new StackIntY();
	
	public static boolean searchingPath(int nbRooms, int cursorX, int cursorY, Direction lastDirection){
		
		System.out.printf("\nTop Remaining rooms= %d\n", nbRooms);
		if( nbRooms == 0 ){
			failure = false;
			//System.out.printf("Saat selesai recursion cursorX=%d ,cursorY=%d\n", cursorX, cursorY);
			tempcursorX = cursorX; tempcursorY = cursorY;
			return true;
		}
		int size, index2;
		double random;
		Direction newDirection;
		
		if(lastDirection != null){
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
			System.out.printf("Fail\n");
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
	
	public static void repairDirection(Direction lastDirection)
	{
		Direction tempsaveLastDirection = lastDirection;
		String repairDirection;
		
		// pop		
		lastoDirections.pop();
		repairDirection = lastoDirections.arrayTop();
		
		//push back again
		lastoDirections.push(tempsaveLastDirection.toString());
		
		//Convert string to enum
		Direction newRepairDirection = Direction.valueOf(repairDirection);
		
		directions[cursorX][cursorY] = new Orientation(newRepairDirection, tempsaveLastDirection);
		
	}
	
	public static void searchingPath2(int nbRooms, int cursorX, int cursorY, Direction lastDirection){
		
		System.out.printf("\nTop Remaining rooms= %d\n", nbRooms);
		if( nbRooms == 0 ){
			failure = false;			
			tempcursorX = cursorX; tempcursorY = cursorY;
			System.out.print("End of searching\n");
			return;
		}
		int size, index2;
		double random;
		Direction newDirection;
		String stringDirection;
		
		if(lastDirection != null)
		{
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
		}
					
		possibleDirections.clear();		
		possibleDirections.addAll(directionList);
		if(cursorX == 30 || directions[cursorX-1][cursorY] != null){
			System.out.printf("PossibleDirection.WEST is not available.");
			possibleDirections.remove(Direction.WEST);
		}
		if(cursorY == 35 || directions[cursorX][cursorY+1] != null){
			System.out.printf("PossibleDirection.NORTH is not available.");
			possibleDirections.remove(Direction.NORTH);
		}
		if(cursorY == 30 || directions[cursorX][cursorY-1] != null){
			System.out.printf("PossibleDirection.SOUTH is not available.");
			possibleDirections.remove(Direction.SOUTH);
		}
		if(cursorX == 35 || directions[cursorX+1][cursorY] != null){
			System.out.printf("PossibleDirection.EAST is not available.");
			possibleDirections.remove(Direction.EAST);
		}
		
		if(stateSearching == false)
		{
			if(tempDirectionWhenFail == Direction.EAST)	
			{
				System.out.printf("PossibleDirection.EAST is not available.");
				possibleDirections.remove(Direction.EAST);
			}
			if(tempDirectionWhenFail == Direction.WEST)	
			{
				System.out.printf("PossibleDirection.WEST is not available.");
				possibleDirections.remove(Direction.WEST);
			}
			if(tempDirectionWhenFail == Direction.NORTH)	
			{
				System.out.printf("PossibleDirection.NORTH is not available.");
				possibleDirections.remove(Direction.NORTH);
			}
			if(tempDirectionWhenFail == Direction.SOUTH)	
			{
				System.out.printf("PossibleDirection.SOUTH is not available.");
				possibleDirections.remove(Direction.SOUTH);
			}
		}
		
		size = possibleDirections.size();
		if(size == 0){
			
			System.out.printf("Fail\n");
			
			//Save the fail direction into temp variable
			stringDirection = lastoDirections.arrayTop();
			Direction failDirection = Direction.valueOf(stringDirection);
			tempDirectionWhenFail = failDirection;
			
			//Empty fail direction[x][y]
			cursorX = lastoCursorX.arrayTop(); cursorY = lastoCursorY.arrayTop();
			System.out.printf("Directions at cursorX=%d cursorY=%d will be emptied\n", cursorX, cursorY);
			directions[cursorX][cursorY] = null;
			
			//Assign with the previous value after popping		
			lastoDirections.pop();
			lastoCursorX.pop(); lastoCursorY.pop();
			stringDirection = lastoDirections.arrayTop();
			Direction topDirection = Direction.valueOf(stringDirection);
			cursorX = lastoCursorX.arrayTop(); cursorY = lastoCursorY.arrayTop();	
			
			//Check around here
			possibleDirections.clear();		
			possibleDirections.addAll(directionList);
			if(directions[cursorX-1][cursorY] != null || tempDirectionWhenFail == Direction.WEST)
				possibleDirections.remove(Direction.WEST);
			if(directions[cursorX+1][cursorY] != null || tempDirectionWhenFail == Direction.EAST)
				possibleDirections.remove(Direction.EAST);
			if(directions[cursorX][cursorY-1] != null || tempDirectionWhenFail == Direction.SOUTH)
				possibleDirections.remove(Direction.SOUTH);
			if(directions[cursorX][cursorY+1] != null || tempDirectionWhenFail == Direction.NORTH)
				possibleDirections.remove(Direction.NORTH);
			
			size = possibleDirections.size();
	
			stateSearching = false;
			nbRooms += 1;
			
			System.out.printf("\n>>>>> FAIL >>");
			System.out.print(topDirection);
			System.out.printf(". cursorX: %d, cursorY: %d .", cursorX, cursorY);
			
			if( size == 0 )
			{
				searchingPath2(nbRooms, cursorX, cursorY, null);
			}
			else
			{
				//random
				searchingPath2(nbRooms, cursorX, cursorY, topDirection);
			}
			
			//failure = true;
			//return false;
		}
		
		
		System.out.printf("\n>>>>> %d >>", counter);
		System.out.print(lastDirection);
		System.out.printf(". cursorX: %d, cursorY: %d .\n", cursorX, cursorY);
		
		if( lastDirection != null){
			//Push and store the direction
			lastoDirections.push(lastDirection.toString());
			lastoCursorX.push(cursorX); lastoCursorY.push(cursorY);
		}
		
		//Select next direction
		random = Math.random();
		index2 = (int)(random*size);
		newDirection = possibleDirections.get(index2);
		if( lastDirection == null)
		{
			stringDirection = lastoDirections.arrayTop();
			Direction topDirection = Direction.valueOf(stringDirection);
			directions[cursorX][cursorY] = new Orientation(topDirection, newDirection);
		}
		else
		{
			directions[cursorX][cursorY] = new Orientation(lastDirection, newDirection);
		}
		
		System.out.printf("This is directions[%d][%d] ", cursorX, cursorY);
		System.out.print(directions[cursorX][cursorY]);
		System.out.printf("\n\n");
		
		lastDirection = newDirection;

		counter++; stateSearching = true;
		searchingPath(nbRooms-1, cursorX, cursorY, lastDirection);
		
		return;
	}
	
	
	
	public static MMLevelLayout random(int nbRooms) {
		
		//1. Random path search
			
		
		int directionGridCenter = directions.length/2;
		//ArrayList<Direction> possibleDirections = new ArrayList<MMLevelLayout.Direction>(10);

		int totRooms;
		totRooms = nbRooms;
		
		do{
			nbRooms = totRooms;
			counter = 1;
			failure = false; stateSearching = true; 
			clearDirectionGrid();
			Direction lastDirection = Direction.EAST;
			tempDirectionWhenFail = Direction.EAST;
			
			cursorX = directionGridCenter;
			cursorY = directionGridCenter;
			//directions[32][32]
			directions[directionGridCenter][directionGridCenter] = new Orientation(null, Direction.EAST);
			
			System.out.printf("\n>>>>> %d >>", counter);
			System.out.print(lastDirection);
			System.out.printf(". cursorX: %d, cursorY: %d\n", cursorX, cursorY);
			
			lastoDirections.push(lastDirection.toString());
			lastoCursorX.push(cursorX); lastoCursorY.push(cursorY);
			
			System.out.printf("This is directions[%d][%d] ", cursorX, cursorY);
			System.out.print(directions[cursorX][cursorY]);
			System.out.printf("\n\n");
			
			nbRooms -= 1; counter++;
			searchingPath2(nbRooms, cursorX, cursorY, lastDirection);
			
			
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
