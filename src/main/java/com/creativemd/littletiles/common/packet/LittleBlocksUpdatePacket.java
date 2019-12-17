package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleBlocksUpdatePacket extends CreativeCorePacket {
	
	public List<BlockPos> positions;
	public List<IBlockState> states;
	public List<SPacketUpdateTileEntity> packets;
	public UUID uuid;
	
	public LittleBlocksUpdatePacket(World world, Set<? extends TileEntity> tileEntities) {
		positions = new ArrayList<>(tileEntities.size());
		states = new ArrayList<>(tileEntities.size());
		packets = new ArrayList<>(tileEntities.size());
		
		for (TileEntity te : tileEntities) {
			positions.add(te.getPos());
			states.add(world.getBlockState(te.getPos()));
			packets.add(te.getUpdatePacket());
		}
		
		if (world instanceof CreativeWorld)
			uuid = ((CreativeWorld) world).parent.getUniqueID();
	}
	
	public LittleBlocksUpdatePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(positions.size());
		for (int i = 0; i < positions.size(); i++) {
			writePos(buf, positions.get(i));
			writeState(buf, states.get(i));
			if (packets.get(i) != null) {
				buf.writeBoolean(true);
				writePacket(buf, packets.get(i));
			} else
				buf.writeBoolean(false);
		}
		
		if (uuid != null) {
			buf.writeBoolean(true);
			writeString(buf, uuid.toString());
		} else
			buf.writeBoolean(false);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		int size = buf.readInt();
		positions = new ArrayList<>(size);
		states = new ArrayList<>(size);
		packets = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			positions.add(readPos(buf));
			states.add(readState(buf));
			if (buf.readBoolean())
				packets.add((SPacketUpdateTileEntity) readPacket(buf));
			else
				packets.add(null);
		}
		
		if (buf.readBoolean())
			uuid = UUID.fromString(readString(buf));
		else
			uuid = null;
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		World world = player.world;
		
		if (uuid != null) {
			EntityAnimation animation = LittleDoorHandler.getHandler(true).findDoor(uuid);
			if (animation == null)
				return;
			
			world = animation.fakeWorld;
		}
		
		for (int i = 0; i < positions.size(); i++) {
			if (world instanceof WorldClient) {
				((WorldClient) world).invalidateRegionAndSetBlock(positions.get(i), states.get(i));
				if (packets.get(i) != null)
					((EntityPlayerSP) player).connection.handleUpdateTileEntity(packets.get(i));
			} else {
				world.setBlockState(positions.get(i), states.get(i), 3);
				TileEntity te = world.getTileEntity(positions.get(i));
				if (packets.get(i) != null)
					te.onDataPacket(((EntityPlayerSP) player).connection.getNetworkManager(), packets.get(i));
			}
			
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
}
