package com.creativemd.littletiles.common.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public abstract class LittleActionInteract extends LittleAction {
	
	public BlockPos blockPos;
	public Vec3d pos;
	public Vec3d look;
	public boolean secondMode;
	
	public LittleActionInteract(BlockPos blockPos, EntityPlayer player) {
		super();
		this.blockPos = blockPos;
		this.pos = player.getPositionEyes(1.0F);
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(1.0F);
		this.look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		this.secondMode = isUsingSecondMode(player);
	}
	
	public LittleActionInteract() {
		super();
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, blockPos);
		writeVec3d(pos, buf);
		writeVec3d(look, buf);
		buf.writeBoolean(secondMode);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
		pos = readVec3d(buf);
		look = readVec3d(buf);
		secondMode = buf.readBoolean();
	}
	
	protected abstract boolean isRightClick();
	
	protected abstract boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		TileEntity tileEntity = player.world.getTileEntity(blockPos);
		World world = player.world;
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			LittleTile tile = te.getFocusedTile(pos, look);
			
			if(!isAllowedToInteract(player, blockPos, isRightClick(), EnumFacing.EAST))
			{
				te.updateBlock();
				return false;
			}
			
			if(tile != null)
			{
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				RayTraceResult moving = rayTrace(te, tile);
				return action(world, te, tile, stack, player, moving, blockPos, secondMode);
			}else
				onTileNotFound();
		}else
			onTileEntityNotFound();
		return false;
			
	}
	
	public RayTraceResult rayTrace(TileEntityLittleTiles te, LittleTile tile)
	{
		return te.rayTrace(pos, look);
	}
	
	protected void onTileNotFound() throws LittleActionException
	{
		throw new LittleActionException.TileNotFoundException();
	}
	
	protected void onTileEntityNotFound() throws LittleActionException
	{
		throw new LittleActionException.TileEntityNotFoundException();
	}
	
}
