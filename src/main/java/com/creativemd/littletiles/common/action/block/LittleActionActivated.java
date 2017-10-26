package com.creativemd.littletiles.common.action.block;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LittleActionActivated extends LittleActionInteract{
	
	public LittleActionActivated(BlockPos blockPos, EntityPlayer player) {
		super(blockPos, player);
	}
	
	public LittleActionActivated() {
		
	}
	
	@Override
	protected void onTileNotFound() throws LittleActionException
	{
		LittleEvent.cancelNext = true;
		BlockTile.cancelNext = true;
	}
	
	@Override
	protected void onTileEntityNotFound() throws LittleActionException
	{
		LittleEvent.cancelNext = true;
		BlockTile.cancelNext = true;
	}

	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player,
			RayTraceResult moving, BlockPos pos) throws LittleActionException {
		if(tile.onBlockActivated(player.world, pos, player.world.getBlockState(pos), player, EnumHand.MAIN_HAND, player.getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord))
		{
			BlockTile.cancelNext = true;
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		super.action(player);
		if(!player.world.isRemote)
			BlockTile.cancelNext = true;
		return true;
	}

	@Override
	public boolean canBeReverted() {
		return false;
	}

	@Override
	public LittleAction revert() {
		return null;
	}

	@Override
	protected boolean isRightClick() {
		return true;
	}
	
}
