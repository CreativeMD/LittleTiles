package com.creativemd.littletiles.common.api;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISpecialBlockSelector {
	
	public LittleGridContext getContext(ItemStack stack);
	
	public void onDeselect(World world, ItemStack stack, EntityPlayer player);
	
	public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state, RayTraceResult result, LittleTilePos absoluteHit);
	
	/**
	 * @return a list of absolute LittleTileBoxes (not relative to the pos)
	 */
	public LittleBoxes getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleTilePos absoluteHit);
	
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleTilePos absoluteHit);
	
	public void rotateLittlePreview(ItemStack stack, Rotation rotation);
	
	public void flipLittlePreview(ItemStack stack, Axis axis);
	
	@SideOnly(Side.CLIENT)
	public default SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
		return null;
	}
}