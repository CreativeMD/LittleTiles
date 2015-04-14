package com.creativemd.littletiles.common.items;

import java.util.ArrayList;

import scala.collection.parallel.ParIterableLike.Min;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTileVec;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
	
	public static LittleTile getLittleTile(ItemStack stack)
	{
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
		Vec3 center = helper.getCenterPos();
		LittleTileVec size = helper.getRelativeSize();
		
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
        else if (y == 255 && helper.tile.block.getMaterial().isSolid())
        {
            return false;
        }
        else// if (world.canPlaceEntityOnSide(helper.tile.block, x, y, z, false, side, player, stack) || helper.canBePlacedInside(x, y, z))
        {
            int i1 = this.getMetadata(stack.getItemDamage());
            int j1 = helper.tile.block.onBlockPlaced(world, x, y, z, side, offsetX, offsetY, offsetZ, i1);

            placeBlockAt(stack, world, center, size, helper, j1, x, y, z);

            return true;
        }
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
        return true;
    }
	
	public boolean placeBlockAt(ItemStack stack, World world, Vec3 center, LittleTileVec size, PlacementHelper helper, int meta, int x, int y, int z)
    {
		helper.tile.meta = meta;
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			PacketHandler.sendPacketToServer(new LittlePlacePacket(stack, center, size, meta, x, y, z));
		
		/*if(x < 0)
			center.xCoord -= 1;
		if(z < 0)
			center.zCoord -= 1;*/
		
		/*byte centerX = (byte) (Math.floor((Math.abs(center.xCoord) - Math.abs((int)center.xCoord))*16D) - 8);
		byte centerY = (byte) (Math.floor((Math.abs(center.yCoord) - Math.abs((int)center.yCoord))*16D) - 8);
		byte centerZ = (byte) (Math.floor((Math.abs(center.zCoord) - Math.abs((int)center.zCoord))*16D) - 8);*/
		byte centerX = (byte) (Math.floor((center.xCoord - (int)center.xCoord)*16D) - 8);
		if(center.xCoord < 0)
		{
			double temp = -center.xCoord + (int)(center.xCoord);
			centerX = (byte) (Math.floor((1D-temp)*16D) - 8);
		}
		byte centerY = (byte) (Math.floor((center.yCoord - (int)center.yCoord)*16D) - 8);
		byte centerZ = (byte) (Math.floor((center.zCoord - (int)center.zCoord)*16D) - 8);
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
			
			ArrayList<LittleTile> splittedTiles = new ArrayList<LittleTile>();
			if(helper.tile.PlaceLittleTile(stack, littleEntity, centerX, centerY, centerZ, size.sizeX, size.sizeY, size.sizeZ, splittedTiles))
			{
				helper.tile.block.onBlockPlacedBy(world, x, y, z, helper.player, stack);
				helper.tile.block.onPostBlockPlaced(world, x, y, z, helper.tile.meta);
				//TODO drop splitted parts
				if(!world.isRemote)
				{
					world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), helper.tile.block.stepSound.func_150496_b(), (helper.tile.block.stepSound.getVolume() + 1.0F) / 2.0F, helper.tile.block.stepSound.getPitch() * 0.8F);
	                --stack.stackSize; 
				}
			}else{
				return false;
			}
		}

       return true;
    }
}
