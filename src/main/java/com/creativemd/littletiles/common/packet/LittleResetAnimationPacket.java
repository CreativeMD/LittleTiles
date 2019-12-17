package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class LittleResetAnimationPacket extends CreativeCorePacket {
	
	public UUID animationUUID;
	
	public LittleResetAnimationPacket(UUID animationUUID) {
		this.animationUUID = animationUUID;
	}
	
	public LittleResetAnimationPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writeString(buf, animationUUID.toString());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		animationUUID = UUID.fromString(readString(buf));
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		EntityAnimation animation = LittleDoorHandler.getHandler(true).findDoor(animationUUID);
		if (animation == null)
			return;
		animation.isDead = true;
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
