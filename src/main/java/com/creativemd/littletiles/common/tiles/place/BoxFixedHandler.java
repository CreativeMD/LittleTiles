package com.creativemd.littletiles.common.tiles.place;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BoxFixedHandler extends FixedHandler {

	public ArrayList<LittleTileBox> boxes = new ArrayList<LittleTileBox>();

	public void init(World world, BlockPos pos) {
		boxes = getBoxes(world, pos);
	}

	public abstract ArrayList<LittleTileBox> getBoxes(World world, BlockPos pos);

	@Override
	@SideOnly(Side.CLIENT)
	public void handleRendering(LittleGridContext context, Minecraft mc, double x, double y, double z) {
		for (int i = 0; i < boxes.size(); i++) {
			GL11.glPushMatrix();
			CubeObject cube = boxes.get(i).getCube(context);
			LittleTileSize size = boxes.get(i).getSize();
			double cubeX = x + cube.minX + size.getPosX(context) / 2D;
			double cubeY = y + cube.minY + size.getPosY(context) / 2D;
			double cubeZ = z + cube.minZ + size.getPosZ(context) / 2D;
			RenderHelper3D.renderBlock(cubeX, cubeY, cubeZ, size.getPosX(context), size.getPosY(context), size.getPosZ(context), 0, 0, 0, 1, 1, 0.5, (Math.sin(System.nanoTime() / 200000000D) + 1.5) * 0.2D);
			GL11.glPopMatrix();
		}

	}

	@Override
	public double getDistance(LittleTilePos suggestedPos) {
		double distance = 2;
		for (int i = 0; i < boxes.size(); i++)
			distance = Math.min(distance, boxes.get(i).distanceTo(suggestedPos.contextVec.vec));
		return 0;
	}

	@Override
	protected LittleTileBox getNewPos(World world, BlockPos pos, LittleGridContext context, LittleTileBox suggested) {
		return null;
	}

}
