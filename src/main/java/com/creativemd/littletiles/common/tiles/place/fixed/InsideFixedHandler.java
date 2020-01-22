package com.creativemd.littletiles.common.tiles.place.fixed;

import com.creativemd.littletiles.common.tiles.math.box.LittleBox;
import com.creativemd.littletiles.common.tiles.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InsideFixedHandler extends FixedHandler {
	
	@Override
	public double getDistance(LittleAbsoluteVec suggestedPos) {
		return 1;
	}
	
	protected void updateBox(Axis axis, LittleGridContext context, LittleBox box) {
		int offset = 0;
		if (box.getSize(axis) <= context.size) {
			if (box.getMin(axis) < 0)
				offset = -box.getMin(axis);
			
			else if (box.getMax(axis) > context.maxPos)
				offset = context.maxPos - box.getMax(axis);
			box.setMin(axis, box.getMin(axis) + offset);
			box.setMax(axis, box.getMax(axis) + offset);
		}
	}
	
	@Override
	protected LittleBox getNewPos(World world, BlockPos pos, LittleGridContext context, LittleBox suggested) {
		updateBox(Axis.X, context, suggested);
		updateBox(Axis.Y, context, suggested);
		updateBox(Axis.Z, context, suggested);
		return suggested;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleRendering(LittleGridContext context, Minecraft mc, double x, double y, double z) {
		
	}
	
}
