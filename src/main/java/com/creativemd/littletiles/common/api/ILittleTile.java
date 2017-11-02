package com.creativemd.littletiles.common.api;

import java.util.List;

import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILittleTile {
	
	public static LittleTileVec rotationCenter = new LittleTileVec(LittleTile.halfGridSize*2, LittleTile.halfGridSize*2, LittleTile.halfGridSize*2);
	
	public boolean hasLittlePreview(ItemStack stack);
	
	public List<LittleTilePreview> getLittlePreview(ItemStack stack);
	
	public default List<LittleTilePreview> getLittlePreview(ItemStack stack, boolean allowLowResolution)
	{
		return getLittlePreview(stack);
	}
	
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews);
	
	public default void rotateLittlePreview(ItemStack stack, Rotation rotation)
	{
		List<LittleTilePreview> previews = getLittlePreview(stack, false);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.rotatePreview(rotation, rotationCenter);
		}
		saveLittlePreview(stack, previews);
	}
	
	public default void flipLittlePreview(ItemStack stack, Axis axis)
	{
		List<LittleTilePreview> previews = getLittlePreview(stack, false);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.flipPreview(axis, rotationCenter);
		}
		saveLittlePreview(stack, previews);
	}
	
	public LittleStructure getLittleStructure(ItemStack stack);
	
	/**
	 * @return Whether it should try to place it or not.
	 */
	@SideOnly(Side.CLIENT)
	public default boolean onRightClick(EntityPlayer player, ItemStack stack, RayTraceResult result)
	{
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public default void onDeselect(EntityPlayer player, ItemStack stack) {}
	
	public default boolean arePreviewsAbsolute()
	{
		return false;
	}
	
	public default boolean containsIngredients(ItemStack stack)
	{
		return !arePreviewsAbsolute();
	}
	
	@SideOnly(Side.CLIENT)
	public default void onClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result) {}
	
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
	public default void tickPreview(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {}
}
