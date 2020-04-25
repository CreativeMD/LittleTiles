package com.creativemd.littletiles.common.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderCubeObject.EnumSideRender;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.cache.RenderCubeLayerCache;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy;
import com.creativemd.littletiles.common.item.ItemBlockTiles;
import com.creativemd.littletiles.common.item.ItemColorTube;
import com.creativemd.littletiles.common.item.ItemLittleSaw;
import com.creativemd.littletiles.common.item.ItemLittleWrench;
import com.creativemd.littletiles.common.item.ItemRubberMallet;
import com.creativemd.littletiles.common.mod.ctm.CTMManager;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox.LittleTileFace;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesRendered;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTicking;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTickingRendered;
import com.creativemd.littletiles.server.LittleTilesServer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.chisel.ctm.api.IFacade;

@Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm")
public class BlockTile extends BlockContainer implements ICreativeRendered, IFacade {// ICustomCachedCreativeRendered {
	
	public static class TEResult {
		
		public final TileEntityLittleTiles te;
		public final LittleTile tile;
		
		public TEResult(TileEntityLittleTiles te, LittleTile tile) {
			this.te = te;
			this.tile = tile;
		}
		
		public boolean isComplete() {
			return te != null && tile != null;
		}
	}
	
	public static TileEntityLittleTiles loadTe(IBlockAccess world, BlockPos pos) {
		if (world == null)
			return null;
		loadingTileEntityFromWorld = true;
		TileEntity tileEntity = null;
		try {
			tileEntity = world.getTileEntity(pos);
		} catch (Exception e) {
			return null;
		}
		loadingTileEntityFromWorld = false;
		if (tileEntity instanceof TileEntityLittleTiles)
			return (TileEntityLittleTiles) tileEntity;
		return null;
	}
	
	public static TEResult loadTeAndTile(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return loadTeAndTile(world, pos, player, TickUtils.getPartialTickTime());
	}
	
	public static TEResult loadTeAndTile(IBlockAccess world, BlockPos pos, EntityPlayer player, float partialTickTime) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			return new TEResult(te, te.getFocusedTile(player, partialTickTime));
		return new TEResult(null, null);
	}
	
	public static boolean selectEntireBlock(EntityPlayer player, boolean secondMode) {
		return secondMode && !(player.getHeldItemMainhand().getItem() instanceof ItemLittleSaw) && !(player.getHeldItemMainhand().getItem() instanceof ItemColorTube);
	}
	
	public static final SoundType SILENT = new SoundType(-1.0F, 1.0F, SoundEvents.BLOCK_STONE_BREAK, SoundEvents.BLOCK_STONE_STEP, SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_HIT, SoundEvents.BLOCK_STONE_FALL);
	
	public final boolean ticking;
	public final boolean rendered;
	
	public BlockTile(Material material, boolean ticking, boolean rendered) {
		super(material);
		this.ticking = ticking;
		this.rendered = rendered;
		setCreativeTab(LittleTiles.littleTab);
		setResistance(3.0F);
		setSoundType(SILENT);
	}
	
	public static int getStateId(TileEntityLittleTiles te) {
		return getStateId(te.isTicking(), te.isRendered());
	}
	
	public static int getStateId(boolean ticking, boolean rendered) {
		return rendered ? (ticking ? 3 : 2) : (ticking ? 1 : 0);
	}
	
	public static IBlockState getState(int id) {
		switch (id) {
		case 0:
			return LittleTiles.blockTileNoTicking.getDefaultState();
		case 1:
			return LittleTiles.blockTileTicking.getDefaultState();
		case 2:
			return LittleTiles.blockTileNoTickingRendered.getDefaultState();
		case 3:
			return LittleTiles.blockTileTickingRendered.getDefaultState();
		}
		return null;
	}
	
	public static IBlockState getState(boolean ticking, boolean rendered) {
		return rendered ? (ticking ? LittleTiles.blockTileTickingRendered.getDefaultState() : LittleTiles.blockTileNoTickingRendered.getDefaultState()) : (ticking ? LittleTiles.blockTileTicking.getDefaultState() : LittleTiles.blockTileNoTicking.getDefaultState());
	}
	
	public static IBlockState getState(TileEntityLittleTiles te) {
		return getState(te.isTicking(), te.isRendered());
	}
	
	public static IBlockState getState(List<LittleTile> tiles) {
		boolean ticking = false;
		boolean rendered = false;
		for (LittleTile tile : tiles) {
			if (tile.shouldTick())
				ticking = true;
			if (tile.needCustomRendering())
				rendered = true;
			
			if (ticking && rendered)
				break;
		}
		return getState(ticking, rendered);
	}
	
	@SideOnly(Side.CLIENT)
	public static Minecraft mc;
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return true;
	}
	
	/** Used to determine ambient occlusion and culling when rebuilding chunks for
	 * render */
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied) {
		
	}
	
	@Override
	public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			LittleStructure bed = null;
			if (player != null) {
				try {
					bed = (LittleStructure) LittleBed.littleBed.get(player);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			if (bed != null)
				for (LittleTile tile : te) {
					if (tile.isConnectedToStructure() && tile.connection.getStructure(te.getWorld()) == bed)
						return true;
				}
			else
				for (LittleTile tile : te) {
					if (tile.isConnectedToStructure() && tile.connection.getStructure(te.getWorld()).isBed(world, pos, (EntityLivingBase) player))
						return true;
				}
		}
		return false;
	}
	
	@Override
	public EnumFacing getBedDirection(IBlockState state, IBlockAccess world, BlockPos pos) {
		return EnumFacing.SOUTH;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasCustomBreakingProgress(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockPos getBedSpawnPosition(IBlockState state, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		int tries = 0;
		EnumFacing enumfacing = EnumFacing.EAST; // (EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();
		
		for (int l = 0; l <= 1; ++l) {
			int i1 = i - enumfacing.getFrontOffsetX() * l - 1;
			int j1 = k - enumfacing.getFrontOffsetZ() * l - 1;
			int k1 = i1 + 2;
			int l1 = j1 + 2;
			
			for (int i2 = i1; i2 <= k1; ++i2) {
				for (int j2 = j1; j2 <= l1; ++j2) {
					BlockPos blockpos = new BlockPos(i2, j, j2);
					
					if (hasRoomForPlayer(world, blockpos)) {
						if (tries <= 0) {
							return blockpos;
						}
						
						--tries;
					}
				}
			}
		}
		
		return null;
	}
	
	protected static boolean hasRoomForPlayer(IBlockAccess worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos, EnumFacing.UP) && !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
	}
	
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null && entity != null && entity.getEntityBoundingBox() != null) {
			AxisAlignedBB bb = entity.getEntityBoundingBox().grow(0.001);
			for (LittleTile tile : te) {
				if (tile.isLadder()) {
					List<LittleBox> collision = tile.getCollisionBoxes();
					for (int j = 0; j < collision.size(); j++) {
						LittleBox box = collision.get(j).copy();
						if (bb.intersects(box.getBox(te.getContext(), te.getPos())))
							return true;
					}
				}
				
			}
		}
		return false;
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return 0.1F;
	}
	
	public static boolean canHarvestBlock(EntityPlayer player, IBlockState state) {
		if (state.getMaterial().isToolNotRequired()) {
			return true;
		}
		
		ItemStack stack = player.getHeldItemMainhand();
		String tool = state.getBlock().getHarvestTool(state);
		if (stack.isEmpty() || tool == null) {
			return player.canHarvestBlock(state);
		}
		
		int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
		if (toolLevel < 0) {
			return player.canHarvestBlock(state);
		}
		
		return toolLevel >= state.getBlock().getHarvestLevel(state);
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
		TEResult result = loadTeAndTile(world, pos, player);
		if (result.isComplete()) {
			
			state = result.tile.getBlockState();
			
			float hardness = state.getBlockHardness(world, pos);
			if (hardness < 0.0F)
				return 0.0F;
			
			if (!canHarvestBlock(player, state)) {
				return player.getDigSpeed(state, pos) / hardness / 40F;
			} else {
				return player.getDigSpeed(state, pos) / hardness / 20F;
			}
		} else
			return super.getPlayerRelativeBlockHardness(state, player, world, pos);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		TEResult result = loadTeAndTile(worldIn, pos, mc.player);
		if (result.isComplete()) {
			if (selectEntireBlock(mc.player, LittleAction.isUsingSecondMode(mc.player)))
				return result.te.getSelectionBox();
			return result.tile.getSelectedBox(pos);
		}
		return new AxisAlignedBB(pos);
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
		TileEntityLittleTiles te = loadTe(worldIn, pos);
		if (te != null) {
			for (LittleTile tile : te) {
				List<LittleBox> boxes = tile.getCollisionBoxes();
				for (int i = 0; i < boxes.size(); i++)
					boxes.get(i).addCollisionBoxes(te.getContext(), entityBox, collidingBoxes, pos);
			}
			for (LittleStructure structure : te.structures(LittleStructureAttribute.EXTRA_COLLSION))
				structure.addCollisionBoxes(pos, entityBox, collidingBoxes, entityIn);
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null && te.isEmpty())
			super.breakBlock(world, pos, state);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (LittleTiles.CONFIG.rendering.enableRandomDisplayTick) {
			TileEntityLittleTiles te = loadTe(worldIn, pos);
			if (te != null) {
				for (LittleTile tile : te)
					tile.randomDisplayTick(stateIn, worldIn, pos, rand);
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (heldItem != null && heldItem.getItem() instanceof ItemRubberMallet)
			return false;
		if (worldIn.isRemote)
			return onBlockActivatedClient(worldIn, pos, state, playerIn, hand, heldItem, facing, hitX, hitY, hitZ);
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean onBlockActivatedClient(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TEResult result = loadTeAndTile(worldIn, pos, playerIn);
		if (result.isComplete() && !(playerIn.getHeldItemMainhand().getItem() instanceof ItemLittleWrench))
			return new LittleActionActivated(worldIn, pos, playerIn).execute();
		return false;
	}
	
	@Override
	public float getSlipperiness(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity entity) {
		float slipperiness = 1;
		boolean found = false;
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null && entity != null && entity.getEntityBoundingBox() != null) {
			AxisAlignedBB bb = entity.getEntityBoundingBox().offset(0, -0.001, 0);
			for (LittleTile tile : te) {
				for (LittleBox box : tile.getCollisionBoxes()) {
					if (box.getBox(te.getContext(), pos).intersects(bb)) {
						slipperiness = Math.min(slipperiness, tile.getSlipperiness(entity));
						found = true;
					}
				}
			}
		}
		if (found)
			return slipperiness;
		return super.getSlipperiness(state, world, pos, entity);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}
	
	public boolean first = true;
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		int light = 0;
		if (!first)
			return 0;
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			for (LittleTile tile : te) {
				first = false;
				int tempLight = tile.getLightValue(state, world, pos);
				first = true;
				if (tempLight > light)
					light = tempLight;
			}
		}
		return light;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		if (isSideSolid(state, worldIn, pos, face))
			return BlockFaceShape.SOLID;
		return BlockFaceShape.UNDEFINED;
	}
	
	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			return te.sideCache.get(face).doesBlockLight();
		return false;
	}
	
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			return te.sideCache.get(side).isFilled();
		return false;
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if (world.isRemote)
			return removedByPlayerClient(state, world, pos, player, willHarvest);
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean removedByPlayerClient(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		TEResult result = loadTeAndTile(world, pos, player, 1.0F);
		if (result.isComplete())
			return new LittleActionDestroy(world, pos, player).execute();
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			return te.isEmpty();
		return true;
	}
	
	@Override
	public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess world, BlockPos pos, Vec3d viewpoint) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			int x = te.getContext().toGrid(viewpoint.x);
			int y = te.getContext().toGrid(viewpoint.y);
			int z = te.getContext().toGrid(viewpoint.z);
			for (LittleTile tile : te)
				if (tile.box.isVecInsideBox(x, y, z))
					return tile.getBlockState();
		}
		return state;
	}
	
	@Override
	/** Blocks will drop before this method is called */
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		/* TileEntityLittleTiles te = loadTe(world, pos); if(te != null) { for (Iterator
		 * iterator = te.getTiles().iterator(); iterator.hasNext();) { LittleTile tile =
		 * (LittleTile) iterator.next(); stacks.addAll(tile.getDrops()); } } */
		return stacks;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TEResult result = loadTeAndTile(world, pos, mc.player);
		if (result.isComplete()) {
			if (selectEntireBlock(mc.player, LittleAction.isUsingSecondMode(player))) {
				ItemStack drop = new ItemStack(LittleTiles.multiTiles);
				LittlePreview.saveTiles(world, result.te.getContext(), result.te, drop);
				return drop;
			}
			ArrayList<ItemStack> drops = result.tile.getDrops();
			if (drops.size() > 0)
				if (drops.get(0) != null)
					return drops.get(0);
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean addLandingEffects(IBlockState state, net.minecraft.world.WorldServer world, BlockPos pos, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			int heighest = 0;
			LittleTile heighestTile = null;
			for (LittleTile tile : te) {
				List<LittleBox> collision = tile.getCollisionBoxes();
				for (int i = 0; i < collision.size(); i++) {
					if (collision.get(i).maxY > heighest) {
						heighest = collision.get(i).maxY;
						heighestTile = tile;
					}
				}
			}
			
			if (heighestTile != null)
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, new int[] { Block.getStateId(heighestTile.getBlockState()) });
		}
		return true;
	}
	
	@Override
	public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			int heighest = 0;
			LittleTile heighestTile = null;
			for (LittleTile tile : te) {
				List<LittleBox> collision = tile.getCollisionBoxes();
				for (int i = 0; i < collision.size(); i++) {
					if (collision.get(i).maxY > heighest) {
						heighest = collision.get(i).maxY;
						heighestTile = tile;
					}
				}
			}
			
			Random random = new Random();
			if (heighestTile != null)
				world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, entity.posX + (random.nextFloat() - 0.5D) * entity.width, entity.getEntityBoundingBox().minY + 0.1D, entity.posZ + (random.nextFloat() - 0.5D) * entity.width, -entity.motionX * 4.0D, 1.5D, -entity.motionZ * 4.0D,
				        Block.getStateId(heighestTile.getBlockState()));
			return true;
		}
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState oldstate, World worldObj, RayTraceResult target, net.minecraft.client.particle.ParticleManager manager) {
		TEResult result = loadTeAndTile(worldObj, target.getBlockPos(), mc.player);
		if (result.isComplete()) {
			IBlockState state = result.tile.getBlockState();
			BlockPos pos = target.getBlockPos();
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			float f = 0.1F;
			AxisAlignedBB axisalignedbb = result.tile.box.getSelectionBox(result.te.getContext(), BlockPos.ORIGIN);
			double d0 = i + worldObj.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
			double d1 = j + worldObj.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
			double d2 = k + worldObj.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;
			EnumFacing side = target.sideHit;
			if (side == EnumFacing.DOWN)
				d1 = j + axisalignedbb.minY - 0.10000000149011612D;
			
			if (side == EnumFacing.UP)
				d1 = j + axisalignedbb.maxY + 0.10000000149011612D;
			
			if (side == EnumFacing.NORTH)
				d2 = k + axisalignedbb.minZ - 0.10000000149011612D;
			
			if (side == EnumFacing.SOUTH)
				d2 = k + axisalignedbb.maxZ + 0.10000000149011612D;
			
			if (side == EnumFacing.WEST)
				d0 = i + axisalignedbb.minX - 0.10000000149011612D;
			
			if (side == EnumFacing.EAST)
				d0 = i + axisalignedbb.maxX + 0.10000000149011612D;
			
			((ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(state))).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
		}
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager) {
		TEResult result = loadTeAndTile(world, pos, mc.player);
		if (result.isComplete()) {
			IBlockState state = result.tile.getBlockState();
			int i = 4;
			
			for (int j = 0; j < 1; ++j) {
				for (int k = 0; k < 1; ++k) {
					for (int l = 0; l < 1; ++l) {
						double d0 = pos.getX() + (j + 0.5D) / 4.0D;
						double d1 = pos.getY() + (k + 0.5D) / 4.0D;
						double d2 = pos.getZ() + (l + 0.5D) / 4.0D;
						manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D, Block.getStateId(state));
					}
				}
			}
		}
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	public SoundType getSoundTypeClient(IBlockState state, World world, BlockPos pos) {
		TEResult result = loadTeAndTile(world, pos, mc.player);
		if (result != null && result.tile != null)
			return result.tile.getSound();
		return null;
	}
	
	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
		if (entity == null)
			return SILENT;
		SoundType sound = null;
		if (entity instanceof EntityPlayer && world.isRemote)
			sound = getSoundTypeClient(state, world, pos);
		
		if (sound == null) {
			// GET HEIGHEST TILE POSSIBLE
			TileEntityLittleTiles te = loadTe(world, pos);
			if (te != null) {
				int heighest = 0;
				LittleTile heighestTile = null;
				for (LittleTile tile : te) {
					List<LittleBox> collision = tile.getCollisionBoxes();
					for (int i = 0; i < collision.size(); i++) {
						if (collision.get(i).maxY > heighest) {
							heighest = collision.get(i).maxY;
							heighestTile = tile;
						}
					}
				}
				
				if (heighestTile != null)
					return heighestTile.getSound();
			}
		}
		
		if (sound == null)
			sound = SoundType.STONE;
		return sound;
	}
	
	/* public boolean rotateBlock(World worldObj, int x, int y, int z,
	 * ForgeDirection axis) { return RotationHelper.rotateVanillaBlock(this,
	 * worldObj, x, y, z, axis); }
	 * 
	 * public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int
	 * z) {
	 * 
	 * } */
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		/* if(loadingTileEntityFromWorld) if is normal cube player will be pushed out of
		 * the block (bad for no-clip structure or water return false; for (int i = 0; i
		 * < 6; i++) { if(!isSideSolid(state, world, pos, EnumFacing.getFront(i)))
		 * return false; } return true; */
		return false;
	}
	
	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		float bonus = 0F;
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			for (LittleTile tile : te)
				bonus += tile.getEnchantPowerBonus(world, pos) * tile.getPercentVolume();
		return bonus;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntityLittleTiles te = loadTe(worldIn, pos);
		if (te != null) {
			te.onNeighbourChanged();
			if (!worldIn.isRemote)
				LittleTilesServer.NEIGHBOR.add(worldIn, pos);
		}
	}
	
	private static boolean loadingTileEntityFromWorld = false;
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		if (loadingTileEntityFromWorld)
			return;
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			te.onNeighbourChanged();
	}
	
	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
		TileEntityLittleTiles te = loadTe(worldIn, pos);
		if (te != null) {
			RayTraceResult moving = te.rayTrace(start, end);
			if (moving != null)
				return new RayTraceResult(moving.hitVec, moving.sideHit, pos);
		}
		return null;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		TileEntityLittleTiles te = loadTe(worldIn, pos);
		if (te != null && te.shouldCheckForCollision())
			for (LittleTile tile : te)
				if (tile.shouldCheckForCollision())
					if (tile.box.getBox(te.getContext()).offset(pos).intersects(entityIn.getEntityBoundingBox()))
						tile.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if (rendered)
			if (ticking)
				return new TileEntityLittleTilesTickingRendered();
			else
				return new TileEntityLittleTilesRendered();
			
		if (ticking)
			return new TileEntityLittleTilesTicking();
		return new TileEntityLittleTiles();
	}
	
	@SideOnly(Side.CLIENT)
	private static TileEntityLittleTiles checkforTileEntity(World world, EnumFacing facing, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
		if (tileEntity instanceof TileEntityLittleTiles)
			return (TileEntityLittleTiles) tileEntity;
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	private static boolean checkforNeighbor(World world, EnumFacing facing, BlockPos pos) {
		BlockPos newPos = pos.offset(facing);
		IBlockState state = world.getBlockState(newPos);
		return !state.doesSideBlockRendering(world, newPos, facing.getOpposite());
	}
	
	@SideOnly(Side.CLIENT)
	private static void updateRenderer(TileEntityLittleTiles tileEntity, EnumFacing facing, HashMap<EnumFacing, Boolean> neighbors, HashMap<EnumFacing, TileEntityLittleTiles> neighborsTiles, RenderCubeObject cube, LittleTileFace face) {
		if (face == null) {
			cube.setSideRender(facing, EnumSideRender.INSIDE_RENDERED);
			return;
		}
		Boolean shouldRender = neighbors.get(facing);
		if (shouldRender == null) {
			shouldRender = checkforNeighbor(tileEntity.getWorld(), facing, tileEntity.getPos());
			neighbors.put(facing, shouldRender);
		}
		
		if (shouldRender == Boolean.TRUE) {
			TileEntityLittleTiles otherTile = null;
			if (!neighborsTiles.containsKey(facing)) {
				otherTile = checkforTileEntity(tileEntity.getWorld(), facing, tileEntity.getPos());
				neighborsTiles.put(facing, otherTile);
			} else
				otherTile = neighborsTiles.get(facing);
			if (otherTile != null) {
				face.move(facing);
				// face.face = facing.getOpposite();
				shouldRender = otherTile.shouldSideBeRendered(facing.getOpposite(), face, (LittleTile) cube.customData);
			}
		}
		cube.setSideRender(facing, shouldRender ? EnumSideRender.OUTSIDE_RENDERED : EnumSideRender.OUTSIDE_NOT_RENDERD);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<? extends RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if (te instanceof TileEntityLittleTiles) {
			return Collections.emptyList();
		}
		return getRenderingCubes(state, te, stack, MinecraftForgeClient.getRenderLayer());
	}
	
	@SideOnly(Side.CLIENT)
	public static List<LittleRenderingCube> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack, BlockRenderLayer layer) {
		ArrayList<LittleRenderingCube> cubes = new ArrayList<>();
		if (te instanceof TileEntityLittleTiles) {
			HashMap<EnumFacing, Boolean> neighbors = new HashMap<>();
			HashMap<EnumFacing, TileEntityLittleTiles> neighborsTiles = new HashMap<>();
			
			TileEntityLittleTiles tileEntity = (TileEntityLittleTiles) te;
			
			RenderCubeLayerCache cache = tileEntity.getCubeCache();
			List<LittleRenderingCube> cachedCubes = cache.getCubesByLayer(layer);
			if (cachedCubes != null) {
				if (tileEntity.hasNeighbourChanged) {
					for (BlockRenderLayer tempLayer : BlockRenderLayer.values()) {
						List<LittleRenderingCube> renderCubes = cache.getCubesByLayer(tempLayer);
						if (renderCubes == null)
							continue;
						for (int i = 0; i < renderCubes.size(); i++) {
							LittleRenderingCube cube = renderCubes.get(i);
							for (int k = 0; k < EnumFacing.VALUES.length; k++) {
								EnumFacing facing = EnumFacing.VALUES[k];
								if (cube.getSidedRendererType(facing).outside) {
									LittleTileFace face = cube.box.getFace(tileEntity.getContext(), facing);
									
									boolean shouldRenderBefore = cube.shouldSideBeRendered(facing);
									// face.move(facing);
									updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, face);
									if (cube.shouldSideBeRendered(facing)) {
										if (!shouldRenderBefore)
											cube.doesNeedQuadUpdate = true;
									} else
										cube.setQuad(facing, null);
								}
							}
						}
					}
				}
				
				return cachedCubes;
			}
			
			for (LittleTile tile : tileEntity) {
				if (tile.shouldBeRenderedInLayer(layer)) {
					// Check for sides which does not need to be rendered
					List<LittleRenderingCube> tileCubes = tile.getRenderingCubes(layer);
					for (int j = 0; j < tileCubes.size(); j++) {
						LittleRenderingCube cube = tileCubes.get(j);
						for (int k = 0; k < EnumFacing.VALUES.length; k++) {
							EnumFacing facing = EnumFacing.VALUES[k];
							LittleTileFace face = cube.box.getFace(tileEntity.getContext(), facing);
							
							cube.customData = tile;
							
							if (face == null) {
								cube.setSideRender(facing, EnumSideRender.INSIDE_RENDERED);
							} else {
								if (face.isFaceInsideBlock()) {
									cube.setSideRender(facing, ((TileEntityLittleTiles) te).shouldSideBeRendered(facing, face, tile) ? EnumSideRender.INSIDE_RENDERED : EnumSideRender.INSIDE_NOT_RENDERED);
								} else {
									updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, face);
								}
							}
						}
					}
					cubes.addAll(tileCubes);
				}
			}
			
			for (LittleStructure structure : tileEntity.structures(LittleStructureAttribute.EXTRA_RENDERING))
				if (structure.load())
					structure.getRenderingCubes(tileEntity.getPos(), layer, cubes);
				
			cache.setCubesByLayer(cubes, layer);
			
		} else if (stack != null)
			return ItemBlockTiles.getItemRenderingCubes(stack);
		return cubes;
	}
	
	@Override
	public boolean canDropFromExplosion(Explosion explosionIn) {
		return false;
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
		TileEntityLittleTiles te = loadTe(world, pos);
		double resistance = 0;
		if (te != null)
			for (LittleTile tile : te)
				resistance += tile.getExplosionResistance() * tile.getPercentVolume();
			
		return (float) resistance;
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			te.updateTiles((x) -> {
				for (LittleTile tile : te)
					tile.destroy(x);
			});
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			for (LittleTile tile : te)
				if (tile.box.getBox(te.getContext(), pos).intersects(entity.getEntityBoundingBox()))
					return tile.getFogColor(world, pos, state, entity, originalColor, partialTicks);
				
		return super.getFogColor(world, pos, state, entity, originalColor, partialTicks);
	}
	
	/* protected Vec3d getFlow(IBlockAccess worldIn, BlockPos pos, IBlockState
	 * state) { double d0 = 0.0D; double d1 = 0.0D; double d2 = 0.0D;
	 * BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos =
	 * BlockPos.PooledMutableBlockPos.retain(); int i =
	 * this.getRenderedDepth(state); BlockPos.PooledMutableBlockPos
	 * blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();
	 * 
	 * for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
	 * blockpos$pooledmutableblockpos.setPos(pos).move(enumfacing); int j =
	 * this.getRenderedDepth(worldIn.getBlockState(blockpos$pooledmutableblockpos));
	 * 
	 * if (j < 0) { if
	 * (!worldIn.getBlockState(blockpos$pooledmutableblockpos).getMaterial().
	 * blocksMovement()) { j =
	 * this.getRenderedDepth(worldIn.getBlockState(blockpos$pooledmutableblockpos.
	 * down()));
	 * 
	 * if (j >= 0) { int k = j - (i - 8); d0 +=
	 * (double)(enumfacing.getFrontOffsetX() * k); d1 +=
	 * (double)(enumfacing.getFrontOffsetY() * k); d2 +=
	 * (double)(enumfacing.getFrontOffsetZ() * k); } } } else if (j >= 0) { int l =
	 * j - i; d0 += (double)(enumfacing.getFrontOffsetX() * l); d1 +=
	 * (double)(enumfacing.getFrontOffsetY() * l); d2 +=
	 * (double)(enumfacing.getFrontOffsetZ() * l); } }
	 * 
	 * Vec3d vec3d = new Vec3d(d0, d1, d2);
	 * 
	 * //if (((Integer)state.getValue(LEVEL)).intValue() >= 8) //{ for (EnumFacing
	 * enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
	 * blockpos$pooledmutableblockpos.setPos(pos).move(enumfacing1);
	 * 
	 * if (this.causesDownwardCurrent(worldIn, blockpos$pooledmutableblockpos,
	 * enumfacing1) || this.causesDownwardCurrent(worldIn,
	 * blockpos$pooledmutableblockpos.up(), enumfacing1)) { vec3d =
	 * vec3d.normalize().addVector(0.0D, -6.0D, 0.0D); break; } } //}
	 * 
	 * blockpos$pooledmutableblockpos.release(); return vec3d.normalize(); }
	 * 
	 * private boolean causesDownwardCurrent(IBlockAccess worldIn, BlockPos pos,
	 * EnumFacing side) { IBlockState iblockstate = worldIn.getBlockState(pos);
	 * Block block = iblockstate.getBlock(); Material material =
	 * iblockstate.getMaterial();
	 * 
	 * if (material == this.blockMaterial) { return false; } else if (side ==
	 * EnumFacing.UP) { return true; } else if (material == Material.ICE) { return
	 * false; } else { boolean flag = isExceptBlockForAttachWithPiston(block) ||
	 * block instanceof BlockStairs; return !flag &&
	 * iblockstate.getBlockFaceShape(worldIn, pos, side) == BlockFaceShape.SOLID; }
	 * } */
	
	@Override
	public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entityIn, Vec3d motion) {
		AxisAlignedBB boundingBox = entityIn.getEntityBoundingBox();
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			Vec3d vec = Vec3d.ZERO;
			for (LittleTile tile : te) {
				if (tile.box.getBox(te.getContext(), pos).intersects(boundingBox)) {
					Vec3d tileMotion = tile.modifyAcceleration(world, pos, entityIn, motion);
					if (tileMotion != null)
						vec = vec.add(tileMotion);
				}
			}
			return motion.add(vec);
		}
		return motion;
	}
	
	@Override
	@Nullable
	public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos blockpos, IBlockState iblockstate, Entity entity, double yToTest, Material materialIn, boolean testingHead) {
		return isAABBInsideMaterial(entity.world, blockpos, entity.getEntityBoundingBox(), materialIn);
	}
	
	@Override
	@Nullable
	public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			for (LittleTile tile : te) {
				if (tile.isMaterial(materialIn) && tile.box.getBox(te.getContext(), pos).intersects(boundingBox))
					return true;
			}
		}
		return false;
	}
	
	@Override
	public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB boundingBox) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			for (LittleTile tile : te) {
				if (tile.isLiquid() && tile.box.getBox(te.getContext(), pos).intersects(boundingBox))
					return true;
			}
		}
		return false;
	}
	
	@Override
	public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material) {
		float height = 0;
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null)
			for (LittleTile tile : te)
				if (tile.isMaterial(material))
					height = Math.max(height, (float) te.getContext().toVanillaGrid(tile.getMaxY()));
		return height;
	}
	
	@Override
	@Method(modid = CTMManager.ctmID)
	public IBlockState getFacade(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return world.getBlockState(pos);
	}
	
	@Override
	@Method(modid = CTMManager.ctmID)
	public IBlockState getFacade(IBlockAccess world, BlockPos pos, EnumFacing side, BlockPos connection) {
		TileEntityLittleTiles te = loadTe(world, pos);
		if (te != null) {
			IBlockState lookingFor = CTMManager.isInstalled() ? CTMManager.getCorrectStateOrigin(world, connection) : world.getBlockState(connection);
			for (LittleTile tile : te) {
				if (tile.getBlock() == lookingFor.getBlock() && tile.getMeta() == lookingFor.getBlock().getMetaFromState(lookingFor))
					return lookingFor;
			}
		}
		return this.getDefaultState();
	}
	
}
