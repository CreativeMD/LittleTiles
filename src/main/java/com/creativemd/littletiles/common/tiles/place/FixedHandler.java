package com.creativemd.littletiles.common.tiles.place;

import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class FixedHandler {

	@SideOnly(Side.CLIENT)
	public abstract void handleRendering(LittleGridContext context, Minecraft mc, double x, double y, double z);

	public abstract double getDistance(LittleTilePos pos);

	protected abstract LittleTileBox getNewPos(World world, BlockPos pos, LittleGridContext context, LittleTileBox suggested);

	public void init(World world, BlockPos pos) {

	}

	public LittleTileBox getNewPosition(World world, BlockPos pos, LittleGridContext context, LittleTileBox suggested) {
		LittleTileBox oldBox = suggested.copy();
		LittleTileBox newBox = getNewPos(world, pos, context, suggested);

		if (newBox != null)
			return newBox;
		return oldBox;
	}

}
