package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.api.IBoxSelector;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;

public class LittleFlipPacket extends CreativeCorePacket {
	
	public LittleFlipPacket() {
		
	}
	
	public Axis axis;
	
	public LittleFlipPacket(Axis axis) {
		this.axis = axis;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(axis.ordinal());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		axis = Axis.values()[buf.readInt()];
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		execute(player);
	}
	
	public void execute(EntityPlayer player) {
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (PlacementHelper.getLittleInterface(stack) != null) {
			ILittleTile itile = PlacementHelper.getLittleInterface(stack);
			
			if (itile != null)
				itile.flipLittlePreview(player, stack, axis);
		}
		
		if (stack.getItem() instanceof IBoxSelector) {
			((IBoxSelector) stack.getItem()).flipLittlePreview(stack, axis);
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		execute(player);
	}
	
}
