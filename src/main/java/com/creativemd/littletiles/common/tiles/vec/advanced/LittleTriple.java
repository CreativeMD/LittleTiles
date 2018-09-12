package com.creativemd.littletiles.common.tiles.vec.advanced;

import net.minecraft.util.EnumFacing.Axis;

public class LittleTriple<T> {

	public T x;
	public T y;
	public T z;

	public LittleTriple(T x, T y, T z) {
		set(x, y, z);
	}

	public void set(T x, T y, T z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public T get(Axis axis) {
		switch (axis) {
		case X:
			return x;
		case Y:
			return y;
		case Z:
			return z;
		}
		return null;
	}

}
