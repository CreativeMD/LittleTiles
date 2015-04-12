package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LittleDestroyPacket extends CreativeCorePacket {
	
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
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeTag(buf, nbt);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		TileEntity entity = player.worldObj.getTileEntity(x, y, z);
		if(entity instanceof TileEntityLittleTiles)
		{
			entity.readFromNBT(nbt);
			((TileEntityLittleTiles) entity).update();
		}
	}

}
