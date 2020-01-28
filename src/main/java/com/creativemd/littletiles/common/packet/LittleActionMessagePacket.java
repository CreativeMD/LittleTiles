package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleActionMessagePacket extends CreativeCorePacket {
	
	public ActionMessage message;
	
	public LittleActionMessagePacket(ActionMessage message) {
		this.message = message;
	}
	
	public LittleActionMessagePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeActionMessage(message, buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		message = LittleAction.readActionMessage(buf);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		LittleTilesClient.displayActionMessage(message);
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
