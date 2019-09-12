package com.creativemd.littletiles.client.render.tiles;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleRenderingCube extends RenderCubeObject {
	
	public LittleTileBox box;
	
	public LittleRenderingCube(CubeObject cube, LittleTileBox box, Block block, int meta) {
		super(cube, block, meta);
		this.box = box;
	}
	
	public void renderCubeLines(double x, double y, double z, float red, float green, float blue, float alpha) {
		RenderGlobal.drawBoundingBox(minX + x - 0.001, minY + y - 0.001, minZ + z - 0.001, maxX + 0.001 + x, maxY + y + 0.001, maxZ + z + 0.001, red, green, blue, alpha);
	}
	
	public void renderCubePreview(double x, double y, double z, ILittleTile iTile) {
		Vec3d size = getSize();
		double cubeX = x + minX + size.x / 2D;
		
		double cubeY = y + minY + size.y / 2D;
		
		double cubeZ = z + minZ + size.z / 2D;
		
		Vec3d color = ColorUtils.IntToVec(this.color);
		RenderHelper3D.renderBlock(cubeX, cubeY, cubeZ, size.x, size.y, size.z, 0, 0, 0, color.x, color.y, color.z, (Math.sin(System.nanoTime() / 200000000D) * 0.2 + 0.5) * iTile.getPreviewAlphaFactor());
	}
}
