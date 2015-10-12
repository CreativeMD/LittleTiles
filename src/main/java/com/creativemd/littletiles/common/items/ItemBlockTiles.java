package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import scala.collection.parallel.ParIterableLike.Min;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.PlacementHelper.PreviewTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
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
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;

public class ItemBlockTiles extends ItemBlock implements ILittleTile, ITilesRenderer{

	public ItemBlockTiles(Block block) {
		super(block);
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack)
    {
		String result = super.getItemStackDisplayName(stack);
		//LittleTile tile = getLittleTile(Minecraft.getMinecraft().theWorld, stack);
		if(stack.stackTagCompound != null)
		{
			result += " (x=" + stack.stackTagCompound.getByte("sizeX") + ",y=" + stack.stackTagCompound.getByte("sizeY") + "z=" + stack.stackTagCompound.getByte("sizeZ") + ")";
		}
		return result;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName(ItemStack stack)
    {
		if(stack.stackTagCompound != null)
		{
			Block block = Block.getBlockFromName(stack.stackTagCompound.getString("block"));
			return block.getUnlocalizedName();
		}
        return super.getUnlocalizedName(stack);
    }
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float offsetX, float offsetY, float offsetZ)
    {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			return false;
		
		PlacementHelper helper = PlacementHelper.getInstance(player);
		
		MovingObjectPosition moving = Minecraft.getMinecraft().objectMouseOver;
		
		//LittleTileSize size = helper.size;
		//size.rotateSize(PreviewRenderer.direction);
		//size.rotateSize(PreviewRenderer.direction2);
		//Vec3 center = helper.getCenterPos(size);
		
		x = moving.blockX;
		y = moving.blockY;
		z = moving.blockZ;
		
		side = moving.sideHit;
		
		if(!helper.canBePlacedInside(x, y, z, moving.hitVec, ForgeDirection.getOrientation(side)))
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
        	if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    			PacketHandler.sendPacketToServer(new LittlePlacePacket(stack, player.getPosition(1.0F), moving.hitVec, x, y, z, side, RotationUtils.getIndex(PreviewRenderer.direction), RotationUtils.getIndex(PreviewRenderer.direction2)));
    		
            placeBlockAt(player, stack, world, player.getPosition(1.0F), moving.hitVec, helper, x, y, z, side, PreviewRenderer.direction, PreviewRenderer.direction2);

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
		
		Minecraft mc = Minecraft.getMinecraft();
		
		x = mc.objectMouseOver.blockX;
		y = mc.objectMouseOver.blockY;
		z = mc.objectMouseOver.blockZ;
		
        if (block == Blocks.snow_layer)
        {
            side = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z) && !PlacementHelper.getInstance(player).canBePlacedInside(x, y, z, mc.objectMouseOver.hitVec, ForgeDirection.getOrientation(side)))
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
        //helper.posX = x;
        //helper.posY = y;
        //helper.posZ = z;
        return block.isReplaceable(world, x, y, z) || PlacementHelper.getInstance(player).canBePlacedInsideBlock(x, y, z);
    }
	
	public boolean placeBlockAt(EntityPlayer player, ItemStack stack, World world, Vec3 playerPos, Vec3 hitVec, PlacementHelper helper, int x, int y, int z, int side, ForgeDirection direction, ForgeDirection direction2)
    {
		ArrayList<PreviewTile> previews = helper.getPreviewTiles(stack, x, y, z, playerPos, hitVec, ForgeDirection.getOrientation(side), direction, direction2);
		
		if(!(world.getBlock(x, y, z) instanceof BlockTile))
			if (!world.setBlock(x, y, z, LittleTiles.blockTile, 0, 3))
				return false;
		
		HashMapList<ChunkCoordinates, PreviewTile> splitted = new HashMapList<ChunkCoordinates, PreviewTile>();
		for (int i = 0; i < previews.size(); i++) {
			if(!previews.get(i).split(splitted, x, y, z))
				return false;
		}
		
		TileEntity mainTile = world.getTileEntity(x, y, z);
		if(mainTile instanceof TileEntityLittleTiles)
		{
			ArrayList<PreviewTile> tiles = splitted.getValues(new ChunkCoordinates(x, y, z));
			if(tiles != null)
			{
				for (int i = 0; i < tiles.size(); i++)
					if(!((TileEntityLittleTiles) mainTile).isSpaceForLittleTile(tiles.get(i).box))
						return false;
			}
		}else
			return false;
		
		ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		
		for (int i = 0; i < splitted.size(); i++) {
			ChunkCoordinates coord = splitted.getKey(i);
			if(!(world.getBlock(coord.posX, coord.posY, coord.posZ) instanceof BlockTile) && world.getBlock(coord.posX, coord.posY, coord.posZ).getMaterial().isReplaceable())
				world.setBlock(coord.posX, coord.posY, coord.posZ, LittleTiles.blockTile, 0, 3);
			
			TileEntity te = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
			if(te instanceof TileEntityLittleTiles)
			{
				TileEntityLittleTiles teLT = (TileEntityLittleTiles) te;
				ArrayList<PreviewTile> placeTiles = splitted.getValues(i);
				for (int j = 0; j < placeTiles.size(); j++) {
					PreviewTile tile = placeTiles.get(j);
					LittleTile LT = tile.preview.getLittleTile(teLT);
					LT.boundingBoxes.clear();
					LT.boundingBoxes.add(tile.box.copy());
					if(teLT.isSpaceForLittleTile(tile.box.copy()))
					{
						LT.place();
						LT.onPlaced(player, stack);
						
					}else
						unplaceableTiles.add(LT);
				}
			}
		}
		
		if(!player.capabilities.isCreativeMode)
		{
			player.inventory.mainInventory[player.inventory.currentItem].stackSize--;
			if(player.inventory.mainInventory[player.inventory.currentItem].stackSize == 0)
				player.inventory.mainInventory[player.inventory.currentItem] = null;
		}
		
		for (int j = 0; j < unplaceableTiles.size(); j++) {
			
			WorldUtils.dropItem(world, unplaceableTiles.get(j).getDrops(), x, y, z);
		}
		
		/*TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			for (int i = 0; i < previews.size(); i++) {
				PreviewTile tile = previews.get(i);
				
			}
		}*/

       return true;
    }

	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<LittleTilePreview>();
		previews.add(LittleTilePreview.getPreviewFromNBT(stack.stackTagCompound));
		return previews;
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack) {
		ArrayList<CubeObject> cubes = new ArrayList<CubeObject>();
		Block block = Block.getBlockFromName(stack.stackTagCompound.getString("block"));
		int meta = stack.stackTagCompound.getInteger("meta");
		LittleTileSize size = new LittleTileSize("size", stack.stackTagCompound);
		if(!(block instanceof BlockAir))
		{
			CubeObject cube = new LittleTileBox(new LittleTileVec(8, 8, 8), size).getCube();
			cube.block = block;
			cube.meta = meta;
			cubes.add(cube);
		}
		return cubes;
	}

	@Override
	public boolean hasBackground(ItemStack stack) {
		return false;
	}
}
