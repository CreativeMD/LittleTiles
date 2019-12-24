package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.DoorActivationResult;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.DoorOpeningResult;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleActivateDoorPacket extends CreativeCorePacket {
	
	public LittleTileIdentifierAbsolute coord;
	public UUID worldUUID;
	public UUID uuid;
	public DoorOpeningResult result;
	
	public LittleActivateDoorPacket(LittleTile tile, UUID uuid, DoorOpeningResult result) {
		this.coord = new LittleTileIdentifierAbsolute(tile);
		this.uuid = uuid;
		this.result = result;
		if (tile.te.getWorld() instanceof CreativeWorld)
			this.worldUUID = ((CreativeWorld) tile.te.getWorld()).parent.getUniqueID();
	}
	
	public LittleActivateDoorPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeAbsoluteCoord(coord, buf);
		writeString(buf, uuid.toString());
		buf.writeBoolean(result.nbt != null);
		if (result.nbt != null)
			writeNBT(buf, result.nbt);
		
		if (worldUUID != null) {
			buf.writeBoolean(true);
			writeString(buf, worldUUID.toString());
		} else
			buf.writeBoolean(false);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		coord = LittleAction.readAbsoluteCoord(buf);
		uuid = UUID.fromString(readString(buf));
		if (buf.readBoolean())
			result = new DoorOpeningResult(readNBT(buf));
		else
			result = LittleDoor.EMPTY_OPENING_RESULT;
		
		if (buf.readBoolean())
			worldUUID = UUID.fromString(readString(buf));
		else
			worldUUID = null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) { // Note it does not take care of synchronization, just sends an activation to the client, only used for already animated doors
		World world = player.world;
		
		if (worldUUID != null) {
			EntityAnimation animation = LittleDoorHandler.getHandler(true).findDoor(worldUUID);
			if (animation == null)
				return;
			
			world = animation.fakeWorld;
		}
		
		try {
			LittleTile tile = LittleAction.getTile(world, coord);
			
			if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleDoor) {
				LittleDoor door = (LittleDoor) tile.connection.getStructureWithoutLoading();
				door.activate(null, tile, uuid, false);
			}
			
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		LittleTile tile;
		EntityAnimation animation = null;
		
		try {
			World world = player.world;
			
			if (worldUUID != null) {
				animation = LittleDoorHandler.getHandler(false).findDoor(worldUUID);
				if (animation == null)
					return;
				
				world = animation.fakeWorld;
			}
			
			tile = LittleAction.getTile(world, coord);
			if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleDoor) {
				LittleDoor door = (LittleDoor) tile.connection.getStructureWithoutLoading();
				
				DoorActivationResult activationResult = door.activate(player, tile, uuid, true);
				if (activationResult == null) {
					if (door instanceof IAnimatedStructure && ((IAnimatedStructure) door).isAnimated())
						PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(((IAnimatedStructure) door).getAnimation().getUniqueID(), ((IAnimatedStructure) door).getAnimation().writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
					else {
						PacketHandler.sendPacketToPlayer(new LittleResetAnimationPacket(uuid), (EntityPlayerMP) player);
						LittleAction.sendBlockResetToClient(world, (EntityPlayerMP) player, door);
					}
					return;
				}
				
				if (activationResult.animation != null && !activationResult.result.equals(this.result)) {
					System.out.println("Different door opening results client: " + this.result + ", server: " + activationResult.result + ". Send animation data to " + player.getDisplayNameString());
					PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(activationResult.animation.getUniqueID(), activationResult.animation.writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
				}
			}
		} catch (LittleActionException e) {
			if (animation != null)
				PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), false), (EntityPlayerMP) player);
			else
				PacketHandler.sendPacketToPlayer(new LittleResetAnimationPacket(uuid), (EntityPlayerMP) player);
			
		}
	}
	
}
