package com.creativemd.littletiles.common.utils.selection;

import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class StateSelector extends BlockSelector {
	
	public int meta;
	
	public StateSelector(IBlockState state) {
		super(state.getBlock());
		this.meta = block.getMetaFromState(state);
	}
	
	public StateSelector() {
		
	}

	@Override
	protected void saveNBT(NBTTagCompound nbt) {
		super.saveNBT(nbt);
		nbt.setInteger("meta", meta);
	}

	@Override
	protected void loadNBT(NBTTagCompound nbt) {
		meta = nbt.getInteger("meta");
	}

	@Override
	public boolean is(LittleTile tile) {
		if(super.is(tile))
			return ((LittleTileBlock) tile).getMeta() == meta;
		return false;
	}

}
