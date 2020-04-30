package com.creativemd.littletiles.common.tileentity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.client.render.cache.BlockLayerRenderBuffer;
import com.creativemd.littletiles.client.render.cache.RenderCubeLayerCache;
import com.creativemd.littletiles.client.render.cache.RenderingThread;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.common.api.te.ILittleTileTE;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.combine.BasicCombiner;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox.LittleTileFace;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.LittleBlockTransformer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityLittleTiles extends TileEntityCreative implements ILittleTileTE, Iterable<LittleTile>, IGridBased {
	
	protected void assign(TileEntityLittleTiles te) {
		try {
			for (Field field : TileEntityLittleTiles.class.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()))
					field.set(this, field.get(te));
			}
			
			for (LittleTile tile : tiles)
				tile.te = this;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		tiles = new TileList(this, isClientSide());
	}
	
	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		if (tiles == null)
			init();
	}
	
	@Override
	protected void setWorldCreate(World worldIn) {
		super.setWorldCreate(worldIn);
		if (tiles == null)
			init();
	}
	
	protected TileList tiles;
	
	@Override
	public Iterator<LittleTile> iterator() {
		return tiles.iterator();
	}
	
	public List<LittleTile> tickingTiles() {
		return tiles.tickingTiles();
	}
	
	@SideOnly(Side.CLIENT)
	public List<LittleTile> renderTiles() {
		return tiles.renderTiles();
	}
	
	protected LittleGridContext context = LittleGridContext.getMin();
	
	@Override
	public LittleGridContext getContext() {
		return context;
	}
	
	@Override
	public void convertToSmallest() {
		int size = LittleGridContext.minSize;
		for (LittleTile tile : tiles) {
			size = Math.max(size, tile.getSmallestContext(context));
		}
		
		if (size < context.size)
			convertTo(LittleGridContext.get(size));
	}
	
	@Override
	public void convertTo(LittleGridContext newContext) {
		for (LittleTile tile : tiles) {
			tile.convertTo(context, newContext);
		}
		this.context = newContext;
	}
	
	public boolean contains(LittleTile tile) {
		return tiles.contains(tile);
	}
	
	public int size() {
		return tiles.size();
	}
	
	private boolean hasLoaded = false;
	
	public boolean hasLoaded() {
		return hasLoaded;
	}
	
	public void setLoaded() {
		hasLoaded = true;
	}
	
	@SideOnly(Side.CLIENT)
	public int renderIndex;
	
	@SideOnly(Side.CLIENT)
	public boolean hasLightChanged;
	
	@SideOnly(Side.CLIENT)
	public boolean hasNeighbourChanged;
	
	public SideSolidCache sideCache = new SideSolidCache();
	
	public boolean shouldCheckForCollision() {
		return tiles.checkCollision();
	}
	
	@SideOnly(Side.CLIENT)
	public RenderChunk lastRenderedChunk;
	
	@SideOnly(Side.CLIENT)
	public void updateQuadCache(Object chunk) {
		if (tiles == null)
			return;
		if (chunk instanceof RenderChunk)
			lastRenderedChunk = (RenderChunk) chunk;
		
		if (renderIndex != LittleChunkDispatcher.currentRenderIndex)
			getCubeCache().clearCache();
		
		boolean doesNeedUpdate = getCubeCache().doesNeedUpdate() || hasNeighbourChanged || hasLightChanged;
		
		hasLightChanged = false;
		hasNeighbourChanged = false;
		
		if (doesNeedUpdate)
			addToRenderUpdate();
	}
	
	@SideOnly(Side.CLIENT)
	public BlockLayerRenderBuffer buffer;
	
	@SideOnly(Side.CLIENT)
	private RenderCubeLayerCache cubeCache;
	
	public RenderCubeLayerCache getCubeCache() {
		if (cubeCache == null)
			cubeCache = new RenderCubeLayerCache();
		return cubeCache;
	}
	
	public void updateLighting() {
		world.checkLight(getPos());
	}
	
	protected void customTilesUpdate() {
		if (world.isRemote)
			return;
		boolean rendered = tiles.hasRendered();
		boolean ticking = tiles.hasTicking();
		if (ticking != isTicking() || rendered != isRendered()) {
			TileEntityLittleTiles newTe;
			if (rendered)
				if (ticking)
					newTe = new TileEntityLittleTilesTickingRendered();
				else
					newTe = new TileEntityLittleTilesRendered();
			else if (ticking)
				newTe = new TileEntityLittleTilesTicking();
			else
				newTe = new TileEntityLittleTiles();
			
			newTe.assign(this);
			
			world.setBlockState(pos, BlockTile.getState(ticking, rendered), 2);
			world.setTileEntity(pos, newTe);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void onNeighbourChangedClient() {
		addToRenderUpdate();
		hasNeighbourChanged = true;
	}
	
	public void onNeighbourChanged() {
		if (isClientSide())
			onNeighbourChangedClient();
		
		notifyStructure();
	}
	
	public void notifyStructure() {
		for (LittleStructure structure : tiles.structures(LittleStructureAttribute.NEIGHBOR_LISTENER))
			structure.neighbourChanged();
	}
	
	public void updateTiles() {
		updateTiles(true);
	}
	
	public void updateTiles(boolean updateNeighbour) {
		notifyStructure();
		
		sideCache.reset();
		
		if (world != null) {
			updateBlock();
			if (updateNeighbour)
				updateNeighbour();
			updateLighting();
		}
		if (isClientSide())
			updateCustomRenderer();
		
		if (!world.isRemote && tiles.isEmpty())
			world.setBlockToAir(getPos());
		
		if (world instanceof CreativeWorld)
			((CreativeWorld) world).hasChanged = true;
		
		customTilesUpdate();
	}
	
	public void updateTiles(Consumer<TileList> action) {
		action.accept(tiles);
		updateTiles();
	}
	
	/** Block will not update */
	public void updateTilesSecretly(Consumer<TileList> action) {
		action.accept(tiles);
	}
	
	@SideOnly(Side.CLIENT)
	public void updateCustomRenderer() {
		updateRenderBoundingBox();
		updateRenderDistance();
		if (inRenderingQueue == null)
			createRenderFields();
		
		synchronized (inRenderingQueue) {
			if (!inRenderingQueue.get() || !buildingCache)
				getCubeCache().clearCache();
			
			addToRenderUpdate();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public AtomicBoolean inRenderingQueue;
	
	@SideOnly(Side.CLIENT)
	public boolean buildingCache;
	
	@SideOnly(Side.CLIENT)
	public boolean rebuildRenderingCache;
	
	private synchronized void createRenderFields() {
		inRenderingQueue = new AtomicBoolean();
	}
	
	@SideOnly(Side.CLIENT)
	public void addToRenderUpdate() {
		if (inRenderingQueue == null)
			createRenderFields();
		
		synchronized (inRenderingQueue) {
			if (inRenderingQueue.compareAndSet(false, true))
				RenderingThread.addCoordToUpdate(this);
			else if (buildingCache)
				rebuildRenderingCache = true;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void resetRenderingState() {
		inRenderingQueue.set(false);
		buildingCache = false;
		rebuildRenderingCache = false;
	}
	
	/** Tries to convert the TileEntity to a vanilla block
	 * 
	 * @return whether it could convert it or not */
	public boolean convertBlockToVanilla() {
		LittleTile firstTile = null;
		if (tiles.isEmpty()) {
			world.setBlockToAir(pos);
			return true;
		}
		
		if (world instanceof IOrientatedWorld)
			return false;
		
		if (tiles.size() == 1) {
			if (!tiles.first().canBeConvertedToVanilla() || !tiles.first().doesFillEntireBlock())
				return false;
			firstTile = tiles.first();
		} else {
			boolean[][][] filled = new boolean[context.size][context.size][context.size];
			for (LittleTile tile : tiles) {
				if (firstTile == null) {
					if (tile.canBeConvertedToVanilla())
						return false;
					
					firstTile = tile;
				} else if (!firstTile.canBeCombined(tile) || !tile.canBeCombined(firstTile))
					return false;
				
				tile.fillInSpace(filled);
			}
			
			for (int x = 0; x < filled.length; x++) {
				for (int y = 0; y < filled[x].length; y++) {
					for (int z = 0; z < filled[x][y].length; z++) {
						if (!filled[x][y][z])
							return false;
					}
				}
			}
		}
		
		world.setBlockState(pos, firstTile.getBlockState());
		
		return true;
	}
	
	public boolean isBoxFilled(LittleBox box) {
		LittleVec size = box.getSize();
		boolean[][][] filled = new boolean[size.x][size.y][size.z];
		
		for (LittleTile tile : tiles)
			tile.fillInSpace(box, filled);
		
		for (int x = 0; x < filled.length; x++) {
			for (int y = 0; y < filled[x].length; y++) {
				for (int z = 0; z < filled[x][y].length; z++) {
					if (!filled[x][y][z])
						return false;
				}
			}
		}
		return true;
	}
	
	public void updateNeighbour() {
		world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
	}
	
	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 0 && tiles != null && tiles.hasRendered();
	}
	
	@SideOnly(Side.CLIENT)
	private double cachedRenderDistance;
	
	@SideOnly(Side.CLIENT)
	public void updateRenderDistance() {
		cachedRenderDistance = 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		if (cachedRenderDistance == 0) {
			double renderDistance = 262144; // 512 blocks
			for (LittleTile tile : tiles.renderTiles())
				renderDistance = Math.max(renderDistance, tile.getMaxRenderDistanceSquared());
			cachedRenderDistance = renderDistance;
		}
		return cachedRenderDistance;
	}
	
	@Override
	public boolean hasFastRenderer() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	private AxisAlignedBB cachedRenderBoundingBox;
	
	@SideOnly(Side.CLIENT)
	private boolean requireRenderingBoundingBoxUpdate;
	
	@SideOnly(Side.CLIENT)
	public void updateRenderBoundingBox() {
		requireRenderingBoundingBoxUpdate = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (requireRenderingBoundingBoxUpdate || cachedRenderBoundingBox == null) {
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double minZ = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			double maxZ = -Double.MAX_VALUE;
			boolean found = false;
			for (LittleTile tile : tiles) {
				if (tile.needCustomRendering()) {
					AxisAlignedBB box = tile.getRenderBoundingBox().offset(pos);
					minX = Math.min(box.minX, minX);
					minY = Math.min(box.minY, minY);
					minZ = Math.min(box.minZ, minZ);
					maxX = Math.max(box.maxX, maxX);
					maxY = Math.max(box.maxY, maxY);
					maxZ = Math.max(box.maxZ, maxZ);
					found = true;
				}
			}
			if (found)
				cachedRenderBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
			else
				cachedRenderBoundingBox = new AxisAlignedBB(pos);
			
			requireRenderingBoundingBoxUpdate = false;
		}
		return cachedRenderBoundingBox;
	}
	
	public AxisAlignedBB getSelectionBox() {
		int minX = context.size;
		int minY = context.size;
		int minZ = context.size;
		int maxX = 0;
		int maxY = 0;
		int maxZ = 0;
		for (LittleTile tile : tiles) {
			LittleBox box = tile.getCompleteBox();
			minX = Math.min(box.minX, minX);
			minY = Math.min(box.minY, minY);
			minZ = Math.min(box.minZ, minZ);
			maxX = Math.max(box.maxX, maxX);
			maxY = Math.max(box.maxY, maxY);
			maxZ = Math.max(box.maxZ, maxZ);
		}
		return new LittleBox(minX, minY, minZ, maxX, maxY, maxZ).getBox(context, pos);
	}
	
	/** Used for rendering */
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(EnumFacing facing, LittleTileFace face, LittleTile rendered) {
		face.ensureContext(context);
		
		for (LittleTile tile : tiles)
			if (tile != rendered && (tile.doesProvideSolidFace(facing) || tile.canBeRenderCombined(rendered)))
				tile.fillFace(face);
			
		return !face.isFilled();
	}
	
	/** @param box
	 * @param cutout
	 *            filled with all boxes which are cutout by tiles
	 * @return all boxes which are not cutout by other tiles */
	public List<LittleBox> cutOut(LittleBox box, List<LittleBox> cutout) {
		List<LittleBox> cutting = new ArrayList<>();
		for (LittleTile tile : tiles)
			tile.getCuttingBoxes(cutting);
		return box.cutOut(cutting, cutout);
	}
	
	public boolean isSpaceForLittleTileStructure(LittleBox box, Predicate<LittleTile> predicate) {
		for (LittleTile tile : tiles) {
			if (predicate != null && !predicate.test(tile))
				continue;
			if ((tile.isChildOfStructure() || !tile.canBeSplitted()) && tile.intersectsWith(box))
				return false;
			
		}
		return true;
	}
	
	public boolean isSpaceForLittleTileStructure(LittleBox box) {
		return isSpaceForLittleTileStructure(box, null);
	}
	
	public boolean isSpaceForLittleTile(LittleBox box, Predicate<LittleTile> predicate) {
		for (LittleTile tile : tiles) {
			if (predicate != null && !predicate.test(tile))
				continue;
			if (tile.intersectsWith(box))
				return false;
			
		}
		return true;
	}
	
	public boolean isSpaceForLittleTile(LittleBox box) {
		return isSpaceForLittleTile(box, null);
	}
	
	public boolean isSpaceForLittleTileIgnore(LittleBox box, LittleTile ignoreTile) {
		for (LittleTile tile : tiles) {
			if (ignoreTile != tile && tile.intersectsWith(box))
				return false;
		}
		return true;
	}
	
	public LittleTile getIntersectingTile(LittleBox box, LittleTile ignoreTile) {
		for (LittleTile tile : tiles) {
			if (ignoreTile != tile && tile.intersectsWith(box))
				return tile;
		}
		return null;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		if (tiles == null)
			init();
		
		if (!tiles.isEmpty())
			tiles.clear();
		context = LittleGridContext.get(nbt);
		
		if (nbt.hasKey("tilesCount")) {
			int count = nbt.getInteger("tilesCount");
			for (int i = 0; i < count; i++) {
				NBTTagCompound tileNBT = new NBTTagCompound();
				tileNBT = nbt.getCompoundTag("t" + i);
				LittleTile tile = LittleTileRegistry.loadTile(this, world, tileNBT);
				if (tile != null)
					tiles.add(tile);
			}
		} else
			tiles.addAll(LittleNBTCompressionTools.readTiles(nbt.getTagList("tiles", 10), this));
		
		if (world != null && !world.isRemote) {
			updateBlock();
			customTilesUpdate();
		}
		
		deleteTempWorld();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		context.set(nbt);
		nbt.setTag("tiles", LittleNBTCompressionTools.writeTiles(tiles));
		return nbt;
	}
	
	@Override
	public void getDescriptionNBT(NBTTagCompound nbt) {
		context.set(nbt);
		
		int i = 0;
		for (LittleTile tile : tiles) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			NBTTagCompound packet = new NBTTagCompound();
			tile.saveTile(tileNBT);
			if (tile.supportsUpdatePacket()) {
				if (tile.requiresCompleteUpdate())
					tile.setCompleteUpdate(false);
				else
					tileNBT.setTag("update", tile.getUpdateNBT());
			}
			
			nbt.setTag("t" + i, tileNBT);
			i++;
		}
		nbt.setInteger("tilesCount", tiles.size());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdatePacket(net, pkt.getNbtCompound());
		super.onDataPacket(net, pkt);
	}
	
	public void handleUpdatePacket(NetworkManager net, NBTTagCompound nbt) {
		LittleGridContext context = LittleGridContext.get(nbt);
		
		if (context != this.context)
			convertTo(context);
		
		ArrayList<LittleTile> exstingTiles = new ArrayList<LittleTile>(tiles);
		ArrayList<LittleTile> tilesToAdd = new ArrayList<LittleTile>();
		int count = nbt.getInteger("tilesCount");
		for (int i = 0; i < count; i++) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tileNBT = nbt.getCompoundTag("t" + i);
			
			LittleTile tile = null;
			if (tileNBT.hasKey("box"))
				tile = getTile(getContext(), LittleBox.createBox(tileNBT.getIntArray("box")).getIdentifier());
			
			if (!exstingTiles.contains(tile))
				tile = null;
			
			boolean isIdentical = tile != null ? tile.isIdenticalToNBT(tileNBT) : false;
			if (isIdentical) {
				if (tile.supportsUpdatePacket() && tileNBT.hasKey("update"))
					tile.receivePacket(tileNBT.getCompoundTag("update"), net);
				else
					tile.loadTile(this, tileNBT);
				
				exstingTiles.remove(tile);
			} else {
				LittleStructure structure = null;
				
				LittleTile newTile = LittleTileRegistry.loadTile(this, world, tileNBT);
				if (tile != null && tile.isConnectedToStructure()) {
					structure = tile.connection.getStructure(world);
					structure.replace(tile, newTile);
				}
				
				tilesToAdd.add(newTile);
			}
		}
		
		synchronized (tiles) {
			tiles.removeAll(exstingTiles);
			tiles.addAll(tilesToAdd);
		}
		
		updateTiles();
		
	}
	
	/** uses the corner and is therefore faster */
	public LittleTile getTile(LittleGridContext context, int[] identifier) {
		for (LittleTile tile : tiles)
			if (tile.is(context, identifier))
				return tile;
		return null;
	}
	
	public RayTraceResult rayTrace(EntityPlayer player) {
		RayTraceResult hit = null;
		
		Vec3d pos = player.getPositionEyes(TickUtils.getPartialTickTime());
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(TickUtils.getPartialTickTime());
		Vec3d vec32 = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		return rayTrace(pos, vec32);
	}
	
	public RayTraceResult rayTrace(Vec3d pos, Vec3d look) {
		RayTraceResult hit = null;
		for (LittleTile tile : tiles) {
			RayTraceResult Temphit = tile.rayTrace(pos, look);
			if (Temphit != null) {
				if (hit == null || hit.hitVec.distanceTo(pos) > Temphit.hitVec.distanceTo(pos)) {
					hit = Temphit;
				}
			}
		}
		return hit;
	}
	
	public LittleTile getFocusedTile(EntityPlayer player, float partialTickTime) {
		if (!isClientSide())
			return null;
		Vec3d pos = player.getPositionEyes(partialTickTime);
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(partialTickTime);
		Vec3d vec32 = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
		
		if (world != player.world && world instanceof CreativeWorld) {
			pos = ((CreativeWorld) world).getOrigin().transformPointToFakeWorld(pos);
			vec32 = ((CreativeWorld) world).getOrigin().transformPointToFakeWorld(vec32);
		}
		
		return getFocusedTile(pos, vec32);
	}
	
	public LittleTile getFocusedTile(Vec3d pos, Vec3d look) {
		LittleTile tileFocus = null;
		RayTraceResult hit = null;
		double distance = 0;
		for (LittleTile tile : tiles) {
			RayTraceResult Temphit = tile.rayTrace(pos, look);
			if (Temphit != null) {
				if (hit == null || distance > Temphit.hitVec.distanceTo(pos)) {
					distance = Temphit.hitVec.distanceTo(pos);
					hit = Temphit;
					tileFocus = tile;
				}
			}
		}
		return tileFocus;
	}
	
	@Override
	public void onLoad() {
		setLoaded();
	}
	
	public boolean isTicking() {
		return false;
	}
	
	public boolean isRendered() {
		return false;
	}
	
	public IBlockState getBlockTileState() {
		return BlockTile.getState(this);
	}
	
	public boolean combineTilesSecretly(LittleStructure structure) {
		boolean changed = BasicCombiner.combineTiles(tiles, structure);
		convertToSmallest();
		return changed;
	}
	
	public boolean combineTiles(LittleStructure structure) {
		boolean changed = BasicCombiner.combineTiles(tiles, structure);
		
		convertToSmallest();
		if (changed)
			updateTiles();
		return changed;
	}
	
	public boolean combineTilesSecretly() {
		boolean changed = BasicCombiner.combineTiles(tiles);
		convertToSmallest();
		return changed;
	}
	
	public boolean combineTiles() {
		boolean changed = BasicCombiner.combineTiles(tiles);
		
		convertToSmallest();
		if (changed)
			updateTiles();
		return changed;
	}
	
	@Override
	@Method(modid = ChiselsAndBitsManager.chiselsandbitsID)
	public Object getVoxelBlob(boolean force) throws Exception {
		return ChiselsAndBitsManager.getVoxelBlob(this, force);
	}
	
	public static enum SideState {
		EMPTY {
			@Override
			public boolean doesBlockCollision() {
				return false;
			}
			
			@Override
			public boolean doesBlockLight() {
				return false;
			}
			
			@Override
			public boolean isFilled() {
				return false;
			}
		},
		SEETHROUGH {
			@Override
			public boolean doesBlockCollision() {
				return true;
			}
			
			@Override
			public boolean doesBlockLight() {
				return false;
			}
			
			@Override
			public boolean isFilled() {
				return true;
			}
		},
		NOCLIP {
			@Override
			public boolean doesBlockCollision() {
				return false;
			}
			
			@Override
			public boolean doesBlockLight() {
				return true;
			}
			
			@Override
			public boolean isFilled() {
				return true;
			}
		},
		SEETHROUGH_NOCLIP {
			@Override
			public boolean doesBlockCollision() {
				return false;
			}
			
			@Override
			public boolean doesBlockLight() {
				return false;
			}
			
			@Override
			public boolean isFilled() {
				return true;
			}
		},
		SOLID {
			@Override
			public boolean doesBlockCollision() {
				return true;
			}
			
			@Override
			public boolean doesBlockLight() {
				return true;
			}
			
			@Override
			public boolean isFilled() {
				return true;
			}
		};
		
		public abstract boolean isFilled();
		
		public abstract boolean doesBlockCollision();
		
		public abstract boolean doesBlockLight();
		
		public static SideState getState(boolean empty, boolean noclip, boolean translucent) {
			if (empty)
				return EMPTY;
			if (noclip && translucent)
				return SEETHROUGH_NOCLIP;
			if (noclip)
				return NOCLIP;
			if (translucent)
				return SideState.SEETHROUGH;
			return SOLID;
		}
	}
	
	public class SideSolidCache {
		SideState DOWN;
		SideState UP;
		SideState NORTH;
		SideState SOUTH;
		SideState WEST;
		SideState EAST;
		
		public void reset() {
			DOWN = null;
			UP = null;
			NORTH = null;
			SOUTH = null;
			WEST = null;
			EAST = null;
		}
		
		protected SideState calculate(EnumFacing facing) {
			LittleBox box;
			switch (facing) {
			case EAST:
				box = new LittleBox(context.size - 1, 0, 0, context.size, context.size, context.size);
				break;
			case WEST:
				box = new LittleBox(0, 0, 0, 1, context.size, context.size);
				break;
			case UP:
				box = new LittleBox(0, context.size - 1, 0, context.size, context.size, context.size);
				break;
			case DOWN:
				box = new LittleBox(0, 0, 0, context.size, 1, context.size);
				break;
			case SOUTH:
				box = new LittleBox(0, 0, context.size - 1, context.size, context.size, context.size);
				break;
			case NORTH:
				box = new LittleBox(0, 0, 0, context.size, context.size, 1);
				break;
			default:
				box = null;
				break;
			}
			return calculateState(facing, box);
		}
		
		protected SideState calculateState(EnumFacing facing, LittleBox box) {
			LittleVec size = box.getSize();
			boolean[][][] filled = new boolean[size.x][size.y][size.z];
			
			boolean translucent = false;
			boolean noclip = false;
			
			for (LittleTile tile : TileEntityLittleTiles.this.tiles)
				if (tile.fillInSpace(box, filled)) {
					if (!tile.doesProvideSolidFace(facing))
						translucent = true;
					if (tile.hasNoCollision())
						noclip = true;
				}
			
			for (int x = 0; x < filled.length; x++) {
				for (int y = 0; y < filled[x].length; y++) {
					for (int z = 0; z < filled[x][y].length; z++) {
						if (!filled[x][y][z])
							return SideState.EMPTY;
					}
				}
			}
			return SideState.getState(false, noclip, translucent);
		}
		
		public SideState get(EnumFacing facing) {
			SideState result;
			
			switch (facing) {
			case DOWN:
				result = DOWN;
				break;
			case UP:
				result = UP;
				break;
			case NORTH:
				result = NORTH;
				break;
			case SOUTH:
				result = SOUTH;
				break;
			case WEST:
				result = WEST;
				break;
			case EAST:
				result = EAST;
				break;
			default:
				result = SideState.EMPTY;
			}
			
			if (result == null)
				set(facing, result = calculate(facing));
			
			return result;
		}
		
		public void set(EnumFacing facing, SideState value) {
			switch (facing) {
			case DOWN:
				DOWN = value;
				break;
			case UP:
				UP = value;
				break;
			case NORTH:
				NORTH = value;
				break;
			case SOUTH:
				SOUTH = value;
				break;
			case WEST:
				WEST = value;
				break;
			case EAST:
				EAST = value;
				break;
			}
		}
		
	}
	
	@Override
	@Nullable
	public IBlockState getState(AxisAlignedBB box, boolean realistic) {
		if (tiles == null)
			return null;
		
		if (realistic) {
			box = box.expand(0, -context.pixelSize, 0);
			for (LittleTile tile : tiles) {
				if (tile.getSelectedBox(getPos()).intersects(box))
					return tile.getBlockState();
			}
			return null;
		}
		box = box.expand(0, -1, 0);
		LittleTile highest = null;
		for (LittleTile tile : tiles) {
			if ((highest == null || tile.getMaxY() > highest.getMaxY()) && tile.getSelectedBox(getPos()).intersects(box))
				highest = tile;
			
		}
		return highest != null ? highest.getBlockState() : null;
	}
	
	public boolean isEmpty() {
		return tiles.isEmpty();
	}
	
	public LittleTile first() {
		return tiles.isEmpty() ? null : tiles.get(0);
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (world.isRemote) {
			tiles = null;
			buffer = null;
			cubeCache = null;
			sideCache = null;
			lastRenderedChunk = null;
			cachedRenderBoundingBox = null;
		}
	}
	
	@Override
	public void rotate(Rotation rotationIn) {
		LittleBlockTransformer.rotateTE(this, RotationUtils.getRotation(rotationIn), RotationUtils.getRotationCount(rotationIn), true);
		updateTiles();
	}
	
	@Override
	public void mirror(Mirror mirrorIn) {
		LittleBlockTransformer.flipTE(this, RotationUtils.getMirrorAxis(mirrorIn), true);
		updateTiles();
	}
	
	@Override
	public String toString() {
		return pos.toString();
	}
	
	public void tick() {
		for (LittleTile tile : tiles.tickingTiles())
			tile.updateEntity();
		
		for (LittleStructure structure : tiles.structures(LittleStructureAttribute.TICKING))
			structure.tick();
	}
	
	public Iterable<LittleStructure> allStructures() {
		return tiles.allStructures();
	}
	
	public Iterable<LittleStructure> structures(int attribute) {
		return tiles.structures(attribute);
	}
}
