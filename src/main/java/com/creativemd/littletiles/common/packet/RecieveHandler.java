package com.creativemd.littletiles.common.packet;

import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RecieveHandler implements IMessageHandler<LittlePacket, IMessage> {

	@Override
	public IMessage onMessage(LittlePacket message, MessageContext ctx) {
		if(message.stack.getItem() instanceof ItemBlockTiles)
		{
			((ItemBlockTiles)message.stack.getItem()).placeBlockAt(message.stack, ctx.getServerHandler().playerEntity.worldObj, message.center, message.size, new PlacementHelper(ctx.getServerHandler().playerEntity), message.meta);
		}
		return null;
	}

}
