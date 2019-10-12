package com.creativemd.littletiles.common.tiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTilesConfig;
import com.creativemd.littletiles.client.render.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.packet.LittleTileUpdatePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.connection.IStructureConnector;
import com.creativemd.littletiles.common.structure.connection.StructureLinkBaseRelative;
import com.creativemd.littletiles.common.structure.connection.StructureLinkTile;
import com.creativemd.littletiles.common.structure.connection.StructureMainTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.combine.ICombinable;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreviewHandler;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox.LittleTileFace;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleTile implements ICombinable {
	
	private static HashMap<Class<? extends LittleTile>, String> tileIDs = new HashMap<Class<? extends LittleTile>, String>();
	private static HashMap<String, Class<? extends LittleTile>> invTileIDs = new HashMap<String, Class<? extends LittleTile>>();
	private static HashMap<String, LittleTilePreviewHandler> previewHandlers = new HashMap<String, LittleTilePreviewHandler>();
	
	public static Class<? extends LittleTile> getClassByID(String id) {
		return invTileIDs.get(id);
	}
	
	public static String getIDByClass(Class<? extends LittleTile> LittleClass) {
		return tileIDs.get(LittleClass);
	}
	
	public static LittleTilePreviewHandler getPreviewHandler(LittleTile tile) {
		return getPreviewHandler(tile.getID());
	}
	
	public static LittleTilePreviewHandler getPreviewHandler(String id) {
		return previewHandlers.getOrDefault(id, LittleTilePreviewHandler.defaultHandler);
	}
	
	/** The id has to be unique and cannot be changed! **/
	public static void registerLittleTile(Class<? extends LittleTile> LittleClass, String id, LittleTilePreviewHandler handler) {
		tileIDs.put(LittleClass, id);
		invTileIDs.put(id, LittleClass);
		previewHandlers.put(id, handler);
	}
	
	public static LittleTile CreateEmptyTile(String id) {
		Class<? extends LittleTile> TileClass = getClassByID(id);
		if (TileClass != null) {
			try {
				return TileClass.getConstructor().newInstance();
			} catch (Exception e) {
				System.out.println("Found invalid tileID=" + id);
			}
		}
		return null;
	}
	
	public static LittleTile CreateandLoadTile(TileEntityLittleTiles te, World world, NBTTagCompound nbt) {
		if (nbt.hasKey("tileID")) { // If it's the old tileentity
			if (nbt.hasKey("block")) {
				Block block = Block.getBlockFromName(nbt.getString("block"));
				int meta = nbt.getInteger("meta");
				LittleTileBox box = new LittleTileBox(new LittleTileVec("i", nbt), new LittleTileVec("a", nbt));
				box.add(new LittleTileVec(LittleGridContext.oldHalfGridSize, LittleGridContext.oldHalfGridSize, LittleGridContext.oldHalfGridSize));
				LittleTileBlock tile = new LittleTileBlock(block, meta);
				tile.box = box;
				return tile;
			}
		} else {
			LittleTile tile = CreateEmptyTile(nbt.getString("tID"));
			if (tile != null) {
				try {
					tile.loadTile(te, nbt);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			return tile;
		}
		return null;
	}
	
	public boolean invisible = false;
	
	public boolean glowing = false;
	
	public TileEntityLittleTiles te;
	
	/** Every LittleTile class has to have this constructor implemented **/
	public LittleTile() {
		
	}
	
	public String getID() {
		return getIDByClass(this.getClass());
	}
	
	// ================Position & Size================
	
	public LittleGridContext getContext() {
		return te.getContext();
	}
	
	public int getSmallestContext(LittleGridContext context) {
		return box.getSmallestContext(context);
	}
	
	public void convertTo(LittleGridContext from, LittleGridContext to) {
		box.convertTo(from, to);
	}
	
	public boolean canBeConvertedToVanilla() {
		return !isChildOfStructure();
	}
	
	public LittleTilePos getAbsolutePos() {
		return new LittleTilePos(te.getPos(), getContext(), box.getMinVec());
	}
	
	public LittleTileBox box;
	
	@Override
	public LittleTileBox getBox() {
		return box;
	}
	
	@Override
	public void setBox(LittleTileBox box) {
		this.box = box;
	}
	
	public LittleTileVec getMinVec() {
		return box.getMinVec();
	}
	
	public int getMaxY() {
		return box.maxY;
	}
	
	public AxisAlignedBB getSelectedBox(BlockPos pos) {
		if (LittleTilesConfig.rendering.highlightStructureBox && isConnectedToStructure() && connection.getStructureWithoutLoading().hasLoaded())
			return connection.getStructureWithoutLoading().getSurroundingBox();
		return box.getSelectionBox(getContext(), pos);
	}
	
	public double getVolume() {
		return box.getVolume();
	}
	
	public double getPercentVolume() {
		return box.getPercentVolume(getContext());
	}
	
	public LittleTileSize getSize() {
		return box.getSize();
	}
	
	public boolean doesFillEntireBlock() {
		return box.doesFillEntireBlock(getContext());
	}
	
	public void fillFace(LittleTileFace face) {
		LittleTileBox box = this.box;
		if (face.context != getContext()) {
			box = box.copy();
			box.convertTo(getContext(), face.context);
		}
		box.fill(face);
	}
	
	public void fillInSpace(boolean[][][] filled) {
		if (!box.isCompletelyFilled())
			return;
		for (int x = box.minX; x < box.maxX; x++) {
			for (int y = box.minY; y < box.maxY; y++) {
				for (int z = box.minZ; z < box.maxZ; z++) {
					filled[x][y][z] = true;
				}
			}
		}
	}
	
	@Override
	public void fillInSpace(LittleTileBox otherBox, boolean[][][] filled) {
		box.fillInSpace(otherBox, filled);
	}
	
	/** Cannot be overridden!
	 * 
	 * @return */
	public final int[] getIdentifier() {
		return box.getIdentifier();
	}
	
	/** It's faster than isAt()
	 * 
	 * @return if the min vec of the box equals the given coordinates */
	public boolean is(LittleGridContext context, int[] identifier) {
		identifier = LittleTileIdentifierAbsolute.convertTo(identifier, context, getContext());
		if (identifier == null)
			return false;
		return box.is(identifier);
	}
	
	/** It's slower than isCornerAt()
	 * 
	 * @return if the coordinates are inside the box(es) of the tile */
	public boolean isAt(int x, int y, int z) {
		return box.isVecInsideBox(x, y, z);
	}
	
	public boolean intersectsWith(LittleTileBox box) {
		return LittleTileBox.intersectsWith(this.box, box);
	}
	
	public List<LittleTileBox> cutOut(LittleTileBox box) {
		return this.box.cutOut(box);
	}
	
	public List<LittleTileBox> cutOut(List<LittleTileBox> boxes, List<LittleTileBox> cutout) {
		return this.box.cutOut(boxes, cutout);
	}
	
	public void getCuttingBoxes(List<LittleTileBox> boxes) {
		boxes.add(box);
	}
	
	public LittleTileBox getCompleteBox() {
		return box;
	}
	
	public LittleTileVec getCenter() {
		return box.getCenter();
	}
	
	public RayTraceResult rayTrace(Vec3d pos, Vec3d look) {
		return box.calculateIntercept(getContext(), te.getPos(), pos, look);
	}
	
	public boolean equalsBox(LittleTileBox box) {
		return this.box.equals(box);
	}
	
	public boolean canBeCombined(LittleTile tile) {
		if (isChildOfStructure() != tile.isChildOfStructure())
			return false;
		
		if (isChildOfStructure() && this.connection.getStructureWithoutLoading() != tile.connection.getStructureWithoutLoading() && !this.connection.equals(tile.connection))
			return false;
		
		if (invisible != tile.invisible)
			return false;
		
		if (glowing != tile.glowing)
			return false;
		
		return true;
	}
	
	public boolean canBeSplitted() {
		return true;
	}
	
	public void combineTiles(LittleTile tile) {
		if (isConnectedToStructure()) {
			connection.getStructure(te.getWorld()).removeTile(tile);
		}
	}
	
	@Override
	public boolean isChildOfStructure() {
		return connection != null;
	}
	
	public boolean isChildOfStructure(LittleStructure structure) {
		if (isChildOfStructure() && connection.isConnected(te.getWorld()))
			return connection.getStructureWithoutLoading() == structure || connection.getStructureWithoutLoading().isChildOf(structure);
		return false;
	}
	
	@Override
	public boolean canCombine(ICombinable combinable) {
		return canBeSplitted() && ((LittleTile) combinable).canBeSplitted() && this.canBeCombined((LittleTile) combinable) && ((LittleTile) combinable).canBeCombined(this);
	}
	
	@Override
	public void combine(ICombinable combinable) {
		this.combineTiles((LittleTile) combinable);
	}
	
	@SideOnly(Side.CLIENT)
	public boolean doesProvideSolidFace(EnumFacing facing) {
		return !invisible;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTile tile) {
		return this.invisible == tile.invisible;
	}
	
	// ================Packets================
	
	protected static Field playerInChunkMapEntry = ReflectionHelper.findField(PlayerChunkMapEntry.class, new String[] { "players", "field_187283_c" });
	
	/** Only works for tiles which support update packets. Example: LittleTileTE
	 * 
	 * @return */
	public boolean sendUpdatePacketToClient() {
		if (supportsUpdatePacket()) {
			if (!te.getWorld().isRemote && te.getWorld() instanceof WorldServer) {
				PlayerChunkMap map = ((WorldServer) te.getWorld()).getPlayerChunkMap();
				ChunkPos pos = new ChunkPos(te.getPos());
				PlayerChunkMapEntry entry = map.getEntry(pos.x, pos.z);
				try {
					List<EntityPlayerMP> players = (List<EntityPlayerMP>) playerInChunkMapEntry.get(entry);
					PacketHandler.sendPacketToPlayers(new LittleTileUpdatePacket(this, getUpdateNBT()), players);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
			
			return true;
		}
		return false;
	}
	
	/** Can be used to force a complete update on client for tiles which support
	 * update packet (example: LittleTileTE) */
	public boolean needsFullUpdate = false;
	
	public boolean supportsUpdatePacket() {
		return false;
	}
	
	public NBTTagCompound getUpdateNBT() {
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public void receivePacket(NBTTagCompound nbt, NetworkManager net) {
		
	}
	
	public boolean isIdenticalToNBT(NBTTagCompound nbt) {
		return getID().equals(nbt.getString("tID")) && glowing == nbt.getBoolean("glowing") && invisible == nbt.getBoolean("invisible") && Arrays.equals(box.getArray(), nbt.getIntArray("box"));
	}
	
	// ================Save & Loading================
	
	public NBTTagCompound startNBTGrouping() {
		NBTTagCompound nbt = new NBTTagCompound();
		saveTile(nbt);
		
		nbt.removeTag("box");
		
		NBTTagList list = new NBTTagList();
		list.appendTag(box.getNBTIntArray());
		nbt.setTag("boxes", list);
		
		return nbt;
	}
	
	public boolean canBeNBTGrouped(LittleTile tile) {
		return tile.canBeCombined(this) && this.canBeCombined(tile) && (this.connection == null || (this.te == tile.te && this.connection.equals(tile.connection)));
	}
	
	public void groupNBTTile(NBTTagCompound nbt, LittleTile tile) {
		NBTTagList list = nbt.getTagList("boxes", 11);
		
		/* for (int i = 0; i < tile.boundingBoxes.size(); i++) {
		 * list.appendTag(tile.boundingBoxes.get(i).getNBTIntArray()); } */
		list.appendTag(tile.box.getNBTIntArray());
	}
	
	public List<NBTTagCompound> extractNBTFromGroup(NBTTagCompound nbt) {
		List<NBTTagCompound> tags = new ArrayList<>();
		NBTTagList list = nbt.getTagList("boxes", 11);
		
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound copy = nbt.copy();
			NBTTagList small = new NBTTagList();
			small.appendTag(list.get(i));
			copy.setTag("boxes", small);
			tags.add(copy);
		}
		return tags;
	}
	
	public void saveTile(NBTTagCompound nbt) {
		saveTileCore(nbt);
		saveTileExtra(nbt);
	}
	
	/** Used to save extra data like block-name, meta, color etc. everything
	 * necessary for a preview **/
	public void saveTileExtra(NBTTagCompound nbt) {
		if (invisible)
			nbt.setBoolean("invisible", invisible);
		if (glowing)
			nbt.setBoolean("glowing", glowing);
		if (getStructureAttribute() == LittleStructureAttribute.PREMADE)
			nbt.setBoolean("nodrop", true);
	}
	
	public void saveTileCore(NBTTagCompound nbt) {
		nbt.setString("tID", getID());
		nbt.setIntArray("box", box.getArray());
		
		if (isChildOfStructure()) {
			NBTTagCompound structureNBT = new NBTTagCompound();
			if (connection.isLink())
				((StructureLinkBaseRelative) connection).writeToNBT(structureNBT);
			else {
				connection.getStructureWithoutLoading().writeToNBT(structureNBT);
				structureNBT.setBoolean("main", true);
			}
			nbt.setTag("structure", structureNBT);
		}
	}
	
	public void loadTile(TileEntityLittleTiles te, NBTTagCompound nbt) {
		this.te = te;
		loadTileCore(nbt);
		loadTileExtra(nbt);
	}
	
	public void loadTileExtra(NBTTagCompound nbt) {
		invisible = nbt.getBoolean("invisible");
		glowing = nbt.getBoolean("glowing");
	}
	
	public void loadTileCore(NBTTagCompound nbt) {
		if (nbt.hasKey("bSize")) // Old (used till 1.4)
		{
			int count = nbt.getInteger("bSize");
			box = LittleTileBox.loadBox("bBox" + 0, nbt);
		} else if (nbt.hasKey("boxes")) { // Out of date (used in pre-releases of 1.5)
			NBTTagList list = nbt.getTagList("boxes", 11);
			box = LittleTileBox.createBox(list.getIntArrayAt(0));
		} else if (nbt.hasKey("box")) { // Active one
			box = LittleTileBox.createBox(nbt.getIntArray("box"));
		}
		
		if (nbt.hasKey("structure", 10)) {
			NBTTagCompound structureNBT = nbt.getCompoundTag("structure");
			if (structureNBT.getBoolean("main")) {
				LittleStructure structure = (connection != null && !connection.isLink()) ? connection.getStructureWithoutLoading() : null;
				if (structure == null)
					structure = LittleStructure.createAndLoadStructure(structureNBT, this);
				else {
					structure.loadStructure(this);
					for (Iterator<LittleTile> iterator = structure.getTiles(); iterator.hasNext();) {
						LittleTile tile = iterator.next();
						if (tile != this)
							tile.connection.reset();
					}
					structure.loadFromNBT(structureNBT);
				}
				connection = new StructureMainTile(this, structure);
			} else
				connection = new StructureLinkTile(structureNBT, this);
			
		} else { // Old
			
			if (nbt.getBoolean("isStructure")) {
				if (nbt.getBoolean("main")) {
					connection = new StructureMainTile(this, LittleStructure.createAndLoadStructure(nbt, this));
				} else {
					if (nbt.hasKey("coX")) {
						LittleTilePosition pos = new LittleTilePosition(nbt);
						connection = new StructureLinkTile(te, pos.coord, LittleGridContext.get(), new int[] { pos.position.x, pos.position.y, pos.position.z }, LittleStructureAttribute.NONE, this);
						System.out.println("Converting old positioning to new relative coordinates " + pos + " to " + connection);
					} else
						connection = new StructureLinkTile(nbt, this);
				}
			}
		}
	}
	
	public void markForUpdate() {
		if (!te.getWorld().isRemote)
			te.updateBlock();
		else
			te.updateRender();
	}
	
	// ================Placing================
	
	/** stack may be null **/
	public void onPlaced(@Nullable EntityPlayer player, ItemStack stack, @Nullable EnumFacing facing) {
		onNeighborChangeInside();
	}
	
	public void place() {
		te.updateTiles((tilesTe) -> tilesTe.addTile(this));
	}
	
	// ================Destroying================
	
	public void destroy() {
		if (isChildOfStructure()) {
			if (isConnectedToStructure())
				connection.getStructure(te.getWorld()).onLittleTileDestroy();
		} else
			te.updateTiles((tilesTe) -> tilesTe.removeTile(this));
	}
	
	// ================Copy================
	
	@Override
	public LittleTile copy() {
		LittleTile tile = null;
		try {
			tile = this.getClass().getConstructor().newInstance();
		} catch (Exception e) {
			System.out.println("Invalid LittleTile class=" + this.getClass().getName());
			tile = null;
		}
		if (tile != null) {
			copyCore(tile);
			copyExtra(tile);
		}
		return tile;
	}
	
	public void assignTo(LittleTile target) {
		copyCore(target);
		copyExtra(target);
	}
	
	public void copyExtra(LittleTile tile) {
		tile.invisible = this.invisible;
		tile.glowing = this.glowing;
	}
	
	public void copyCore(LittleTile tile) {
		tile.box = box != null ? box.copy() : null;
		tile.te = this.te;
		
		if (this.connection != null)
			tile.connection = this.connection.copy(tile);
	}
	
	// ================Drop================
	
	public ArrayList<ItemStack> getDrops() {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		ItemStack stack = null;
		if (isChildOfStructure()) {
			if (isConnectedToStructure())
				stack = connection.getStructure(te.getWorld()).getStructureDrop();
		} else
			stack = getDrop();
		if (stack != null)
			drops.add(stack);
		
		return drops;
	}
	
	public abstract ItemStack getDrop();
	
	public LittleTilePreview getPreviewTile() {
		NBTTagCompound nbt = new NBTTagCompound();
		saveTileExtra(nbt);
		nbt.setString("tID", getID());
		return new LittleTilePreview(box.copy(), nbt);
	}
	
	// ================Notifcations/Events================
	
	public void onNeighborChangeOutside() {
		onNeighborChange();
	}
	
	public void onNeighborChangeInside() {
		onNeighborChange();
	}
	
	public void onNeighborChange() {
	}
	
	// ================Rendering================
	
	public boolean needCustomRendering() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID;
	}
	
	@SideOnly(Side.CLIENT)
	public final List<LittleRenderingCube> getRenderingCubes() {
		if (invisible)
			return new ArrayList<>();
		return getInternalRenderingCubes();
	}
	
	@SideOnly(Side.CLIENT)
	protected abstract List<LittleRenderingCube> getInternalRenderingCubes();
	
	@SideOnly(Side.CLIENT)
	public void renderTick(double x, double y, double z, float partialTickTime) {
	}
	
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 4096;
	}
	
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}
	
	// ================Sound================
	
	public abstract SoundType getSound();
	
	// ================Tick================
	
	public void updateEntity() {
		
	}
	
	public boolean shouldTick() {
		return false;
	}
	
	// ================Interaction================
	
	protected abstract boolean canSawResize(EnumFacing facing, EntityPlayer player);
	
	public boolean canSawResizeTile(EnumFacing facing, EntityPlayer player) {
		return !isChildOfStructure() && canSawResize(facing, player);
	}
	
	public boolean canBeMoved(EnumFacing facing) {
		return true;
	}
	
	// ================Block Event================
	
	public abstract float getExplosionResistance();
	
	public void onTileExplodes(Explosion explosion) {
	}
	
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
	}
	
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (isConnectedToStructure())
			return connection.getStructure(te.getWorld()).onBlockActivated(worldIn, this, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ, action);
		return false;
	}
	
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return glowing ? 14 : 0;
	}
	
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return 0;
	}
	
	public boolean isLadder() {
		return getStructureAttribute() == LittleStructureAttribute.LADDER;
	}
	
	public float getSlipperiness(Entity entity) {
		return 0;
	}
	
	public boolean isMaterial(Material material) {
		return false;
	}
	
	public boolean isLiquid() {
		return false;
	}
	
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		return originalColor;
	}
	
	public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
		return null;
	}
	
	// ================Collision================
	
	public List<LittleTileBox> getCollisionBoxes() {
		if (getStructureAttribute() == LittleStructureAttribute.COLLISION)
			return new ArrayList<>();
		List<LittleTileBox> boxes = new ArrayList<>();
		boxes.add(box);
		return boxes;
	}
	
	public boolean shouldCheckForCollision() {
		if (getStructureAttribute() == LittleStructureAttribute.COLLISION && isConnectedToStructure() && connection.getStructure(te.getWorld()).shouldCheckForCollision())
			return true;
		return false;
	}
	
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (getStructureAttribute() == LittleStructureAttribute.COLLISION && isConnectedToStructure())
			connection.getStructure(te.getWorld()).onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
	}
	
	// ================Structure================
	
	public IStructureConnector<LittleTile> connection;
	
	public boolean isConnectedToStructure() {
		return connection != null && connection.isConnected(te.getWorld());
	}
	
	public LittleStructureAttribute getStructureAttribute() {
		if (isChildOfStructure())
			return connection.getAttribute();
		return LittleStructureAttribute.NONE;
	}
	
	@Deprecated
	public static class LittleTilePosition {
		
		public BlockPos coord;
		public LittleTileVec position;
		
		public LittleTilePosition(BlockPos coord, LittleTileVec position) {
			this.coord = coord;
			this.position = position;
		}
		
		public LittleTilePosition(String id, NBTTagCompound nbt) {
			coord = new BlockPos(nbt.getInteger(id + "coX"), nbt.getInteger(id + "coY"), nbt.getInteger(id + "coZ"));
			position = new LittleTileVec(id + "po", nbt);
		}
		
		public LittleTilePosition(NBTTagCompound nbt) {
			this("", nbt);
		}
		
		public void writeToNBT(String id, NBTTagCompound nbt) {
			nbt.setInteger(id + "coX", coord.getX());
			nbt.setInteger(id + "coY", coord.getY());
			nbt.setInteger(id + "coZ", coord.getZ());
			position.writeToNBT(id + "po", nbt);
		}
		
		public void writeToNBT(NBTTagCompound nbt) {
			writeToNBT("", nbt);
		}
		
		@Override
		public String toString() {
			return "coord:" + coord + "|position:" + position;
		}
		
		public LittleTilePosition copy() {
			return new LittleTilePosition(new BlockPos(coord), position.copy());
		}
		
	}
	
}
