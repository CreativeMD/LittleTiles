package com.creativemd.littletiles.common.packet;

import java.util.Iterator;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class LittleEntityRequestPacket extends CreativeCorePacket {
	
	public LittleEntityRequestPacket() {
		
	}
	
	public UUID uuid;
	public NBTTagCompound nbt;
	
	public LittleEntityRequestPacket(UUID uuid, NBTTagCompound nbt) {
		this.uuid = uuid;
		this.nbt = nbt;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writeString(buf, uuid.toString());
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		uuid = UUID.fromString(readString(buf));
		nbt = readNBT(buf);
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		for (Iterator<EntityAnimation> iterator = player.world.getEntities(EntityAnimation.class, new Predicate<EntityAnimation>() {
			
			@Override
			public boolean apply(EntityAnimation input) {
				return true;
			}
			
		}).iterator();iterator.hasNext();) {
			Entity entity = iterator.next();
			if (entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid)) {
				EntityAnimation animation = (EntityAnimation) entity;
				animation.readFromNBT(nbt);
				animation.updateBoundingBox();
				animation.preventPush = true;
				animation.onUpdateForReal();
				animation.preventPush = false;
				
				animation.prevWorldOffsetX = animation.worldOffsetX;
				animation.prevWorldOffsetY = animation.worldOffsetY;
				animation.prevWorldOffsetZ = animation.worldOffsetZ;
				
				animation.prevWorldRotX = animation.worldRotX;
				animation.prevWorldRotY = animation.worldRotY;
				animation.prevWorldRotZ = animation.worldRotZ;
				return;
			}
		}
		System.out.println("Entity not found!");
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
