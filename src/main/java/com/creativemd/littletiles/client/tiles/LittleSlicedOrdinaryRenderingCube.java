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
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleSlice;
import com.creativemd.littletiles.common.tiles.vec.advanced.LittleTileSlicedOrdinaryBox;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LittleSlicedOrdinaryRenderingCube extends LittleRenderingCube {
	
	protected LittleDynamicCube dynamicCube;
	
	public LittleSlicedOrdinaryRenderingCube(CubeObject cube, LittleTileSlicedOrdinaryBox box, Block block, int meta) {
		super(cube, box, block, meta);
		dynamicCube = new LittleDynamicCube(this, box.slice, box.getSize());
	}
	
	@Override
	public CubeObject offset(BlockPos pos)
	{
		return new LittleSlicedOrdinaryRenderingCube(new CubeObject(minX-pos.getX(), minY-pos.getY(), minZ-pos.getZ(), maxX-pos.getX(), maxY-pos.getY(), maxZ-pos.getZ(), this), (LittleTileSlicedOrdinaryBox) this.box, block, meta);
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
		
		GlStateManager.color((float)color.x, (float)color.y, (float)color.z, (float)(Math.sin(System.nanoTime()/200000000D)*0.2+0.5) * iTile.getPreviewAlphaFactor());
		
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		
		LittleTileSize size = box.getSize();
		
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing facing = EnumFacing.VALUES[i];
			if(box.slice.shouldRenderSide(facing, size))
				renderFace(dynamicCube, facing, facing.getAxis() == box.slice.axis, box.slice);
		}
		
        GlStateManager.popMatrix();
	}
	
	public static void renderFace(LittleDynamicCube cube, EnumFacing facing, boolean isTraingle, LittleSlice slice)
	{
		GL11.glBegin(GL11.GL_POLYGON);
		
		Vec3i normal = facing.getDirectionVec();
		GlStateManager.glNormal3f(normal.getX(), normal.getY(), normal.getZ());
		
		Vector3f vec = new Vector3f();
		EnumFaceDirection face = EnumFaceDirection.getFacing(facing);
		
		for (int i = 0; i < 4; i++) {			
			vec = cube.get(facing, face.getVertexInformation(i), vec);
			
			GlStateManager.glVertex3f(vec.x, vec.y, vec.z);
		}
		
		GlStateManager.glEnd();
	}
	
	@Override
	public List<BakedQuad> getBakedQuad(BlockPos offset, IBlockState state, IBakedModel blockModel, EnumFacing facing, long rand, boolean overrideTint, int defaultColor)
	{
		LittleTileSlicedOrdinaryBox box = (LittleTileSlicedOrdinaryBox) this.box;
		LittleTileSize size = box.getSize();
		
		if(!box.slice.shouldRenderSide(facing, size))
			return Collections.emptyList();
		
		List<BakedQuad> blockQuads = blockModel.getQuads(state, facing, rand);
		if(blockQuads.isEmpty())
			return Collections.emptyList();
		
		List<BakedQuad> quads = new ArrayList<>();
		
		boolean isTraingle = facing.getAxis() == box.slice.axis;
		Vector3f vec = new Vector3f();
		
		int color = this.color != -1 ? this.color : defaultColor;
		
		for(int i = 0; i < blockQuads.size(); i++)
		{
			BakedQuad oldQuad = blockQuads.get(i);
			
			int index = 0;
			float tempMinX = Float.intBitsToFloat(oldQuad.getVertexData()[index]);
			float tempMinY = Float.intBitsToFloat(oldQuad.getVertexData()[index + 1]);
			float tempMinZ = Float.intBitsToFloat(oldQuad.getVertexData()[index + 2]);
			
			index = 2 * oldQuad.getFormat().getIntegerSize();
			float tempMaxX = Float.intBitsToFloat(oldQuad.getVertexData()[index]);
			float tempMaxY = Float.intBitsToFloat(oldQuad.getVertexData()[index + 1]);
			float tempMaxZ = Float.intBitsToFloat(oldQuad.getVertexData()[index + 2]);
			
			float minX = Math.min(tempMinX, tempMaxX);
			float minY = Math.min(tempMinY, tempMaxY);
			float minZ = Math.min(tempMinZ, tempMaxZ);
			float maxX = Math.max(tempMinX, tempMaxX);
			float maxY = Math.max(tempMinY, tempMaxY);
			float maxZ = Math.max(tempMinZ, tempMaxZ);
			
			
			//Check if it is intersecting, otherwise there is no need to render it
			switch(facing.getAxis())
			{
			case X:
				if(!(maxY > dynamicCube.defaultCube.minY && minY < dynamicCube.defaultCube.maxY && maxZ > dynamicCube.defaultCube.minZ && minZ < dynamicCube.defaultCube.maxZ))
					continue;
				break;
			case Y:
				if(!(maxX > dynamicCube.defaultCube.minX && minX < dynamicCube.defaultCube.maxX && maxZ > dynamicCube.defaultCube.minZ && minZ < dynamicCube.defaultCube.maxZ))
					continue;
				break;
			case Z:
				if(!(maxX > dynamicCube.defaultCube.minX && minX < dynamicCube.defaultCube.maxX && maxY > dynamicCube.defaultCube.minY && minY < dynamicCube.defaultCube.maxY))
					continue;
				break;
			}
			
			float sizeX = maxX - minX;
			float sizeY = maxY - minY;
			float sizeZ = maxZ - minZ;
			
			BakedQuad quad = new CreativeBakedQuad(blockQuads.get(i), this, color, overrideTint && (defaultColor == -1 || blockQuads.get(i).hasTintIndex()) && color != -1, facing);
			
			int uvIndex = quad.getFormat().getUvOffsetById(0) / 4;
			float u1 = Float.intBitsToFloat(quad.getVertexData()[uvIndex]);
			float v1 = Float.intBitsToFloat(quad.getVertexData()[uvIndex+1]);
			uvIndex = 2 * quad.getFormat().getIntegerSize() + quad.getFormat().getUvOffsetById(0) / 4;
			float u2 = Float.intBitsToFloat(quad.getVertexData()[uvIndex]);
			float v2 = Float.intBitsToFloat(quad.getVertexData()[uvIndex+1]);
			
			float sizeU = Math.abs(u2 - u1);
			float sizeV = Math.abs(v2 - v1);
			
			EnumFaceDirection direction = EnumFaceDirection.getFacing(facing);
			
			for (int k = 0; k < 4; k++) {
				VertexInformation vertex = direction.getVertexInformation(k);
				
				index = k * quad.getFormat().getIntegerSize();
				
				float x = facing.getAxis() == Axis.X ? dynamicCube.defaultCube.getVertexInformationPosition(vertex.xIndex) : MathHelper.clamp(dynamicCube.defaultCube.getVertexInformationPosition(vertex.xIndex), minX, maxX);
				float y = facing.getAxis() == Axis.Y ? dynamicCube.defaultCube.getVertexInformationPosition(vertex.yIndex) : MathHelper.clamp(dynamicCube.defaultCube.getVertexInformationPosition(vertex.yIndex), minY, maxY);
				float z = facing.getAxis() == Axis.Z ? dynamicCube.defaultCube.getVertexInformationPosition(vertex.zIndex) : MathHelper.clamp(dynamicCube.defaultCube.getVertexInformationPosition(vertex.zIndex), minZ, maxZ);
				
				vec.set(x, y, z);
				
				dynamicCube.sliceVector(facing, vec);
				
				x = vec.x;
				y = vec.y;
				z = vec.z;
				
				float oldX = Float.intBitsToFloat(quad.getVertexData()[index]);
				float oldY = Float.intBitsToFloat(quad.getVertexData()[index+1]);
				float oldZ = Float.intBitsToFloat(quad.getVertexData()[index+2]);
				
				quad.getVertexData()[index] = Float.floatToIntBits(x);
				quad.getVertexData()[index+1] = Float.floatToIntBits(y);
				quad.getVertexData()[index+2] = Float.floatToIntBits(z);
				
				if(keepVU)
					continue;
				
				float uOffset = ((RotationUtils.getUFromFacing(facing, oldX, oldY, oldZ) - RotationUtils.getUFromFacing(facing, x, y, z)) / RotationUtils.getUFromFacing(facing, sizeX, sizeY, sizeZ)) * sizeU;
				float vOffset = ((RotationUtils.getVFromFacing(facing, oldX, oldY, oldZ) - RotationUtils.getVFromFacing(facing, x, y, z)) / RotationUtils.getVFromFacing(facing, sizeX, sizeY, sizeZ)) * sizeV;
				
				uvIndex = index + quad.getFormat().getUvOffsetById(0) / 4;
				
				if(facing == EnumFacing.NORTH || facing == EnumFacing.EAST)
					uOffset *= -1;
				
				if(facing == EnumFacing.DOWN || facing.getAxis() != Axis.Y)
					vOffset *= -1;
				
				quad.getVertexData()[uvIndex] = Float.floatToRawIntBits(Float.intBitsToFloat(quad.getVertexData()[uvIndex]) - uOffset);
				quad.getVertexData()[uvIndex + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(quad.getVertexData()[uvIndex + 1]) - vOffset);
			}
			quads.add(quad);
		}
		return quads;
	}
}
