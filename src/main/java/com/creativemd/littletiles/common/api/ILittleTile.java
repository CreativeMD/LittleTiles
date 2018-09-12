package com.creativemd.littletiles.common.api;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.MarkMode;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ILittleTile {
	
	public boolean hasLittlePreview(ItemStack stack);
	
	public LittlePreviews getLittlePreview(ItemStack stack);
	
	public default LittlePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution, boolean marked) {
		return getLittlePreview(stack);
	}
	
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews);
	
	public default LittleGridContext rotateLittlePreview(ItemStack stack, Rotation rotation) {
		LittlePreviews previews = getLittlePreview(stack, false, false);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.rotatePreview(rotation, previews.context.rotationCenter);
		}
		saveLittlePreview(stack, previews);
		return previews.context;
	}
	
	public default LittleGridContext flipLittlePreview(ItemStack stack, Axis axis) {
		LittlePreviews previews = getLittlePreview(stack, false, false);
		for (int i = 0; i < previews.size(); i++) {
			LittleTilePreview preview = previews.get(i);
			preview.flipPreview(axis, previews.context.rotationCenter);
		}
		saveLittlePreview(stack, previews);
		return previews.context;
	}
	
	public LittleStructure getLittleStructure(ItemStack stack);
	
	@SideOnly(Side.CLIENT)
	public default LittleGridContext getPositionContext(ItemStack stack) {
		return LittleGridContext.get();
	}
	
	/**
	 * @return Whether it should try to place it or not.
	 */
	@SideOnly(Side.CLIENT)
	public default boolean onRightClick(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public default void onDeselect(EntityPlayer player, ItemStack stack) {
	}
	
	public boolean containsIngredients(ItemStack stack);
	
	@SideOnly(Side.CLIENT)
	public default void onClickAir(EntityPlayer player, ItemStack stack) {
	}
	
	@SideOnly(Side.CLIENT)
	public default boolean onClickBlock(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public default boolean onMouseWheelClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result) {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public default float getPreviewAlphaFactor() {
		return 1;
	}
	
	@SideOnly(Side.CLIENT)
	public default boolean shouldCache() {
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public default void tickPreview(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {
	}
	
	public default PlacementMode getPlacementMode(ItemStack stack) {
		if (stack.hasTagCompound())
			return PlacementMode.getModeOrDefault(stack.getTagCompound().getString("mode"));
		return PlacementMode.getDefault();
	}
	
	@SideOnly(Side.CLIENT)
	public default SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public default MarkMode onMark(EntityPlayer player, ItemStack stack) {
		return new MarkMode();
	}
	
	public default boolean snapToGridByDefault() {
		return false;
	}
	
	/**
	 * needs to be implemented by any ILittleTile which supports low resolution and
	 * only uses full blocks
	 * 
	 * @param stack
	 * @return
	 */
	public default LittleTileSize getCachedSize(ItemStack stack) {
		return null;
	}
	
	/**
	 * needs to be implemented by any ILittleTile which supports low resolution and
	 * only uses full blocks
	 * 
	 * @param stack
	 * @return
	 */
	public default LittleTileVec getCachedOffset(ItemStack stack) {
		return null;
	}
}
