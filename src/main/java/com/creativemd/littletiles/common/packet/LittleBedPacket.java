package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.structure.LittleBed;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class LittleBedPacket extends CreativeCorePacket {
	
	public BlockPos pos;
	public LittleTileVec vec;
	public int playerID;
	
	public LittleBedPacket() {
		
	}
	
	public LittleBedPacket(BlockPos pos, LittleTileVec vec) {
		this.pos = pos;
		this.vec = vec;
		this.playerID = -1;
	}
	
	public LittleBedPacket(BlockPos pos, LittleTileVec vec, EntityPlayer player) {
		this.pos = pos;
		this.vec = vec;
		this.playerID = player.getEntityId();
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		buf.writeInt(vec.x);
		buf.writeInt(vec.y);
		buf.writeInt(vec.z);
		buf.writeInt(playerID);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		vec = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
		playerID = buf.readInt();
	}

	@Override
	public void executeClient(EntityPlayer player) {
		Entity entity = playerID == -1 ? player : player.worldObj.getEntityByID(playerID);
		if(entity instanceof EntityPlayer)
		{
			TileEntityLittleTiles te = BlockTile.loadTe(player.worldObj, pos);
			if(te != null)
			{
				LittleTile tile = te.getTile(vec);
				if(tile != null && tile.isLoaded() && tile.structure instanceof LittleBed)
				{
					((LittleBed) tile.structure).trySleep((EntityPlayer) entity, tile.structure.getHighestCenterPoint());
				}
			}
		}
	}

	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
	
}
