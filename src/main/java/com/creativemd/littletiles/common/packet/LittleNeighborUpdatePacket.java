package com.creativemd.littletiles.common.packet;

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
	public BlockPos pos;
	public BlockPos fromPos;
	
	public LittleNeighborUpdatePacket(World world, BlockPos pos, BlockPos fromPos) {
		this.pos = pos;
		this.fromPos = fromPos;
		if (world instanceof CreativeWorld)
			uuid = ((CreativeWorld) world).parent.getUniqueID();
	}
	
	public LittleNeighborUpdatePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		writePos(buf, fromPos);
		
		if (uuid != null) {
			buf.writeBoolean(true);
			writeString(buf, uuid.toString());
		} else
			buf.writeBoolean(false);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		fromPos = readPos(buf);
		
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
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockTile)
			state.getBlock().neighborChanged(state, world, pos, state.getBlock(), fromPos);
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
