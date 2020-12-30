package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.item.ItemLittleScrewdriver;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class LittleScrewdriverSelectionPacket extends CreativeCorePacket {
	
	public BlockPos pos;
	public boolean rightClick;
	
	public LittleScrewdriverSelectionPacket(BlockPos pos, boolean rightClick) {
		this.pos = pos;
		this.rightClick = rightClick;
	}
	
	public LittleScrewdriverSelectionPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		buf.writeBoolean(rightClick);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		rightClick = buf.readBoolean();
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemLittleScrewdriver)
			((ItemLittleScrewdriver) stack.getItem()).onClick(player, rightClick, pos, stack);
	}
	
}
