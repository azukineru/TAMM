package com.minemeander.procedural;

public class Ladder {
	public int x, y, length;

	/** Global coords */
	public Ladder(int x, int y, int length) {
		if (length <= 0) {
			throw new RuntimeException("Zero length");
		}
		this.x = x;
		this.y = y;
		this.length = length;
	}
}
