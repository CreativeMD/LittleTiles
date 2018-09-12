package com.creativemd.littletiles.common.tiles.place;

import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InsideFixedHandler extends FixedHandler {

	@Override
	public double getDistance(LittleTilePos suggestedPos) {
		return 1;
	}

	@Override
	protected LittleTileBox getNewPos(World world, BlockPos pos, LittleGridContext context, LittleTileBox suggested) {
		LittleTileSize size = suggested.getSize();

		double offset = 0;
		if (size.sizeX <= context.size) {
			if (suggested.minX < context.minPos) {
				offset = context.minPos - suggested.minX;

			} else if (suggested.maxX > context.maxPos) {
				offset = context.maxPos - suggested.maxX;
			}
			suggested.minX += offset;
			suggested.maxX += offset;
		}

		if (size.sizeY <= context.size) {
			offset = 0;
			if (suggested.minY < context.minPos) {
				offset = context.minPos - suggested.minY;

			} else if (suggested.maxY > context.maxPos) {
				offset = context.maxPos - suggested.maxY;
			}
			suggested.minY += offset;
			suggested.maxY += offset;
		}

		if (size.sizeZ <= context.size) {
			offset = 0;
			if (suggested.minZ < context.minPos) {
				offset = context.minPos - suggested.minZ;

			} else if (suggested.maxZ > context.maxPos) {
				offset = context.maxPos - suggested.maxZ;
			}
			suggested.minZ += offset;
			suggested.maxZ += offset;
		}

		return suggested;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleRendering(LittleGridContext context, Minecraft mc, double x, double y, double z) {

	}

}
