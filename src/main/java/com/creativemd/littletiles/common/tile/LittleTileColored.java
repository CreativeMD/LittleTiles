package com.creativemd.littletiles.common.tile;

import java.util.List;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class LittleTileColored extends LittleTile {
	
	public int color;
	
	public LittleTileColored(Block block, int meta, Vec3i color) {
		this(block, meta, ColorUtils.RGBToInt(color));
	}
	
	public LittleTileColored(Block block, int meta, int color) {
		super(block, meta);
		this.color = color;
	}
	
	public LittleTileColored() {
		super();
	}
	
	@Override
	public void updateTranslucent() {
		super.updateTranslucent();
		if (ColorUtils.isTransparent(color))
			cachedTranslucent = 2;
	}
	
	@Override
	public List<LittleRenderingCube> getInternalRenderingCubes(BlockRenderLayer layer) {
		List<LittleRenderingCube> cubes = super.getInternalRenderingCubes(layer);
		int color = this.color;
		for (int i = 0; i < cubes.size(); i++) {
			cubes.get(i).color = color;
		}
		return cubes;
	}
	
	@Override
	public void copyExtra(LittleTile tile) {
		super.copyExtra(tile);
		if (tile instanceof LittleTileColored) {
			LittleTileColored thisTile = (LittleTileColored) tile;
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
	public boolean isIdenticalToNBT(NBTTagCompound nbt) {
		return super.isIdenticalToNBT(nbt) && color == nbt.getInteger("color");
	}
	
	@Override
	public boolean canBeCombined(LittleTile tile) {
		if (tile instanceof LittleTileColored && super.canBeCombined(tile)) {
			int color1 = ((LittleTileColored) tile).color;
			int color2 = this.color;
			return color1 == color2;
		}
		return false;
	}
	
	@Override
	public boolean canBeRenderCombined(LittleTile tile) {
		if (tile instanceof LittleTileColored)
			return super.canBeRenderCombined(tile) && ((LittleTileColored) tile).color == this.color;
		return false;
	}
	
	@Override
	public boolean canBeConvertedToVanilla() {
		return false;
	}
	
	@Override
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer) {
		if (ColorUtils.isTransparent(color))
			return layer == BlockRenderLayer.TRANSLUCENT;
		return super.shouldBeRenderedInLayer(layer);
	}
	
	@Override
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		Vec3d result = super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
		Vec3d color = ColorUtils.IntToVec(this.color);
		return new Vec3d(result.x * color.x, result.y * color.y, result.z * color.z);
	}
	
	public static boolean needsToBeRecolored(LittleTile tile, int color) {
		if (ColorUtils.isWhite(color) && !ColorUtils.isTransparent(color))
			return tile.getClass() != LittleTile.class;
		return tile.getClass() != LittleTileColored.class || ((LittleTileColored) tile).color != color;
	}
	
	public static LittleTile setColor(LittleTile tile, int color) {
		if (ColorUtils.isWhite(color) && !ColorUtils.isTransparent(color))
			return removeColor(tile);
		if (tile instanceof LittleTileColored) {
			((LittleTileColored) tile).color = color;
			if (tile.te.getWorld().isRemote)
				tile.updateTranslucent();
		} else {
			LittleTile newTile = new LittleTileColored();
			tile.assignTo(newTile);
			((LittleTileColored) newTile).color = color;
			if (tile.te.getWorld().isRemote)
				newTile.updateTranslucent();
			return newTile;
		}
		return null;
	}
	
	public static LittleTile removeColor(LittleTile tile) {
		if (tile instanceof LittleTileColored) {
			LittleTile newTile = new LittleTile();
			tile.assignTo(newTile);
			return newTile;
		}
		return null;
	}
	
	public static int getColor(LittleTile tile) {
		return tile instanceof LittleTileColored ? ((LittleTileColored) tile).color : -1;
	}
	
}
