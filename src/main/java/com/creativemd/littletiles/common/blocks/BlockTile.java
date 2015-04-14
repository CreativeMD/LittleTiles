package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.packet.LittleDestroyPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
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
		if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(player))
		{
			return tempEntity.loadedTile.block.getPlayerRelativeBlockHardness(player, world, x, y, z);
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
			if(tempEntity.loadedTile == null)
				System.out.println("Failed");
			else
				return tempEntity.loadedTile.getCoordBox(x, y, z);
		}
		return AxisAlignedBB.getBoundingBox(x, y, z, x, y, z);
    }
	
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axis, List list, Entity entity)
    {
		if(loadTileEntity(world, x, y, z))
		{
			for (int i = 0; i < tempEntity.tiles.size(); i++) {
				AxisAlignedBB box = tempEntity.tiles.get(i).getCoordBox(x, y, z);
				if(axis.intersectsWith(box))
					list.add(box);
			}
		}
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
			for (int i = 0; i < tempEntity.tiles.size(); i++) {
				tempEntity.tiles.get(i).block.randomDisplayTick(world, x, y, z, random);
			}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ)
    {
		if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(player))
			return tempEntity.loadedTile.block.onBlockActivated(world, x, y, z, player, side, moveX, moveY, moveZ);
        return false;
    }
	
	/* TODO Add Power
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
    
	@SideOnly(Side.CLIENT)
	public boolean first;
	
	@Override
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
		if(first)
			return 0;
		int light = 0;
    	if(loadTileEntity(world, x, y, z))
    	{
    		for (int i = 0; i < tempEntity.tiles.size(); i++) {
    			first = true;
				int tempLight = tempEntity.tiles.get(i).block.getLightValue(world, x, y, z);
				first = false;
				if(tempLight == 0)
					tempLight = tempEntity.tiles.get(i).block.getLightValue();
				if(tempLight > light)
					light = tempLight;
			}
    	}
    	return light;
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
    			PacketHandler.sendPacketToServer(new LittleDestroyPacket(x, y, z, nbt));
    			tempEntity.updateRender();
    		}
    		
    	}
        return true;
    }
    
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
    	return removedByPlayer(world, player, x, y, z);
    }
    
    @Override
    /**Blocks will drop before this method is called*/
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
    {
    	return new ArrayList<ItemStack>();
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon overrideIcon;
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
    {
    	if(loadTileEntity(worldObj, target.blockX, target.blockY, target.blockZ) && tempEntity.updateLoadedTile(mc.thePlayer))
    		overrideIcon = tempEntity.loadedTile.block.getIcon(worldObj, target.blockX, target.blockY, target.blockZ, target.sideHit);
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
    {
    	if(loadTileEntity(world, x, y, z) && tempEntity.updateLoadedTile(mc.thePlayer))
    		overrideIcon = tempEntity.loadedTile.block.getIcon(world, x, y, z, 0);
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	if(overrideIcon != null)
    	{
    		IIcon temp = overrideIcon;
    		overrideIcon = null;
    		return temp;
    	}
    	else
    		return Blocks.stone.getBlockTextureFromSide(0); //mc.getTextureMapBlocks().getAtlasSprite("MISSING");
    }
    
    /*TODO Add once it's important
    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable)
    {
    	
    }*/
    
    /*TODO Add this once the core mechanic is working
    public int getLightOpacity(IBlockAccess world, int x, int y, int z)
    {
    	
    }*/
    
    /*TODO Check it before prerelease
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
    		for (int i = 0; i < tempEntity.tiles.size(); i++) {
    			bonus += tempEntity.tiles.get(i).block.getEnchantPowerBonus(world, x, y, z) * tempEntity.tiles.get(i).size.getPercentVolume();
			}
    	}
    	return bonus;
    }
    
    /*TODO add it before prereleas3
    public void onEntityCollidedWithBlock(World p_149670_1_, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity p_149670_5_) {}
    
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
    {
    	
    }
    
    public void onBlockDestroyedByPlayer(World p_149664_1_, int p_149664_2_, int p_149664_3_, int p_149664_4_, int p_149664_5_) {}
	
	public void onNeighborBlockChange(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_, Block p_149695_5_) {}*/
    
    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 vec1, Vec3 vec2)
    {
    	if(loadTileEntity(world, x, y, z))
    	{
    		for (int i = 0; i < tempEntity.tiles.size(); i++) {
    			MovingObjectPosition moving = tempEntity.tiles.get(i).getCoordBox(x, y, z).calculateIntercept(vec1, vec2);
    			
    			if(moving != null)
    			{
    				moving.blockX = x;
        			moving.blockY = y;
        			moving.blockZ = z;
    				return moving;
    			}
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
