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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class LittleTileTileEntity extends LittleTile{
	
	public LittleTileTileEntity()
	{
		super();
	}
	
	public LittleTileTileEntity(Block block, int meta, LittleTileSize size, TileEntity tileEntity)
	{
		super(block, meta, size);
		this.tileEntity = tileEntity;
	}
	
	public boolean firstSended = false;
	
	public TileEntity tileEntity;
	
	/**All information the client needs*/
	@Override
	public void sendToClient(NBTTagCompound nbt)
	{
		super.sendToClient(nbt);
		if(!firstSended)
		{
			firstSended = true;
			NBTTagCompound nbtTag = new NBTTagCompound();
			tileEntity.writeToNBT(nbtTag);
			nbt.setTag("tileentity", nbtTag);
			nbt.setBoolean("isFirst", true);
		}else{
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
			}else{
				//TODO Send packet. No idea how!
			}
		}
	}
	
	/**Should apply all information from sendToCLient**/
	@Override
	@SideOnly(Side.CLIENT)
	public void recieveFromServer(NetworkManager net, NBTTagCompound nbt)
	{
		super.recieveFromServer(net, nbt);
		if(nbt.getBoolean("isFirst"))
		{
			tileEntity = TileEntity.createAndLoadEntity(nbt.getCompoundTag("tileentity"));
		}else{
			NBTTagCompound tileNBT = nbt.getCompoundTag("tileentity");
			if(tileEntity != null)
				tileEntity.onDataPacket(net, new S35PacketUpdateTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, meta, tileNBT));
		}
	}
	
	
	@Override
	public void load(World world, NBTTagCompound nbt)
	{
		super.load(world, nbt);
		NBTTagCompound tileNBT = nbt.getCompoundTag("tileEntity");
		if(tileNBT != null)
		{
			tileEntity = TileEntity.createAndLoadEntity(tileNBT);
			tileEntity.setWorldObj(world);
			if(tileEntity.isInvalid())
				setInValid();
		}
	}
	
	@Override
	public void save(World world, NBTTagCompound nbt)
	{
		super.save(world,nbt);
		if(tileEntity != null)
		{
			NBTTagCompound tileNBT = new NBTTagCompound();
			tileEntity.writeToNBT(tileNBT);
			nbt.setTag("tileEntity", tileNBT);
		}
	}
	
	@Override
	public void updateEntity(World world)
	{
		if(tileEntity != null)
		{
			if(tileEntity.getWorldObj() == null)
				tileEntity.setWorldObj(world);
			tileEntity.updateEntity();
		}
	}
	
	@Override
	public LittleTile copy()
	{
		LittleTileTileEntity tile = new LittleTileTileEntity(block, meta, size, tileEntity);
		copyCore(tile);
		return tile;
	}
	
	public TileEntityLittleTiles te;
	
	public boolean loadTileEntity()
	{
		if(tileEntity != null && tileEntity.getWorldObj() != null)
		{
			TileEntity Tempte = tileEntity.getWorldObj().getTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			if(Tempte instanceof TileEntityLittleTiles)
			{
				te = (TileEntityLittleTiles) Tempte;
				return true;
			}
		}
		return false;
	}
}
