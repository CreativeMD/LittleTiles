package com.creativemd.littletiles.common.util.outdated.identifier;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

@Deprecated
public class LittleIdentifierRelative {
	
	public BlockPos coord;
	public LittleGridContext context;
	public int[] identifier;
	
	public LittleIdentifierRelative(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier) {
		this.coord = new BlockPos(relativeX, relativeY, relativeZ);
		this.context = context;
		this.identifier = identifier;
	}
	
	public static LittleIdentifierRelative loadIdentifierOld(String id, NBTTagCompound nbt) {
		return new LittleIdentifierRelative(id, nbt);
	}
	
	private LittleIdentifierRelative(String id, NBTTagCompound nbt) {
		if (nbt.hasKey(id + "coord")) {
			int[] array = nbt.getIntArray(id + "coord");
			if (array.length == 3)
				coord = new BlockPos(array[0], array[1], array[2]);
			else
				throw new InvalidParameterException("No valid coord given " + nbt);
		} else if (nbt.hasKey(id + "coordX"))
			coord = new BlockPos(nbt.getInteger(id + "coordX"), nbt.getInteger(id + "coordY"), nbt.getInteger(id + "coordZ"));
		else
			coord = new BlockPos(0, 0, 0);
		if (nbt.hasKey(id + "pos")) {
			LittleVec position = new LittleVec(id + "pos", nbt);
			identifier = new int[] { position.x, position.y, position.z };
		} else
			identifier = nbt.getIntArray("id");
		context = LittleGridContext.get(nbt);
	}
	
	public LittleIdentifierRelative(NBTTagCompound nbt) {
		if (nbt.hasKey("coord")) {
			int[] array = nbt.getIntArray("coord");
			if (array.length == 3)
				coord = new BlockPos(array[0], array[1], array[2]);
			else
				throw new InvalidParameterException("No valid coord given " + nbt);
		} else if (nbt.hasKey("coordX"))
			coord = new BlockPos(nbt.getInteger("coordX"), nbt.getInteger("coordY"), nbt.getInteger("coordZ"));
		else
			coord = new BlockPos(0, 0, 0);
		if (nbt.hasKey("pos")) {
			LittleVec position = new LittleVec("pos", nbt);
			identifier = new int[] { position.x, position.y, position.z };
		} else
			identifier = nbt.getIntArray("id");
		context = LittleGridContext.get(nbt);
	}
	
	public BlockPos getAbsolutePosition(TileEntity te) {
		return getAbsolutePosition(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
	}
	
	public BlockPos getAbsolutePosition(BlockPos origin) {
		return getAbsolutePosition(origin.getX(), origin.getY(), origin.getZ());
	}
	
	public BlockPos getAbsolutePosition(int x, int y, int z) {
		return new BlockPos(coord.getX() + x, coord.getY() + y, coord.getZ() + z);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setIntArray("coord", new int[] { coord.getX(), coord.getY(), coord.getZ() });
		nbt.setIntArray("id", identifier);
		context.set(nbt);
		return nbt;
	}
	
	@Override
	public int hashCode() {
		return coord.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LittleIdentifierRelative) {
			if (!coord.equals(((LittleIdentifierRelative) obj).coord))
				return false;
			return Arrays.equals(identifier, LittleIdentifierAbsolute.convertTo(((LittleIdentifierRelative) obj).identifier, ((LittleIdentifierRelative) obj).context, context));
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "coord:[" + coord.getX() + "," + coord.getY() + "," + coord.getZ() + "]|position:" + Arrays.toString(identifier);
	}
	
	public LittleIdentifierRelative copy() {
		return new LittleIdentifierRelative(coord.getX(), coord.getY(), coord.getZ(), context, identifier.clone());
	}
	
	private static HashMap<Integer, LittleIdentifierRelative> converted = new HashMap<>();
	
	@Deprecated
	public int generateIndex(BlockPos pos) {
		int[] array = LittleIdentifierAbsolute.convertTo(identifier, context, LittleGridContext.getMax());
		int index = (pos.getX() + coord.getX()) + ((pos.getY() + coord.getY()) << 4) + ((pos.getZ() + coord.getZ()) << 8) + ((IntStream.of(array).parallel().sum()) << 16);
		if (converted.containsKey(index)) {
			if (!this.equals(converted.get(index)))
				System.out.println("Found duplicate index=" + index + ", " + this + "!=" + converted.get(index));
		} else
			System.out.println(this + "+" + pos + "->" + index);
		return index;
	}
}
