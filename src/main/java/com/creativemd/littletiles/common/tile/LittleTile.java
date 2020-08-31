package com.creativemd.littletiles.common.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.api.block.ISpecialBlockHandler;
import com.creativemd.littletiles.common.api.block.SpecialBlockHandler;
import com.creativemd.littletiles.common.item.ItemBlockTiles;
import com.creativemd.littletiles.common.tile.combine.ICombinable;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.face.LittleBoxFace;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tile.registry.LittleTileType;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTile implements ICombinable {
	
	private LittleBox box;
	
	public boolean invisible = false;
	public boolean glowing = false;
	
	private Block block;
	private int meta;
	
	private ISpecialBlockHandler handler;
	protected byte cachedTranslucent;
	protected IBlockState state = null;
	
	/** Every LittleTile class has to have this constructor implemented **/
	public LittleTile() {
		
	}
	
	public LittleTile(Block block, int meta) {
		setBlock(block, meta);
	}
	
	public LittleTileType getType() {
		return LittleTileRegistry.getTileType(this.getClass());
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
	
	public int getSmallestContext(LittleGridContext context) {
		return box.getSmallestContext(context);
	}
	
	public void convertTo(LittleGridContext from, LittleGridContext to) {
		box.convertTo(from, to);
	}
	
	public boolean canBeConvertedToVanilla() {
		if (box.isSolid())
			return false;
		if (hasSpecialBlockHandler())
			return handler.canBeConvertedToVanilla(this);
		return true;
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
	
	public AxisAlignedBB getSelectedBox(BlockPos pos, LittleGridContext context) {
		return box.getSelectionBox(context, pos);
	}
	
	public double getVolume() {
		return box.getVolume();
	}
	
	public double getPercentVolume(LittleGridContext context) {
		return box.getPercentVolume(context);
	}
	
	public LittleVec getSize() {
		return box.getSize();
	}
	
	public boolean doesFillEntireBlock(LittleGridContext context) {
		return box.doesFillEntireBlock(context);
	}
	
	public void fillFace(LittleBoxFace face, LittleGridContext context) {
		LittleBox box = this.box;
		if (face.context != context) {
			box = box.copy();
			box.convertTo(context, face.context);
		}
		box.fill(face);
	}
	
	public boolean fillInSpace(boolean[][][] filled) {
		if (!box.isSolid())
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
	
	public RayTraceResult rayTrace(LittleGridContext context, BlockPos blockPos, Vec3d pos, Vec3d look) {
		return box.calculateIntercept(context, blockPos, pos, look);
	}
	
	public boolean equalsBox(LittleBox box) {
		return this.box.equals(box);
	}
	
	public boolean canBeCombined(LittleTile tile) {
		if (invisible != tile.invisible)
			return false;
		
		if (glowing != tile.glowing)
			return false;
		
		return block == tile.block && meta == tile.meta;
	}
	
	@Override
	public boolean canCombine(ICombinable combinable) {
		return this.canBeCombined((LittleTile) combinable) && ((LittleTile) combinable).canBeCombined(this);
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
		return tile.canBeCombined(this) && this.canBeCombined(tile);
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
	}
	
	public void loadTile(NBTTagCompound nbt) {
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
		if (nbt.hasKey("bSize")) { // Old (used till 1.4)
			int count = nbt.getInteger("bSize");
			box = LittleBox.loadBox("bBox" + 0, nbt);
		} else if (nbt.hasKey("boxes")) { // Out of date (used in pre-releases of 1.5)
			NBTTagList list = nbt.getTagList("boxes", 11);
			box = LittleBox.createBox(list.getIntArrayAt(0));
		} else if (nbt.hasKey("box")) { // Active one
			box = LittleBox.createBox(nbt.getIntArray("box"));
		}
		
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
	}
	
	// ================Drop================
	
	public ItemStack getDrop(LittleGridContext context) {
		return getDropInternal(context);
	}
	
	protected ItemStack getDropInternal(LittleGridContext context) {
		return ItemBlockTiles.getStackFromPreview(context, getPreviewTile());
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
	public final List<LittleRenderBox> getRenderingCubes(LittleGridContext context, BlockRenderLayer layer) {
		if (invisible)
			return new ArrayList<>();
		return getInternalRenderingCubes(context, layer);
	}
	
	@SideOnly(Side.CLIENT)
	protected List<LittleRenderBox> getInternalRenderingCubes(LittleGridContext context, BlockRenderLayer layer) {
		ArrayList<LittleRenderBox> cubes = new ArrayList<>();
		if (block != Blocks.BARRIER)
			cubes.add(box.getRenderingCube(context, block, meta));
		return cubes;
	}
	
	// ================Sound================
	
	public SoundType getSound() {
		return block.getSoundType();
	}
	
	// ================Interaction================
	
	public boolean canSawResizeTile(EnumFacing facing, EntityPlayer player) {
		return true;
	}
	
	public boolean canBeMoved(EnumFacing facing) {
		return true;
	}
	
	// ================Block Event================
	
	public float getExplosionResistance() {
		return block.getExplosionResistance(null);
	}
	
	public void onTileExplodes(IParentTileList parent, Explosion explosion) {
		if (hasSpecialBlockHandler())
			handler.onTileExplodes(parent, this, explosion);
	}
	
	public void randomDisplayTick(IParentTileList parent, Random rand) {
		if (hasSpecialBlockHandler())
			handler.randomDisplayTick(parent, this, rand);
		else
			block.randomDisplayTick(getBlockState(), parent.getWorld(), parent.getPos(), rand);
		
		if (block == Blocks.BARRIER)
			spawnBarrierParticles(parent.getPos());
	}
	
	@SideOnly(Side.CLIENT)
	private void spawnBarrierParticles(BlockPos pos) {
		Minecraft mc = Minecraft.getMinecraft();
		ItemStack itemstack = mc.player.getHeldItemMainhand();
		if (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER))
			mc.world.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	public boolean onBlockActivated(IParentTileList parent, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
		if (hasSpecialBlockHandler())
			return handler.onBlockActivated(parent, this, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		return block.onBlockActivated(parent.getWorld(), parent.getPos(), getBlockState(), playerIn, hand, side, hitX, hitY, hitZ);
	}
	
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		if (glowing)
			return glowing ? 14 : 0;
		return block.getLightValue(getBlockState());
	}
	
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return block.getEnchantPowerBonus(world, pos);
	}
	
	public float getSlipperiness(IBlockAccess world, BlockPos pos, Entity entity) {
		return block.getSlipperiness(getBlockState(), world, pos, entity);
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
	
	public Vec3d getFogColor(IParentTileList parent, Entity entity, Vec3d originalColor, float partialTicks) {
		if (hasSpecialBlockHandler())
			return handler.getFogColor(parent, this, entity, originalColor, partialTicks);
		return originalColor;
	}
	
	public Vec3d modifyAcceleration(IParentTileList parent, Entity entityIn, Vec3d motion) {
		if (hasSpecialBlockHandler())
			return handler.modifyAcceleration(parent, this, entityIn, motion);
		return null;
	}
	
	// ================Collision================
	
	public boolean shouldCheckForCollision() {
		return true;
	}
	
	public void onEntityCollidedWithBlock(IParentTileList parent, Entity entityIn) {
		if (hasSpecialBlockHandler())
			handler.onEntityCollidedWithBlock(parent, this, entityIn);
	}
	
	public boolean hasNoCollision() {
		if (hasSpecialBlockHandler())
			return handler.canWalkThrough(this);
		return false;
	}
	
	public LittleBox getCollisionBox() {
		if (hasSpecialBlockHandler())
			return handler.getCollisionBox(this, box);
		
		return box;
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
