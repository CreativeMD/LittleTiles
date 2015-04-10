package com.creativemd.littletiles.common.tileentity;

import java.util.ArrayList;

import com.creativemd.littletiles.common.utils.LittleTile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class TileEntityLittleTiles extends TileEntity{
	
	public ArrayList<LittleTile> tiles = new ArrayList<LittleTile>();
	
	/**Used for**/
	public LittleTile loadedTile = null;
	
	/**Used for placing a tile and can be used if a "cable" can connect to a direction*/
	public boolean isSpaceForLittleTile(AxisAlignedBB alignedBB)
	{
		for (int i = 0; i < tiles.size(); i++) {
			if(alignedBB.intersectsWith(tiles.get(i).getBox()))
				return false;
		}
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        tiles = new ArrayList<LittleTile>();
        int count = nbt.getInteger("tilesCount");
        for (int i = 0; i < count; i++) {
        	NBTTagCompound tileNBT = new NBTTagCompound();
        	tileNBT = nbt.getCompoundTag("t" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(tileNBT);
			if(tile != null)
				tiles.add(tile);
		}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tiles.get(i).save(tileNBT);
			nbt.setTag("t" + i, tileNBT);
		}
        nbt.setInteger("tilesCount", tiles.size());
    }
    
    @Override
    public Packet getDescriptionPacket()
    {
    	NBTTagCompound nbt = new NBTTagCompound();
    	for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tiles.get(i).sendToClient(tileNBT);
			nbt.setTag("t" + i, tileNBT);
		}
        nbt.setInteger("tilesCount", tiles.size());
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, nbt);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	tiles = new ArrayList<LittleTile>();
        int count = pkt.func_148857_g().getInteger("tilesCount");
        for (int i = 0; i < count; i++) {
        	NBTTagCompound tileNBT = new NBTTagCompound();
        	tileNBT = pkt.func_148857_g().getCompoundTag("t" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(tileNBT, true, net);
			if(tile != null)
				tiles.add(tile);
		}
    }
	
	public boolean updateLoadedTile(EntityPlayer player)
	{
		loadedTile = null;
		MovingObjectPosition hit = null;
		Vec3 look = player.getLook(player.capabilities.isCreativeMode ? 5.0F : 4.5F);
		Vec3 pos = player.getPosition(1);
		for (int i = 0; i < tiles.size(); i++) {
			MovingObjectPosition Temphit = tiles.get(i).getBox().calculateIntercept(pos, look); //TODO Check if this works out
			if(Temphit != null)
			{
				if(hit == null || hit.hitVec.distanceTo(pos) > Temphit.hitVec.distanceTo(pos))
				{
					hit = Temphit;
					loadedTile = tiles.get(i);
				}
			}
		}
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && hit != null)
			checkClientLoadedTile(hit.hitVec.distanceTo(pos));
		return loadedTile != null;
	}
	
	@SideOnly(Side.CLIENT)
	public void checkClientLoadedTile(double distance)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Vec3 pos = mc.thePlayer.getPosition(1);
		if(mc.objectMouseOver.hitVec.distanceTo(pos) < distance)
			loadedTile = null;
	}
	
	@Override
	public void updateEntity()
	{
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).updateEntity();
		}
	}
}
