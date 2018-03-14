package com.creativemd.littletiles.client.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderHelper3D;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedQuad;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleUtils;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedBox;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.EnumFaceDirection.VertexInformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LittleSlicedRenderingCube extends LittleSlicedOrdinaryRenderingCube {
	
	public LittleRenderingCube cubeOne;
	public LittleRenderingCube cubeTwo;
	
	public LittleGridContext context;
	
	public LittleSlicedRenderingCube(LittleGridContext context, CubeObject cube, LittleTileSlicedBox box, Block block, int meta) {
		super(cube, box, block, meta);
		this.context = context;
		dynamicCube = new LittleDynamicCube(box.getSlicedCube(context), box.slice, box.getSize());
		
		Axis one = RotationUtils.getDifferentAxisFirst(box.slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(box.slice.axis);
		
		if(box.hasAdditionalBoxTwo())
		{
			cubeTwo = new LittleRenderingCube(box.getCube(context), box, block, meta);
			
			cubeTwo.setMin(one, context.toVanillaGrid((float) box.getMinSlice(one)));
			cubeTwo.setMax(one, context.toVanillaGrid((float) box.getMaxSlice(one)));
        	
        	if(box.slice.isFacingPositive(two))
        	{
        		cubeTwo.setMin(two, context.toVanillaGrid((float) box.getMin(two)));
        		cubeTwo.setMax(two, context.toVanillaGrid((float) box.getMinSlice(two)));
        	}else{
        		cubeTwo.setMin(two, context.toVanillaGrid((float) box.getMaxSlice(two)));
        		cubeTwo.setMax(two, context.toVanillaGrid((float) box.getMax(two)));
        	}
		}
		
		if(box.hasAdditionalBoxOne())
		{
			cubeOne = new LittleRenderingCube(box.getCube(context), box, block, meta);
			
			if(box.slice.isFacingPositive(one))
        	{
				cubeOne.setMin(one, context.toVanillaGrid((float) box.getMin(one)));
				cubeOne.setMax(one, context.toVanillaGrid((float) box.getMinSlice(one)));
        	}else{
        		cubeOne.setMin(one, context.toVanillaGrid((float) box.getMaxSlice(one)));
        		cubeOne.setMax(one, context.toVanillaGrid((float) box.getMax(one)));
        	}
        	
        	if(box.slice.isFacingPositive(two))
        	{
        		cubeOne.setMin(two, cubeTwo != null ? context.toVanillaGrid((float) box.getMin(two)) : context.toVanillaGrid((float) box.getMinSlice(two)));
        		cubeOne.setMax(two, context.toVanillaGrid((float) box.getMaxSlice(two)));
        	}else{
        		cubeOne.setMin(two, context.toVanillaGrid((float) box.getMinSlice(two)));
        		cubeOne.setMax(two, cubeTwo != null ? context.toVanillaGrid((float) box.getMax(two)) : context.toVanillaGrid((float) box.getMaxSlice(two)));
        	}
		}
	}
	
	@Override
	public CubeObject offset(BlockPos pos)
	{
		return new LittleSlicedRenderingCube(context, new CubeObject(minX-pos.getX(), minY-pos.getY(), minZ-pos.getZ(), maxX-pos.getX(), maxY-pos.getY(), maxZ-pos.getZ(), this), (LittleTileSlicedBox) this.box, block, meta);
	}

	@Override
	public void renderCubePreview(boolean absolute, double x, double y, double z, ILittleTile iTile) {
		
		double cubeX = x;
		if(absolute)
			cubeX -= x+TileEntityRendererDispatcher.staticPlayerX;
		
		double cubeY = y;
		if(absolute)
			cubeY -= y+TileEntityRendererDispatcher.staticPlayerY;
		
		double cubeZ = z;
		if(absolute)
			cubeZ -= z+TileEntityRendererDispatcher.staticPlayerZ;
		
		Vec3d color = ColorUtils.IntToVec(this.color);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(cubeX, cubeY, cubeZ);
		GlStateManager.enableRescaleNormal();
		
		GlStateManager.color((float)color.xCoord, (float)color.yCoord, (float)color.zCoord, (float)(Math.sin(System.nanoTime()/200000000D)*0.2+0.5) * iTile.getPreviewAlphaFactor());
		
		LittleTileSlicedBox box = (LittleTileSlicedBox) this.box;
		
		Axis one = RotationUtils.getDifferentAxisFirst(box.slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(box.slice.axis);
		
		LittleTileSize size = box.getSize();
		
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			if((one != facing.getAxis() || cubeOne == null || facing == box.slice.getEmptySide(one)) && (two != facing.getAxis() || cubeTwo == null || facing == box.slice.getEmptySide(two)))
				LittleSlicedOrdinaryRenderingCube.renderFace(dynamicCube, facing, facing.getAxis() == box.slice.axis, box.slice);
			
			if(cubeOne != null && facing != box.slice.getEmptySide(one))
				renderFace(cubeOne, facing, false, box.slice);
			
			if(cubeTwo != null && facing != box.slice.getEmptySide(two) && (cubeOne == null || facing != box.slice.getEmptySide(one).getOpposite()))
				renderFace(cubeTwo, facing, false, box.slice);
		}
		
        GlStateManager.popMatrix();
	}
	
	public static void renderFace(CubeObject cube, EnumFacing facing, boolean isTraingle, LittleSlice slice)
	{
		GL11.glBegin(GL11.GL_POLYGON);
		
		Vec3i normal = facing.getDirectionVec();
		GlStateManager.glNormal3f(normal.getX(), normal.getY(), normal.getZ());
		
		Vector3f vec = new Vector3f();
		EnumFaceDirection face = EnumFaceDirection.getFacing(facing);
		
		for (int i = 0; i < 4; i++) {			
			vec = cube.get(face.getVertexInformation(i), vec);
			
			GlStateManager.glVertex3f(vec.x, vec.y, vec.z);
		}
		
		GlStateManager.glEnd();
	}
	
	@Override
	public List<BakedQuad> getBakedQuad(BlockPos offset, IBlockState state, IBakedModel blockModel, EnumFacing facing, long rand, boolean overrideTint, int defaultColor)
	{
		LittleTileSlicedBox box = (LittleTileSlicedBox) this.box;
		LittleTileSize size = box.getSize();
		
		//if(!box.slice.shouldRenderSide(facing, size))
			//return Collections.emptyList();
		
		List<BakedQuad> blockQuads = blockModel.getQuads(state, facing, rand);
		if(blockQuads.isEmpty())
			return Collections.emptyList();
		
		List<BakedQuad> quads = new ArrayList<>();
		
		boolean isTraingle = facing.getAxis() == box.slice.axis;
		Vector3f vec = new Vector3f();
		
		int color = this.color != -1 ? this.color : defaultColor;
		
		Axis one = RotationUtils.getDifferentAxisFirst(box.slice.axis);
		Axis two = RotationUtils.getDifferentAxisSecond(box.slice.axis);
		
		if((one != facing.getAxis() || cubeOne == null || facing == box.slice.getEmptySide(one)) && (two != facing.getAxis() || cubeTwo == null || facing == box.slice.getEmptySide(two)))
		{
			quads.addAll(super.getBakedQuad(offset, state, blockModel, facing, rand, overrideTint, defaultColor));
		}
		
		if(cubeOne != null && facing != box.slice.getEmptySide(one))
			quads.addAll(cubeOne.getBakedQuad(offset, state, blockModel, facing, rand, overrideTint, defaultColor));
		
		if(cubeTwo != null && facing != box.slice.getEmptySide(two) && (cubeOne == null || facing != box.slice.getEmptySide(one).getOpposite()))
			quads.addAll(cubeTwo.getBakedQuad(offset, state, blockModel, facing, rand, overrideTint, defaultColor));
		
		return quads;
	}
}
