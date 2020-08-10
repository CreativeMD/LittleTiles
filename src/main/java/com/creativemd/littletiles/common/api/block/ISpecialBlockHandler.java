package com.creativemd.littletiles.common.api.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISpecialBlockHandler {
	
	public default boolean canBeConvertedToVanilla(LittleTile tile) {
		return true;
	}
	
	public default LittleBox getCollisionBox(LittleTile tile, LittleBox defaultBox) {
		if (canWalkThrough(tile))
			return null;
		return defaultBox;
	}
	
	public default boolean canWalkThrough(LittleTile tile) {
		return false;
	}
	
	public default boolean onBlockActivated(IParentTileList parent, LittleTile tile, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	public default void onTileExplodes(IParentTileList parent, LittleTile tile, Explosion explosion) {
		
	}
	
	public default void randomDisplayTick(IParentTileList parent, LittleTile tile, Random rand) {
		
	}
	
	public default boolean isMaterial(LittleTile tile, Material material) {
		return tile.getBlockState().getMaterial() == material;
	}
	
	public default boolean isLiquid(LittleTile tile) {
		return tile.getBlockState().getMaterial().isLiquid();
	}
	
	public default Vec3d modifyAcceleration(IParentTileList parent, LittleTile tile, Entity entityIn, Vec3d motion) {
		return null;
	}
	
	public default LittlePreview getPreview(LittleTile tile) {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public default boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
		return false;
	}
	
	public default Vec3d getFogColor(IParentTileList parent, LittleTile tile, Entity entity, Vec3d originalColor, float partialTicks) {
		return originalColor;
	}
	
	public default void flipPreview(Axis axis, LittlePreview preview, LittleVec doubledCenter) {
		
	}
	
	public default void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
		
	}
	
	public default boolean shouldCheckForCollision(LittleTile tile) {
		return false;
	}
	
	public default void onEntityCollidedWithBlock(IParentTileList parent, LittleTile tile, Entity entityIn) {
		
	}
	
}
