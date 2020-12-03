package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class LittleSelectionModePacket extends CreativeCorePacket {
	
	public BlockPos pos;
	public boolean rightClick;
	
	public LittleSelectionModePacket(BlockPos pos, boolean rightClick) {
		this.pos = pos;
		this.rightClick = rightClick;
	}
	
	public LittleSelectionModePacket() {
		
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
		if (stack.getItem() instanceof ItemLittleRecipeAdvanced)
			if (rightClick)
				ItemLittleRecipeAdvanced.getSelectionMode(stack).onRightClick(player, stack, pos);
			else
				ItemLittleRecipeAdvanced.getSelectionMode(stack).onLeftClick(player, stack, pos);
	}
	
}
