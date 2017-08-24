package com.creativemd.littletiles.common.action.tool;

import java.util.Iterator;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class LittleActionMove extends LittleActionInteract {
	
	public LittleActionMove(BlockPos blockPos, EntityPlayer player) {
		super(blockPos, player);
	}
	
	public LittleActionMove() {
		super();
	}

	@Override
	protected boolean isRightClick() {
		return true;
	}
	
	public static boolean action(LittleTile tile, EnumFacing direction, boolean push) throws LittleActionException {
		TileEntityLittleTiles te = tile.te;
		if(push)
			direction = direction.getOpposite();
		if(tile.canBeMoved(direction))
		{
			if(tile.isStructureBlock)
			{
				if(tile.checkForStructure())
				{
					LittleStructure structure = tile.structure;
					if(structure.hasLoaded())
					{
						HashMapList<TileEntityLittleTiles, LittleTile> tiles = structure.copyOfTiles();
						for (Iterator<LittleTile> iterator = tiles.iterator(); iterator.hasNext();)
						{
							LittleTile tileOfCopy = iterator.next();
							if(!ItemRubberMallet.moveTile(tileOfCopy.te, direction, tileOfCopy, true, push))
								return true;
						}
						
						for (Iterator<LittleTile> iterator = tiles.iterator(); iterator.hasNext();)
						{
							LittleTile tileOfCopy = iterator.next();
							ItemRubberMallet.moveTile(tileOfCopy.te, direction, tileOfCopy, false, push);
						}
						
						structure.combineTiles();
						structure.selectMainTile();
						structure.moveStructure(direction);
					}else
						throw new LittleActionException("action.move.notloaded");
				}
			}else
				if(ItemRubberMallet.moveTile(te, direction, tile, false, push))
					te.updateTiles();
				else
					return true;
			return true;
		}
		return false;
	}
	
	public EnumFacing side;
	public boolean push;
	public LittleTileVec position;
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos) throws LittleActionException {
		push = !player.isSneaking();
		side = moving.sideHit;
		position = tile.getAbsoluteCoordinates();
		position.addVec(new LittleTileVec(push ? side.getOpposite() : side));
		return action(tile, side, push);
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		return new LittleActionMoveRevert(position, side, !push);
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
	}
	
	public static class LittleActionMoveRevert extends LittleAction {
		
		public EnumFacing side;
		public boolean push;
		public LittleTileVec position;
		
		public LittleActionMoveRevert(LittleTileVec position, EnumFacing side, boolean push) {
			this.position = position;
			this.side = side;
			this.push = push;
		}
		
		public LittleActionMoveRevert() {
			
		}

		@Override
		public boolean canBeReverted() {
			return true;
		}

		@Override
		public LittleAction revert() throws LittleActionException {
			LittleTileVec position = this.position.copy();
			position.addVec(new LittleTileVec(push ? side.getOpposite() : side));
			return new LittleActionMoveRevert(position, side, !push);
		}

		@Override
		protected boolean action(EntityPlayer player) throws LittleActionException {
			LittleTile tile = getTileAtPosition(player.world, position);
			if(tile == null)
				throw new LittleActionException.TileNotFoundException();
			return LittleActionMove.action(tile, side, push);
		}

		@Override
		public void writeBytes(ByteBuf buf) {
			writeLittleVec(position, buf);
			writeFacing(buf, side);
			buf.writeBoolean(push);
		}

		@Override
		public void readBytes(ByteBuf buf) {
			position = readLittleVec(buf);
			side = readFacing(buf);
			push = buf.readBoolean();
		}
		
	}
}
