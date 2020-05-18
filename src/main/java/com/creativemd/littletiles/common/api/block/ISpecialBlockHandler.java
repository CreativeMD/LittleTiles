package com.creativemd.littletiles.common.api.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISpecialBlockHandler {
	
	public default boolean canBeConvertedToVanilla(LittleTile tile) {
		return true;
	}
	
	public default List<LittleBox> getCollisionBoxes(LittleTile tile, List<LittleBox> defaultBoxes) {
		if (canWalkThrough(tile))
			return Collections.EMPTY_LIST;
		return defaultBoxes;
	}
	
	public default boolean canWalkThrough(LittleTile tile) {
		return false;
	}
	
	public default boolean onBlockActivated(LittleTile tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	public default void onTileExplodes(LittleTile tile, Explosion explosion) {
		
	}
	
	public default void randomDisplayTick(LittleTile tile, IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
	}
	
	public default boolean isMaterial(LittleTile tile, Material material) {
		return tile.getBlockState().getMaterial() == material;
	}
	
	public default boolean isLiquid(LittleTile tile) {
		return tile.getBlockState().getMaterial().isLiquid();
	}
	
	public default Vec3d modifyAcceleration(LittleTile tile, Entity entityIn, Vec3d motion) {
		return null;
	}
	
	public default LittlePreview getPreview(LittleTile tile) {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public default boolean canBeRenderCombined(LittleTile thisTile, LittleTile tile) {
		return false;
	}
	
	public default boolean shouldCheckForCollision(LittleTile tile) {
		return false;
	}
	
	public default void onEntityCollidedWithBlock(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, Entity entityIn) {
		
	}
	
	public default Vec3d getFogColor(World world, LittleTile tile, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		return originalColor;
	}
	
	public default void flipPreview(Axis axis, LittlePreview preview, LittleVec doubledCenter) {
		
	}
	
	public default void rotatePreview(Rotation rotation, LittlePreview preview, LittleVec doubledCenter) {
		
	}
	
}
