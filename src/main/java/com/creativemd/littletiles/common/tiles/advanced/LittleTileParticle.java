package com.creativemd.littletiles.common.tiles.advanced;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.tiles.LittleTileTE;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileParticle extends LittleTileTE {
	
	public LittleTileParticle()
	{
		super();
	}
	
	public LittleTileParticle(Block block, int meta, TileEntity tileEntity)
	{
		super(block, meta, tileEntity);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(!worldIn.isRemote)
				LittleGuiHandler.openGui("littleparticle", new NBTTagCompound(), player, this);
		return true;
	}
	
	@Override
	public ArrayList<RenderCubeObject> getInternalRenderingCubes() {
		if(!LittleTiles.hideParticleBlock)
			return super.getInternalRenderingCubes();
		return new ArrayList<>();
	}
	
	@Override
	public void updateEntity()
	{
		((TileEntityParticle) getTileEntity()).tile = this;
		super.updateEntity();
	}
}
