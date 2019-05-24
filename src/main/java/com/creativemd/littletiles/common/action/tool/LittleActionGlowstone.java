package com.creativemd.littletiles.common.action.tool;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionGlowstone extends LittleActionInteract {
	
	public LittleActionGlowstone(World world, BlockPos blockPos, EntityPlayer player) {
		super(world, blockPos, player);
	}
	
	public LittleActionGlowstone() {
		super();
	}
	
	@Override
	protected boolean isRightClick() {
		return true;
	}
	
	public LittleTile changedTile;
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
		if (stack.getItem() == Items.GLOWSTONE_DUST && player.isSneaking()) {
			if (needIngredients(player)) {
				if (tile.glowing) {
					if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLOWSTONE_DUST)))
						player.dropItem(new ItemStack(Items.GLOWSTONE_DUST), true);
				} else
					stack.shrink(1);
			}
			
			if (tile.glowing)
				player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
			else
				player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
			
			tile.glowing = !tile.glowing;
			te.updateBlock();
			te.updateLighting();
			
			this.changedTile = tile;
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	@Override
	public LittleAction revert() throws LittleActionException {
		if (isTileStillInPlace(changedTile))
			return new LittleActionGlowstoneRevert(changedTile);
		throw new LittleActionException.TileNotThereException();
	}
	
	public static class LittleActionGlowstoneRevert extends LittleAction {
		public LittleTileIdentifierAbsolute coord;
		
		public LittleTile changedTile;
		
		public LittleActionGlowstoneRevert(LittleTile tile) {
			this.coord = new LittleTileIdentifierAbsolute(tile);
		}
		
		public LittleActionGlowstoneRevert() {
			
		}
		
		@Override
		public boolean canBeReverted() {
			return true;
		}
		
		@Override
		public LittleAction revert() throws LittleActionException {
			if (isTileStillInPlace(changedTile))
				return new LittleActionGlowstoneRevert(changedTile);
			throw new LittleActionException.TileNotThereException();
		}
		
		@Override
		protected boolean action(EntityPlayer player) throws LittleActionException {
			
			LittleTile tile = getTile(player.world, coord);
			
			if (needIngredients(player)) {
				ItemStack stack = new ItemStack(Items.GLOWSTONE_DUST);
				if (!InventoryUtils.consumeItemStack(player.inventory, stack))
					throw new NotEnoughIngredientsException.NotEnoughStackException(stack);
			}
			
			if (tile.glowing)
				player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
			else
				player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
			
			tile.glowing = !tile.glowing;
			tile.te.updateBlock();
			tile.te.updateLighting();
			
			this.changedTile = tile;
			
			return false;
		}
		
		@Override
		public void writeBytes(ByteBuf buf) {
			writeAbsoluteCoord(coord, buf);
		}
		
		@Override
		public void readBytes(ByteBuf buf) {
			coord = readAbsoluteCoord(buf);
		}
		
	}
	
}
