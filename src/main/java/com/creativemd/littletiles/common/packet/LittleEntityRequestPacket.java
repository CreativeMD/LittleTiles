package com.creativemd.littletiles.common.packet;

import java.util.Iterator;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
		EntityAnimation animation = null;
		for (Iterator<Entity> iterator = player.worldObj.getLoadedEntityList().iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if(entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid))
			{
				animation = (EntityAnimation) entity;
				break;
			}
		}
		
		if(animation != null)
			animation.readFromNBT(nbt);
	}

	@Override
	public void executeServer(EntityPlayer player) {
		EntityAnimation animation = null;
		for (Iterator<Entity> iterator = player.worldObj.getLoadedEntityList().iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if(entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid))
			{
				animation = (EntityAnimation) entity;
				break;
			}
		}
		
		if(animation != null)
			PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(uuid, animation.writeToNBT(new NBTTagCompound())), (EntityPlayerMP) player);
	}

	
	
}
