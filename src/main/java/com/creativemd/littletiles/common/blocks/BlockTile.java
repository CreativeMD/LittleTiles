package com.creativemd.littletiles.common.blocks;

import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeLayerCache;
import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.RenderCubeObject.EnumSideRender;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.CreativeModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.client.rendering.model.ICustomCachedCreativeRendered;
import com.creativemd.creativecore.client.rendering.model.QuadCache;
import com.creativemd.creativecore.common.block.TileEntityState;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.RenderingThread;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleNeighborUpdatePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.transform.patmat.Solving.Solver.Lit;

public class BlockTile extends BlockContainer implements ICreativeRendered {//ICustomCachedCreativeRendered {
	
	public static class TEResult {
		
		public final TileEntityLittleTiles te;
		public final LittleTile tile;
		
		public TEResult(TileEntityLittleTiles te, LittleTile tile) {
			this.te = te;
			this.tile = tile;
		}
		
		public boolean isComplete()
		{
			return te != null && tile != null;
		}
	}
	
	/*public static TileEntityLittleTiles tempEntity;
	
	public static boolean loadTileEntity(IBlockAccess world, BlockPos pos)
	{
		if(world == null)
		{
			tempEntity = null;
			return false;
		}
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
			tempEntity = (TileEntityLittleTiles) tileEntity;
		else
			tempEntity = null;
		return tempEntity != null;
	}*/
	
	public static TileEntityLittleTiles loadTe(IBlockAccess world, BlockPos pos)
	{
		if(world == null)
			return null;
		loadingTileEntityFromWorld = true;
		TileEntity tileEntity = world.getTileEntity(pos);
		loadingTileEntityFromWorld = false;
		if(tileEntity instanceof TileEntityLittleTiles)
			return (TileEntityLittleTiles) tileEntity;
		return null;
	}
	
	public static TEResult loadTeAndTile(IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
			return new TEResult(te, te.getFocusedTile(player));
		return new TEResult(null, null);
	}
	
	public static final SoundType SILENT = new SoundType(-1.0F, 1.0F, SoundEvents.BLOCK_STONE_BREAK, SoundEvents.BLOCK_STONE_STEP, SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_HIT, SoundEvents.BLOCK_STONE_FALL);

	public BlockTile(Material material) {
		super(material);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setResistance(3.0F);
		setSoundType(SILENT);
	}
	
	@SideOnly(Side.CLIENT)
	public static Minecraft mc;
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		return new TileEntityState(state, world.getTileEntity(pos));
    }
	
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return true;
    }
	
	/**
     * Checks if an IBlockState represents a block that is opaque and a full cube.
     */
	@Override
    public boolean isFullyOpaque(IBlockState state)
    {
        return false;
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
	@Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
	
	@Override
	public boolean isFullCube(IBlockState state)
    {
        return false;
    }
	
	public LittleTile sleepingTile = null;
	
	@Override
	public EnumFacing getBedDirection(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		if(sleepingTile != null && sleepingTile.isStructureBlock && sleepingTile.structure != null)
			return sleepingTile.structure.getBedDirection(state, world, pos);
		return EnumFacing.EAST;
    }
	
	@Override
	public void setBedOccupied(IBlockAccess world, BlockPos pos, EntityPlayer player, boolean occupied)
    {
        
    }
	
	@Override
	public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, Entity player)
    {
		TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
		{
			for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				if(tile.isBed(world, pos, (EntityLivingBase) player))
					return true;
			}
		}
        return false;
    }
	
	@Override
	public BlockPos getBedSpawnPosition(IBlockState state, IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
    	int tries = 0;
    	EnumFacing enumfacing = EnumFacing.EAST; //(EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();

        for (int l = 0; l <= 1; ++l)
        {
            int i1 = i - enumfacing.getFrontOffsetX() * l - 1;
            int j1 = k - enumfacing.getFrontOffsetZ() * l - 1;
            int k1 = i1 + 2;
            int l1 = j1 + 2;

            for (int i2 = i1; i2 <= k1; ++i2)
            {
                for (int j2 = j1; j2 <= l1; ++j2)
                {
                    BlockPos blockpos = new BlockPos(i2, j, j2);

                    if (hasRoomForPlayer(world, blockpos))
                    {
                        if (tries <= 0)
                        {
                            return blockpos;
                        }

                        --tries;
                    }
                }
            }
        }

        return null;
    }
	
	protected static boolean hasRoomForPlayer(IBlockAccess worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos.down()).isFullyOpaque() && !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
    }
	
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
    {
		AxisAlignedBB bb = entity.getEntityBoundingBox();
        int mX = MathHelper.floor(bb.minX);
        int mY = MathHelper.floor(bb.minY);
        int mZ = MathHelper.floor(bb.minZ);
        for (int y2 = mY; y2 < bb.maxY; y2++)
        {
            for (int x2 = mX; x2 < bb.maxX; x2++)
            {
                for (int z2 = mZ; z2 < bb.maxZ; z2++)
                {
                	TileEntity te = world.getTileEntity(new BlockPos(x2, y2, z2));
                	if(te instanceof TileEntityLittleTiles)
                	{
                		TileEntityLittleTiles littleTE = (TileEntityLittleTiles) te;
                		for (Iterator iterator = littleTE.getTiles().iterator(); iterator.hasNext();) {
        					LittleTile tile = (LittleTile) iterator.next();
                			if(tile.isLadder())
                			{
	                			for (int j = 0; j < tile.boundingBoxes.size(); j++) {
	                				LittleTileBox box = tile.boundingBoxes.get(j).copy();
	                				box.addOffset(new LittleTileVec(x2*LittleTile.gridSize, y2*LittleTile.gridSize, z2*LittleTile.gridSize));
	                				double expand = 0.0001;
	                				if(bb.intersectsWith(box.getBox().expand(expand, expand, expand)))
	                					return true;
								}
                			}
							
						}
                	}
                }
            }
        }
        return false;
    }
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos)
    {
		/*if(loadTileEntity(worldIn, pos) && tempEntity.updateLoadedTile(player) && tempEntity.loadedTile instanceof LittleTileBlock)
		{
			return ((LittleTileBlock)tempEntity.loadedTile).block.getBlockHardness(((LittleTileBlock)tempEntity.loadedTile).getBlockState(), worldIn, pos);
		}*/
        return 0.1F;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
		return true;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
		TEResult result = loadTeAndTile(worldIn, pos, mc.player);
		if(result.isComplete())
		{
			ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
			if(stack != null && stack.getItem() instanceof ISpecialBlockSelector)
			{
				LittleTileBox box = ((ISpecialBlockSelector) stack.getItem()).getBox(result.te, result.tile, pos, mc.player, result.te.getMoving(mc.player));
				if(box != null)
					return box.getBox().offset(pos);
			}
			
			return result.tile.getSelectedBox().offset(pos);
		}
		return new AxisAlignedBB(pos);
    }
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
		TileEntityLittleTiles te = loadTe(worldIn, pos);
		if(te != null)
		{
			for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				ArrayList<LittleTileBox> boxes = tile.getCollisionBoxes();
				for (int i = 0; i < boxes.size(); i++) {
					addCollisionBoxToList(pos, entityBox, collidingBoxes, boxes.get(i).getBox());
				}
				
			}
		}
    }
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
		TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null && te.getTiles().size() == 0)
			super.breakBlock(world, pos, state);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		TileEntityLittleTiles te = loadTe(worldIn, pos);
		if(te != null)
		{
			for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				tile.randomDisplayTick(stateIn, worldIn, pos, rand);
			}
		}
	}
	
	public static boolean cancelNext = false;
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if(heldItem != null && heldItem.getItem() instanceof ItemRubberMallet)
			return false;
		if(worldIn.isRemote)
			return onBlockActivatedClient(worldIn, pos, state, playerIn, hand, heldItem, facing, hitX, hitY, hitZ);
		if(cancelNext)
		{
			cancelNext = false;
			return true;
		}
        return false;
    }
	
	@SideOnly(Side.CLIENT)
	public boolean onBlockActivatedClient(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		TEResult result = loadTeAndTile(worldIn, pos, mc.player);
		if(result.isComplete())
		{
			if(result.tile.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ))
			{
				if(worldIn.isRemote)
					PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, playerIn, 0));
				return true;
			}
			return false;
		}
		return false;
    }
	
	/*
	public int isProvidingWeakPower(IBlockAccess p_149709_1_, int p_149709_2_, int p_149709_3_, int p_149709_4_, int p_149709_5_)
    {
        return 0;
    }
	
	public boolean canProvidePower()
    {
        return false;
    }

    public int isProvidingStrongPower(IBlockAccess p_149748_1_, int p_149748_2_, int p_149748_3_, int p_149748_4_, int p_149748_5_)
    {
        return 0;
    }
    public boolean hasComparatorInputOverride()
    {
        return false;
    }
    
    public int getComparatorInputOverride(World p_149736_1_, int p_149736_2_, int p_149736_3_, int p_149736_4_, int p_149736_5_)
    {
        return 0;
    }*/
    
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> items) {}
    
	public boolean first = true;
	
	@Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		int light = 0;
		if(!first)
			return 0;
		TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
		{
    		for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				first = false;
				int tempLight = tile.getLightValue(state, world, pos);
				first = true;
				if(tempLight > light)
					light = tempLight;
			}
    	}
    	return light;
    }
    
	@Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
		TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
		{
			LittleTileBox box = new LittleTileBox(0, 0, 0, 1, LittleTile.gridSize, LittleTile.gridSize);
			box.rotateBox(side.getOpposite());
			return te.isBoxFilled(box);
		}
		return false;
    }
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
		if(world.isRemote)
			removedByPlayerClient(state, world, pos, player, willHarvest);
		return true;
    }
	
	@SideOnly(Side.CLIENT)
	public void removedByPlayerClient(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		TEResult result = loadTeAndTile(world, pos, player);
		if(result.isComplete())
		{				
			LittleTileBox box = null;
			
			ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
			if(stack != null && stack.getItem() instanceof ISpecialBlockSelector)
			{
				box = ((ISpecialBlockSelector) stack.getItem()).getBox(result.te, result.tile, pos, player, result.te.getMoving(player));
				/*if(box != null)
				{
					tempEntity.removeBoxFromTile(loaded, box);
				}*/
			}
			
			if(box == null)
			{
				result.tile.destroy();
				result.te.updateRender();
			}
			PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, player, 1));
			
		}
    }
    
	@Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos)
    {
		TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
      		return te.getTiles().size() == 0;
      	return true;
    }
    
    @Override
    /**Blocks will drop before this method is called*/
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
    	ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
    	TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
		{
    		for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				stacks.addAll(tile.getDrops());
			}
    	}
    	return stacks;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
    	TEResult result = loadTeAndTile(world, pos, mc.player);
		if(result.isComplete())
		{
			ArrayList<ItemStack> drops = result.tile.getDrops();
			if(drops.size() > 0)
				if(drops.get(0) != null)
					return drops.get(0);
    	}
    	return ItemStack.EMPTY;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState oldstate, World worldObj, RayTraceResult target, net.minecraft.client.particle.ParticleManager manager)
    {
		TEResult result = loadTeAndTile(worldObj, target.getBlockPos(), mc.player);
		if(result.isComplete() && result.tile instanceof LittleTileBlock)
    	{
    		IBlockState state = ((LittleTileBlock)result.tile).getBlockState();
    		BlockPos pos = target.getBlockPos();
    		int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            AxisAlignedBB axisalignedbb = result.tile.getSelectedBox();
            double d0 = (double)i + worldObj.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double)j + worldObj.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double)k + worldObj.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;
            EnumFacing side = target.sideHit;
            if (side == EnumFacing.DOWN)
            {
                d1 = (double)j + axisalignedbb.minY - 0.10000000149011612D;
            }

            if (side == EnumFacing.UP)
            {
                d1 = (double)j + axisalignedbb.maxY + 0.10000000149011612D;
            }

            if (side == EnumFacing.NORTH)
            {
                d2 = (double)k + axisalignedbb.minZ - 0.10000000149011612D;
            }

            if (side == EnumFacing.SOUTH)
            {
                d2 = (double)k + axisalignedbb.maxZ + 0.10000000149011612D;
            }

            if (side == EnumFacing.WEST)
            {
                d0 = (double)i + axisalignedbb.minX - 0.10000000149011612D;
            }

            if (side == EnumFacing.EAST)
            {
                d0 = (double)i + axisalignedbb.maxX + 0.10000000149011612D;
            }

            manager.addEffect(((ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(state))).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
    	}
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager)
    {
    	TEResult result = loadTeAndTile(world, pos, mc.player);
		if(result.isComplete() && result.tile instanceof LittleTileBlock)
    	{
    		//overrideIcon = tempEntity.loadedTile.block.getIcon(world, x, y, z, 0);
    		//AxisAlignedBB box = tempEntity.loadedTile.getSelectedBox();
    		IBlockState state = ((LittleTileBlock)result.tile).getBlockState();
    		//manager.addBlockDestroyEffects(pos, state);
            int i = 4;

            for (int j = 0; j < 1; ++j)
            {
                for (int k = 0; k < 1; ++k)
                {
                    for (int l = 0; l < 1; ++l)
                    {
                        double d0 = (double)pos.getX() + ((double)j + 0.5D) / 4.0D;
                        double d1 = (double)pos.getY() + ((double)k + 0.5D) / 4.0D;
                        double d2 = (double)pos.getZ() + ((double)l + 0.5D) / 4.0D;
                        manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, d0 - (double)pos.getX() - 0.5D, d1 - (double)pos.getY() - 0.5D, d2 - (double)pos.getZ() - 0.5D, Block.getStateId(state));
                        //manager.addEffect((new ParticleDigging(world, d0, d1, d2, d0 - (double)pos.getX() - 0.5D, d1 - (double)pos.getY() - 0.5D, d2 - (double)pos.getZ() - 0.5D, state)).setBlockPos(pos));
                    }
                }
            }
            //overrideIcon = null;
            return true;
    	}
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public SoundType getSoundTypeClient(IBlockState state, World world, BlockPos pos)
    {
    	TEResult result = loadTeAndTile(world, pos, mc.player);
    	if(result != null && result.tile != null)
	    	return result.tile.getSound();
    	return null;
    }
    
    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity)
    {
    	if(entity == null)
    		return SILENT;
    	SoundType sound = null;
    	if(entity instanceof EntityPlayer && world.isRemote)
    		sound = getSoundTypeClient(state, world, pos);
    	
    	if(sound == null)
    	{
    		//GET HEIGHEST TILE POSSIBLE
    		TileEntityLittleTiles te = loadTe(world, pos);
        	if(te != null)
        	{
        		int heighest = 0;
        		LittleTile heighestTile = null;
        		for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
    				LittleTile tile = (LittleTile) iterator.next();
    				for (int i = 0; i < tile.boundingBoxes.size(); i++) {
						if(tile.boundingBoxes.get(i).maxY > heighest)
						{
							heighest = tile.boundingBoxes.get(i).maxY;
							heighestTile = tile;
						}
					}
        		}
        		
        		if(heighestTile != null)
	        		return heighestTile.getSound();
        	}
    	}
    	
        return sound;
    }
    
    /*
   	public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        return RotationHelper.rotateVanillaBlock(this, worldObj, x, y, z, axis);
    }
    
    public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
    {
    	
    }*/
    
    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
    {
    	for (int i = 0; i < 6; i++) {
			if(!isSideSolid(state, world, pos, EnumFacing.getFront(i)))
				return false;
		}
    	return true;
    }
    
    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos)
    {
    	float bonus = 0F;
    	TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
		{
    		for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			bonus += tile.getEnchantPowerBonus(world, pos) * tile.getPercentVolume();
			}
    	}
    	return bonus;
    }
    
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
    	TileEntityLittleTiles te = loadTe(worldIn, pos);
		if(te != null)
		{
    		if(worldIn.isRemote)
    		{
    			//System.out.println("Update Neighbor Changed");
    			te.onNeighBorChangedClient();
    		}else{
    			PacketHandler.sendPacketToNearPlayers(worldIn, new LittleNeighborUpdatePacket(pos, fromPos), 100, pos);
	    		/*for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
	    			tile.onNeighborChangeOutside();;
				}*/
    		}
    	}
    }
    
    private static boolean loadingTileEntityFromWorld = false;
	
    @Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
    	if(loadingTileEntityFromWorld)
    		return ;
    	TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null)
		{
			if(te.getWorld().isRemote)
    			te.onNeighBorChangedClient();
    		else{
				/*for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
	    			tile.onNeighborChangeOutside();
				}*/
    		}
    	}
	}
    
    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
    	TileEntityLittleTiles te = loadTe(worldIn, pos);
		if(te != null)
		{
			RayTraceResult moving = null;
			for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				for (int i = 0; i < tile.boundingBoxes.size(); i++) {
					RayTraceResult tempMoving = tile.boundingBoxes.get(i).getBox().offset(pos).calculateIntercept(start, end);
	    			
	    			if(tempMoving != null)
	    			{
	    				if(moving == null || moving.hitVec.distanceTo(start) > tempMoving.hitVec.distanceTo(start))
	    					moving = tempMoving;
	    			}
				}
				
			}
			
			if(moving != null)
				return new RayTraceResult(moving.hitVec, moving.sideHit, pos);
    	}
    	return null;
    }
    
    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
    	TileEntityLittleTiles te = loadTe(worldIn, pos);
    	if(te != null && te.shouldCheckForCollision())
    	{
    		for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
    			LittleTile tile = (LittleTile) iterator.next();
    			if(tile.shouldCheckForCollision())
    			{
    				for (int i = 0; i < tile.boundingBoxes.size(); i++) {
						if(tile.boundingBoxes.get(i).getBox().offset(pos).intersectsWith(entityIn.getEntityBoundingBox()))
							tile.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
					}
    			}
    		}
    	}
    }
    
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLittleTiles();
	}
	
	/*public static TileEntity getTileEntityInWorld(IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles && ((TileEntityLittleTiles) tileEntity).loadedTile instanceof LittleTileTileEntity)
		{
			return ((LittleTileTileEntity)((TileEntityLittleTiles) tileEntity).loadedTile).tileEntity;
		}
		return tileEntity;
	}
	
	public static LittleTile getLittleTileInWorld(IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			return ((TileEntityLittleTiles) tileEntity).loadedTile;
		}
		return null;
	}*/
	
	@SideOnly(Side.CLIENT)
	private static TileEntityLittleTiles checkforTileEntity(World world, EnumFacing facing, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
		if(tileEntity instanceof TileEntityLittleTiles)
			return (TileEntityLittleTiles) tileEntity;
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	private static boolean checkforNeighbor(World world, EnumFacing facing, BlockPos pos)
	{
		BlockPos newPos = pos.offset(facing);
		IBlockState state = world.getBlockState(newPos);
		return !state.doesSideBlockRendering(world, newPos, facing.getOpposite());
	}
	
	@SideOnly(Side.CLIENT)
	private static void updateRenderer(TileEntityLittleTiles tileEntity, EnumFacing facing, HashMap<EnumFacing, Boolean> neighbors, HashMap<EnumFacing, TileEntityLittleTiles> neighborsTiles, RenderCubeObject cube, LittleTileBox box)
	{
		Boolean shouldRender = neighbors.get(facing);
		if(shouldRender == null)
		{
			shouldRender = checkforNeighbor(tileEntity.getWorld(), facing, tileEntity.getPos());
			neighbors.put(facing, shouldRender);
		}
		
		if(shouldRender == Boolean.TRUE)
		{
			TileEntityLittleTiles otherTile = null;
			if(!neighborsTiles.containsKey(facing))
			{
				otherTile = checkforTileEntity(tileEntity.getWorld(), facing, tileEntity.getPos());
				neighborsTiles.put(facing, otherTile);
			}else
				otherTile = neighborsTiles.get(facing);
			if(otherTile != null)
				shouldRender = otherTile.shouldSideBeRendered(facing.getOpposite(), box.createInsideBlockBox(facing), (LittleTile) cube.customData);
		}
		cube.setSideRender(facing, shouldRender ? EnumSideRender.OUTSIDE_RENDERED : EnumSideRender.OUTSIDE_NOT_RENDERD);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if(te instanceof TileEntityLittleTiles)
		{
			//((TileEntityLittleTiles) te).updateQuadCache();			
			return Collections.emptyList();
		}
		return getRenderingCubes(state, te, stack, MinecraftForgeClient.getRenderLayer());
	}
	
	@SideOnly(Side.CLIENT)
	public static ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack, BlockRenderLayer layer) {
		ArrayList<RenderCubeObject> cubes = new ArrayList<>();
		if(te instanceof TileEntityLittleTiles)
		{
			
			HashMap<EnumFacing, Boolean> neighbors = new HashMap<>();
			HashMap<EnumFacing, TileEntityLittleTiles> neighborsTiles = new HashMap<>();
			
			TileEntityLittleTiles tileEntity = (TileEntityLittleTiles) te;
			
			RenderCubeLayerCache cache = tileEntity.getCubeCache();
			ArrayList<RenderCubeObject> cachedCubes = cache.getCubesByLayer(layer);
			if(cachedCubes != null)
			{
				if(tileEntity.hasNeighborChanged)
				{
					for (BlockRenderLayer tempLayer : BlockRenderLayer.values()) {
						List<RenderCubeObject> renderCubes = cache.getCubesByLayer(tempLayer);
						if(renderCubes == null)
							continue;
						for (int i = 0; i < renderCubes.size(); i++) {
							RenderCubeObject cube = renderCubes.get(i);
							for (int k = 0; k < EnumFacing.VALUES.length; k++) {
								EnumFacing facing = EnumFacing.VALUES[k];
								if(cube.getSidedRendererType(facing).outside)
								{
									LittleTileBox box = new LittleTileBox(cube).getSideOfBox(facing);
									
									boolean shouldRenderBefore = cube.shouldSideBeRendered(facing);
									updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, box);
									if(cube.shouldSideBeRendered(facing))
									{
										if(!shouldRenderBefore)
											cube.doesNeedQuadUpdate = true;
									}else
										cube.setQuad(facing, null);		
								}
							}
						}
					}
					
					tileEntity.hasNeighborChanged = false;
				}
				
				return cachedCubes;
			}
			/*HashMap<BlockRenderLayer, List<RenderCubeObject>> cached = tileEntity.cachedCubes;
			if(cached != null)
			{
				List<RenderCubeObject> cachedCubes = cached.get(layer);
				if(cachedCubes != null)
				{
					if(tileEntity.hasNeighborChanged)
					{
						for (List<RenderCubeObject> renderCubes : cached.values()) {
							for (int i = 0; i < renderCubes.size(); i++) {
								RenderCubeObject cube = renderCubes.get(i);
								for (int k = 0; k < EnumFacing.VALUES.length; k++) {
									EnumFacing facing = EnumFacing.VALUES[k];
									if(cube.getSidedRendererType(facing).outside)
									{
										LittleTileBox box = new LittleTileBox(cube).getSideOfBox(facing);
										
										updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, box);
									}
								}
							}
						}
						
						tileEntity.hasNeighborChanged = false;
					}
					
					return cachedCubes;
				}
			}*/
			
			for (Iterator iterator = tileEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				if(tile.shouldBeRenderedInLayer(layer))
				{
					//Check for sides which does not need to be rendered
					ArrayList<RenderCubeObject> tileCubes = tile.getRenderingCubes();
					boolean canUseBoundingBoxes = tile.boundingBoxes.size() == tileCubes.size();
					for (int j = 0; j < tileCubes.size(); j++) {
						RenderCubeObject cube = tileCubes.get(j);
						for (int k = 0; k < EnumFacing.VALUES.length; k++) {
							EnumFacing facing = EnumFacing.VALUES[k];
							LittleTileBox box = null;
							if(!canUseBoundingBoxes)
								box = new LittleTileBox(cube).getSideOfBox(facing);
							else
								box = tile.boundingBoxes.get(j).copy().getSideOfBox(facing);
							
							//cubes.add(new RenderCubeObject(box.getCube(), Blocks.STONE, 0));
							cube.customData = tile;
							
							if(box.isBoxInsideBlock())
								cube.setSideRender(facing, ((TileEntityLittleTiles) te).shouldSideBeRendered(facing, box, tile) ? EnumSideRender.INSIDE_RENDERED : EnumSideRender.INSIDE_NOT_RENDERED);
							else{
								updateRenderer(tileEntity, facing, neighbors, neighborsTiles, cube, box);
							}
							
						}
					}
					cubes.addAll(tileCubes);
				}
			}
			
			cache.setCubesByLayer(cubes, layer);
			
			//return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
			
		}else if(stack != null){
			return ItemBlockTiles.getItemRenderingCubes(stack);
		}
		return cubes;
	}
	
	/*@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded)
	{
		if(te instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles littleTe = (TileEntityLittleTiles) te;
			if(threaded && littleTe.cachedQuads == null)
			{
				if(!littleTe.isRendering)
					RenderingThread.addCoordToUpdate(littleTe, state);
				littleTe.isRendering = true;
				return new ArrayList<>();
			}else{
				if(littleTe.cachedQuads == null)
					return null;
				HashMap<EnumFacing, List<BakedQuad>> quads = littleTe.cachedQuads.get(layer);
				if(quads != null)
				{
					if(layer == BlockRenderLayer.SOLID)
						littleTe.updateQuadCache();
					return quads.get(facing);
				}
			}
		}
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded)
	{
		if(te instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles littleTe = (TileEntityLittleTiles) te;
			if(littleTe.cachedQuads == null)
				littleTe.cachedQuads = new HashMap<>();
			HashMap<EnumFacing, List<BakedQuad>> quads = littleTe.cachedQuads.get(layer);
			if(quads == null)
			{
				quads = new HashMap<>();
				littleTe.cachedQuads.put(layer, quads);
			}
			
			quads.put(facing, cachedQuads);
				
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public QuadCache[] getCustomCachedQuads(BlockRenderLayer layer, EnumFacing facing, TileEntity te, ItemStack stack) {
		if(te instanceof TileEntityLittleTiles)
			return ((TileEntityLittleTiles) te).getQuadCache(layer, facing);
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedQuads(QuadCache[] quads, BlockRenderLayer layer, EnumFacing facing, TileEntity te, ItemStack stack) {
		if(te instanceof TileEntityLittleTiles)
			((TileEntityLittleTiles) te).setQuadCache(quads, layer, facing);
	}*/
	
	@Override
	public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return false;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
    	TileEntityLittleTiles te = loadTe(world, pos);
		if(te != null && !world.isRemote)
    	{
    		float size = ReflectionHelper.getPrivateValue(Explosion.class, explosion, "explosionSize", "field_77280_f");
    		Vec3d center = explosion.getPosition();
    		ArrayList<LittleTile> removeTiles = new ArrayList<>();
    		for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				if(tile.boundingBoxes.size() > 0 && !tile.isStructureBlock)
				{
					LittleTileVec vec = tile.boundingBoxes.get(0).getCenter();
					Vec3d newVec = new Vec3d(pos);//.addVector(0.5, 0.5, 0.5);
					newVec = newVec.addVector(vec.getPosX(), vec.getPosY(), vec.getPosZ());
					
					int explosionStrength = (int) ((50D/center.distanceTo(newVec))*size);
					double random = Math.random()*explosionStrength;
					if(random > tile.getExplosionResistance())
					{
						//System.out.println("strength="+explosionStrength+",random="+random+",resistance=" +tile.getExplosionResistance());
						
						removeTiles.add(tile);
					}
				}
			}
    		for (int i = 0; i < removeTiles.size(); i++) {
    			removeTiles.get(i).onTileExplodes(explosion);
    			removeTiles.get(i).destroy();
    			//te.removeTile(removeTiles.get(i));
			}
    		te.update();
    	}
        /*world.setBlockToAir(pos);
        onBlockDestroyedByExplosion(world, pos, explosion);*/
    }

}
