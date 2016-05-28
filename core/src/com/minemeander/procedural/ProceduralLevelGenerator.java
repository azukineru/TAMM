package com.minemeander.procedural;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.GridPoint2;

import com.minemeander.engine.tiles.CommonTile;
import com.minemeander.engine.tiles.MMLevelLayout;
import com.minemeander.engine.tiles.MMLevelLayout.Direction;
import com.minemeander.engine.tiles.Room;
import com.minemeander.engine.tiles.RoomType;
import com.minemeander.engine.tiles.WorldTile;
import com.minemeander.Constant;
import com.minemeander.Level;

public class ProceduralLevelGenerator {
	
	private TiledMapTileLayer platformLayer;
	private TiledMapTileLayer ladderLayer;
	private TiledMapTileLayer spriteLayer;
	private TiledMapTileLayer backgroundLayer;
	
	private TiledMapTileSet commonTileSet;
	private TiledMapTileSet worldTileSet;
	
	private int roomHeight;
	private int roomWidth;
	private static int roomNum = 1;
	private static int flowerNum = 0, spiderNum = 0, zombieNum = 0; 
	private Random rng;
	
	private int[][] groundAltitudes;

	private List<Platform>[] generatedPlatforms;
	private List<Ladder> ladderList;
	private int worldId;
	
	GridPoint2 startPosition = null;
	GridPoint2 endPosition = null;
	
	public ProceduralLevelGenerator(TiledMap tiledMap, int roomHeight, int roomWidth, int worldId){
		super();
		this.worldId = worldId;
		this.rng = new Random();
		this.platformLayer = (TiledMapTileLayer) tiledMap.getLayers().get(Constant.PLATFORM_LAYER);
		this.ladderLayer = (TiledMapTileLayer) tiledMap.getLayers().get(Constant.LADDER_LAYER);
		this.spriteLayer = (TiledMapTileLayer) tiledMap.getLayers().get(Constant.SPRITE_LAYER);
		this.backgroundLayer = (TiledMapTileLayer) tiledMap.getLayers().get(Constant.PARRALAX_LAYER);
		this.commonTileSet = tiledMap.getTileSets().getTileSet(0);
		this.worldTileSet = tiledMap.getTileSets().getTileSet(1);
		this.roomHeight = roomHeight;
		this.roomWidth = roomWidth;
		
		System.out.println(String.format("layer width: %d, height: %d", platformLayer.getWidth(), platformLayer.getHeight()));
		int horizontalRooms = (platformLayer.getWidth()) / roomWidth;
		int verticalRooms = (platformLayer.getHeight()) / roomHeight;
		System.out.println(String.format("Number of horizontal rooms: %d, vertical: %d", horizontalRooms, verticalRooms));
		
		this.groundAltitudes = new int[horizontalRooms * verticalRooms][roomWidth];
		this.generatedPlatforms = new ArrayList[horizontalRooms * verticalRooms];
		this.ladderList = new ArrayList<Ladder>();
	}
	
	private void fillRoom(Room room) {
		for (int y = 0; y < roomHeight; y++) {
			for (int x = 0; x < roomWidth; x++) {
				setPlatformTile(room.offsetX, room.offsetY, x, y);
			}
		}
	}
	
	public void decorateRoom(Room room) {
		if (room.ground) {
			generateGround(room.id, room.offsetX, room.offsetY);
		}

		createRoomWalls(room);
		
		if (room.roomType != RoomType.FILLED) {			
			createRandomPlatforms(room.id, room.offsetX, room.offsetY, room.ground);
			createRandomLadders(room.id, room.offsetX, room.offsetY);
		}
		
		createSprites(room.id);
		createEnvironmentalHazard(room);
		System.out.printf("Total flower= %d, spider= %d, zombie= %d\n", flowerNum, spiderNum, zombieNum);
		
	}
	
	private void createEnvironmentalHazard(Room room) {
		for (int y = 0; y < roomHeight - 1; y++) {
			for (int x = 1; x < roomWidth - 1; x++) {
				if (blockAt(room.offsetX, room.offsetY, x, y)) {
					if (!blockAt(room.offsetX, room.offsetY, x, y+1) && !ladderAt(room.offsetX, room.offsetY, x, y)) {
						if (rng.nextFloat() > 0.95) {
							ladderLayer.setCell(room.offsetX+x, room.offsetY+y+1, WorldTile.SPIKE.toCell(worldTileSet));
						}
					}
				}
			}
		}		
	}
	
	public void setOnLadderLayerIfEmpty(WorldTile tile, int x, int y) {
		if (ladderLayer.getCell(x, y) == null) {
			ladderLayer.setCell(x, y, tile.toCell(worldTileSet));
		}
	}
	
	private void leveldesignWorld1(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}	
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}			
				
				if ( findStatus == true ) {
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					System.out.printf("\tPutting flower\n");
					spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
				}
			}
		}	
		roomNum++;
	}
	
	private void leveldesignWorld2(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}			
				
				if ( findStatus == true ) 
				{
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( roomNum % 2 != 0 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( roomNum % 2 == 0)
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
				}
			}
		}

		roomNum++;
	}
	
	private void leveldesignWorld3(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
				
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
				
				if ( findStatus == true ) 
				{
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( roomNum % 2 != 0 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( roomNum % 2 == 0)
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
				}			
			}
		}

		roomNum++;
	}

	private void leveldesignWorld4(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}		
				
				if ( findStatus == true ) 
				{
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( roomNum % 2 == 0 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( roomNum % 2 != 0)
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
				}
			}
		}
		

		roomNum++;
	}
	
	private void leveldesignWorld5(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
			
				
				if ( findStatus == true ) 
				{
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( roomNum % 2 != 0 && roomNum != 6 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( roomNum % 2 == 0 && roomNum != 6 )
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					else if( roomNum == 6 )
					{
						System.out.printf("\tPutting zombie\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
					}
				}			
			}
		}
		

		roomNum++;
	}

	private void leveldesignWorld6(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}			
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( roomNum % 2 != 0 && roomNum != 7 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( roomNum % 2 == 0 && roomNum != 6 )
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					else if( roomNum == 6 || roomNum == 7 )
					{
						System.out.printf("\tPutting zombie\n");
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}			
			}
		}
		

		roomNum++;
	}
	
	private void leveldesignWorld7(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 2;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else
				{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}				
				
				if ( findStatus == true ) 
				{
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i % 2 != 0 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( i % 2 == 0 )
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( roomNum == 6 || roomNum == 7 )
					{
						System.out.printf("\tPutting zombie\n");
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}			
			}
		}

		roomNum++;
	}

	private void leveldesignWorld8(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}				
				
				if ( findStatus == true ) 
				{
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
					}
					else if( i == 2 )
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( i != 2 && (roomNum == 6 || roomNum == 8) )
					{
						System.out.printf("\tPutting zombie\n");
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}
			}
		}	
		roomNum++;
	}
	
	private void leveldesignWorld9(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( i != 2 && (roomNum == 1 || roomNum == 4 || roomNum == 7) )
					{
						System.out.printf("\tPutting zombie\n");
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}				
			}
		}
		

		roomNum++;
	}
	
	private void leveldesignWorld10(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{
						System.out.printf("\tPutting spider\n");
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( i != 2 && (roomNum == 1 || roomNum == 4 || roomNum == 7) )
					{
						System.out.printf("\tPutting zombie\n");
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}				
			}
		}
		roomNum++;
	}
	
	private void leveldesignWorld11(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						flowerNum++;
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{
						System.out.printf("\tPutting spider\n");
						spiderNum++;
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( i == 2 )
					{
						System.out.printf("\tPutting zombie\n");
						zombieNum++;
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}				
			}
		}
		roomNum++;
	}
	
	private void leveldesignWorld12(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						flowerNum++;
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{
						System.out.printf("\tPutting spider\n");
						spiderNum++;
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( i == 2 )
					{
						System.out.printf("\tPutting zombie\n");
						zombieNum++;
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}				
			}
		}
		roomNum++;
	}
	
	private void leveldesignWorld13(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						flowerNum++;
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{
						System.out.printf("\tPutting spider\n");
						spiderNum++;
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
					if( i == 2 )
					{
						System.out.printf("\tPutting zombie\n");
						zombieNum++;
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
				}				
			}
		}
		roomNum++;
	}
	
	private void leveldesignWorld14(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						flowerNum++;
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{			
						System.out.printf("\tPutting zombie\n");
						zombieNum++;
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
					if( i != 2 )
					{
						System.out.printf("\tPutting spider\n");
						spiderNum++;
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
				}				
			}
		}
		roomNum++;
	}
	
	private void leveldesignWorld15(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}		
		
		int obstacle = 3;
		int loop = 1;
		boolean findStatus;
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
		
		for (int i = 0; i < obstacle; i++) {
			findStatus = false;
			while( findStatus == false )
			{
				System.out.printf("This is loop %d, Room index %d\n", loop, roomNum);
				loop++;
				Platform platform = platformList.get(rng.nextInt(platformList.size()));
				System.out.printf("\tPlatform length is = %d\n", platform.length);
				
				int randomPlacement = 0;
								
				if(platform.length <= 1)
				{
					findStatus = false;
				}
				else{
					randomPlacement = rng.nextInt(platform.length);			
					if(!blockAt(platform.x+randomPlacement, platform.y+1))
					{					
						findStatus = true;	
					}
					else
					{
						findStatus = false;
					}
				}
						
				if ( findStatus == true ) 
				{				
					System.out.printf("\tRandom placement at = %d\n", randomPlacement);
					if( i != 2 )
					{
						System.out.printf("\tPutting flower\n");
						flowerNum++;
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));					
					}
					else if( i == 2 )
					{				
						System.out.printf("\tPutting zombie\n");
						zombieNum++;
						if(!blockAt(platform.x+randomPlacement+1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement+1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
						else if(!blockAt(platform.x+randomPlacement-1, platform.y+1)){
							spriteLayer.setCell(platform.x+randomPlacement-1, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
						}
					}
					if( i != 2 )
					{
						System.out.printf("\tPutting spider\n");
						spiderNum++;
						ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
						spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					}
				}				
			}
		}
		roomNum++;
	}
	
	private void leveldesignWorld16(int roomIndex)
	{
		List<Platform> platformList = generatedPlatforms[roomIndex];		
		if (platformList == null) {
			return;
		}
		//int zombies = rng.nextInt(5);
		
		
		int zombies = 5;
		int loop = 1;
		for (int i = 0; i < zombies; i++) {
			// get a random platform
			System.out.printf("This is loop %d\n", loop);
			loop++;
			Platform platform = platformList.get(rng.nextInt(platformList.size()));
			System.out.printf("\tPlatform length is = %d\n", platform.length);
			
			int randomPlacement = 0;
			boolean findStatus = false;
			
			for(int j=0; j<platform.length; j++)
			{
				if(!blockAt(platform.x+j, platform.y+1))
				{
					randomPlacement = j;
					findStatus = true;
				}
			}
			
			//int randomPlacement = rng.nextInt(platform.length);
			System.out.printf("\tRandom placement at = %d\n", randomPlacement);
			
			//if ( !blockAt(platform.x+randomPlacement, platform.y+1) ) {
			if ( findStatus == true ) {
				int random = rng.nextInt(3);
				if (random == 0) {
					System.out.printf("\tPutting flower\n");
					spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.FLOWER.toCell(commonTileSet));
				}
				else if (random == 1) {
					System.out.printf("\tPutting spider\n");
					ladderLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
					spriteLayer.setCell(platform.x+randomPlacement, platform.y, CommonTile.SPIDER.toCell(commonTileSet));
				}
				else {
					System.out.printf("\tPutting zombie\n");
					if (worldId % Level.NUMBER_OF_WORLDS == 1 || worldId % Level.NUMBER_OF_WORLDS == 3) {
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.ZOMBIE.toCell(commonTileSet));
					}
					else if (worldId % Level.NUMBER_OF_WORLDS == 2) {
						spriteLayer.setCell(platform.x+randomPlacement, platform.y+1, CommonTile.ESKIMO.toCell(commonTileSet));
					}
				}
			}
		}
		
		// Collectables
		for(Platform platform : platformList) {
			if (rng.nextDouble() > 0.5d) {
				for (int jewelIndex = 0; jewelIndex < platform.length; jewelIndex++) {
					if (!blockAt(platform.x+jewelIndex, platform.y+1) && rng.nextDouble() > 0.6) {
						spriteLayer.setCell(platform.x+jewelIndex, platform.y+1, CommonTile.BLUE_JEWEL.toCell(commonTileSet));
					}
				}
			}
		}
	}
	
	private void createSprites(int roomIndex) {
		if(worldId == 1)
		{
			leveldesignWorld1(roomIndex);
		}
		else if(worldId == 2)
		{
			leveldesignWorld2(roomIndex);
		}
		else if(worldId == 3)
		{
			leveldesignWorld3(roomIndex);
		}
		else if(worldId == 4)
		{
			leveldesignWorld4(roomIndex);
		}
		else if(worldId == 5)
		{
			leveldesignWorld5(roomIndex);
		}
		else if(worldId == 6)
		{
			leveldesignWorld6(roomIndex);
		}
		else if(worldId == 7)
		{
			leveldesignWorld7(roomIndex);
		}
		else if(worldId == 8)
		{
			leveldesignWorld8(roomIndex);
		}
		else if(worldId == 9)
		{
			leveldesignWorld9(roomIndex);
		}
		else if(worldId == 10)
		{
			leveldesignWorld10(roomIndex);
		}
		else if(worldId == 11)
		{
			leveldesignWorld11(roomIndex);
		}
		else if(worldId == 12)
		{
			leveldesignWorld12(roomIndex);
		}
		else if(worldId == 13)
		{
			leveldesignWorld13(roomIndex);
		}
		else if(worldId == 14)
		{
			leveldesignWorld14(roomIndex);
		}
		else if(worldId == 15)
		{
			leveldesignWorld15(roomIndex);
		}
		else
		{
			leveldesignWorld16(roomIndex);
		}
	}
	
	private void createRoomWalls(Room room) {

		for (int y = roomHeight - 1; y >= 0; y--) {
			if (room.leftWall) {
				setPlatformTile(room.offsetX, room.offsetY, 0, y);
			}
			if (room.rightWall) {
				setPlatformTile(room.offsetX, room.offsetY, roomWidth - 1, y);
			}
		}

		if (room.bottomWall) {
			for (int x = 0; x < roomWidth; x++) {
				platformLayer.setCell(room.offsetX + x, room.offsetY, WorldTile.DIRT.toCell(worldTileSet));
			}
		}

		if (room.topWall) {
			for (int x = 0; x < roomWidth; x++) {
				platformLayer.setCell(room.offsetX + x, room.offsetY + roomHeight - 1, WorldTile.DIRT.toCell(worldTileSet));
			}
		}
	}
	
	private void generateGround(int roomIndex, int roomOffsetX, int roomOffsetY) {
		int groundAltitude = 0;
		boolean hasHole = false;
		int currentHoleWidth = 0;
		// Generate random terrain
		for (int x = 0; x < roomWidth; x++) {
			int noise1 = -rng.nextInt(4) + 2;

			groundAltitude += noise1;
			if (groundAltitude > 3) {
				groundAltitude = 3;
			}
			if (groundAltitude < 0) {
				groundAltitude = 0;
			}

			// Max hole size == 4
			else if (currentHoleWidth >= 4 && groundAltitude == 0) {
				groundAltitude = 1;
				currentHoleWidth = 0;
			}

			groundAltitudes[roomIndex][x] = groundAltitude;

			if (groundAltitude == 0) { // current hole
				hasHole = true;
				currentHoleWidth++;
			} else if (currentHoleWidth > 0) { // end of hole
				currentHoleWidth = 0;
			}

			if (groundAltitude > 0) {
				// Fill Ground
				for (int i = 0; i < groundAltitude; i++) {
					setPlatformTile(roomOffsetX, roomOffsetY, x, i);
				}

				int groundX = roomOffsetX + x;
				int groundY = roomOffsetY + groundAltitudes[roomIndex][x];
				platformLayer.setCell(groundX, groundY, WorldTile.GROUND.toCell(worldTileSet));
			}
		}

		if (!hasHole) { // Create at least one hole
			int randomIndex = 1 + (int) Math.random() * (roomWidth - 2);
			groundAltitudes[roomIndex][randomIndex] = 0;
		}
	}
	
	private void createRandomPlatforms(int roomIndex, int roomOffsetX, int roomOffsetY, boolean ground) {

		generatedPlatforms[roomIndex] = new ArrayList<Platform>();

		for (int y = roomHeight - 3; y > 0; y--) {
			if (Math.random() <= 0.7) {
				for (int x = 1; x < roomWidth - 2; x++) {
					double nextGaussian = rng.nextGaussian() + 1;
					if (nextGaussian < 0) {
						nextGaussian = 0;
					}
					if (nextGaussian > 2) {
						nextGaussian = 2;
					}

					if (Math.random() < 0.2) {
						int platformLength = (int) (nextGaussian * 4);
						if (x + platformLength > roomWidth) {
							platformLength = roomWidth - x;
						}
						int i = 0;
						if (platformLength > 0) {
							for (i = 0; i < platformLength; i++) {
								if (!blockAt(roomOffsetX, roomOffsetY, x + i, y - 2) && !blockAt(roomOffsetX, roomOffsetY, x + i, y - 1)) {
									setPlatformTile(roomOffsetX, roomOffsetY, x + i, y);
								}
								else {
									break;
								}
							}

							if (i > 0) {
								generatedPlatforms[roomIndex].add(new Platform(roomOffsetX + x, roomOffsetY + y, i));
							}
						}

						int variableSpaceLength = 1 + (int) rng.nextDouble() * 8;
						x += i + variableSpaceLength;
					}
				}
			}
		}
	}
	
	private void createRandomLadders(int roomIndex, int roomOffsetX, int roomOffsetY) {

		List<Platform> platformList = generatedPlatforms[roomIndex];
		for (Platform platform : platformList) {

			int ladderX = rng.nextInt(platform.length);
			if (!blockAt(platform.x + ladderX, platform.y + 1) && !blockAt(platform.x + ladderX, platform.y - 1)) {
				setLadderTile(platform.x + ladderX, platform.y, true);
				ladderList.add(new Ladder(platform.x + ladderX, platform.y, 1));
			}

		}
	}

	@SuppressWarnings("unused")
	private void dumpRoom(int roomIndex, int roomOffsetX, int roomOffsetY) {
		for (int y = roomHeight - 1; y >= 0; y--) {
			System.out.print(String.format("%02d", y));
			for (int x = 0; x < roomWidth; x++) {
				if (blockAt(roomOffsetX, roomOffsetY, x, y)) {
					System.out.print("@");
				} else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		System.out.print("  ");
		for (int x = 0; x < roomWidth; x++) {
			System.out.print(groundAltitudes[roomIndex][x]);
		}
		System.out.println();
	}	
	
	private boolean ladderAt(int roomOffsetX, int roomOffsetY, int relativeX, int relativeY) {
		int x = roomOffsetX + relativeX;
		int y = roomOffsetY + relativeY;
		return ladderLayer.getCell(x, y) != null && ladderLayer.getCell(x, y).getTile().getProperties().containsKey("ladder");
	}
	
	private boolean blockAt(int roomOffsetX, int roomOffsetY, int relativeX, int relativeY) {
		int x = roomOffsetX + relativeX;
		int y = roomOffsetY + relativeY;
		return blockAt(x, y);
	}	
	
	private boolean blockAt(int x, int y) {
		return platformLayer.getCell(x, y) != null;
	}	
	
	private void setLadderTile(int x, int y, boolean top) {
		if (x >= 0 && x < platformLayer.getWidth() && y >= 0 && y < platformLayer.getHeight()) {
			ladderLayer.setCell(x, y, top ? WorldTile.LADDER_TOP.toCell(worldTileSet) : WorldTile.LADDER.toCell(worldTileSet));
		}
	}
	
	private void clearLadderTile(int x, int y) {
		if (x >= 0 && x < platformLayer.getWidth() && y >= 0 && y < platformLayer.getHeight()) {
			ladderLayer.setCell(x, y, null);
		}
	}
	
	private void setPlatformTile(int roomOffsetX, int roomOffsetY, int relativeX, int relativeY) {
		int x = roomOffsetX + relativeX;
		int y = roomOffsetY + relativeY;
		setPlatformTile(x, y, WorldTile.DIRT);
	}
	
	private void setPlatformTile(int x, int y, WorldTile tile) {
		if (x >= 0 && x < platformLayer.getWidth() && y >= 0 && y < platformLayer.getHeight()) {
			platformLayer.setCell(x, y, tile.toCell(worldTileSet));
		} else {
			throw new RuntimeException("setPlatform out of bounds" + x + "," + y + " => " + platformLayer.getWidth() + "," + platformLayer.getHeight());
		}
	}
	
	public void tilingPostProcessing(TiledMap map) {
		// Create platform tiles in the layer
		for (int y = platformLayer.getHeight() - 2; y > 0; y--) {
			for (int x = 0; x < platformLayer.getWidth(); x++) {
				if (blockAt(x, y) && !blockAt(x, y - 1) && !blockAt(x, y + 1)) {
					boolean blockLeft = blockAt(x - 1, y);
					boolean blockRight = blockAt(x + 1, y);

					if ((blockLeft && blockRight) || (!blockLeft && !blockRight)) {
						setPlatformTile(x, y, WorldTile.PLATFORM);
					} else if (blockLeft) {
						setPlatformTile(x, y, WorldTile.PLATFORM_RIGHT);
					} else if (blockRight) {
						setPlatformTile(x, y, WorldTile.PLATFORM_LEFT);
					}
				}
			}
		}

		for (int y = platformLayer.getHeight() - 1; y >= 0; y--) {
			for (int x = 0; x < platformLayer.getWidth(); x++) {
				if (blockAt(x, y) && blockAt(x, y - 1) && !blockAt(x, y + 1)) {
					platformLayer.setCell(x, y, WorldTile.GROUND.toCell(worldTileSet));
				}
			}
		}
		for (int y = platformLayer.getHeight() - 1; y >= 0; y--) {
			for (int x = 0; x < platformLayer.getWidth(); x++) {
				if (blockAt(x, y) && blockAt(x, y + 1) && !blockAt(x, y - 1)) {
					platformLayer.setCell(x, y, WorldTile.HALF_DIRT.toCell(worldTileSet));
				}
			}
		}

		new TmxExporter(map).export(new File("data/output/generated1.tmx"));
		
		// Grow ladders
		for (Ladder ladder : ladderList) {
			int ladderX = ladder.x, ladderY = ladder.y - 1;
			boolean groundFound = false;
			while (!groundFound && ladderY > 1) {
				Cell cell = platformLayer.getCell(ladderX, ladderY - 1);
				if (cell != null) {
					groundFound = true;
				} else {
					setLadderTile(ladderX, ladderY, false);
				}
				ladderY--;
			}
		}
		new TmxExporter(map).export(new File("data/output/generated2.tmx"));
	}	
	
	public static TiledMap generateMap(TiledMap master, MMLevelLayout levelLayout, int worldId) {		
		TiledMap map = cloneMapWithLayout(master, levelLayout, worldId);

		ProceduralLevelGenerator proceduralArtGenerator = new ProceduralLevelGenerator(map, levelLayout.roomHeightInTiles, levelLayout.roomWidthInTiles, worldId);
		
		System.out.println("Fill background");
		proceduralArtGenerator.fillBackground();
		
		System.out.println("Build path");
		proceduralArtGenerator.buildLevelPath(levelLayout);
		
		
		for (Room room : levelLayout) {
			proceduralArtGenerator.decorateRoom(room);
		}
		
		for (Room room : levelLayout.filledRoomsList) {
			proceduralArtGenerator.fillRoom(room);
		}
				
		proceduralArtGenerator.tilingPostProcessing(map);
		
		return map;
	}	
	
	private void buildLevelPath(MMLevelLayout levelLayout) {		
		GridPoint2 wpStartPosition = null;
		GridPoint2 wpTargetPosition = null;
		
		for (Room room : levelLayout) {
			if (wpStartPosition == null) {
				wpStartPosition = levelLayout.randomPositionInRoom(room, rng);
				this.startPosition = wpStartPosition;
			}			 
			
			Room nextRoom = levelLayout.nextRoom(room);
			if (nextRoom == null) {
				continue;
			}
			wpTargetPosition = levelLayout.randomPositionInRoom(nextRoom, rng);
			
				buildLevelPathBetween(wpStartPosition, wpTargetPosition, room.orientation.current);
			wpStartPosition = wpTargetPosition;
		}
		this.endPosition = wpTargetPosition;

		System.out.println("Avatar tile position at " + startPosition.x + ", " + startPosition.y);
		spriteLayer.setCell(startPosition.x, startPosition.y, CommonTile.JACK.toCell(commonTileSet));
		
		ladderLayer.setCell(endPosition.x, endPosition.y, CommonTile.EXIT.toCell(commonTileSet));
		platformLayer.setCell(endPosition.x, endPosition.y, null);
		platformLayer.setCell(endPosition.x, endPosition.y - 1, WorldTile.DIRT.toCell(worldTileSet));
		
		System.out.println("Done building level path.");
	}	
	
private void buildLevelPathBetween(GridPoint2 startPosition, GridPoint2 targetPosition, Direction direction) {
		
		GridPoint2 cursor = new GridPoint2(startPosition);
		int dx = targetPosition.x-cursor.x;
		int dy = targetPosition.y-cursor.y;
		
		int randomDx = -1; // Random x position to place a roadsign on this path section -1 if not applicable (vertical movement)
		if (direction == Direction.EAST || direction == Direction.WEST) {
			randomDx = cursor.x + (int)(rng.nextFloat()*dx);
		}
					
		int last = -1; // 1 platform 2 ladder up 3 ladder down
		
		while(cursor.x != targetPosition.x || cursor.y != targetPosition.y) {
			
			dx = targetPosition.x-cursor.x;
			dy = targetPosition.y-cursor.y;
			
			if (Math.abs(dx) > Math.abs(dy)) {
				int sign = (int)Math.signum(dx);
				
				// Create a random platform									
				
				if (last != 1) {
					clearLadderTile(cursor.x, cursor.y);
				}
				
				for (int pi = 0; pi < Math.abs(dx) + 1; pi++) {						
					int platformX = cursor.x+(pi*sign);
					int platformY = cursor.y-1;
					setPlatformTile(platformX, platformY, WorldTile.DIRT);	
					if (platformX == randomDx) {
						//setOnLadderLayerIfEmpty(sign > 0 ? WorldTile.ROAD_SIGN_RIGHT : WorldTile.ROAD_SIGN_LEFT, platformX, platformY+1);
					}
				}										
				cursor.x+=dx;
				
				last = 1;
			}
			else {
				int sign = (int)Math.signum(dy);
				
				// Create a random ladder								
				if (dy<=2 && dy > 0) {					
					if (last != 1) {
						clearLadderTile(cursor.x, cursor.y);
					}
					for (int pi = 0; pi <= Math.abs(dy)-1; pi++) {						
						setPlatformTile(cursor.x, cursor.y+(pi*sign), WorldTile.DIRT);							
					}
				}
				else {
					int startLadderIndex = (last == 1 ? 1 : 0);
					
					for (int pi = startLadderIndex; pi <= Math.abs(dy); pi++) {					
						setLadderTile(cursor.x, cursor.y+(pi*sign), false);
					}						
				}
				cursor.y+=dy;
				
				last = sign > 0 ? 2 : 3;
			}
		}
	}

	private static TiledMap cloneMapWithLayout(TiledMap master, MMLevelLayout levelLayout, int worldId) {
		return cloneMap(master, levelLayout.width, levelLayout.height, worldId);
	}
	
	private static TiledMap cloneMap(TiledMap master, int width, int height, int worldId) {
		TiledMap map = new TiledMap();
		TiledMapTileLayer parrallaxLayer = new TiledMapTileLayer(width, height, 32, 32);
		TiledMapTileLayer platformLayer = new TiledMapTileLayer(width, height, 32, 32);
		TiledMapTileLayer ladderLayer = new TiledMapTileLayer(width, height, 32, 32);
		TiledMapTileLayer spriteLayer = new TiledMapTileLayer(width, height, 32, 32);		
		map.getLayers().add(parrallaxLayer);
		map.getLayers().add(platformLayer);
		map.getLayers().add(ladderLayer);
		map.getLayers().add(spriteLayer);
		TiledMapTileSet commonTileSet = master.getTileSets().getTileSet(0);		
		TiledMapTileSet worldTileSet = master.getTileSets().getTileSet(worldId % Level.NUMBER_OF_WORLDS);		
		
		System.out.println("* common tileset");
		/*
		for (TiledMapTile tiledMapTile : commonTileSet) {
			Object tileId = tiledMapTile.getProperties().get("id");
			if (tileId!=null)
				System.out.println(String.format("%s(%d),", tileId, tiledMapTile.getId()));
		}
		for (TiledMapTile tiledMapTile : worldTileSet) {
			Object tileId = tiledMapTile.getProperties().get("id");
			if (tileId!=null)
				System.out.println(String.format("%s(%d),", tileId, tiledMapTile.getId()));
		}
		*/
		
		map.getTileSets().addTileSet(commonTileSet);
		map.getTileSets().addTileSet(worldTileSet);
		map.getProperties().putAll(master.getProperties());
		return map;
	}	
	
	private void fillBackground() {
		for (int x = 0; x < backgroundLayer.getWidth(); x++) {
			for (int y = 0; y <= backgroundLayer.getHeight(); y++) {
				backgroundLayer.setCell(x, y, randomTile());				
			}
		}
		int maxSize = backgroundLayer.getHeight() > backgroundLayer.getWidth() ? backgroundLayer.getHeight() : backgroundLayer.getWidth();		
		Amortized2DNoise noise = new Amortized2DNoise(maxSize);
		noise.generate2DNoise(backgroundLayer, worldTileSet, WorldTile.BACK_LIGHT1, WorldTile.BACK_LIGHT2, 5, 5, 0, 0);
		
		// Antialias tiles
		for (int x = 1; x < backgroundLayer.getWidth() - 1; x++) {
			for (int y = 1; y < backgroundLayer.getHeight() - 1; y++) {
				Cell cell = backgroundLayer.getCell(x, y);
				
				Cell top = backgroundLayer.getCell(x, y+1);
				Cell bottom = backgroundLayer.getCell(x, y-1);
				Cell left = backgroundLayer.getCell(x-1, y);
				Cell right = backgroundLayer.getCell(x+1, y);
				
				if (!isLight(cell)) {
					if (isLight(top) && isLight(left) && !isLight(right) && !isLight(bottom)) {
						cell.setTile(WorldTile.BACK_NORTH_WEST.fromTileSet(worldTileSet));
					}
					if (isLight(top) && isLight(right) && !isLight(left) && !isLight(bottom)) {
						cell.setTile(WorldTile.BACK_NORTH_EAST.fromTileSet(worldTileSet));
					}
					if (isLight(bottom) && isLight(left) && !isLight(right) && !isLight(top)) {
						cell.setTile(WorldTile.BACK_SOUTH_WEST.fromTileSet(worldTileSet));
					}
					if (isLight(bottom) && isLight(right) && !isLight(left) && !isLight(top)) {
						cell.setTile(WorldTile.BACK_SOUTH_EAST.fromTileSet(worldTileSet));
					}
				}
			}
		}
	}
	
	private Cell randomTile() {
		return rng.nextBoolean() ? WorldTile.BACK_DARK1.toCell(worldTileSet) : WorldTile.BACK_DARK2.toCell(worldTileSet);
	}
	
	private boolean isLight(Cell cell) {		
		String name = cell.getTile().getProperties().get("id").toString();
		return name.equals(WorldTile.BACK_LIGHT1.name()) || name.equals(WorldTile.BACK_LIGHT2.name());
	}
	
}