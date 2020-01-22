package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.math.identifier.LittleIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileUpdatePacket extends CreativeCorePacket {
	
	public LittleIdentifierAbsolute coord;
	public NBTTagCompound nbt;
	
	public LittleTileUpdatePacket(LittleTile tile, NBTTagCompound nbt) {
		this.coord = new LittleIdentifierAbsolute(tile);
		this.nbt = nbt;
	}
	
	public LittleTileUpdatePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeAbsoluteCoord(coord, buf);
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		coord = LittleAction.readAbsoluteCoord(buf);
		nbt = readNBT(buf);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		LittleTile tile;
		try {
			tile = LittleAction.getTile(player.world, coord);
			if (tile.supportsUpdatePacket())
				tile.receivePacket(nbt, FMLClientHandler.instance().getClientToServerNetworkManager());
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
