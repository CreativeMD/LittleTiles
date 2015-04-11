package com.creativemd.littletiles.common.packet;

import net.minecraft.tileentity.TileEntity;

import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.PlacementHelper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class RecieveHandlerDestroy implements IMessageHandler<LittleDestroyPacket, IMessage> {

	@Override
	public IMessage onMessage(LittleDestroyPacket message, MessageContext ctx) {
		TileEntity entity = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
		if(entity instanceof TileEntityLittleTiles)
		{
			entity.readFromNBT(message.nbt);
			((TileEntityLittleTiles) entity).update();
		}
		return null;
	}

}
