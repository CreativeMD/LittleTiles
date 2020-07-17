package com.creativemd.littletiles.common.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleTileParticle extends LittleTileTE {
	
	public LittleTileParticle() {
		super();
	}
	
	public LittleTileParticle(Block block, int meta, TileEntity tileEntity) {
		super(block, meta, tileEntity);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (!worldIn.isRemote)
			LittleGuiHandler.openGui("littleparticle", new NBTTagCompound(), player, this);
		return true;
	}
	
	@Override
	public List<LittleRenderBox> getInternalRenderingCubes(BlockRenderLayer layer) {
		if (!LittleTiles.CONFIG.rendering.hideParticleBlock)
			return super.getInternalRenderingCubes(layer);
		return new ArrayList<>();
	}
	
	@Override
	public void updateEntity() {
		((TileEntityParticle) getTileEntity()).tile = this;
		super.updateEntity();
	}
}
