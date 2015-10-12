package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;

public class BlockTile extends BlockContainer{

	public BlockTile(Material material) {
		super(material);
		setCreativeTab(CreativeTabs.tabDecorations);
	}
	
	@SideOnly(Side.CLIENT)
	public static Minecraft mc;
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderAsNormalBlock()
    {
        return false;
    }
	
	@Override
	public boolean isOpaqueCube()
    {
        return false;
    }

	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
    {
        return LittleTilesClient.modelID;
    }
	
	@Override
	public boolean isNormalCube()
    {
        return false;
    }
	
	@Override
	public int getMobilityFlag()
    {
		return 2;
    }
	
	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z)
    {
		if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(player) && tempEntity.loadedTile instanceof LittleTileBlock)
		{
			return ((LittleTileBlock)tempEntity.loadedTile).block.getPlayerRelativeBlockHardness(player, world, x, y, z);
		}
        return super.getBlockHardness(world, x, y, z);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
    {
		return true;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
    {
		if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(mc.thePlayer))
		{
			try{ //Why try? because the number of tiles can change while this method is called
				return tempEntity.loadedTile.getSelectedBox().getOffsetBoundingBox(x, y, z);
			}catch(Exception e){
				
			}
		}
		return AxisAlignedBB.getBoundingBox(x, y, z, x, y, z);
    }
	
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axis, List list, Entity entity)
    {
		if(loadTileEntity(world, x, y, z))
		{
			for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				for (int i = 0; i < tile.boundingBoxes.size(); i++) {
					AxisAlignedBB box = tile.boundingBoxes.get(i).getBox().getOffsetBoundingBox(x, y, z);
					if(axis.intersectsWith(box))
						list.add(box);
				}
				
			}
		}
    }
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		setBlockBounds(0, 0, 0, 0, 0, 0);
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
		if(loadTileEntity(world, x, y, z) && tempEntity.tiles.size() == 0)
			super.breakBlock(world, x, y, z, block, meta);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		if(loadTileEntity(world, x, y, z))
			for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				tile.randomDisplayTick(world, x, y, z, random);
			}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ)
    {
		if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(player))
		{
			if(tempEntity.loadedTile.onBlockActivated(world, x, y, z, player, side, moveX, moveY, moveZ))
			{
				PacketHandler.sendPacketToServer(new LittleBlockPacket(x, y, z, player, 0));
			}
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
    public void getSubBlocks(Item item, CreativeTabs tab, List items)
    {
		/*ItemStack stack = new ItemStack(LittleTiles.blockTile);
		LittleTile tile = new LittleTile(Blocks.stone, 0, new LittleTileVec(1, 1, 1));
		for (byte x = 1; x <= 16; x++)
			for (byte y = 1; y <= 16; y++)
				for (byte z = 1; z <= 16; z++)
				{
					tile.size = new LittleTileVec(x, y, z);
					ItemStack newStack = stack.copy();
					newStack.stackTagCompound = new NBTTagCompound();
					tile.save(newStack.stackTagCompound);
					items.add(newStack);
				}*/
    }
	
    //TODO Add this once it's important
	//public void fillWithRain(World p_149639_1_, int p_149639_2_, int p_149639_3_, int p_149639_4_) {}
    
	public boolean first = true;
	
	@Override
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
		try{ //Why try? because the number of tiles can change while this method is called
			int light = 0;
			if(!first)
				return 0;
	    	if(loadTileEntity(world, x, y, z))
	    	{
	    		for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					first = false;
					int tempLight = tile.getLightValue(world, x, y, z);
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
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
    {
    	//TODO Add before a prerelease
    	 return false;
    }
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    	{
    		if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(player))
    		{
    			tempEntity.tiles.remove(tempEntity.loadedTile);
    			NBTTagCompound nbt = new NBTTagCompound();
    			tempEntity.writeToNBT(nbt);
    			PacketHandler.sendPacketToServer(new LittleBlockPacket(x, y, z, player, 1));
    			tempEntity.updateRender();
    		}
    		
    	}
        return true;
    }
    
	@Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
    	return removedByPlayer(world, player, x, y, z);
    }
    
	@Override
    public boolean isReplaceable(IBlockAccess world, int x, int y, int z)
    {
      	if(loadTileEntity(world, x, y, z))
      		return tempEntity.tiles.size() == 0;
      	return true;
    }
    
    @Override
    /**Blocks will drop before this method is called*/
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
    	ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
    	if(loadTileEntity(world, x, y, z))
    	{
    		for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
				stacks.addAll(tile.getDrops());
			}
    	}
    	return stacks;
    }
    
    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
    {
    	if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(player))
    	{
    		try{
    			return tempEntity.loadedTile.getDrop();
    		}catch(Exception e){
    			
    		}
    	}
    	return null;
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon overrideIcon;
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
    	try{ //Why try? because the loaded tile can change while setting this icon
	    	if(loadTileEntity(worldObj, target.blockX, target.blockY, target.blockZ) && tempEntity.updateLoadedTile(mc.thePlayer))
	    		overrideIcon = tempEntity.loadedTile.getIcon(target.sideHit);
    	}catch(Exception e){
    		
    	}
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
    	try{ //Why try? because the loaded tile can change while setting this icon
	    	if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(mc.thePlayer))
	    	{
	    		//overrideIcon = tempEntity.loadedTile.block.getIcon(world, x, y, z, 0);
	    		AxisAlignedBB box = tempEntity.loadedTile.getSelectedBox();
	    		byte b0 = 1;

	            for (int i1 = 0; i1 < b0; ++i1)
	            {
	                for (int j1 = 0; j1 < b0; ++j1)
	                {
	                    for (int k1 = 0; k1 < b0; ++k1)
	                    {
	                        double d0 = (double)x + ((double)i1 + box.maxX) / (double)b0;
	                        double d1 = (double)y + ((double)j1 + box.maxY) / (double)b0;
	                        double d2 = (double)z + ((double)k1 + box.maxZ) / (double)b0;
	                        EntityDiggingFX fx = (new EntityDiggingFX(world, d0, d1, d2, 0, 0, 0, this, meta)).applyColourMultiplier(x, y, z);
	                        fx.setParticleIcon(tempEntity.loadedTile.getIcon(0));
	                        effectRenderer.addEffect(fx);
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
    
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	if(overrideIcon != null)
    	{
    		IIcon temp = overrideIcon;
    		//overrideIcon = null;
    		return temp;
    	}
    	else
    		return Blocks.stone.getBlockTextureFromSide(0); //mc.getTextureMapBlocks().getAtlasSprite("MISSING");
    }
    
    /*TODO Add once it's important
    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable)
    {
    	
    }*/
    
    /*
    public int getLightOpacity(IBlockAccess world, int x, int y, int z)
    {
    	
    }*/
    
    /*
   	public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        return RotationHelper.rotateVanillaBlock(this, worldObj, x, y, z, axis);
    }
    
    public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
    {
    	
    }*/
    
    @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
    {
    	for (int i = 0; i < 6; i++) {
			if(!isSideSolid(world, x, y, z, ForgeDirection.getOrientation(i)))
				return false;
		}
    	return true;
    }
    
    @Override
    public float getEnchantPowerBonus(World world, int x, int y, int z)
    {
    	float bonus = 0F;
    	if(loadTileEntity(world, x, y, z))
    	{
    		for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			bonus += tile.getEnchantPowerBonus(world, x, y, z) * tile.getPercentVolume();
			}
    	}
    	return bonus;
    }
    
    /*
    public void onEntityCollidedWithBlock(World p_149670_1_, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity p_149670_5_) {}*/
    
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
    {
    	if(loadTileEntity(world, x, y, z))
    	{
    		for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			tile.onNeighborChangeOutside();;
			}
    	}
    }
	
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(loadTileEntity(world, x, y, z))
    	{
			for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
				LittleTile tile = (LittleTile) iterator.next();
    			tile.onNeighborChangeOutside();
			}
    	}
	}
    
    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 vec1, Vec3 vec2)
    {
    	if(loadTileEntity(world, x, y, z))
    	{
    		try{ //Why try? because the number of tiles can change while this method is called
    			MovingObjectPosition moving = null;
    			for (Iterator iterator = tempEntity.tiles.iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					for (int i = 0; i < tile.boundingBoxes.size(); i++) {
						MovingObjectPosition tempMoving = tile.boundingBoxes.get(i).getBox().getOffsetBoundingBox(x, y, z).calculateIntercept(vec1, vec2);
		    			
		    			if(tempMoving != null)
		    			{
		    				if(moving == null || moving.hitVec.distanceTo(vec1) > tempMoving.hitVec.distanceTo(vec1))
		    					moving = tempMoving;
		    			}
					}
					
				}
    			
    			if(moving != null)
    			{
    				moving.blockX = x;
        			moving.blockY = y;
        			moving.blockZ = z;
        			return moving;
    			}
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
	
	public TileEntityLittleTiles tempEntity;
	
	public boolean loadTileEntity(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
			tempEntity = (TileEntityLittleTiles) tileEntity;
		else
			tempEntity = null;
		return tempEntity != null;
	}
	
	public static TileEntity getTileEntityInWorld(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles && ((TileEntityLittleTiles) tileEntity).loadedTile instanceof LittleTileTileEntity)
		{
			return ((LittleTileTileEntity)((TileEntityLittleTiles) tileEntity).loadedTile).tileEntity;
		}
		return tileEntity;
	}
	
	public static LittleTile getLittleTileInWorld(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			return ((TileEntityLittleTiles) tileEntity).loadedTile;
		}
		return null;
	}

}
