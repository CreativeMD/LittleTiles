package com.creativemd.littletiles.common.tile.math.vec;

public class LittleRay {
	
	public LittleVec origin = new LittleVec(0, 0, 0);
	public LittleVec direction = new LittleVec(0, 0, 0);
	
	public LittleRay(LittleVec start, LittleVec end) {
		set(start, end);
	}
	
	public void set(LittleVec start, LittleVec end) {
		origin.set(start.x, start.y, start.z);
		direction.x = end.x - start.x;
		direction.y = end.y - start.y;
		direction.z = end.z - start.z;
	}
	
	public boolean parallel(LittleRay other) {
		int scale;
		int scaleOther;
		if ((direction.x > 0) != (other.direction.x > 0) || (direction.y > 0) != (other.direction.y > 0) || (direction.z > 0) != (other.direction.z > 0))
			return false;
		if (direction.x != 0) {
			scale = other.direction.x;
			scaleOther = direction.x;
		} else if (direction.y != 0) {
			scale = other.direction.y;
			scaleOther = direction.y;
		} else {
			scale = other.direction.z;
			scaleOther = direction.z;
		}
		
		return direction.x * scale == other.direction.x * scaleOther && direction.y * scale == other.direction.y * scaleOther && direction.z * scale == other.direction.z * scaleOther;
	}
	
	@Override
	public String toString() {
		return "o:" + origin + ",d:" + direction;
	}
}
