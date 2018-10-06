package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class LittleSelectionModePacket extends CreativeCorePacket {
	
	public BlockPos pos;
	
	public LittleSelectionModePacket(BlockPos pos) {
		this.pos = pos;
	}
	
	public LittleSelectionModePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemRecipeAdvanced)
			ItemRecipeAdvanced.getSelectionMode(stack).onRightClick(player, stack, pos);
	}
	
}
