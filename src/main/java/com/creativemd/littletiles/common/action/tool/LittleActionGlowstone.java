package com.creativemd.littletiles.common.action.tool;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionGlowstone extends LittleActionInteract {
	
	public LittleActionGlowstone(BlockPos blockPos, EntityPlayer player) {
		super(blockPos, player);
	}
	
	public LittleActionGlowstone() {
		super();
	}

	@Override
	protected boolean isRightClick() {
		return true;
	}
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos) throws LittleActionException {
		if(stack.getItem() == Items.GLOWSTONE_DUST && player.isSneaking())
		{
			if(!player.isCreative())
			{
				if(tile.glowing){
					if(!player.inventory.addItemStackToInventory(new ItemStack(Items.GLOWSTONE_DUST)))
						player.dropItem(new ItemStack(Items.GLOWSTONE_DUST), true);
				}else
					stack.shrink(1);
			}
			
			if(tile.glowing)
				player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
			else
				player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
			
			tile.glowing = !tile.glowing;
			te.updateBlock();
			te.updateLighting();
			
			return true;
		}
		return false;
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
