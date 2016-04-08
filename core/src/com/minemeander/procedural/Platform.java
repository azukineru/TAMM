package com.minemeander.procedural;

public class Platform {
	public int x, y, length;

	public Platform(int x, int y, int length) {
		if (length <= 0) {
			throw new RuntimeException("Zero length");
		}
		this.x = x;
		this.y = y;
		this.length = length;
	}
}
