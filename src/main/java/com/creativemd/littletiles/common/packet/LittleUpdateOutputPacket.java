package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class LittleUpdateOutputPacket extends CreativeCorePacket {
	
	public StructureLocation location;
	public int index;
	public boolean[] state;
	
	public LittleUpdateOutputPacket() {
		
	}
	
	public LittleUpdateOutputPacket(StructureLocation location, int index, boolean[] state) {
		this.location = location;
		this.index = index;
		this.state = state;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		LittleAction.writeStructureLocation(location, buf);
		buf.writeInt(index);
		buf.writeInt(state.length);
		for (int i = 0; i < state.length; i++)
			buf.writeBoolean(state[i]);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		location = LittleAction.readStructureLocation(buf);
		index = buf.readInt();
		state = new boolean[buf.readInt()];
		for (int i = 0; i < state.length; i++)
			state[i] = buf.readBoolean();
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		try {
			LittleStructure structure = location.find(player.world);
			BooleanUtils.set(structure.getOutput(index).getState(), state);
		} catch (LittleActionException e) {}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
