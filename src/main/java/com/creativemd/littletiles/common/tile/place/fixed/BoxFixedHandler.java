package com.creativemd.littletiles.common.tile.place.fixed;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BoxFixedHandler extends FixedHandler {
	
	public ArrayList<LittleBox> boxes = new ArrayList<LittleBox>();
	
	@Override
	public void init(World world, BlockPos pos) {
		boxes = getBoxes(world, pos);
	}
	
	public abstract ArrayList<LittleBox> getBoxes(World world, BlockPos pos);
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleRendering(LittleGridContext context, Minecraft mc, double x, double y, double z) {
		for (int i = 0; i < boxes.size(); i++) {
			GL11.glPushMatrix();
			CubeObject cube = boxes.get(i).getCube(context);
			LittleVec size = boxes.get(i).getSize();
			double cubeX = x + cube.minX + size.getPosX(context) / 2D;
			double cubeY = y + cube.minY + size.getPosY(context) / 2D;
			double cubeZ = z + cube.minZ + size.getPosZ(context) / 2D;
			RenderHelper3D.renderBlock(cubeX, cubeY, cubeZ, size.getPosX(context), size.getPosY(context), size.getPosZ(context), 0, 0, 0, 1, 1, 0.5, (Math.sin(System.nanoTime() / 200000000D) + 1.5) * 0.2D);
			GL11.glPopMatrix();
		}
		
	}
	
	@Override
	public double getDistance(LittleAbsoluteVec suggestedPos) {
		double distance = 2;
		for (int i = 0; i < boxes.size(); i++)
			distance = Math.min(distance, boxes.get(i).distanceTo(suggestedPos.getVec()));
		return 0;
	}
	
	@Override
	protected LittleBox getNewPos(World world, BlockPos pos, LittleGridContext context, LittleBox suggested) {
		return null;
	}
	
}
