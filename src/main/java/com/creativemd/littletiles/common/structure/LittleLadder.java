package com.creativemd.littletiles.common.structure;

import javax.annotation.Nonnull;

import com.creativemd.creativecore.gui.container.GuiParent;
import com.creativemd.littletiles.common.blocks.BlockTile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;

public class LittleLadder extends LittleStructure {
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		
	}
	
	public static boolean isLivingOnLadder(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityLivingBase entity) {
		boolean isSpectator = (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator());
		if (isSpectator)
			return false;
		if (!ForgeModContainer.fullBoundingBoxLadders) {
			return state.getBlock().isLadder(state, world, pos, entity);
		} else {
			AxisAlignedBB bb = entity.getEntityBoundingBox();
			int mX = MathHelper.floor(bb.minX);
			int mY = MathHelper.floor(bb.minY);
			int mZ = MathHelper.floor(bb.minZ);
			for (int y2 = mY; y2 < bb.maxY; y2++) {
				for (int x2 = mX; x2 < bb.maxX; x2++) {
					for (int z2 = mZ; z2 < bb.maxZ; z2++) {
						BlockPos tmp = new BlockPos(x2, y2, z2);
						state = world.getBlockState(tmp);
						if (state.getBlock().isLadder(state, world, tmp, entity)) {
							return true;
						}
					}
				}
			}
			bb = entity.getEntityBoundingBox().grow(0.0001);
			mX = MathHelper.floor(bb.minX);
			mY = MathHelper.floor(bb.minY);
			mZ = MathHelper.floor(bb.minZ);
			for (int y2 = mY; y2 < bb.maxY; y2++) {
				for (int x2 = mX; x2 < bb.maxX; x2++) {
					for (int z2 = mZ; z2 < bb.maxZ; z2++) {
						BlockPos tmp = new BlockPos(x2, y2, z2);
						state = world.getBlockState(tmp);
						if (state.getBlock() instanceof BlockTile && state.getBlock().isLadder(state, world, tmp, entity)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}
	
	public static class LittleLadderParser extends LittleStructureParser<LittleLadder> {
		
		public LittleLadderParser(String id, GuiParent parent) {
			super(id, parent);
		}
		
		@Override
		public void createControls(ItemStack stack, LittleStructure structure) {
			
		}
		
		@Override
		public LittleLadder parseStructure(ItemStack stack) {
			return new LittleLadder();
		}
	}
}
