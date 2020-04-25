package com.creativemd.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.CubeObject;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.ISignalInput;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewFacing;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.SurroundingBox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleSignalInput extends LittleSignalCableBase implements ISignalInput {
	
	private boolean[] state;
	@StructureDirectional
	public EnumFacing facing;
	
	public LittleSignalInput(LittleStructureType type) {
		super(type);
		this.state = new boolean[getBandwidth()];
	}
	
	@Override
	public boolean canConnect(EnumFacing facing) {
		return facing == this.facing;
	}
	
	@Override
	public void setState(boolean[] state) {
		this.state = state;
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		super.loadFromNBTExtra(nbt);
		nbt.setInteger("state", BooleanUtils.boolToInt(state));
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		super.writeToNBTExtra(nbt);
		BooleanUtils.intToBool(nbt.getInteger("state"), state);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFace(EnumFacing facing, LittleGridContext context, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, int color, List<LittleRenderingCube> cubes) {
		super.renderFace(facing, context, renderBox.copy(), distance, axis, one, two, positive, color, cubes);
		
		LittleRenderingCube cube = renderBox.getRenderingCube(context, LittleTiles.inputArrow, facing.ordinal());
		//cube.color = color;
		cube.keepVU = true;
		cube.allowOverlap = true;
		
		if (positive) {
			cube.setMin(axis, cube.getMax(axis));
			cube.setMax(axis, cube.getMax(axis) + (float) context.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
		} else {
			cube.setMax(axis, cube.getMin(axis));
			cube.setMin(axis, cube.getMin(axis) - (float) context.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
		}
		float shrink = 0.14F;
		float shrinkOne = cube.getSize(one) * shrink;
		float shrinkTwo = cube.getSize(two) * shrink;
		cube.setMin(one, cube.getMin(one) + shrinkOne);
		cube.setMax(one, cube.getMax(one) - shrinkOne);
		cube.setMin(two, cube.getMin(two) + shrinkTwo);
		cube.setMax(two, cube.getMax(two) - shrinkTwo);
		cubes.add(cube);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderingCube> cubes, int color) {
		super.render(box, overallBox, cubes, color);
		color = getMainTile() instanceof LittleTileColored ? ((LittleTileColored) getMainTile()).color : -13619152;
		
		CubeObject cube = new CubeObject(overallBox.getBox(box.getContext()));
		
		Axis axis = facing.getAxis();
		
		float sizePercentage = 0.25F;
		
		Axis one = RotationUtils.getDifferentAxisFirst(axis);
		Axis two = RotationUtils.getDifferentAxisSecond(axis);
		
		float sizeOne = cube.getSize(one);
		float sizeTwo = cube.getSize(two);
		float sizeAxis = cube.getSize(axis);
		
		LittleRenderingCube top = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(color);
		top.allowOverlap = true;
		top.setMin(one, top.getMax(one) - sizeOne * sizePercentage);
		cubes.add(top);
		
		LittleRenderingCube bottom = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(color);
		bottom.allowOverlap = true;
		bottom.setMax(one, bottom.getMin(one) + sizeOne * sizePercentage);
		cubes.add(bottom);
		
		LittleRenderingCube left = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(color);
		left.allowOverlap = true;
		left.setMin(two, top.getMax(two) - sizeTwo * sizePercentage);
		cubes.add(left);
		
		LittleRenderingCube right = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(color);
		right.allowOverlap = true;
		right.setMax(two, right.getMin(two) + sizeTwo * sizePercentage);
		cubes.add(right);
		
		LittleRenderingCube behind = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(color);
		behind.allowOverlap = true;
		
		behind.setMin(one, behind.getMin(one) + sizeOne * sizePercentage);
		behind.setMax(one, behind.getMax(one) - sizeOne * sizePercentage);
		
		behind.setMin(two, behind.getMin(two) + sizeTwo * sizePercentage);
		behind.setMax(two, behind.getMax(two) - sizeTwo * sizePercentage);
		
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			behind.setMax(axis, behind.getMin(axis) + sizeAxis * 0.5F);
		else
			behind.setMin(axis, behind.getMax(axis) - sizeAxis * 0.5F);
		cubes.add(behind);
		
		LittleRenderingCube front = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
		
		front.allowOverlap = true;
		
		front.setMin(one, front.getMin(one) + sizeOne * sizePercentage);
		front.setMax(one, front.getMax(one) - sizeOne * sizePercentage);
		
		front.setMin(two, front.getMin(two) + sizeTwo * sizePercentage);
		front.setMax(two, front.getMax(two) - sizeTwo * sizePercentage);
		
		if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
			front.setMin(axis, front.getMax(axis) - sizeAxis * 0.5F);
			front.setMax(axis, front.getMax(axis) - sizeAxis * sizePercentage);
		} else {
			front.setMax(axis, front.getMin(axis) + sizeAxis * 0.5F);
			front.setMin(axis, front.getMin(axis) + sizeAxis * sizePercentage);
		}
		cubes.add(front);
		
		float thickness = 0.0001F;
		LittleRenderingCube frontTop = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
		
		frontTop.allowOverlap = true;
		
		frontTop.setMin(one, frontTop.getMin(one) + sizeOne * sizePercentage);
		frontTop.setMax(one, frontTop.getMin(one) + thickness);
		
		frontTop.setMin(two, frontTop.getMin(two) + sizeTwo * sizePercentage);
		frontTop.setMax(two, frontTop.getMax(two) - sizeTwo * sizePercentage);
		
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			frontTop.setMin(axis, frontTop.getMax(axis) - sizeAxis * sizePercentage);
		else
			frontTop.setMax(axis, frontTop.getMin(axis) + sizeAxis * sizePercentage);
		
		cubes.add(frontTop);
		
		LittleRenderingCube frontBottom = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
		
		frontBottom.allowOverlap = true;
		frontBottom.setMax(one, frontBottom.getMax(one) - sizeOne * sizePercentage);
		frontBottom.setMin(one, frontBottom.getMax(one) - thickness);
		
		frontBottom.setMin(two, frontBottom.getMin(two) + sizeTwo * sizePercentage);
		frontBottom.setMax(two, frontBottom.getMax(two) - sizeTwo * sizePercentage);
		
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			frontBottom.setMin(axis, frontBottom.getMax(axis) - sizeAxis * sizePercentage);
		else
			frontBottom.setMax(axis, frontBottom.getMin(axis) + sizeAxis * sizePercentage);
		
		cubes.add(frontBottom);
		
		LittleRenderingCube frontRight = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
		
		frontRight.allowOverlap = true;
		frontRight.setMin(one, frontRight.getMin(one) + sizeOne * sizePercentage);
		frontRight.setMax(one, frontRight.getMax(one) - sizeOne * sizePercentage);
		
		frontRight.setMin(two, frontRight.getMin(two) + sizeTwo * sizePercentage);
		frontRight.setMax(two, frontRight.getMin(two) + thickness);
		
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			frontRight.setMin(axis, frontRight.getMax(axis) - sizeAxis * sizePercentage);
		else
			frontRight.setMax(axis, frontRight.getMin(axis) + sizeAxis * sizePercentage);
		
		cubes.add(frontRight);
		
		LittleRenderingCube frontLeft = (LittleRenderingCube) new LittleRenderingCube(cube, null, LittleTiles.coloredBlock, 0).setColor(ColorUtils.LIGHT_BLUE);
		
		frontLeft.allowOverlap = true;
		frontLeft.setMin(one, frontLeft.getMin(one) + sizeOne * sizePercentage);
		frontLeft.setMax(one, frontLeft.getMax(one) - sizeOne * sizePercentage);
		
		frontLeft.setMax(two, frontLeft.getMax(two) - sizeTwo * sizePercentage);
		frontLeft.setMin(two, frontLeft.getMax(two) - thickness);
		
		if (facing.getAxisDirection() == AxisDirection.POSITIVE)
			frontLeft.setMin(axis, frontLeft.getMax(axis) - sizeAxis * sizePercentage);
		else
			frontLeft.setMax(axis, frontLeft.getMin(axis) + sizeAxis * sizePercentage);
		
		cubes.add(frontLeft);
	}
	
	@Override
	public int getIndex(EnumFacing facing) {
		return 0;
	}
	
	@Override
	public EnumFacing getFacing(int index) {
		return facing;
	}
	
	public static class LittleStructureTypeInput extends LittleStructureTypeNetwork {
		
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> cubes;
		
		public LittleStructureTypeInput(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth) {
			super(id, category, structureClass, attribute, modid, bandwidth, 1);
		}
		
		@Override
		public List<PlacePreview> getSpecialTiles(LittlePreviews previews) {
			List<PlacePreview> result = super.getSpecialTiles(previews);
			EnumFacing facing = (EnumFacing) loadDirectional(previews, "facing");
			LittleBox box = previews.getSurroundingBox();
			result.add(new PlacePreviewFacing(box, facing, ColorUtils.LIGHT_BLUE));
			return result;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes(LittlePreviews previews) {
			if (cubes == null) {
				float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F) * 1.4);
				cubes = new ArrayList<>();
				cubes.add(new RenderCubeObject(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.coloredBlock).setColor(-13619152));
				//cubes.add(new RenderCubeObject(0 + size * 2, 0.5F - size * 0.8F, 0.5F - size * 0.8F, 1 - size * 2, 0.5F + size * 0.8F, 0.5F + size * 0.8F, LittleTiles.singleCable).setColor(-13619152).setKeepUV(true));
				//cubes.add(new RenderCubeObject(1 - size * 2, 0.5F - size, 0.5F - size, 1, 0.5F + size, 0.5F + size, LittleTiles.coloredBlock).setColor(-13619152));
			}
			return cubes;
		}
		
	}
	
}
