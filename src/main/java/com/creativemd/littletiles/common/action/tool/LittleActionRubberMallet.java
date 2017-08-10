package com.creativemd.littletiles.common.action.tool;

import java.util.Iterator;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class LittleActionRubberMallet extends LittleActionInteract {
	
	public boolean wholeBlock;
	
	public LittleActionRubberMallet(BlockPos blockPos, EntityPlayer player, boolean wholeBlock) {
		super(blockPos, player);
		this.wholeBlock = wholeBlock;
	}
	
	public LittleActionRubberMallet() {
		super();
	}

	@Override
	protected boolean isRightClick() {
		return true;
	}
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
			EntityPlayer player, RayTraceResult moving, BlockPos pos) throws LittleActionException {
		boolean push = !player.isSneaking();
		EnumFacing direction = moving.sideHit;
		if(!push)
			direction = direction.getOpposite();
		if(tile.canBeMoved(direction))
		{
			int times = wholeBlock ? LittleTile.gridSize : 1;
			while(times > 0)
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
							player.sendMessage(new TextComponentString("Cannot move structure (not all tiles are loaded)."));
					}
				}else
					if(ItemRubberMallet.moveTile(te, direction, tile, false, push))
						te.updateTiles();
					else
						return true;
				times--;
			}
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
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeBoolean(wholeBlock);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		wholeBlock = buf.readBoolean();
	}
}
