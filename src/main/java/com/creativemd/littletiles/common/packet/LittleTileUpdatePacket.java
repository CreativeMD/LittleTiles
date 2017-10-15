package com.creativemd.littletiles.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileTE;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileUpdatePacket extends CreativeCorePacket {
	
	public BlockPos pos;
	public LittleTileVec cornerVec;
	public NBTTagCompound nbt;
	
	public LittleTileUpdatePacket(LittleTile tile, NBTTagCompound nbt) {
		this.pos = tile.te.getPos();
		this.cornerVec = tile.getCornerVec();
		this.nbt = nbt;
	}
	
	public LittleTileUpdatePacket() {
		
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		buf.writeInt(cornerVec.x);
		buf.writeInt(cornerVec.y);
		buf.writeInt(cornerVec.z);
		writeNBT(buf, nbt);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		cornerVec = new LittleTileVec(buf.readInt(), buf.readInt(), buf.readInt());
		nbt = readNBT(buf);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		TileEntity te = player.world.getTileEntity(pos);
		if(te instanceof TileEntityLittleTiles)
		{
			LittleTile tile = ((TileEntityLittleTiles) te).getTile(cornerVec);
			if(tile.supportsUpdatePacket())
				tile.receivePacket(nbt, FMLClientHandler.instance().getClientToServerNetworkManager());
		}
	}

	@Override
	public void executeServer(EntityPlayer player) {
		
	}

}
