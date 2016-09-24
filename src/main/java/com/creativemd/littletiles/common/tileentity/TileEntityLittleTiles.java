package com.creativemd.littletiles.common.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.creativemd.creativecore.client.rendering.model.QuadCache;
import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.transform.patmat.Solving.Solver.Lit;

public class TileEntityLittleTiles extends TileEntityCreative implements ITickable{
	
	public static CopyOnWriteArrayList<LittleTile> createTileList()
	{
		return new CopyOnWriteArrayList<LittleTile>();
	}
	
	public TileEntityLittleTiles() {
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			completeTileUpdate();
	}
	
	@SideOnly(Side.CLIENT)
	private LittleTile[][][] boundingArray;
	
	private CopyOnWriteArrayList<LittleTile> tiles = createTileList();
	
	public CopyOnWriteArrayList<LittleTile> getTiles()
	{
		return tiles;
	}
	
	public void setTiles(CopyOnWriteArrayList<LittleTile> tiles)
	{
		this.tiles = tiles;
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			completeTileUpdate();
	}
	
	@SideOnly(Side.CLIENT)
	public boolean forceChunkRenderUpdate;
	
	@SideOnly(Side.CLIENT)
	public boolean isRendering;
	
	@SideOnly(Side.CLIENT)
	private BlockInfo info;
	
	@SideOnly(Side.CLIENT)
	private HashMap<BlockRenderLayer, HashMap<EnumFacing, QuadCache[]>> quadCache;
	
	public HashMap<BlockRenderLayer, HashMap<EnumFacing, QuadCache[]>> getRenderCacheQuads()
	{
		if(quadCache == null)
			quadCache = new HashMap<>();
		return quadCache;
	}
	
	@SideOnly(Side.CLIENT)
	public void setQuadCache(BlockInfo info, QuadCache[] cache, BlockRenderLayer layer, EnumFacing facing)
	{
		this.info = info;
		HashMap<EnumFacing, QuadCache[]> facingCache = getRenderCacheQuads().get(layer);
		if(facingCache == null)
			facingCache = new HashMap<>();
		facingCache.put(facing, cache);
		getRenderCacheQuads().put(layer, facingCache);
	}
	
	@SideOnly(Side.CLIENT)
	public QuadCache[] getQuadCache(BlockInfo info, BlockRenderLayer layer, EnumFacing facing)
	{
		if(doesNeedRenderUpdate(info))
			return null;
		HashMap<EnumFacing, QuadCache[]> facingCache = getRenderCacheQuads().get(layer);
		if(facingCache != null)
			return facingCache.get(facing);
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean doesNeedRenderUpdate(BlockInfo newInfo)
	{
		if(info == null)
			return true;
		
		boolean[][][] translucent = info.getTranslucent();
		boolean[][][] translucentNew = newInfo.getTranslucent();
		for (int x = 0; x < translucent.length; x++) {
			for (int y = 0; y < translucent[x].length; y++) {
				for (int z = 0; z < translucent[x][y].length; z++) {
					if(translucent[x][y][z] != translucentNew[x][y][z])
						return true;
				}
			}
		}
	    
	    float[][][][] skyLight = info.getSkyLight();
	    float[][][][] skyLightNew = newInfo.getSkyLight();
	    for (int x = 0; x < skyLight.length; x++) {
			for (int y = 0; y < skyLight[x].length; y++) {
				for (int z = 0; z < skyLight[x][y].length; z++) {
					for (int w = 0; w < skyLight[x][y][z].length; w++) {
						if(skyLight[x][y][z][w] != skyLightNew[x][y][z][w])
							return true;
					}
				}
			}
		}
	    
	    float[][][][] blockLight = info.getBlockLight();
	    float[][][][] blockLightNew = newInfo.getBlockLight();
	    for (int x = 0; x < blockLight.length; x++) {
			for (int y = 0; y < blockLight[x].length; y++) {
				for (int z = 0; z < blockLight[x][y].length; z++) {
					for (int w = 0; w < blockLight[x][y][z].length; w++) {
						if(blockLight[x][y][z][w] != blockLightNew[x][y][z][w])
							return true;
					}
				}
			}
		}
	    
	    if(info.getShx() != newInfo.getShx() || info.getShy() != newInfo.getShy() || info.getShz() != newInfo.getShz() || info.getCachedTint() != newInfo.getCachedTint() || info.getCachedMultiplier() != newInfo.getCachedMultiplier())
	    	return true;
	    
	    return false;
	}
	
	
	@SideOnly(Side.CLIENT)
	public HashMap<BlockRenderLayer, List<RenderCubeObject>> cachedCubes;
	
	/*public HashMap<BlockRenderLayer, List<RenderCubeObject>> getCachedCubes()
	{
		if(cachedCubes == null)
			cachedCubes = new HashMap<>();
		return cachedCubes;
	}
	
	@SideOnly(Side.CLIENT)
	public void setCachedCubes(HashMap<BlockRenderLayer, List<RenderCubeObject>> cachedCubes)
	{
		this.cachedCubes = cachedCubes;
	}*/
	
	@SideOnly(Side.CLIENT)
	public HashMap<BlockRenderLayer, HashMap<EnumFacing, List<BakedQuad>>> cachedQuads;
	
	/*@SideOnly(Side.CLIENT)
	public void setCachedQuads(HashMap<BlockRenderLayer, HashMap<EnumFacing, List<BakedQuad>>> cachedQuads)
	{
		this.cachedQuads = cachedQuads;
	}
	
	@SideOnly(Side.CLIENT)
	public HashMap<BlockRenderLayer, HashMap<EnumFacing, List<BakedQuad>>> getCachedQuads()
	{
		if(cachedQuads == null)
			cachedQuads = new HashMap<>();
		return cachedQuads;
	}*/
	
	public boolean removeLittleTile(LittleTile tile)
	{
		boolean result = tiles.remove(tile);
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			removeLittleTileClient(tile);
		return result;
	}
	
	public boolean removeTile(LittleTile tile)
	{
		boolean result = removeLittleTile(tile);
		updateTiles();
		return result;
	}
	
	@SideOnly(Side.CLIENT)
	private void removeLittleTileClient(LittleTile tile)
	{
		for(int i = 0; i < tile.boundingBoxes.size(); i++){
			LittleTileBox box = tile.boundingBoxes.get(i);
			for (int littleX = box.minX; littleX < box.maxX; littleX++) {
				for (int littleY = box.minY; littleY < box.maxY; littleY++) {
					for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
						boundingArray[littleX][littleY][littleZ] = null;
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void addLittleTileClient(LittleTile tile)
	{
		for(int i = 0; i < tile.boundingBoxes.size(); i++){
			LittleTileBox box = tile.boundingBoxes.get(i);
			for (int littleX = box.minX; littleX < box.maxX; littleX++) {
				for (int littleY = box.minY; littleY < box.maxY; littleY++) {
					for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
						boundingArray[littleX][littleY][littleZ] = tile;
					}
				}
			}
		}
	}
	
	private boolean addLittleTile(LittleTile tile)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			addLittleTileClient(tile);
		return tiles.add(tile);
	}
	
	public void addTiles(ArrayList<LittleTile> tiles)
	{
		for (int i = 0; i < tiles.size(); i++)
			addLittleTile(tiles.get(i));
		//this.tiles.addAll(tiles);
		updateTiles();
	}
	
	public boolean addTile(LittleTile tile)
	{
		boolean result = addLittleTile(tile);
		updateTiles();
		return result;
	}
	
	@SideOnly(Side.CLIENT)
	public void completeTileUpdate()
	{
		boundingArray = new LittleTile[16][16][16];
		for (int i = 0; i < tiles.size(); i++) {
			addLittleTileClient(tiles.get(i));
		}
	}
	
	public void updateTiles()
	{
		if(preventUpdate)
			return ;
		if(worldObj != null)
		{
			updateBlock();
			updateNeighbor();
			//System.out.println("Update Light");
			worldObj.checkLight(getPos());
		}
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			updateCustomRenderer();
		
	}
	
	@SideOnly(Side.CLIENT)
	public void updateCustomRenderer()
	{
		/*getCachedQuads().clear();
		getCachedCubes().clear();*/
		cachedCubes = null;
		cachedQuads = null;
		getRenderCacheQuads().clear();
		info = null;
	}
	
	
	@SideOnly(Side.CLIENT)
	public void onNeighBorChangedClient()
	{
		//getCachedQuads().clear();
		cachedQuads = null;
		
		updateRender();
	}
	
	@SideOnly(Side.CLIENT)
	public boolean isBoxFilledClient(LittleTileBox box)
	{
		for (int littleX = box.minX; littleX < box.maxX; littleX++) {
			for (int littleY = box.minY; littleY < box.maxY; littleY++) {
				for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
					if(getTileFromPositionClient(littleX, littleY, littleZ) == null)
						return false;
				}
			}
		}
		return true;
	}
	
	public boolean isBoxFilled(LittleTileBox box)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			isBoxFilledClient(box);
		for (int littleX = box.minX; littleX < box.maxX; littleX++) {
			for (int littleY = box.minY; littleY < box.maxY; littleY++) {
				for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
					if(isSpaceForLittleTile(new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1)))
						return false;
				}
			}
		}
		return true;
	}
	
	public void updateNeighbor()
	{
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).onNeighborChangeInside();
		}
		worldObj.notifyNeighborsOfStateChange(getPos(), LittleTiles.blockTile);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
		double renderDistance = 0;
		for (int i = 0; i < tiles.size(); i++) {
			renderDistance = Math.max(renderDistance, tiles.get(i).getMaxRenderDistanceSquared());
		}
        return renderDistance;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
		double minX = getPos().getX();
		double minY = getPos().getY();
		double minZ = getPos().getZ();
		double maxX = getPos().getX()+1;
		double maxY = getPos().getY()+1;
		double maxZ = getPos().getZ()+1;
		for (int i = 0; i < tiles.size(); i++) {
			AxisAlignedBB box = tiles.get(i).getRenderBoundingBox();
			minX = Math.min(box.minX, minX);
			minY = Math.min(box.minY, minY);
			minZ = Math.min(box.minZ, minZ);
			maxX = Math.max(box.maxX, maxX);
			maxY = Math.max(box.maxY, maxY);
			maxZ = Math.max(box.maxZ, maxZ);
		}
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
	
	public boolean needFullUpdate = true;
	
	/**Used for**/
	public LittleTile loadedTile = null;

	public boolean preventUpdate = false;
	
	public LittleTile getTileFromPositionClient(int x, int y, int z)
	{
		if(x < 0 || y < 0 || z < 0 || x > 15 || y > 15 || z > 15)
			return null;
		if(boundingArray == null)
			return null;
		return boundingArray[x][y][z];
	}
	
	public LittleTile getTileFromPosition(int x, int y, int z)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			return getTileFromPositionClient(x, y, z);
		LittleTileBox box = new LittleTileBox(new LittleTileVec(x, y, z));
		for (int i = 0; i < tiles.size(); i++) {
			for (int j = 0; j < tiles.get(i).boundingBoxes.size(); j++) {
				if(box.intersectsWith(tiles.get(i).boundingBoxes.get(j)))
					return tiles.get(i);
			}
		}
		return null;
	}
	
	/**Used for rendering*/
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(EnumFacing facing, LittleTileBox box, LittleTile rendered)
	{
		for (int littleX = box.minX; littleX < box.maxX; littleX++) {
			for (int littleY = box.minY; littleY < box.maxY; littleY++) {
				for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
					LittleTile tile = getTileFromPosition(littleX, littleY, littleZ);
					if((tile == null) || !tile.doesProvideSolidFace(facing) && !tile.canBeRenderCombined(rendered))
						return true;
				}
			}
		}
		return false;
	}
	
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
		for (int i = 0; i < tiles.size(); i++) {
			for (int j = 0; j < tiles.get(i).boundingBoxes.size(); j++) {
				if(box.intersectsWith(tiles.get(i).boundingBoxes.get(j)))
					return false;
			}
			
		}
		return true;
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
        if(FMLCommonHandler.instance().getEffectiveSide().isClient())
        	completeTileUpdate();
        updateTiles();
    }

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tiles.get(i).saveTile(tileNBT);
			nbt.setTag("t" + i, tileNBT);
		}
        nbt.setInteger("tilesCount", tiles.size());
		return nbt;
    }
    
    @Override
    public void getDescriptionNBT(NBTTagCompound nbt)
	{
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
			if(needFullUpdate)
				nbt.setBoolean("f" + i, true);
		}
        nbt.setInteger("tilesCount", tiles.size());
        needFullUpdate = false;
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
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
    	NBTTagCompound nbt = pkt.getNbtCompound();
    	ArrayList<LittleTile> exstingTiles = new ArrayList<LittleTile>();
    	ArrayList<LittleTile> tilesToAdd = new ArrayList<LittleTile>();
    	exstingTiles.addAll(tiles);
        int count = nbt.getInteger("tilesCount");
        for (int i = 0; i < count; i++) {
        	NBTTagCompound tileNBT = new NBTTagCompound();
        	tileNBT = nbt.getCompoundTag("t" + i);
			LittleTile tile = getTile(tileNBT.getByte("cVecx"), tileNBT.getByte("cVecy"), tileNBT.getByte("cVecz"));
			if(tile != null && tile.getID().equals(tileNBT.getString("tID")) && !nbt.getBoolean("f" + i))
			{
				tile.receivePacket(tileNBT.getCompoundTag("update"), net);
				exstingTiles.remove(tile);
			}
			else
			{
				tile = LittleTile.CreateandLoadTile(this, worldObj, tileNBT);
				if(tile != null)
					tilesToAdd.add(tile);
			}
		}
        for (int i = 0; i < exstingTiles.size(); i++) {
        	removeLittleTile(exstingTiles.get(i));
		}
        for (int i = 0; i < tilesToAdd.size(); i++) {
			addLittleTile(tilesToAdd.get(i));
		}
        updateTiles();
        super.onDataPacket(net, pkt);
    }
    
    public RayTraceResult getMoving(EntityPlayer player)
    {
    	return getMoving(player, false);
    }
    
    public RayTraceResult getMoving(EntityPlayer player, boolean loadTile)
    {
    	RayTraceResult hit = null;
		
		Vec3d pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		Vec3d vec32 = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
		return getMoving(pos, vec32, loadTile);
    }
    
    public RayTraceResult getMoving(Vec3d pos, Vec3d look, boolean loadTile)
    {
    	RayTraceResult hit = null;
    	for (int i = 0; i < tiles.size(); i++) {
    		for (int j = 0; j < tiles.get(i).boundingBoxes.size(); j++) {
    			RayTraceResult Temphit = tiles.get(i).boundingBoxes.get(j).getBox().offset(getPos()).calculateIntercept(pos, look);
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
	
	public boolean updateLoadedTileServer(Vec3d pos, Vec3d look)
	{
		loadedTile = null;
		getMoving(pos, look, true);
		return loadedTile != null;
	}
	
	@SideOnly(Side.CLIENT)
	public void checkClientLoadedTile(double distance)
	{
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d pos = mc.thePlayer.getPositionEyes(TickUtils.getPartialTickTime());
		if(mc.objectMouseOver.hitVec.distanceTo(pos) < distance)
			loadedTile = null;
	}
	
	@Override
	public void update()
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			if(forceChunkRenderUpdate)
			{
				updateRender();
				forceChunkRenderUpdate = false;
			}
		}
		
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).updateEntity();
		}
		if(!worldObj.isRemote && tiles.size() == 0)
			worldObj.setBlockToAir(getPos());
	}
	
	public void combineTiles(LittleStructure structure) {
		//ArrayList<LittleTile> newTiles = new ArrayList<>();
		int size = 0;
		while(size != tiles.size())
		{
			size = tiles.size();
			int i = 0;
			while(i < tiles.size()){
				if(tiles.get(i).structure != structure)
				{
					i++;
					continue;
				}
				
				int j = 0;
				
				while(j < tiles.size()) {
					if(tiles.get(j).structure != structure)
					{
						j++;
						continue;
					}
					
					if(i != j && tiles.get(i).boundingBoxes.size() == 1 && tiles.get(j).boundingBoxes.size() == 1 && tiles.get(i).canBeCombined(tiles.get(j)) && tiles.get(j).canBeCombined(tiles.get(i)))
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
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			completeTileUpdate();
		//updateBlock();
		updateTiles();
	}
	
	public void combineTiles() {
		combineTilesList(tiles);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			completeTileUpdate();
		updateBlock();	
	}

	public static void combineTilesList(ArrayList<LittleTile> tiles) {
		//ArrayList<LittleTile> newTiles = new ArrayList<>();
		int size = 0;
		while(size != tiles.size())
		{
			size = tiles.size();
			int i = 0;
			while(i < tiles.size()){
				int j = 0;
				while(j < tiles.size()) {
					if(i != j && tiles.get(i).boundingBoxes.size() == 1 && tiles.get(j).boundingBoxes.size() == 1 && tiles.get(i).canBeCombined(tiles.get(j)) && tiles.get(j).canBeCombined(tiles.get(i)))
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
		
	}

	public void removeBoxFromTile(LittleTile loaded, LittleTileBox box) {
		ArrayList<LittleTileBox> boxes = new ArrayList<>(loaded.boundingBoxes);
		loaded.boundingBoxes.clear();
		ArrayList<LittleTile> newTiles = new ArrayList<>();
		for (int i = 0; i < boxes.size(); i++) {
			LittleTileBox oldBox = boxes.get(i);
			for (int littleX = oldBox.minX; littleX < oldBox.maxX; littleX++) {
				for (int littleY = oldBox.minY; littleY < oldBox.maxY; littleY++) {
					for (int littleZ = oldBox.minZ; littleZ < oldBox.maxZ; littleZ++) {
						if(box.minX != littleX || box.minY != littleY || box.minZ != littleZ){
							LittleTile newTile = loaded.copy();
							newTile.boundingBoxes.add(new LittleTileBox(new LittleTileVec(littleX, littleY, littleZ)));
							newTiles.add(newTile);
						}
					}
				}
			}
		}
		
		loaded.destroy();
		
		TileEntityLittleTiles.combineTilesList(newTiles);
		for (int i = 0; i < newTiles.size(); i++) {
			addTile(newTiles.get(i));
		}
		
		combineTiles();
	}

	
}
