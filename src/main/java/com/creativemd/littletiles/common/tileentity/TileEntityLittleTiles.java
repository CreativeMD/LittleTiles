package com.creativemd.littletiles.common.tileentity;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

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
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class TileEntityLittleTiles extends TileEntity{
	
	public TileList<LittleTile> tiles = new TileList<LittleTile>();
	
	//public boolean needFullUpdate = true;
	
	public boolean removeTile(LittleTile tile)
	{
		update();
		//needFullUpdate = true;
		boolean result = tiles.remove(tile);
		updateNeighbor();
		return result;
	}
	
	public boolean addTile(LittleTile tile)
	{
		update();
		//needFullUpdate = true;
		boolean result = tiles.add(tile);
		updateNeighbor();
		return result;
	}
	
	public void updateNeighbor()
	{
		
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).onNeighborChangeInside();
		}
		worldObj.notifyBlockChange(xCoord, yCoord, zCoord, LittleTiles.blockTile);
	}
	
	//public boolean needFullUpdate = true;
	
	/**Used for**/
	public LittleTile loadedTile = null;
	
	/**Used for placing a tile and can be used if a "cable" can connect to a direction*/
	public boolean isSpaceForLittleTile(CubeObject cube)
	{
		return isSpaceForLittleTile(cube.getAxis());
	}
	
	/**Used for placing a tile and can be used if a "cable" can connect to a direction*/
	public boolean isSpaceForLittleTile(AxisAlignedBB alignedBB, LittleTile ignoreTile)
	{
		for (int i = 0; i < tiles.size(); i++) {
			for (int j = 0; j < tiles.get(i).boundingBoxes.size(); j++) {
				if(ignoreTile != tiles.get(i) && alignedBB.intersectsWith(tiles.get(i).boundingBoxes.get(j).getBox()))
					return false;
			}
			
		}
		return true;
	}
	
	/**Used for placing a tile and can be used if a "cable" can connect to a direction*/
	public boolean isSpaceForLittleTile(AxisAlignedBB alignedBB)
	{
		return isSpaceForLittleTile(alignedBB, null);
	}
	
	public boolean isSpaceForLittleTile(LittleTileBox box)
	{
		return isSpaceForLittleTile(box.getBox());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if(tiles != null)
        	tiles.clear();
        tiles = new TileList<LittleTile>();
        int count = nbt.getInteger("tilesCount");
        for (int i = 0; i < count; i++) {
        	NBTTagCompound tileNBT = new NBTTagCompound();
        	tileNBT = nbt.getCompoundTag("t" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(this, worldObj, tileNBT);
			if(tile != null)
				tiles.add(tile);
		}
        //update();
    }

	@Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tiles.get(i).saveTile(tileNBT);
			nbt.setTag("t" + i, tileNBT);
		}
        nbt.setInteger("tilesCount", tiles.size());
    }
    
    @Override
    public Packet getDescriptionPacket()
    {
    	NBTTagCompound nbt = new NBTTagCompound();
    	//writeToNBT(nbt);
    	for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			NBTTagCompound packet = new NBTTagCompound();
			tiles.get(i).saveTile(tileNBT);
			tiles.get(i).updatePacket(packet);
			//tileNBT.setByte("x", tiles.get(i).cornerVec.x);
			//tileNBT.setByte("y", tiles.get(i).cornerVec.y);
			//tileNBT.setByte("z", tiles.get(i).cornerVec.z);
			tileNBT.setTag("update", packet);
			nbt.setTag("t" + i, tileNBT);
		}
        nbt.setInteger("tilesCount", tiles.size());
        //if(needFullUpdate)
        //{
        //nbt.setBoolean("fullUpdate", true);
        	//needFullUpdate = false;
        //}
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, blockMetadata, nbt);
    }
    
    public LittleTile getTile(LittleTileVec vec)
    {
    	return getTile((byte)vec.x, (byte)vec.y, (byte)vec.z);
    }
    
    public LittleTile getTile(byte minX, byte minY, byte minZ)
    {
    	for (int i = 0; i < tiles.size(); i++) {
			if(tiles.get(i).cornerVec.x == minX && tiles.get(i).cornerVec.y == minY && tiles.get(i).cornerVec.z == minZ)
				return tiles.get(i);
		}
    	return null;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	/*if(pkt.func_148857_g().getBoolean("fullUpdate"))
    	{
    		tiles = new ArrayList<LittleTile>();
	        int count = pkt.func_148857_g().getInteger("tilesCount");
	        for (int i = 0; i < count; i++) {
	        	NBTTagCompound tileNBT = new NBTTagCompound();
	        	tileNBT = pkt.func_148857_g().getCompoundTag("t" + i);
				LittleTile tile = LittleTile.CreateandLoadTile(worldObj, tileNBT);
				if(tile != null)
					tiles.add(tile);
			}
    	}else{*/
    	
    	ArrayList<LittleTile> exstingTiles = new ArrayList<LittleTile>();
    	exstingTiles.addAll(tiles);
        int count = pkt.func_148857_g().getInteger("tilesCount");
        for (int i = 0; i < count; i++) {
        	NBTTagCompound tileNBT = new NBTTagCompound();
        	tileNBT = pkt.func_148857_g().getCompoundTag("t" + i);
			LittleTile tile = getTile(tileNBT.getByte("cVecx"), tileNBT.getByte("cVecy"), tileNBT.getByte("cVecz"));
			if(tile != null)
			{
				tile.receivePacket(tileNBT.getCompoundTag("update"), net);
				exstingTiles.remove(tile);
			}
			else
			{
				tile = LittleTile.CreateandLoadTile(this, worldObj, tileNBT);
				if(tile != null)
					tiles.add(tile);
				//else
					//System.out.println("Failed to load tileentity nbt=" + tileNBT.toString());
			}
		}
        for (int i = 0; i < exstingTiles.size(); i++) {
			tiles.remove(exstingTiles.get(i));
		}
    	//}
        updateRender();
        /*if(tiles.size() == 0)
        {
        	System.out.println("===============================");
        	System.out.println("Receiving littleTiles packet x=" + xCoord + ",y=" + yCoord + ",z" + zCoord);
        	
        	System.out.println(pkt.func_148857_g().toString());
        	
        	System.out.println("-------------------------------");
        	System.out.println("Loaded " + tiles.size() + " tiles");
        }*/
    }
    
    public MovingObjectPosition getMoving(EntityPlayer player)
    {
    	return getMoving(player, false);
    }
    
    public MovingObjectPosition getMoving(EntityPlayer player, boolean loadTile)
    {
    	MovingObjectPosition hit = null;
		
		Vec3 pos = player.getPosition(1);
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3 look = player.getLook(1.0F);
		Vec3 vec32 = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
		return getMoving(pos, vec32, loadTile);
    }
    
    public MovingObjectPosition getMoving(Vec3 pos, Vec3 look, boolean loadTile)
    {
    	MovingObjectPosition hit = null;
    	for (int i = 0; i < tiles.size(); i++) {
    		for (int j = 0; j < tiles.get(i).boundingBoxes.size(); j++) {
    			MovingObjectPosition Temphit = tiles.get(i).boundingBoxes.get(j).getBox().getOffsetBoundingBox(xCoord, yCoord, zCoord).calculateIntercept(pos, look);
    			if(Temphit != null)
    			{
    				if(hit == null || hit.hitVec.distanceTo(pos) > Temphit.hitVec.distanceTo(pos))
    				{
    					hit = Temphit;
    					if(loadTile)
    						loadedTile = tiles.get(i);
    				}
    			}
			}
		}
		return hit;
    }
	
	public boolean updateLoadedTile(EntityPlayer player)
	{
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			return false;
		loadedTile = null;
		getMoving(player, true);
		return loadedTile != null;
	}
	
	public boolean updateLoadedTileServer(Vec3 pos, Vec3 look)
	{
		loadedTile = null;
		getMoving(pos, look, true);
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
	
	public void update()
	{
		worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord, this.zCoord, this);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@SideOnly(Side.CLIENT)
	public void updateRender()
	{
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void updateEntity()
	{
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).updateEntity();
		}
		if(!worldObj.isRemote && tiles.size() == 0)
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}

	public ChunkCoordinates getCoord() {
		return new ChunkCoordinates(xCoord, yCoord, zCoord);
	}

	public void combineTiles() {
		//ArrayList<LittleTile> newTiles = new ArrayList<>();
		int size = 0;
		while(size != tiles.size())
		{
			size = tiles.size();
			int i = 0;
			while(i < tiles.size()){
				int j = 0;
				while(j < tiles.size()) {
					if(i != j && tiles.get(i).boundingBoxes.size() == 1 && tiles.get(j).boundingBoxes.size() == 1 && tiles.get(i).canBeCombined(tiles.get(j)))
					{
						LittleTileBox box = tiles.get(i).boundingBoxes.get(0).combineBoxes(tiles.get(j).boundingBoxes.get(0));
						if(box != null)
						{
							tiles.get(i).boundingBoxes.set(0, box);
							tiles.get(i).combineTiles(tiles.get(j));
							tiles.get(i).updateCorner();
							tiles.remove(j);
							if(i > j)
								i--;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}
		update();
		
	}
}
