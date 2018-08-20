package com.creativemd.littletiles.common.packet;

import java.util.Iterator;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.utils.transformation.DoorTransformation;
import com.google.common.base.Predicate;

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
	public boolean completeData;
	
	public LittleEntityRequestPacket(UUID uuid, NBTTagCompound nbt, boolean completeData) {
		this.uuid = uuid;
		this.nbt = nbt;
		this.completeData = completeData;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writeString(buf, uuid.toString());
		buf.writeBoolean(completeData);
		writeNBT(buf, nbt);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		uuid = UUID.fromString(readString(buf));
		completeData = buf.readBoolean();
		nbt = readNBT(buf);
	}

	@Override
	public void executeClient(EntityPlayer player) {
		EntityDoorAnimation animation = null;
		for (Iterator<EntityDoorAnimation> iterator = player.world.getEntities(EntityDoorAnimation.class, new Predicate<EntityDoorAnimation>() {

			@Override
			public boolean apply(EntityDoorAnimation input) {
				return true;
			}
			
		}).iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if(entity instanceof EntityDoorAnimation && entity.getUniqueID().equals(uuid))
			{
				animation = (EntityDoorAnimation) entity;
				break;
			}
		}
		
		if(animation != null)
		{
			if(nbt.getBoolean("failed"))
			{
				animation.setDead();
			}else if(completeData){
				animation.readFromNBT(nbt);
				//animation.createClient();
				animation.updateBoundingBox();
				animation.approved = true;
				//animation.setPosition(animation.getAxisPos().getX(), animation.getAxisPos().getY(), animation.getAxisPos().getZ());
			}else{
				DoorTransformation transformation = DoorTransformation.loadFromNBT(nbt);
				animation.approved = animation.transformation.equals(transformation);
				//if(animation.approved)
					//animation.started = System.currentTimeMillis();
			}
			
		}else
			System.out.println("Something went wrong!");
	}

	@Override
	public void executeServer(EntityPlayer player) {
		EntityDoorAnimation animation = null;
		for (Iterator<EntityDoorAnimation> iterator = player.world.getEntities(EntityDoorAnimation.class, new Predicate<EntityDoorAnimation>() {

			@Override
			public boolean apply(EntityDoorAnimation input) {
				return true;
			}
			
		}).iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();
			if(entity instanceof EntityDoorAnimation && entity.getUniqueID().equals(uuid))
			{
				animation = (EntityDoorAnimation) entity;
				break;
			}
		}
		
		if(animation != null)
		{
			if(completeData)
				PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(uuid, animation.writeToNBT(new NBTTagCompound()), completeData), (EntityPlayerMP) player);
			else{
				PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(uuid,  animation.transformation.writeToNBT(new NBTTagCompound()), completeData), (EntityPlayerMP) player);
			}
		}else{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("failed", true);
			PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(uuid, nbt, false), (EntityPlayerMP) player);
		}
	}

	
	
}
