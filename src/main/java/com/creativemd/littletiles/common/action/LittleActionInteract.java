package com.creativemd.littletiles.common.action;

import java.util.UUID;

import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class LittleActionInteract extends LittleAction {
	
	public BlockPos blockPos;
	public Vec3d pos;
	public Vec3d look;
	
	public Vec3d transformedPos;
	public Vec3d transformedLook;
	
	public boolean secondMode;
	public UUID uuid;
	
	public boolean transformedCoordinates = false;
	
	public LittleActionInteract(World world, BlockPos blockPos, EntityPlayer player) {
		super();
		this.blockPos = blockPos;
		this.pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		this.look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		this.secondMode = isUsingSecondMode(player);
		if (world instanceof CreativeWorld)
			uuid = ((CreativeWorld) world).parent.getUniqueID();
	}
	
	public LittleActionInteract(World world, BlockPos blockPos, Vec3d pos, Vec3d look, boolean secondMode) {
		super();
		this.blockPos = blockPos;
		this.pos = pos;
		this.look = look;
		this.secondMode = secondMode;
		if (world instanceof CreativeWorld)
			uuid = ((CreativeWorld) world).parent.getUniqueID();
	}
	
	public LittleActionInteract() {
		super();
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, blockPos);
		writeVec3d(transformedPos, buf);
		writeVec3d(transformedLook, buf);
		buf.writeBoolean(secondMode);
		
		if (uuid != null) {
			buf.writeBoolean(true);
			writeString(buf, uuid.toString());
		} else
			buf.writeBoolean(false);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
		pos = readVec3d(buf);
		look = readVec3d(buf);
		secondMode = buf.readBoolean();
		if (buf.readBoolean())
			uuid = UUID.fromString(readString(buf));
		else
			uuid = null;
		
		transformedCoordinates = true;
	}
	
	protected abstract boolean isRightClick();
	
	protected abstract boolean action(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, boolean secondMode) throws LittleActionException;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		World world = player.world;
		
		transformedPos = this.pos;
		transformedLook = this.look;
		
		if (uuid != null) {
			EntityAnimation animation = WorldAnimationHandler.findAnimation(player.world.isRemote, uuid);
			if (animation == null)
				onEntityNotFound();
			
			if (!isAllowedToInteract(player, animation, isRightClick())) {
				sendEntityResetToClient((EntityPlayerMP) player, animation);
				return false;
			}
			
			world = animation.fakeWorld;
			if (!transformedCoordinates) {
				transformedPos = animation.origin.transformPointToFakeWorld(transformedPos);
				transformedLook = animation.origin.transformPointToFakeWorld(transformedLook);
			}
		}
		
		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity instanceof TileEntityLittleTiles) {
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			LittleTile tile = te.getFocusedTile(transformedPos, transformedLook);
			
			if (!isAllowedToInteract(world, player, blockPos, isRightClick(), EnumFacing.EAST)) {
				sendBlockResetToClient(world, (EntityPlayerMP) player, te);
				return false;
			}
			
			if (tile != null) {
				ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
				RayTraceResult moving = rayTrace(te, tile, transformedPos, transformedLook);
				if (moving != null)
					return action(world, te, tile, stack, player, moving, blockPos, secondMode);
			} else
				onTileNotFound();
		} else
			onTileEntityNotFound();
		return false;
		
	}
	
	public RayTraceResult rayTrace(TileEntityLittleTiles te, LittleTile tile, Vec3d pos, Vec3d look) {
		return te.rayTrace(pos, look);
	}
	
	protected void onEntityNotFound() throws LittleActionException {
		throw new LittleActionException.EntityNotFoundException();
	}
	
	protected void onTileNotFound() throws LittleActionException {
		throw new LittleActionException.TileNotFoundException();
	}
	
	protected void onTileEntityNotFound() throws LittleActionException {
		throw new LittleActionException.TileEntityNotFoundException();
	}
	
}
