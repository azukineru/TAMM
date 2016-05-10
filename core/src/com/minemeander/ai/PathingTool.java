package com.minemeander.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;


public class PathingTool {
	private PathNode[][] pathNodeTopology;
	
	public PathingTool(TiledMapTileLayer platformTiles, TiledMapTileLayer interactionLayer) {
		this.pathNodeTopology = new PathNode[platformTiles.getHeight()][platformTiles.getWidth()];
			
		for (int y = 0; y < pathNodeTopology.length; y++) {
			PathNode[] row = pathNodeTopology[y];
			for (int x = 0; x < row.length; x++) {
				if (isPlatformAt(platformTiles, x, y) && y < platformTiles.getHeight() && platformTiles.getCell(x, y+1) == null) { // floors
					markAsWalkable(x,y+1);					
				}				
			}
		}	
		
		for (int y = 0; y < pathNodeTopology.length; y++) {
			PathNode[] row = pathNodeTopology[y];
			for (int x = 0; x < row.length; x++) {
				if (isLadderAt(interactionLayer, x, y)) { // ladders
						markAsWalkable(x, y);
				}
			}
		}
//		dump();

		this.shortestPathSearchNodes = new PriorityQueue<PathNode>(256, new ShortestPathNodeComparator(this));
		this.randomPathSearchNodes = new PriorityQueue<PathNode>(256, new RandomPathNodeComparator(this));
	}
	
	private boolean isLadderAt(TiledMapTileLayer interactionLayer, int x, int y) {
		Cell cell = interactionLayer.getCell(x, y);
		return cell != null && cell.getTile().getProperties().containsKey("ladder");
	}
	
	private boolean isPlatformAt(TiledMapTileLayer platformLayer, int x, int y) {
		Cell cell = platformLayer.getCell(x, y);
		return cell!= null && cell.getTile().getProperties().containsKey("col");
	}
	

	private void markAsWalkable(int x, int y) {
		if (y < pathNodeTopology.length && y >= 0) {
			pathNodeTopology[y][x] = new PathNode(x, y);
			pathNodeTopology[y][x].wayPoint = true;
		}
	}
	
	private void markAsWaypoint(int x, int y) {
		if (y < pathNodeTopology.length  && y >= 0) {
			pathNodeTopology[y][x].wayPoint = true;
		}
	}
	
	public boolean isWalkable(int x, int y) {
		if (x < pathNodeTopology[0].length - 1 && y < pathNodeTopology.length - 1 && y >= 0 && x >= 0) {
			return (pathNodeTopology[y][x] != null);
		}
		else {
			return false;
		}
	}


	private PriorityQueue<PathNode> shortestPathSearchNodes;
	private PriorityQueue<PathNode> randomPathSearchNodes;
	private Vector2 wayPointTile = new Vector2();
	private PathNode targetPathNode = null;
	
	public PathNode getTargetPathNode() {		
		return targetPathNode;
	}
	

	public Vector2 findRandomWayPointTile(ArrayList<PathNode> wayPointsContainer, Vector2 sourceTileCoords, int steps) {
		wayPointsContainer.clear();
		this.targetPathNode = getPathNodeAt(sourceTileCoords); // This makes the scoring algorithm works
		PathNode sourcePathNode = getPathNodeAt(sourceTileCoords);
		if (sourcePathNode == null) {
			// Untrackable source
			return null;
		}
		clearDirtyData();
		
		sourcePathNode.previousNode = sourcePathNode;
		
		randomPathSearchNodes.offer(sourcePathNode);				
		PathNode lastNode = null;

		for (int i = 0; i < steps; i++) {
		
			PathNode currentNode = randomPathSearchNodes.poll();
			if (currentNode == null) {
				break;
			}
			lastNode = currentNode;
			
				int pathNodeX = (int)currentNode.x;
				int pathNodeY = (int)currentNode.y;
				if (isWalkable(pathNodeX + 1, pathNodeY)) {
					PathNode nextNode = pathNodeTopology[pathNodeY][pathNodeX + 1];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + Math.random();
						randomPathSearchNodes.offer(nextNode);
					}
				}
				if (isWalkable(pathNodeX - 1, pathNodeY)) {
					PathNode nextNode = pathNodeTopology[pathNodeY][pathNodeX - 1];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + Math.random();
						randomPathSearchNodes.offer(nextNode);
					}
				}
				if (isWalkable(pathNodeX, pathNodeY + 1)) {					
					PathNode nextNode = pathNodeTopology[pathNodeY + 1][pathNodeX];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + Math.random();
						randomPathSearchNodes.offer(nextNode);
					}
				}
				if (isWalkable(pathNodeX, pathNodeY - 1)) {
					PathNode nextNode = pathNodeTopology[pathNodeY - 1][pathNodeX];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + Math.random();
						randomPathSearchNodes.offer(nextNode);
					}
				}
			
		}
		
		// Search for the first waypoint node on the constructed path			
		if (sourcePathNode.equals(lastNode)) {
			wayPointsContainer.add(lastNode);
			return wayPointTile.set(sourcePathNode.x, sourcePathNode.y);
		}
		
		boolean foundWayPoint = false;
		while(lastNode.getPreviousNode().x != sourcePathNode.x || lastNode.getPreviousNode().y != sourcePathNode.y) {		
			lastNode = lastNode.getPreviousNode();			
			if (lastNode.isWayPoint()) {
				wayPointsContainer.add(lastNode);
				wayPointTile.set(lastNode.x, lastNode.y);
				foundWayPoint = true;
			}
		}
		
		if (!foundWayPoint) {
			wayPointTile.set(lastNode.x, lastNode.y);
			wayPointsContainer.add(lastNode);
		}
		
		return wayPointTile;	
	}
	
	
	public Vector2 findWayPointTile(ArrayList<PathNode> wayPointContainer, final Vector2 sourceTileCoords, final Vector2 targetTileCoords) {			
		wayPointContainer.clear();
		
		this.targetPathNode = getPathNodeAt(targetTileCoords);
		if (targetPathNode == null) {
			// Target is unreachable
			return null;
		}
				
		PathNode sourcePathNode = getPathNodeAt(sourceTileCoords);
		if (sourcePathNode == null) {
			// Untrackable source
			return null;
		}
		clearDirtyData();
		
//		System.out.println("source node = " + sourcePathNode + " dest node " + targetPathNode);
		
		sourcePathNode.previousNode = sourcePathNode;
		
		shortestPathSearchNodes.offer(sourcePathNode);		
		PathNode finalPathNode = null;
		PathNode lastNode = null;

		// Classic A* node exploration
		while(finalPathNode == null && !shortestPathSearchNodes.isEmpty()) {
//			dumpOpenNodes();
			PathNode currentNode = shortestPathSearchNodes.poll();
			lastNode = currentNode;
			if (currentNode.x == targetPathNode.x && currentNode.y == targetPathNode.y) {
				finalPathNode = currentNode;
			}
			else {
				int pathNodeX = (int)currentNode.x;
				int pathNodeY = (int)currentNode.y;
				if (isWalkable(pathNodeX + 1, pathNodeY)) {
					PathNode nextNode = pathNodeTopology[pathNodeY][pathNodeX + 1];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + 1;
						shortestPathSearchNodes.offer(nextNode);
					}
				}
				if (isWalkable(pathNodeX - 1, pathNodeY)) {
					PathNode nextNode = pathNodeTopology[pathNodeY][pathNodeX - 1];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + 1;
						shortestPathSearchNodes.offer(nextNode);
					}
				}
				if (isWalkable(pathNodeX, pathNodeY + 1)) {					
					PathNode nextNode = pathNodeTopology[pathNodeY + 1][pathNodeX];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + 1;
						shortestPathSearchNodes.offer(nextNode);
					}
				}
				if (isWalkable(pathNodeX, pathNodeY - 1)) {
					PathNode nextNode = pathNodeTopology[pathNodeY - 1][pathNodeX];
					if (nextNode.previousNode == null) {
						nextNode.previousNode = currentNode;
						nextNode.weight = currentNode.weight + 1;
						shortestPathSearchNodes.offer(nextNode);
					}
				}
			}
		}
		if (finalPathNode == null) {
			finalPathNode = lastNode;
		}
		
		// Search for the first waypoint node on the constructed path		
		PathNode currentNode = finalPathNode;	
		if (sourcePathNode.equals(finalPathNode)) {
			wayPointContainer.add(sourcePathNode);
			return wayPointTile.set(sourcePathNode.x, sourcePathNode.y);
			
		}
		else {
			wayPointTile.set(finalPathNode.x, finalPathNode.y); // Default waypoint. Useful is there none left on the constructed path.
			wayPointContainer.add(finalPathNode);
		}
				
		while(currentNode.getPreviousNode().x != sourcePathNode.x || currentNode.getPreviousNode().y != sourcePathNode.y) {		
			currentNode = currentNode.getPreviousNode();			
			if (currentNode.isWayPoint()) {
				wayPointContainer.add(currentNode);
				wayPointTile.set(currentNode.x, currentNode.y);
			}
		}

		return wayPointTile;		
	}
	
	private void clearDirtyData() {
		for (int y = 0; y < pathNodeTopology.length; y++) {
			PathNode[] row = pathNodeTopology[y];
			for (int x = 0; x < row.length; x++) {
				if (pathNodeTopology[y][x] != null) {
					pathNodeTopology[y][x].weight = 0;
					pathNodeTopology[y][x].previousNode = null;
				}
			}
		}	
		shortestPathSearchNodes.clear();		
		randomPathSearchNodes.clear();
	}

	private PathNode getPathNodeAt(Vector2 tileCoords) {		
		int y = (int) tileCoords.y;
		int x = (int) tileCoords.x;
		if (x >= 0 && y >= 0 && x < pathNodeTopology[0].length && y < pathNodeTopology.length) {
			PathNode pathNode = pathNodeTopology[y][x];		
			return pathNode;
		}
		return null;
	}

	public void dumpOpenNodes() {
		for (PathNode pathNode : shortestPathSearchNodes) {
			System.out.print(pathNode);
			System.out.print(" -> ");
		}
		System.out.println();
	}
	
	public void dump() {
		System.out.println("------------------------------------------");
		for (int y = pathNodeTopology.length - 1; y > 0; y--) {
			System.out.print(y+":");
			PathNode[] row = pathNodeTopology[y];
			for (int x = 0; x < row.length; x++) {
				if (pathNodeTopology[y][x] != null) {
					System.out.print(pathNodeTopology[y][x]);
				}
				else {
					System.out.print("         ");
				}
			}
			System.out.println();
		}
		System.out.println("------------------------------------------");
	}
	
	private final class ShortestPathNodeComparator implements Comparator<PathNode> {
		private final PathingTool pathingTool;

		private ShortestPathNodeComparator(PathingTool pathingTool) {
			this.pathingTool = pathingTool;
		}

		@Override
		public int compare(PathNode o1, PathNode o2) {		
			PathNode targetPathNode = pathingTool.getTargetPathNode();
			double delta = (o1.getScore(targetPathNode) - o2.getScore(targetPathNode));
			if (delta < 0) {
				return -1;
			}
			else if (delta > 0) {
				return 1;
			}
			else {
				return 0;
			}
		}		
	}

	private final class RandomPathNodeComparator implements Comparator<PathNode> {
		private final PathingTool pathingTool;

		private RandomPathNodeComparator(PathingTool pathingTool) {
			this.pathingTool = pathingTool;
		}

		@Override
		public int compare(PathNode o1, PathNode o2) {		
			PathNode targetPathNode = pathingTool.getTargetPathNode();
			double delta = (o2.getScore(targetPathNode) - o1.getScore(targetPathNode));
			if (delta < 0) {
				return -1;
			}
			else if (delta > 0) {
				return 1;
			}
			else {
				return 0;
			}
		}		
	}

}
