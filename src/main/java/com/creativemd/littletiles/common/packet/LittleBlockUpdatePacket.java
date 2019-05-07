package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;

public class LittleBlockUpdatePacket extends CreativeCorePacket {
	
	public List<BlockPos> positions;
	public List<IBlockState> states;
	public List<NBTTagCompound> data;
	
	public LittleBlockUpdatePacket(Set<TileEntityLittleTiles> tileEntities) {
		positions = new ArrayList<>(tileEntities.size());
		states = new ArrayList<>(tileEntities.size());
		data = new ArrayList<>(tileEntities.size());
		
		for (TileEntityLittleTiles te : tileEntities) {
			positions.add(te.getPos());
			states.add(te.getBlockTileState());
			NBTTagCompound nbt = new NBTTagCompound();
			te.getDescriptionNBT(nbt);
			data.add(nbt);
		}
	}
	
	public LittleBlockUpdatePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(positions.size());
		for (int i = 0; i < positions.size(); i++) {
			writePos(buf, positions.get(i));
			writeState(buf, states.get(i));
			writeNBT(buf, data.get(i));
		}
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		int size = buf.readInt();
		positions = new ArrayList<>(size);
		states = new ArrayList<>(size);
		data = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			positions.add(readPos(buf));
			states.add(readState(buf));
			data.add(readNBT(buf));
		}
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		for (int i = 0; i < positions.size(); i++) {
			((WorldClient) player.world).invalidateRegionAndSetBlock(positions.get(i), states.get(i));
			((EntityPlayerSP) player).connection.handleUpdateTileEntity(new SPacketUpdateTileEntity(positions.get(i), 0, data.get(i)));
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
}
