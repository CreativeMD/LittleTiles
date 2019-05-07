package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.type.UUIDSupplier;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor;
import com.creativemd.littletiles.common.structure.type.door.LittleDoor.DoorOpeningResult;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleDoorPacket extends CreativeCorePacket {
	
	public LittleTileIdentifierAbsolute coord;
	public UUID uuid;
	public DoorOpeningResult result;
	
	public LittleDoorPacket(LittleTile tile, UUID uuid, DoorOpeningResult result) {
		this.coord = new LittleTileIdentifierAbsolute(tile);
		this.uuid = uuid;
		this.result = result;
	}
	
	public LittleDoorPacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeAbsoluteCoord(coord, buf);
		writeString(buf, uuid.toString());
		buf.writeBoolean(result.nbt != null);
		if (result.nbt != null)
			writeNBT(buf, result.nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		coord = LittleAction.readAbsoluteCoord(buf);
		uuid = UUID.fromString(readString(buf));
		if (buf.readBoolean())
			result = new DoorOpeningResult(readNBT(buf));
		else
			result = LittleDoor.EMPTY_OPENING_RESULT;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		// Only send if door failed to open on server side but was opened on client side
		EntityAnimation animation = LittleDoorHandler.getHandler(player.world).findDoor(uuid);
		if (animation != null)
			animation.isDead = true;
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		LittleTile tile;
		try {
			tile = LittleAction.getTile(player.world, coord);
			if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleDoor) {
				LittleDoor door = (LittleDoor) tile.connection.getStructureWithoutLoading();
				DoorOpeningResult doorResult = door.canOpenDoor(player);
				if (doorResult == null) {
					PacketHandler.sendPacketToPlayer(this, (EntityPlayerMP) player);
					LittleAction.sendBlockResetToClient((EntityPlayerMP) player, door);
					return;
				}
				EntityAnimation animation = door.openDoor(player, new UUIDSupplier(uuid), doorResult);
				if (animation != null && !doorResult.equals(result)) {
					System.out.println("Different door opening results client: " + result + ", server: " + doorResult + ". Send animation data to " + player.getDisplayNameString());
					PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound())), (EntityPlayerMP) player);
				}
			}
		} catch (LittleActionException e) {
			e.printStackTrace();
		}
	}
	
}
