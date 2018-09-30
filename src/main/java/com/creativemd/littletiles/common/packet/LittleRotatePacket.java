package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class LittleRotatePacket extends CreativeCorePacket {
	
	public LittleRotatePacket() {
		
	}
	
	public Rotation rotation;
	
	public LittleRotatePacket(Rotation rotation) {
		this.rotation = rotation;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(rotation.ordinal());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		rotation = Rotation.values()[buf.readInt()];
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		execute(player);
	}
	
	public void execute(EntityPlayer player) {
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (PlacementHelper.getLittleInterface(stack) != null) {
			ILittleTile itile = null;
			
			if (stack.getItem() instanceof ILittleTile) {
				itile = (ILittleTile) stack.getItem();
			} else if (Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile) {
				itile = (ILittleTile) Block.getBlockFromItem(stack.getItem());
			}
			
			if (itile != null)
				itile.rotateLittlePreview(player, stack, rotation);
		}
		
		if (stack.getItem() instanceof ISpecialBlockSelector) {
			((ISpecialBlockSelector) stack.getItem()).rotateLittlePreview(stack, rotation);
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		execute(player);
	}
	
}
