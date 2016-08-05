package com.creativemd.littletiles.common.blocks;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.block.TileEntityState;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTile extends BlockContainer implements ICreativeRendered {

	public BlockTile(Material material) {
		super(material);
		setCreativeTab(CreativeTabs.DECORATIONS);
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
	
	@Override
	public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, Entity player)
    {
		if(loadTileEntity(world, pos))
		{
			for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
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
        if (world instanceof World)
        	return BlockBed.getSafeExitLocation((World)world, pos, 0);
        return null;
    }
	
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
    {
		AxisAlignedBB bb = entity.getEntityBoundingBox();
        int mX = MathHelper.floor_double(bb.minX);
        int mY = MathHelper.floor_double(bb.minY);
        int mZ = MathHelper.floor_double(bb.minZ);
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
                		TileList<LittleTile> tiles = littleTE.getTiles();
                		for (int i = 0; i < tiles.size(); i++) {
                			if(tiles.get(i).isLadder())
                			{
	                			for (int j = 0; j < tiles.get(i).boundingBoxes.size(); j++) {
	                				LittleTileBox box = tiles.get(i).boundingBoxes.get(j).copy();
	                				box.addOffset(new LittleTileVec(x2*16, y2*16, z2*16));
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
		if(loadTileEntity(worldIn, pos) && tempEntity.updateLoadedTile(player) && tempEntity.loadedTile instanceof LittleTileBlock)
		{
			return ((LittleTileBlock)tempEntity.loadedTile).block.getPlayerRelativeBlockHardness(((LittleTileBlock)tempEntity.loadedTile).getBlockState(), player, worldIn, pos);
		}
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
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
		if(loadTileEntity(worldIn, pos) && tempEntity.updateLoadedTile(mc.thePlayer))
		{
			try{ //Why try? because the number of tiles can change while this method is called
				return tempEntity.loadedTile.getSelectedBox().offset(pos);
			}catch(Exception e){
				
			}
		}
		return new AxisAlignedBB(pos);
    }
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> list, @Nullable Entity entityIn)
    {
		if(loadTileEntity(world, pos))
		{
			for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				for (int i = 0; i < tile.boundingBoxes.size(); i++) {
					AxisAlignedBB box = tile.boundingBoxes.get(i).getBox();
					addCollisionBoxToList(pos, entityBox, list, box);
				}
				
			}
		}
    }
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
		if(loadTileEntity(world, pos) && tempEntity.getTiles().size() == 0)
			super.breakBlock(world, pos, state);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		if(loadTileEntity(worldIn, pos))
			for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				tile.randomDisplayTick(stateIn, worldIn, pos, rand);
			}
	}
	
	public static boolean cancelNext = false;
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		if(loadTileEntity(worldIn, pos) && tempEntity.updateLoadedTile(playerIn))
		{
			try
			{
				if(worldIn.isRemote)
					PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, playerIn, 0));
				return tempEntity.loadedTile.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
			}catch(Exception e){
				
			}
		}
		if(cancelNext)
		{
			cancelNext = false;
			return true;
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
    public void getSubBlocks(Item item, CreativeTabs tab, List items) {}
    
	public boolean first = true;
	
	@Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		try{ //Why try? because the number of tiles can change while this method is called
			int light = 0;
			if(!first)
				return 0;
	    	if(loadTileEntity(world, pos))
	    	{
	    		for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					first = false;
					int tempLight = tile.getLightValue(state, world, pos);
					first = true;
					if(tempLight > light)
						light = tempLight;
				}
	    	}
	    	return light;
		}catch(Exception e){
			return 0;
		}
    }
    
	@Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityLittleTiles)
		{
			LittleTileBox box = new LittleTileBox(0, 0, 0, 1, 16, 16);
			box.rotateBox(side.getOpposite());
			return ((TileEntityLittleTiles) te).isBoxFilled(box);
		}
		return false;
    }
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	{
    		if(loadTileEntity(world, pos) && tempEntity.updateLoadedTile(player))
    		{
    			try{
	    			tempEntity.loadedTile.destroy();
	    			NBTTagCompound nbt = new NBTTagCompound();
	    			tempEntity.writeToNBT(nbt);
	    			PacketHandler.sendPacketToServer(new LittleBlockPacket(pos, player, 1));
	    			tempEntity.updateRender();
    			}catch(Exception e){
    				
    			}
    		}
    		
    	}
        return true;
    }
    
	@Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
      	if(loadTileEntity(worldIn, pos))
      		return tempEntity.getTiles().size() == 0;
      	return true;
    }
    
    @Override
    /**Blocks will drop before this method is called*/
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
    	ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
    	if(loadTileEntity(world, pos))
    	{
    		for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				stacks.addAll(tile.getDrops());
			}
    	}
    	return stacks;
    }
    
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
    	if(loadTileEntity(world, pos) && tempEntity.updateLoadedTile(player))
    	{
    		try{
    			ArrayList<ItemStack> drops = tempEntity.loadedTile.getDrops();
    			if(drops.size() > 0)
    				return drops.get(0);
    		}catch(Exception e){
    			
    		}
    	}
    	return null;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState oldstate, World worldObj, RayTraceResult target, net.minecraft.client.particle.ParticleManager manager)
    {
    	try{ //Why try? because the loaded tile can change while setting this icon
	    	if(loadTileEntity(worldObj, target.getBlockPos()) && tempEntity.updateLoadedTile(mc.thePlayer) && tempEntity.loadedTile instanceof LittleTileBlock)
	    	{
	    		IBlockState state = ((LittleTileBlock)tempEntity.loadedTile).getBlockState();
	    		BlockPos pos = target.getBlockPos();
	    		int i = pos.getX();
	            int j = pos.getY();
	            int k = pos.getZ();
	            float f = 0.1F;
	            AxisAlignedBB axisalignedbb = state.getBoundingBox(worldObj, pos);
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
    	}catch(Exception e){
    		
    	}
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager)
    {
    	try{ //Why try? because the loaded tile can change while setting this icon
	    	if(loadTileEntity(world, pos) && tempEntity.updateLoadedTile(mc.thePlayer) && tempEntity.loadedTile instanceof LittleTileBlock)
	    	{
	    		//overrideIcon = tempEntity.loadedTile.block.getIcon(world, x, y, z, 0);
	    		//AxisAlignedBB box = tempEntity.loadedTile.getSelectedBox();
	    		IBlockState state = ((LittleTileBlock)tempEntity.loadedTile).getBlockState();
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
	    }catch(Exception e){
			
		}
        return false;
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
    	if(loadTileEntity(world, pos))
    	{
    		for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			bonus += tile.getEnchantPowerBonus(world, pos) * tile.getPercentVolume();
			}
    	}
    	return bonus;
    }
    
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn)
    {
    	if(loadTileEntity(worldIn, pos))
    	{
    		//tempEntity.markFullRenderUpdate();
    		for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			tile.onNeighborChangeOutside();;
			}
    	}
    }
	
    @Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		if(loadTileEntity(world, pos))
    	{
			//tempEntity.markFullRenderUpdate();
			for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			tile.onNeighborChangeOutside();
			}
    	}
	}
    
    @Override
    @Nullable
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
    	if(loadTileEntity(worldIn, pos))
    	{
    		try{ //Why try? because the number of tiles can change while this method is called
    			RayTraceResult moving = null;
    			for (Iterator iterator = tempEntity.getTiles().iterator(); iterator.hasNext();) {
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
    		}catch(Exception e){
    			return null;
    		}
    	}
    	return null;
    }
    
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityLittleTiles();
	}
	
	public static TileEntityLittleTiles tempEntity;
	
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
	}
	
	public static TileEntity getTileEntityInWorld(IBlockAccess world, BlockPos pos)
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
	}
	
	public boolean checkforNeighbor(TileEntity te, EnumFacing facing)
	{
		return !te.getWorld().getBlockState(te.getPos().offset(facing)).doesSideBlockRendering(te.getWorld(), te.getPos().offset(facing), facing.getOpposite());
	}

	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		ArrayList<RenderCubeObject> cubes = new ArrayList<>();
		if(te instanceof TileEntityLittleTiles)
		{
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
			
			HashMap<EnumFacing, Boolean> neighbors = new HashMap<>();
			
			TileEntityLittleTiles tileEntity = (TileEntityLittleTiles) te;
			for (int i = 0; i < tileEntity.getTiles().size(); i++) {
				LittleTile tile = tileEntity.getTiles().get(i);
				if(tile.shouldBeRenderedInLayer(layer))
				{
					//Check for sides which does not need to be rendered
					ArrayList<RenderCubeObject> tileCubes = tile.getRenderingCubes();
					for (int j = 0; j < tileCubes.size(); j++) {
						RenderCubeObject cube = tileCubes.get(j);
						for (int k = 0; k < EnumFacing.VALUES.length; k++) {
							EnumFacing facing = EnumFacing.VALUES[k];
							LittleTileBox box = new LittleTileBox(cube).getSideOfBox(facing);
							//cubes.add(new RenderCubeObject(box.getCube(), Blocks.STONE, 0));
							if(box.isBoxInsideBlock())
								cube.setSideRender(facing, ((TileEntityLittleTiles) te).shouldSideBeRendered(facing, box, tile));
							else{
								Boolean isSolid = neighbors.get(facing);
								if(isSolid == null)
								{
									isSolid = checkforNeighbor(te, facing);
									neighbors.put(facing, isSolid);
								}
								cube.setSideRender(facing, isSolid);
							}
						}
					}
					cubes.addAll(tileCubes);
				}
			}
			
			
			
			//return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
			
		}else if(stack != null){
			return ItemBlockTiles.getItemRenderingCubes(stack);
		}
		return cubes;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack)
	{
		if(te instanceof TileEntityLittleTiles)
		{
			HashMap<EnumFacing, List<BakedQuad>> quads = ((TileEntityLittleTiles) te).getCachedQuads().get(layer);
			if(quads != null)
				return quads.get(facing);
		}
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack)
	{
		if(te instanceof TileEntityLittleTiles)
		{
			//System.out.println("updated block model for: " + te.getPos());
			HashMap<EnumFacing, List<BakedQuad>> quads = ((TileEntityLittleTiles) te).getCachedQuads().get(layer);
			if(quads == null)
			{
				quads = new HashMap<>();
				((TileEntityLittleTiles) te).getCachedQuads().put(layer, quads);
			}
			
			quads.put(facing, cachedQuads);
				
		}
	}

}
