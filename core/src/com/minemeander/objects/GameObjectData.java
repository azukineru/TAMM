package com.minemeander.objects;

public class GameObjectData {
	public CollisionCategory collisionCategory;
	public boolean flying = false;
	public boolean hidden = false;
	public int id;

	public GameObjectData(int id, CollisionCategory collisionCategory) {
		this.id = id;
		this.collisionCategory = collisionCategory;
	}

	@Override
	public String toString() {
		return collisionCategory.toString(); 
	}
}
