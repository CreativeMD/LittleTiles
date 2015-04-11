package com.creativemd.littletiles.common.packet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class LittleDestroyPacket implements IMessage {
	
	public LittleDestroyPacket()
	{
		
	}
	
	public int x;
	public int y;
	public int z;
	public NBTTagCompound nbt;
	
	public LittleDestroyPacket(int x, int y, int z, NBTTagCompound nbt)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.nbt = nbt;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeTag(buf, nbt);
	}

}
