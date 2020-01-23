package com.creativemd.littletiles.common.tile.place.fixed;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class FixedHandler {
	
	@SideOnly(Side.CLIENT)
	public abstract void handleRendering(LittleGridContext context, Minecraft mc, double x, double y, double z);
	
	public abstract double getDistance(LittleAbsoluteVec pos);
	
	protected abstract LittleBox getNewPos(World world, BlockPos pos, LittleGridContext context, LittleBox suggested);
	
	public void init(World world, BlockPos pos) {
		
	}
	
	public LittleBox getNewPosition(World world, BlockPos pos, LittleGridContext context, LittleBox suggested) {
		LittleBox oldBox = suggested.copy();
		LittleBox newBox = getNewPos(world, pos, context, suggested);
		
		if (newBox != null)
			return newBox;
		return oldBox;
	}
	
}
