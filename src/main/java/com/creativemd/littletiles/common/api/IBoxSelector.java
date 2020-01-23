package com.creativemd.littletiles.common.api;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IBoxSelector {
	
	public LittleGridContext getContext(ItemStack stack);
	
	public void onDeselect(World world, ItemStack stack, EntityPlayer player);
	
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state, RayTraceResult result, LittleAbsoluteVec absoluteHit);
	
	/** @return a list of absolute LittleTileBoxes (not relative to the pos) */
	public LittleBoxes getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleAbsoluteVec absoluteHit);
	
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleAbsoluteVec absoluteHit);
	
	public void rotateLittlePreview(ItemStack stack, Rotation rotation);
	
	public void flipLittlePreview(ItemStack stack, Axis axis);
	
	@SideOnly(Side.CLIENT)
	public default SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
		return null;
	}
	
	public default SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
		return new SubContainerConfigure(player, stack);
	}
	
	@SideOnly(Side.CLIENT)
	public default SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
		return null;
	}
	
	public default SubContainerConfigure getConfigureContainerAdvanced(EntityPlayer player, ItemStack stack) {
		return new SubContainerConfigure(player, stack);
	}
}