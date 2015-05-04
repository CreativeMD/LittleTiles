package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import scala.collection.parallel.ParIterableLike.Min;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileSize;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemBlockTiles extends ItemBlock{

	public ItemBlockTiles(Block block) {
		super(block);
		hasSubtypes = true;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack)
    {
		String result = super.getItemStackDisplayName(stack);
		LittleTile tile = getLittleTile(stack);
		if(tile != null)
		{
			result += " (" + tile.size.sizeX + "x" + tile.size.sizeY + "x" + tile.size.sizeZ + ")";
		}
		return result;
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
    {
		LittleTile tile = getLittleTile(stack);
		if(tile != null)
		{
			return tile.block.getUnlocalizedName();
		}
        return super.getUnlocalizedName(stack);
    }
	
	public static LittleTile getLittleTile(ItemStack stack)
	{
		if(stack == null || stack.stackTagCompound == null)
			return null;
		return LittleTile.CreateandLoadTile(stack.stackTagCompound);
	}
	
	public static void saveLittleTile(ItemStack stack, LittleTile tile)
	{
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		tile.save(stack.stackTagCompound);
	}
	
	@SideOnly(Side.CLIENT)
	public static PlacementHelper createPlacementHelper()
	{
		return new PlacementHelper(Minecraft.getMinecraft().objectMouseOver, Minecraft.getMinecraft().thePlayer);
	}
	
	public boolean canPlaceInside(Vec3 hit, int side)
	{
		switch(side)
		{
		case 0: 
		case 1:
			return (int)hit.yCoord != hit.yCoord;
		case 2:
		case 3:
			return (int)hit.zCoord != hit.zCoord;
		case 4:
		case 5:
			return (int)hit.xCoord != hit.xCoord;
		default: return false;
		}
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float offsetX, float offsetY, float offsetZ)
    {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			return false;
		PlacementHelper helper = createPlacementHelper();
		
		LittleTileSize size = helper.size;
		Vec3 center = helper.getCenterPos(size);
		
		x = helper.moving.blockX;
		y = helper.moving.blockY;
		z = helper.moving.blockZ;
		
		side = helper.moving.sideHit;
		
		if(!helper.canBePlacedInside() || !canPlaceInside(helper.moving.hitVec, side))
		{
			if (side == 0)
	        {
	            --y;
	        }
	
	        if (side == 1)
	        {
	            ++y;
	        }
	
	        if (side == 2)
	        {
	            --z;
	        }
	
	        if (side == 3)
	        {
	            ++z;
	        }
	
	        if (side == 4)
	        {
	            --x;
	        }
	
	        if (side == 5)
	        {
	            ++x;
	        }
		}
		
		if (stack.stackSize == 0)
        {
            return false;
        }
        else if (!player.canPlayerEdit(x, y, z, side, stack))
        {
            return false;
        }
        else if (y == 255)
        {
            return false;
        }
        else// if (world.canPlaceEntityOnSide(helper.tile.block, x, y, z, false, side, player, stack) || helper.canBePlacedInside(x, y, z))
        {
            placeBlockAt(stack, world, center, size, helper, x, y, z, offsetX, offsetY, offsetZ);

            return true;
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconregister)
    {
        
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item stack, CreativeTabs tab, List list)
    {
        
    }

	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack)
    {
		Block block = world.getBlock(x, y, z);
		
		PlacementHelper helper = createPlacementHelper();
		
		x = helper.moving.blockX;
		y = helper.moving.blockY;
		z = helper.moving.blockZ;
		
        if (block == Blocks.snow_layer)
        {
            side = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z) && !helper.canBePlacedInside())
        {
            if (side == 0)
            {
                --y;
            }

            if (side == 1)
            {
                ++y;
            }

            if (side == 2)
            {
                --z;
            }

            if (side == 3)
            {
                ++z;
            }

            if (side == 4)
            {
                --x;
            }

            if (side == 5)
            {
                ++x;
            }
        }
        block = world.getBlock(x, y, z);
        helper.posX = x;
        helper.posY = y;
        helper.posZ = z;
        return block.isReplaceable(world, x, y, z) || helper.canBePlacedInside();
    }
	
	public boolean placeBlockAt(ItemStack stack, World world, Vec3 center, LittleTileSize size, PlacementHelper helper, int x, int y, int z, float offsetX, float offsetY, float offsetZ)
    {		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			PacketHandler.sendPacketToServer(new LittlePlacePacket(stack, center, size, x, y, z, offsetX, offsetY, offsetZ, helper.side));
		
		/*if(x < 0)
			center.xCoord -= 1;
		if(z < 0)
			center.zCoord -= 1;*/
		
		/*byte centerX = (byte) (Math.floor((Math.abs(center.xCoord) - Math.abs((int)center.xCoord))*16D) - 8);
		byte centerY = (byte) (Math.floor((Math.abs(center.yCoord) - Math.abs((int)center.yCoord))*16D) - 8);
		byte centerZ = (byte) (Math.floor((Math.abs(center.zCoord) - Math.abs((int)center.zCoord))*16D) - 8);*/
		byte centerX = (byte) (Math.floor((center.xCoord - (int)x)*16D) - 8);
		if(center.xCoord < 0)
		{
			double temp = -center.xCoord + (int)(center.xCoord);
			centerX = (byte) (Math.floor((1D-temp)*16D) - 8);
		}
		byte centerY = (byte) (Math.floor((center.yCoord - (int)y)*16D) - 8);
		byte centerZ = (byte) (Math.floor((center.zCoord - (int)z)*16D) - 8);
		if(center.zCoord < 0)
		{
			double temp = -center.zCoord + (int)(center.zCoord);
			centerZ = (byte) (Math.floor((1D-temp)*16D) - 8);
		}
		
		if(!(world.getBlock(x, y, z) instanceof BlockTile))
			if (!world.setBlock(x, y, z, LittleTiles.blockTile, 0, 3))
				return false;
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles littleEntity = (TileEntityLittleTiles) tileEntity;
		
			for (int i = 0; i < helper.tiles.size(); i++) {
				Vec3 offset = helper.getOffset(i, size);
				if(!helper.tiles.get(i).canPlaceBlock(littleEntity, centerX, centerY, centerZ, helper.tiles.get(i).size.sizeX, helper.tiles.get(i).size.sizeY, helper.tiles.get(i).size.sizeZ, (byte)Math.floor(offset.xCoord*16), (byte)Math.floor(offset.yCoord*16), (byte)Math.floor(offset.zCoord*16)))
				{
					if(littleEntity.tiles.size() == 0)
						world.setBlockToAir(x, y, z);
					return false;
				}
			}
				
			ArrayList<LittleTile> splittedTiles = new ArrayList<LittleTile>();
			LittleTile tile = null;
			for (int i = 0; i < helper.tiles.size(); i++) {
				tile = helper.tiles.get(i);
				Vec3 offset = helper.getOffset(i, size);
				
				byte offX = (byte)Math.floor(offset.xCoord*16);
				byte offY = (byte)Math.floor(offset.yCoord*16);
				byte offZ = (byte)Math.floor(offset.zCoord*16);
				
				if(tile.PlaceLittleTile(littleEntity, centerX, centerY, centerZ, tile.size.sizeX, tile.size.sizeY, tile.size.sizeZ, splittedTiles, offX, offY, offZ))
				{
					if(helper.isSingle())
			    	{
			            int i1 = tile.meta;
			            int j1 = tile.block.onBlockPlaced(world, x, y, z, helper.side, offsetX, offsetY, offsetZ, i1);
			            tile.meta = j1;
			    	}
					
					tile.block.onBlockPlacedBy(world, x, y, z, helper.player, stack);
					tile.block.onPostBlockPlaced(world, x, y, z, tile.meta);
				}else{
					return false;
				}
			}
			if(!helper.player.capabilities.isCreativeMode)
			{
				helper.player.inventory.mainInventory[helper.player.inventory.currentItem].stackSize--;
				if(helper.player.inventory.mainInventory[helper.player.inventory.currentItem].stackSize == 0)
					helper.player.inventory.mainInventory[helper.player.inventory.currentItem] = null;
			}
			if(!world.isRemote)
			{
				for (int j = 0; j < splittedTiles.size(); j++) {
					WorldUtils.dropItem(world, splittedTiles.get(j).getItemStack(), x, y, z);
				}
				world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), tile.block.stepSound.func_150496_b(), (tile.block.stepSound.getVolume() + 1.0F) / 2.0F, tile.block.stepSound.getPitch() * 0.8F);
			}
		}

       return true;
    }
}
