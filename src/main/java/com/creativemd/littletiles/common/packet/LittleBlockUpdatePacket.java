package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleBlockUpdatePacket extends CreativeCorePacket {
	
	public UUID uuid;
	public IBlockState state;
	public BlockPos pos;
	public SPacketUpdateTileEntity packet;
	
	public LittleBlockUpdatePacket(World world, BlockPos pos, @Nullable TileEntity te) {
		this.pos = pos;
		this.state = world.getBlockState(pos);
		if (te != null)
			packet = te.getUpdatePacket();
		if (world instanceof CreativeWorld)
			uuid = ((CreativeWorld) world).parent.getUniqueID();
	}
	
	public LittleBlockUpdatePacket() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		writeState(buf, state);
		if (packet != null) {
			buf.writeBoolean(true);
			writePacket(buf, packet);
			
		} else
			buf.writeBoolean(false);
		
		if (uuid != null) {
			buf.writeBoolean(true);
			writeString(buf, uuid.toString());
		} else
			buf.writeBoolean(false);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		state = readState(buf);
		if (buf.readBoolean())
			packet = (SPacketUpdateTileEntity) readPacket(buf);
		
		if (buf.readBoolean())
			uuid = UUID.fromString(readString(buf));
		else
			uuid = null;
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		World world = player.world;
		
		if (uuid != null) {
			EntityAnimation animation = WorldAnimationHandler.findAnimation(true, uuid);
			if (animation == null)
				return;
			
			world = animation.fakeWorld;
		}
		
		if (world instanceof WorldClient)
			((WorldClient) world).invalidateRegionAndSetBlock(pos, state);
		else
			world.setBlockState(pos, state, 3);
		if (packet != null)
			packet.processPacket(((EntityPlayerSP) player).connection);
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
}
