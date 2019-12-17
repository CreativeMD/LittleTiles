package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class LittlePlacedAnimationPacket extends CreativeCorePacket {
	
	public UUID uuid;
	public LittleTileIdentifierAbsolute identifier;
	
	public LittlePlacedAnimationPacket(UUID uuid, LittleTile tile) {
		this.uuid = uuid;
		this.identifier = new LittleTileIdentifierAbsolute(tile);
	}
	
	public LittlePlacedAnimationPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writeString(buf, uuid.toString());
		writeNBT(buf, identifier.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		uuid = UUID.fromString(readString(buf));
		identifier = new LittleTileIdentifierAbsolute(readNBT(buf));
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		EntityAnimation animation = LittleDoorHandler.getHandler(true).findDoor(uuid);
		if (animation != null && !animation.controller.isWaitingForRender())
			animation.controller.onPlacedByServer();
		else {
			try {
				LittleTile tile = LittleAction.getTile(player.world, identifier);
				if (tile.isChildOfStructure() && tile.connection.getStructure(player.world) instanceof LittleDoor)
					((LittleDoor) tile.connection.getStructureWithoutLoading()).waitingForApproval = false;
			} catch (LittleActionException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
