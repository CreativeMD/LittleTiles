package com.creativemd.littletiles.common.tile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.api.block.SpecialBlockHandler;
import com.creativemd.littletiles.common.item.ItemBlockTiles;
import com.creativemd.littletiles.common.packet.LittleTileUpdatePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.connection.IStructureConnector;
import com.creativemd.littletiles.common.structure.connection.StructureLinkBaseRelative;
import com.creativemd.littletiles.common.structure.connection.StructureLinkTile;
import com.creativemd.littletiles.common.structure.connection.StructureMainTile;
import com.creativemd.littletiles.common.tile.combine.ICombinable;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox.LittleTileFace;
import com.creativemd.littletiles.common.tile.math.identifier.LittleIdentifierAbsolute;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tile.registry.LittleTileType;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTile implements ICombinable {
	
	public TileEntityLittleTiles te;
	public LittleBox box;
	
	public boolean invisible = false;
	public boolean glowing = false;
	
	private Block block;
	private int meta;
	
	private ISpecialBlockHandler handler;
	
	/** Every LittleTile class has to have this constructor implemented **/
	public LittleTile() {
		
	}
	
	public LittleTile(Block block, int meta) {
		setBlock(block, meta);
	}
	
	public LittleTileType getType() {
		return LittleTileRegistry.getTileType(this.getClass());
	}
	
	// ================Basics================
	
	public World getWorld() {
		return te.getWorld();
	}
	
	public BlockPos getBlockPos() {
		return te.getPos();
	}
	
	// ================Block================
	
	private void updateSpecialHandler() {
		if (!(block instanceof BlockAir))
			handler = SpecialBlockHandler.getSpecialBlockHandler(block, meta);
		updateBlockState();
	}
	
	public boolean hasSpecialBlockHandler() {
		return handler != null;
	}
	
	protected void setBlock(String defaultName, Block block, int meta) {
		if (block == null || block instanceof BlockAir) {
			this.block = Blocks.AIR;
			this.meta = meta;
			this.handler = MissingBlockHandler.getHandler(defaultName);
		} else
			setBlock(block, meta);
	}
	
	public void setBlock(Block block, int meta) {
		this.block = block;
		this.meta = meta;
		updateSpecialHandler();
	}
	
	public void setMeta(int meta) {
		this.meta = meta;
		updateSpecialHandler();
	}
	
	public void setBlock(Block block) {
		this.block = block;
		updateSpecialHandler();
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public int getMeta() {
		return this.meta;
	}
	
	protected byte cachedTranslucent;
	protected IBlockState state = null;
	
	public IBlockState getBlockState() {
		if (state == null)
			updateBlockState();
		return state;
	}
	
	public void updateBlockState() {
		state = BlockUtils.getState(block, meta);
		if (state == null)
			state = block.getDefaultState();
	}
	
	public boolean isTranslucent() {
		if (cachedTranslucent == 0)
			updateTranslucent();
		return cachedTranslucent == 2;
	}
	
	public void updateTranslucent() {
		if (!getBlockState().getMaterial().blocksLight() || !getBlockState().getMaterial().isSolid() || !getBlockState().isOpaqueCube())
			cachedTranslucent = 2;
		else
			cachedTranslucent = 1;
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
		if (isChildOfStructure())
			return false;
		
		if (hasSpecialBlockHandler())
			return handler.canBeConvertedToVanilla(this);
		return true;
	}
	
	public LittleAbsoluteVec getAbsolutePos() {
		return new LittleAbsoluteVec(te.getPos(), getContext(), box.getMinVec());
	}
	
	@Override
	public LittleBox getBox() {
		return box;
	}
	
	@Override
	public void setBox(LittleBox box) {
		this.box = box;
	}
	
	public LittleVec getMinVec() {
		return box.getMinVec();
	}
	
	public int getMaxY() {
		return box.maxY;
	}
	
	public AxisAlignedBB getSelectedBox(BlockPos pos) {
		if (LittleTiles.CONFIG.rendering.highlightStructureBox && isConnectedToStructure() && connection.getStructureWithoutLoading().load())
			return connection.getStructureWithoutLoading().getSurroundingBox();
		return box.getSelectionBox(getContext(), pos);
	}
	
	public double getVolume() {
		return box.getVolume();
	}
	
	public double getPercentVolume() {
		return box.getPercentVolume(getContext());
	}
	
	public LittleVec getSize() {
		return box.getSize();
	}
	
	public boolean doesFillEntireBlock() {
		return box.doesFillEntireBlock(getContext());
	}
	
	public void fillFace(LittleTileFace face) {
		LittleBox box = this.box;
		if (face.context != getContext()) {
			box = box.copy();
			box.convertTo(getContext(), face.context);
		}
		box.fill(face);
	}
	
	public boolean fillInSpace(boolean[][][] filled) {
		if (!box.isCompletelyFilled())
			return false;
		
		boolean changed = false;
		for (int x = box.minX; x < box.maxX; x++) {
			for (int y = box.minY; y < box.maxY; y++) {
				for (int z = box.minZ; z < box.maxZ; z++) {
					filled[x][y][z] = true;
					changed = true;
				}
			}
		}
		return changed;
	}
	
	@Override
	public boolean fillInSpace(LittleBox otherBox, boolean[][][] filled) {
		return box.fillInSpace(otherBox, filled);
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
		identifier = LittleIdentifierAbsolute.convertTo(identifier, context, getContext());
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
	
	public boolean intersectsWith(LittleBox box) {
		return LittleBox.intersectsWith(this.box, box);
	}
	
	public List<LittleBox> cutOut(LittleBox box) {
		return this.box.cutOut(box);
	}
	
	public List<LittleBox> cutOut(List<LittleBox> boxes, List<LittleBox> cutout) {
		return this.box.cutOut(boxes, cutout);
	}
	
	public void getCuttingBoxes(List<LittleBox> boxes) {
		boxes.add(box);
	}
	
	public LittleBox getCompleteBox() {
		return box;
	}
	
	public LittleVec getCenter() {
		return box.getCenter();
	}
	
	public RayTraceResult rayTrace(Vec3d pos, Vec3d look) {
		return box.calculateIntercept(getContext(), te.getPos(), pos, look);
	}
	
	public boolean equalsBox(LittleBox box) {
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
		
		return block == tile.block && meta == tile.meta;
	}
	
	public boolean canBeSplitted() {
		return true;
	}
	
	public void combineTiles(LittleTile tile) {
		if (isConnectedToStructure())
			connection.getStructure(te.getWorld()).remove(tile);
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
	
	public boolean doesProvideSolidFace(EnumFacing facing) {
		return !invisible && box.isFaceSolid(facing) && !isTranslucent() && block != Blocks.BARRIER;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean canBeRenderCombined(LittleTile tile) {
		if (this.invisible != tile.invisible)
			return false;
		
		if (block == tile.block && meta == tile.meta && block != Blocks.BARRIER && tile.block != Blocks.BARRIER)
			return true;
		
		if (hasSpecialBlockHandler() && handler.canBeRenderCombined(this, tile))
			return true;
		
		return false;
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
	public boolean requiresCompleteUpdate() {
		return false;
	}
	
	/** Can be used to force a complete update on client for tiles which support
	 * update packet (example: LittleTileTE) */
	public void setCompleteUpdate(boolean value) {
		
	}
	
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
		return getType() == LittleTileRegistry.getTypeFromNBT(nbt) && glowing == nbt.getBoolean("glowing") && invisible == nbt.getBoolean("invisible") && Arrays.equals(box.getArray(), nbt.getIntArray("box")) && Block.REGISTRY.getNameForObject(block).toString().equals(
		        nbt.getString("block")) && meta == nbt.getInteger("meta");
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
		nbt.setString("block", handler instanceof MissingBlockHandler ? ((MissingBlockHandler) handler).blockname : Block.REGISTRY.getNameForObject(block).toString() + (meta != 0 ? ":" + meta : ""));
	}
	
	public void saveTileCore(NBTTagCompound nbt) {
		LittleTileType type = getType();
		if (type.saveId)
			nbt.setString("tID", type.id);
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
		if (nbt.hasKey("meta"))
			setBlock(nbt.getString("block"), Block.getBlockFromName(nbt.getString("block")), nbt.getInteger("meta"));
		else {
			String[] parts = nbt.getString("block").split(":");
			if (parts.length == 3)
				setBlock(nbt.getString("block"), Block.getBlockFromName(parts[0] + ":" + parts[1]), Integer.parseInt(parts[2]));
			else
				setBlock(nbt.getString("block"), Block.getBlockFromName(parts[0] + ":" + parts[1]), 0);
		}
	}
	
	public void loadTileCore(NBTTagCompound nbt) {
		if (nbt.hasKey("bSize")) // Old (used till 1.4)
		{
			int count = nbt.getInteger("bSize");
			box = LittleBox.loadBox("bBox" + 0, nbt);
		} else if (nbt.hasKey("boxes")) { // Out of date (used in pre-releases of 1.5)
			NBTTagList list = nbt.getTagList("boxes", 11);
			box = LittleBox.createBox(list.getIntArrayAt(0));
		} else if (nbt.hasKey("box")) { // Active one
			box = LittleBox.createBox(nbt.getIntArray("box"));
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
	
	// ================Placing================
	
	/** stack may be null **/
	public void placed(@Nullable EntityPlayer player, @Nullable EnumFacing facing) {
		
	}
	
	public void place(TileList list) {
		list.add(this);
	}
	
	// ================Destroying================
	
	public void destroy(TileList list) {
		if (isChildOfStructure()) {
			if (isConnectedToStructure())
				connection.getStructure(te.getWorld()).onLittleTileDestroy();
		} else
			list.remove(this);
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
		tile.handler = this.handler;
		tile.block = this.block;
		tile.meta = this.meta;
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
	
	public ItemStack getDrop() {
		return ItemBlockTiles.getStackFromPreview(getContext(), getPreviewTile());
	}
	
	public LittlePreview getPreviewTile() {
		if (hasSpecialBlockHandler()) {
			LittlePreview preview = handler.getPreview(this);
			if (preview != null)
				return preview;
		}
		
		NBTTagCompound nbt = new NBTTagCompound();
		saveTileExtra(nbt);
		LittleTileType type = getType();
		if (type.saveId)
			nbt.setString("tID", type.id);
		return new LittlePreview(box.copy(), nbt);
	}
	
	// ================Rendering================
	
	public boolean needCustomRendering() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldBeRenderedInLayer(BlockRenderLayer layer) {
		if (FMLClientHandler.instance().hasOptifine() && block.canRenderInLayer(state, BlockRenderLayer.CUTOUT))
			return layer == BlockRenderLayer.CUTOUT_MIPPED; // Should fix an Optifine bug
			
		try {
			return block.canRenderInLayer(getBlockState(), layer);
		} catch (Exception e) {
			try {
				return block.getBlockLayer() == layer;
			} catch (Exception e2) {
				return layer == BlockRenderLayer.SOLID;
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public final List<LittleRenderingCube> getRenderingCubes(BlockRenderLayer layer) {
		if (invisible)
			return new ArrayList<>();
		return getInternalRenderingCubes(layer);
	}
	
	@SideOnly(Side.CLIENT)
	protected List<LittleRenderingCube> getInternalRenderingCubes(BlockRenderLayer layer) {
		ArrayList<LittleRenderingCube> cubes = new ArrayList<>();
		if (block != Blocks.BARRIER)
			cubes.add(box.getRenderingCube(getContext(), block, meta));
		return cubes;
	}
	
	@SideOnly(Side.CLIENT)
	public void renderTick(BlockPos pos, double x, double y, double z, float partialTickTime) {
		
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
	
	public SoundType getSound() {
		return block.getSoundType();
	}
	
	// ================Tick================
	
	public void updateEntity() {
		
	}
	
	public boolean shouldTick() {
		return false;
	}
	
	// ================Interaction================
	
	protected boolean canSawResize(EnumFacing facing, EntityPlayer player) {
		return true;
	}
	
	public boolean canSawResizeTile(EnumFacing facing, EntityPlayer player) {
		return !isChildOfStructure() && canSawResize(facing, player);
	}
	
	public boolean canBeMoved(EnumFacing facing) {
		return true;
	}
	
	// ================Block Event================
	
	public float getExplosionResistance() {
		return block.getExplosionResistance(null);
	}
	
	public void onTileExplodes(Explosion explosion) {
		if (hasSpecialBlockHandler())
			handler.onTileExplodes(this, explosion);
	}
	
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (hasSpecialBlockHandler())
			handler.randomDisplayTick(this, stateIn, worldIn, pos, rand);
		else
			block.randomDisplayTick(getBlockState(), worldIn, pos, rand);
		
		if (block == Blocks.BARRIER)
			spawnBarrierParticles(pos);
	}
	
	@SideOnly(Side.CLIENT)
	private void spawnBarrierParticles(BlockPos pos) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack itemstack = mc.player.getHeldItemMainhand();
		if (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER))
			mc.world.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
		if (isConnectedToStructure())
			return connection.getStructure(te.getWorld()).onBlockActivated(worldIn, this, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ, action);
		
		if (hasSpecialBlockHandler())
			return handler.onBlockActivated(this, worldIn, pos, getBlockState(), playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		return block.onBlockActivated(worldIn, pos, getBlockState(), playerIn, hand, side, hitX, hitY, hitZ);
	}
	
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (glowing)
			return glowing ? 14 : 0;
		return block.getLightValue(getBlockState());
	}
	
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return block.getEnchantPowerBonus(world, pos);
	}
	
	public boolean isLadder() {
		return LittleStructureAttribute.ladder(getStructureAttribute());
	}
	
	public float getSlipperiness(Entity entity) {
		return block.getSlipperiness(getBlockState(), te.getWorld(), te.getPos(), entity);
	}
	
	public boolean isMaterial(Material material) {
		if (hasSpecialBlockHandler())
			return handler.isMaterial(this, material);
		return material == block.getMaterial(state);
	}
	
	public boolean isLiquid() {
		if (hasSpecialBlockHandler())
			return handler.isLiquid(this);
		return getBlockState().getMaterial().isLiquid();
	}
	
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		if (hasSpecialBlockHandler())
			return handler.getFogColor(world, this, pos, getBlockState(), entity, originalColor, partialTicks);
		return originalColor;
	}
	
	public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
		if (hasSpecialBlockHandler())
			return handler.modifyAcceleration(this, entityIn, motion);
		return null;
	}
	
	public boolean hasNoCollision() {
		if (LittleStructureAttribute.noCollision(getStructureAttribute()))
			return true;
		if (hasSpecialBlockHandler())
			return handler.canWalkThrough(this);
		return false;
	}
	
	// ================Collision================
	
	public List<LittleBox> getCollisionBoxes() {
		if (LittleStructureAttribute.noCollision(getStructureAttribute()))
			return new ArrayList<>();
		
		List<LittleBox> boxes = new ArrayList<>();
		boxes.add(box);
		
		if (hasSpecialBlockHandler())
			return handler.getCollisionBoxes(this, boxes);
		
		return boxes;
	}
	
	public boolean shouldCheckForCollision() {
		if (LittleStructureAttribute.noCollision(getStructureAttribute()))
			return true;
		if (hasSpecialBlockHandler())
			return handler.shouldCheckForCollision(this);
		return false;
	}
	
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (LittleStructureAttribute.noCollision(getStructureAttribute()) && isConnectedToStructure())
			connection.getStructure(te.getWorld()).onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		if (hasSpecialBlockHandler())
			handler.onEntityCollidedWithBlock(worldIn, this, pos, state, entityIn);
	}
	
	// ================Structure================
	
	public IStructureConnector<LittleTile> connection;
	
	public boolean isConnectedToStructure() {
		return connection != null && connection.isConnected(te.getWorld());
	}
	
	public int getStructureAttribute() {
		if (isChildOfStructure())
			return connection.getAttribute();
		return LittleStructureAttribute.NONE;
	}
	
	@Deprecated
	public static class LittleTilePosition {
		
		public BlockPos coord;
		public LittleVec position;
		
		public LittleTilePosition(BlockPos coord, LittleVec position) {
			this.coord = coord;
			this.position = position;
		}
		
		public LittleTilePosition(String id, NBTTagCompound nbt) {
			coord = new BlockPos(nbt.getInteger(id + "coX"), nbt.getInteger(id + "coY"), nbt.getInteger(id + "coZ"));
			position = new LittleVec(id + "po", nbt);
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
	
	public static class MissingBlockHandler implements ISpecialBlockHandler {
		
		private static HashMap<String, MissingBlockHandler> handlers = new HashMap<>();
		
		public static MissingBlockHandler getHandler(String blockname) {
			MissingBlockHandler handler = handlers.get(blockname);
			if (handler != null)
				return handler;
			handler = new MissingBlockHandler(blockname);
			handlers.put(blockname, handler);
			return handler;
		}
		
		public final String blockname;
		
		private MissingBlockHandler(String blockname) {
			this.blockname = blockname;
		}
		
		public static void unload() {
			handlers.clear();
		}
	}
	
}
