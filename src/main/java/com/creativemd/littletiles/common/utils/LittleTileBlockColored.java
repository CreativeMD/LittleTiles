package com.creativemd.littletiles.common.utils;

import java.util.ArrayList;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.math.Vec3i;

public class LittleTileBlockColored extends LittleTileBlock{
	
	public int color;
	
	public LittleTileBlockColored(Block block, int meta, Vec3i color)
	{
		super(block, meta);
		this.color = ColorUtils.RGBToInt(color);
	}
	
	public LittleTileBlockColored()
	{
		super();
	}
	
	@Override
	public void updatePacket(NBTTagCompound nbt)
	{
		super.updatePacket(nbt);
		nbt.setInteger("color", color);
	}
	
	@Override
	public void receivePacket(NBTTagCompound nbt, NetworkManager net, NBTTagCompound completeData)
	{
		super.receivePacket(nbt, net, completeData);
		color = nbt.getInteger("color");
	}
	
	@Override
	public ArrayList<RenderCubeObject> getInternalRenderingCubes() {
		ArrayList<RenderCubeObject> cubes = super.getInternalRenderingCubes();
		int color = this.color;
		for (int i = 0; i < cubes.size(); i++) {
			cubes.get(i).color = color;
		}
		return cubes;
	}
	
	@Override
	public void copyExtra(LittleTile tile) {
		super.copyExtra(tile);
		if(tile instanceof LittleTileBlockColored)
		{
			LittleTileBlockColored thisTile = (LittleTileBlockColored) tile;
			thisTile.color = color;
		}
	}
	
	@Override
	public void saveTileExtra(NBTTagCompound nbt) {
		super.saveTileExtra(nbt);
		nbt.setInteger("color", color);
	}

	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		super.loadTileExtra(nbt);
		color = nbt.getInteger("color");
	}
	
	@Override
	public boolean isIdenticalToNBT(NBTTagCompound nbt)
	{
		return super.isIdenticalToNBT(nbt) && color == nbt.getInteger("color");
	}
	
	@Override
	public boolean canBeCombined(LittleTile tile) {
		if(tile instanceof LittleTileBlockColored && super.canBeCombined(tile))
		{
			int color1 = ((LittleTileBlockColored) tile).color;
			int color2 = this.color;
			return color1 == color2;
		}
		return false;
	}
	
	@Override
	public boolean canBeRenderCombined(LittleTile tile) {
		if(tile instanceof LittleTileBlockColored)
			return super.canBeRenderCombined(tile) && ((LittleTileBlockColored) tile).color == this.color;
		return false;
	}
	
	public static LittleTileBlock setColor(LittleTileBlock tile, int color)
	{
		if(color == ColorUtils.WHITE || color == -1)
			return removeColor(tile);
		if(tile instanceof LittleTileBlockColored)
		{
			((LittleTileBlockColored) tile).color = color;
		}else{
			LittleTileBlock newTile = new LittleTileBlockColored();
			tile.assignTo(newTile);
			((LittleTileBlockColored) newTile).color = color;
			return newTile;
		}
		return null;
	}
	
	public static LittleTileBlock removeColor(LittleTileBlock tile)
	{
		if(tile instanceof LittleTileBlockColored)
		{
			LittleTileBlock newTile = new LittleTileBlock();
			tile.assignTo(newTile);
			return newTile;
		}
		return null;
	}
	
}
