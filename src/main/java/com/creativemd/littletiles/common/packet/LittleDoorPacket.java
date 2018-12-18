package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.type.LittleDoorBase;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoorPacket extends CreativeCorePacket {
	
	public LittleTileIdentifierAbsolute coord;
	public UUID uuid;
	
	public LittleDoorPacket(LittleTile tile, UUID uuid) {
		this.coord = new LittleTileIdentifierAbsolute(tile);
		this.uuid = uuid;
	}
	
	public LittleDoorPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeAbsoluteCoord(coord, buf);
		writeString(buf, uuid.toString());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		coord = LittleAction.readAbsoluteCoord(buf);
		uuid = UUID.fromString(readString(buf));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		LittleTile tile;
		try {
			tile = LittleAction.getTile(player.world, coord);
			if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleDoorBase)
				((LittleDoorBase) tile.connection.getStructureWithoutLoading()).openDoor(player.world, player, uuid);
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		LittleTile tile;
		try {
			tile = LittleAction.getTile(player.world, coord);
			if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleDoorBase)
				((LittleDoorBase) tile.connection.getStructureWithoutLoading()).openDoor(player.world, player, uuid);
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
	}
	
}
