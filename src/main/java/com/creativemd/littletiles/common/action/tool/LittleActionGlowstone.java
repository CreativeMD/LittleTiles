package com.creativemd.littletiles.common.action.tool;

import com.creativemd.creativecore.common.utils.InventoryUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
	
	public LittleTile changedTile;
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos) throws LittleActionException {
		if(stack.getItem() == Items.GLOWSTONE_DUST && player.isSneaking())
		{
			if(needIngredients(player))
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
		if(isTileStillInPlace(changedTile))
			return new LittleActionGlowstoneRevert(changedTile);
		throw new LittleActionException.TileNotThereException();
	}
	
	public static class LittleActionGlowstoneRevert extends LittleAction
	{
		public BlockPos pos;
		public LittleTileVec corner;
		
		public LittleTile changedTile;
		
		public LittleActionGlowstoneRevert(LittleTile tile) {
			this.pos = tile.te.getPos();
			this.corner = tile.cornerVec;
		}
		
		public LittleActionGlowstoneRevert() {
			
		}

		@Override
		public boolean canBeReverted() {
			return true;
		}

		@Override
		public LittleAction revert() throws LittleActionException {
			if(isTileStillInPlace(changedTile))
				return new LittleActionGlowstoneRevert(changedTile);
			throw new LittleActionException.TileNotThereException();
		}

		@Override
		protected boolean action(EntityPlayer player) throws LittleActionException {
			
			TileEntity te = player.world.getTileEntity(pos);
			if(te instanceof TileEntityLittleTiles)
			{
				LittleTile tile = ((TileEntityLittleTiles) te).getTile(corner);
				if(tile != null)
				{
					if(needIngredients(player))
					{
						ItemStack stack = new ItemStack(Items.GLOWSTONE_DUST);
						if(!InventoryUtils.consumeItemStack(player.inventory, stack))
							throw new NotEnoughIngredientsException.NotEnoughStackException(stack);
					}
					
					if(tile.glowing)
						player.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
					else
						player.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
					
					tile.glowing = !tile.glowing;
					tile.te.updateBlock();
					tile.te.updateLighting();
					
					this.changedTile = tile;
				}else
					throw new LittleActionException.TileNotFoundException();
			}else
				throw new LittleActionException.TileEntityNotFoundException();
			
			return false;
		}

		@Override
		public void writeBytes(ByteBuf buf) {
			writePos(buf, pos);
			buf.writeInt(corner.x);
			buf.writeInt(corner.y);
			buf.writeInt(corner.z);
		}

		@Override
		public void readBytes(ByteBuf buf) {
			pos = readPos(buf);
			corner = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
		}
		
	}
	
}
