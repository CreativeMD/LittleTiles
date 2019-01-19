package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

public class LittleEntityInteractPacket extends CreativeCorePacket {
	
	public UUID uuid;
	
	public LittleEntityInteractPacket(UUID uuid) {
		this.uuid = uuid;
	}
	
	public LittleEntityInteractPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writeString(buf, uuid.toString());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		uuid = UUID.fromString(readString(buf));
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		for (EntityAnimation animation : LittleDoorHandler.client.openDoors) {
			if (animation.getUniqueID().equals(uuid)) {
				animation.onRightClick(player);
				break;
			}
		}
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		for (EntityAnimation animation : LittleDoorHandler.server.openDoors) {
			if (animation.getUniqueID().equals(uuid)) {
				if (animation.onRightClick(player))
					for (EntityPlayer toPlayer : ((WorldServer) animation.world).getEntityTracker().getTrackingPlayers(animation))
						PacketHandler.sendPacketToPlayer(new LittleEntityInteractPacket(uuid), (EntityPlayerMP) toPlayer);
				break;
			}
		}
	}
	
}
