package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.world.CreativeWorld;
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
import net.minecraft.world.World;

public class LittlePlacedAnimationPacket extends CreativeCorePacket {
	
	public UUID worldUUID;
	public LittleTileIdentifierAbsolute identifier;
	
	public LittlePlacedAnimationPacket(LittleTile tile) {
		this.identifier = new LittleTileIdentifierAbsolute(tile);
		if (tile.te.getWorld() instanceof CreativeWorld)
			this.worldUUID = ((CreativeWorld) tile.te.getWorld()).parent.getUniqueID();
	}
	
	public LittlePlacedAnimationPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writeNBT(buf, identifier.writeToNBT(new NBTTagCompound()));
		
		if (worldUUID != null) {
			buf.writeBoolean(true);
			writeString(buf, worldUUID.toString());
		} else
			buf.writeBoolean(false);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		identifier = new LittleTileIdentifierAbsolute(readNBT(buf));
		
		if (buf.readBoolean())
			worldUUID = UUID.fromString(readString(buf));
		else
			worldUUID = null;
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		World world = player.world;
		
		if (worldUUID != null) {
			EntityAnimation animation = LittleDoorHandler.getHandler(true).findDoor(worldUUID);
			if (animation == null)
				return;
			
			world = animation.fakeWorld;
		}
		
		try {
			LittleTile tile = LittleAction.getTile(world, identifier);
			if (tile.isChildOfStructure() && tile.connection.getStructure(world) instanceof LittleDoor)
				((LittleDoor) tile.connection.getStructureWithoutLoading()).waitingForApproval = false;
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
