package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleNeighborUpdatePacket extends CreativeCorePacket {
	
	public UUID uuid;
	public List<BlockPos> positions;
	
	public LittleNeighborUpdatePacket(World world, List<BlockPos> positions) {
		this.positions = positions;
		if (world instanceof CreativeWorld)
			uuid = ((CreativeWorld) world).parent.getUniqueID();
	}
	
	public LittleNeighborUpdatePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(positions.size());
		for (int i = 0; i < positions.size(); i++) {
			writePos(buf, positions.get(i));
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
		for (int i = 0; i < size; i++) {
			positions.add(readPos(buf));
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
			BlockPos pos = positions.get(i);
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockTile)
				state.getBlock().neighborChanged(state, world, pos, state.getBlock(), null);
		}
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
