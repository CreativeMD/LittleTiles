package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class LittleCustomPlacePacket extends CreativeCorePacket {
	
	public LittleTileVec min;
	public LittleTileVec max;
	public EnumFacing facing;
	public LittleTileVec originalMin;
	public LittleTileVec originalMax;
	
	public LittleCustomPlacePacket() {
		
	}
	
	public LittleCustomPlacePacket(LittleTileVec min, LittleTileVec max, EnumFacing facing, LittleTileVec originalMin, LittleTileVec originalMax) {
		this.min = min;
		this.max = max;
		this.facing = facing;
		this.originalMin = originalMin;
		this.originalMax = originalMax;
	}
	

	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(min.x);
		buf.writeInt(min.y);
		buf.writeInt(min.z);
		
		buf.writeInt(max.x);
		buf.writeInt(max.y);
		buf.writeInt(max.z);
		
		writeFacing(buf, facing);
		
		buf.writeInt(originalMin.x);
		buf.writeInt(originalMin.y);
		buf.writeInt(originalMin.z);
		
		buf.writeInt(originalMax.x);
		buf.writeInt(originalMax.y);
		buf.writeInt(originalMax.z);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		min = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
		max = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
		
		facing = readFacing(buf);
		
		originalMin = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
		originalMax = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
	}

	@Override
	public void executeClient(EntityPlayer player) {
		
	}

	@Override
	public void executeServer(EntityPlayer player) {
		ItemStack stack = player.getHeldItemMainhand();
		if(stack.getItem() instanceof ItemLittleChisel)
		{
			ItemLittleChisel.placePreviews(player.world, min, max, player, stack, facing, originalMin, originalMax);
		}
	}

}
