package com.creativemd.littletiles.common.utils;

import java.io.IOException;

import io.netty.buffer.Unpooled;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;

public class LittleTileTileEntity extends LittleTile{
	
	public LittleTileTileEntity()
	{
		
	}
	
	public LittleTileTileEntity(Block block, int meta, LittleTileVec size, TileEntity tileEntity)
	{
		this.tileEntity = tileEntity;
	}
	
	public TileEntity tileEntity;
	
	/**All information the client needs*/
	@Override
	public void sendToClient(NBTTagCompound nbt)
	{
		super.sendToClient(nbt);
		Packet packet = tileEntity.getDescriptionPacket();
		if(packet instanceof S35PacketUpdateTileEntity)
		{
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				packet.writePacketData(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int x = buffer.readInt();
	        int y = buffer.readShort();
	        int z = buffer.readInt();
	        int meta = buffer.readUnsignedByte();
	        NBTTagCompound newNBT = null;
	        try {
				newNBT = buffer.readNBTTagCompoundFromBuffer();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        if(newNBT != null)
	        	nbt.setTag("tileentity", newNBT);
			//TODO Check if this cheat works
		}else{
			//TODO Send packet. No idea how!
		}
	}
	
	/**Should apply all information from sendToCLient**/
	@Override
	@SideOnly(Side.CLIENT)
	public void recieveFromServer(NetworkManager net, NBTTagCompound nbt)
	{
		super.recieveFromServer(net, nbt);
		NBTTagCompound tileNBT = nbt.getCompoundTag("tileentity");
		tileEntity.onDataPacket(net, new S35PacketUpdateTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, meta, tileNBT)); //TODO Check if using coords from tileentity is valid
	}
	
	
	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		tileEntity = TileEntity.createAndLoadEntity(nbt);
		if(tileEntity.isInvalid())
			setInValid();
	}
	
	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		if(tileEntity != null)
			tileEntity.writeToNBT(nbt);
	}
	
	@Override
	public void updateEntity()
	{
		if(tileEntity != null)
			tileEntity.updateEntity();
	}
	
	@Override
	public LittleTile copy()
	{
		LittleTileTileEntity tile = new LittleTileTileEntity(block, meta, size, tileEntity);
		copyCore(tile);
		return tile;
	}
	
	@Override
	public void onPlaced(ItemStack stack, TileEntityLittleTiles tileEntity)
	{
		super.onPlaced(stack, tileEntity);
		
	}
}
