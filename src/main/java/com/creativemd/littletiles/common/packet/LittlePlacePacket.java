package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LittlePlacePacket extends CreativeCorePacket{
	
	public LittlePlacePacket()
	{
		
	}
	
	public LittlePlacePacket(ItemStack stack, Vec3 center, LittleTileVec size, int meta)
	{
		this.stack = stack;
		this.center = center;
		this.size = size;
		this.meta = meta;
	}
	
	public ItemStack stack;
	public Vec3 center;
	public LittleTileVec size;
	public int meta;
	
	@Override
	public void writeBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, stack);
		buf.writeDouble(center.xCoord);
		buf.writeDouble(center.yCoord);
		buf.writeDouble(center.zCoord);
		buf.writeByte(size.sizeX);
		buf.writeByte(size.sizeY);
		buf.writeByte(size.sizeZ);
		buf.writeInt(meta);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		stack = ByteBufUtils.readItemStack(buf);
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		center = Vec3.createVectorHelper(x, y, z);
		byte sizeX = buf.readByte();
		byte sizeY = buf.readByte();
		byte sizeZ = buf.readByte();
		size = new LittleTileVec(sizeX, sizeY, sizeZ);
		meta = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		if(stack.getItem() instanceof ItemBlockTiles)
		{
			if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemBlockTiles)
			{
				player.getHeldItem().stackTagCompound = stack.stackTagCompound;
			}
				
			((ItemBlockTiles)stack.getItem()).placeBlockAt(stack, player.worldObj, center, size, new PlacementHelper(player), meta);
		}
	}

}
