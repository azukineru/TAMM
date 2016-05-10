package com.minemeander.ai;

public class PathNode {
	public PathNode previousNode = null;
	
	public int x;
	public int y;	
	public double weight;
	
	public boolean wayPoint = false;
	
	public PathNode(int x, int y) {
		this.x = x;
		this.y = y;
	}
		
	public boolean isWayPoint() {
		return wayPoint;
	}
	
	public PathNode getPreviousNode() {
		return previousNode;
	}
	
	@Override
	public String toString() {	
		if (wayPoint) {
			return "*{"+String.format("%02d",x)+","+String.format("%02d",y)+"} ";
		}
		else {
			return ".{"+String.format("%02d",x)+","+String.format("%02d",y)+"} ";
		}		
	}

	public double getScore(PathNode targetPathNode) {
		float x_d = x - targetPathNode.x;
		float y_d = y - targetPathNode.y;
		double dst1 = Math.sqrt(x_d * x_d + y_d * y_d) + weight;
		return dst1;
	}
	
}
