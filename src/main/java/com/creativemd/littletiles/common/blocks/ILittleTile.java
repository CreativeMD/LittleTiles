package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILittleTile {
	
	public boolean hasLittlePreview(ItemStack stack);
	
	public List<LittleTilePreview> getLittlePreview(ItemStack stack);
	
	public default List<LittleTilePreview> getLittlePreview(ItemStack stack, boolean allowLowResolution)
	{
		return getLittlePreview(stack);
	}
	
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews);
	
	public default void rotateLittlePreview(ItemStack stack, EnumFacing facing)
	{
		List<LittleTilePreview> previews = getLittlePreview(stack, false);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.rotatePreview(facing);
		}
		saveLittlePreview(stack, previews);
	}
	
	public default void flipLittlePreview(ItemStack stack, EnumFacing facing)
	{
		List<LittleTilePreview> previews = getLittlePreview(stack, false);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.flipPreview(facing);
		}
		saveLittlePreview(stack, previews);
	}
	
	public LittleStructure getLittleStructure(ItemStack stack);
	
	@SideOnly(Side.CLIENT)
	public default float getPreviewAlphaFactor()
	{
		return 1;
	}
	
	@SideOnly(Side.CLIENT)
	public default boolean shouldCache()
	{
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public default void tickPreview(EntityPlayer player, ItemStack stack, PositionResult position) {}
	
	/**
	 * absolute previews cannot be placed directly
	 */
	public default boolean arePreviewsAbsolute()
	{
		return false;
	}
}
