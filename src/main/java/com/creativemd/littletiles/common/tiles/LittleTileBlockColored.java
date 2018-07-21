package com.creativemd.littletiles.common.tiles;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class LittleTileBlockColored extends LittleTileBlock{
	
	public int color;
	
	public LittleTileBlockColored(Block block, int meta, Vec3i color)
	{
		this(block, meta, ColorUtils.RGBToInt(color));
	}
	
	public LittleTileBlockColored(Block block, int meta, int color)
	{
		super(block, meta);
		this.color = color;
	}
	
	public LittleTileBlockColored()
	{
		super();
	}
	
	@Override
	public List<LittleRenderingCube> getInternalRenderingCubes() {
		List<LittleRenderingCube> cubes = super.getInternalRenderingCubes();
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
	
	@Override
	public boolean canBeConvertedToVanilla()
	{
		return false;
	}
	
	@Override
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer) {
		
		if(ColorUtils.isTransparent(color))
			return layer == BlockRenderLayer.TRANSLUCENT;
		return super.shouldBeRenderedInLayer(layer);
	}
	
	@Override
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks)
	{
		Vec3d color = ColorUtils.IntToVec(this.color);
		return new Vec3d(originalColor.xCoord * color.xCoord, originalColor.yCoord * color.yCoord, originalColor.zCoord * color.zCoord);
	}
	
	public static boolean needsToBeRecolored(LittleTileBlock tile, int color)
	{
		if(ColorUtils.isWhite(color) && !ColorUtils.isTransparent(color))
			return tile.getClass() != LittleTileBlock.class;
		return tile.getClass() != LittleTileBlockColored.class || ((LittleTileBlockColored) tile).color != color;
	}
	
	public static LittleTileBlock setColor(LittleTileBlock tile, int color)
	{
		if(ColorUtils.isWhite(color) && !ColorUtils.isTransparent(color))
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

	public static int getColor(LittleTileBlock tile)
	{
		return tile instanceof LittleTileBlockColored ? ((LittleTileBlockColored) tile).color : -1;
	}
	
}
