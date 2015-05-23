package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LittlePlacePacket extends CreativeCorePacket{
	
	public LittlePlacePacket()
	{
		
	}
	
	public LittlePlacePacket(ItemStack stack, Vec3 center, LittleTileSize size, int x, int y, int z, float offsetX, float offsetY, float offsetZ, int side, int direction, int direction2)
	{
		this.stack = stack;
		this.center = center;
		this.size = size;
		this.x = x;
		this.y = y;
		this.z = z;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.side = side;
		this.direction = direction;
		this.direction2 = direction2;
	}
	
	public ItemStack stack;
	public Vec3 center;
	public LittleTileSize size;
	public int x;
	public int y;
	public int z;
	public float offsetX;
	public float offsetY;
	public float offsetZ;
	public int side;
	public int direction;
	public int direction2;
	
	@Override
	public void writeBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, stack);
		buf.writeDouble(center.xCoord);
		buf.writeDouble(center.yCoord);
		buf.writeDouble(center.zCoord);
		buf.writeByte(size.sizeX);
		buf.writeByte(size.sizeY);
		buf.writeByte(size.sizeZ);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeFloat(offsetX);
		buf.writeFloat(offsetY);
		buf.writeFloat(offsetZ);
		buf.writeInt(side);
		buf.writeInt(direction);
		buf.writeInt(direction2);
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
		size = new LittleTileSize(sizeX, sizeY, sizeZ);
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.offsetX = buf.readFloat();
		this.offsetY = buf.readFloat();
		this.offsetZ = buf.readFloat();
		this.side = buf.readInt();
		this.direction = buf.readInt();
		this.direction2 = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		if(PlacementHelper.isLittleBlock(stack))
		{
			PlacementHelper helper = new PlacementHelper(player, x, y, z);
			helper.side = side;
			
			((ItemBlockTiles)Item.getItemFromBlock(LittleTiles.blockTile)).placeBlockAt(stack, player.worldObj, center, size, helper, x, y, z, offsetX, offsetY, offsetZ, ForgeDirection.getOrientation(direction), ForgeDirection.getOrientation(direction2));
			
			EntityPlayerMP playerMP = (EntityPlayerMP) player;
			playerMP.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(playerMP.openContainer.windowId, playerMP.inventory.currentItem, playerMP.inventory.getCurrentItem()));
			
		}
	}

}
