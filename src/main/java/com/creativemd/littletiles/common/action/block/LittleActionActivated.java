package com.creativemd.littletiles.common.action.block;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.LittleActionInteract;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LittleActionActivated extends LittleActionInteract {
	
	public LittleActionActivated(World world, BlockPos blockPos, EntityPlayer player) {
		super(world, blockPos, player);
	}
	
	public LittleActionActivated(World world, BlockPos blockPos, Vec3d pos, Vec3d look, boolean secondMode) {
		super(world, blockPos, pos, look, secondMode);
	}
	
	public LittleActionActivated() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeBoolean(preventInteraction);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		preventInteraction = buf.readBoolean();
	}
	
	public boolean preventInteraction = false;
	
	@Override
	protected boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException {
		if (tile.onBlockActivated(player.world, pos, player.world.getBlockState(pos), player, EnumHand.MAIN_HAND, player.getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float) moving.hitVec.x, (float) moving.hitVec.y, (float) moving.hitVec.z, this))
			return true;
		return false;
	}
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		if (!player.world.isRemote && uuid == null) // Block server right click event
			LittleEventHandler.addBlockTilePrevent(player);
		if (preventInteraction)
			return true;
		return super.action(player);
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
	
	@Override
	public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
		return null;
	}
	
}
