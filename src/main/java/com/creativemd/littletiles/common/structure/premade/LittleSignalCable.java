package com.creativemd.littletiles.common.structure.premade;

import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class LittleSignalCable extends LittleStructurePremade {
	
	public LittleSignalCable(LittleStructureType type) {
		super(type);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void neighbourChanged() {
		// TODO Auto-generated method stub
		super.neighbourChanged();
	}
	
	@Override
	public void addCollisionBoxes(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		// TODO Auto-generated method stub
		super.addCollisionBoxes(pos, entityBox, collidingBoxes, entityIn);
	}
	
	@Override
	public void getRenderingCubes(BlockPos pos, BlockRenderLayer layer, List<LittleRenderingCube> cubes) {
		// TODO Auto-generated method stub
		super.getRenderingCubes(pos, layer, cubes);
	}
	
	public class LittleCableFace {
		
		public final EnumFacing facing;
		
		public LittleCableFace(EnumFacing facing) {
			this.facing = facing;
		}
		
	}
}
