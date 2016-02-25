package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.Block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
			result += " (x=" + stack.stackTagCompound.getByte("sizex") + ",y=" + stack.stackTagCompound.getByte("sizey") + "z=" + stack.stackTagCompound.getByte("sizez") + ")";
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
    			PacketHandler.sendPacketToServer(new LittlePlacePacket(stack, player.getPosition(1.0F), moving.hitVec, x, y, z, side)); //, RotationUtils.getIndex(PreviewRenderer.direction), RotationUtils.getIndex(PreviewRenderer.direction2)));
    		
            placeBlockAt(player, stack, world, player.getPosition(1.0F), moving.hitVec, helper, x, y, z, side); //, PreviewRenderer.direction, PreviewRenderer.direction2);

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
	
	public static HashMapList<ChunkCoordinates, PreviewTile> getSplittedTiles(ArrayList<PreviewTile> tiles, int x, int y, int z)
	{
		HashMapList<ChunkCoordinates, PreviewTile> splitted = new HashMapList<ChunkCoordinates, PreviewTile>();
		for (int i = 0; i < tiles.size(); i++) {
			if(!tiles.get(i).split(splitted, x, y, z))
				return null;
		}
		return splitted;
	}
	
	public static boolean canPlaceTiles(World world, HashMapList<ChunkCoordinates, PreviewTile> splitted, ArrayList<ChunkCoordinates> coordsToCheck)
	{
		for (int i = 0; i < coordsToCheck.size(); i++) {
			ChunkCoordinates coord = coordsToCheck.get(i);
			TileEntity mainTile = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
			if(mainTile instanceof TileEntityLittleTiles)
			{
				
				ArrayList<PreviewTile> tiles = splitted.getValues(coord);
				if(tiles != null)
				{
					for (int j = 0; j < tiles.size(); j++)
						if(tiles.get(j).needsCollisionTest() && !((TileEntityLittleTiles) mainTile).isSpaceForLittleTile(tiles.get(j).box))
							return false;
				}
			}else if(!(world.getBlock(coord.posX, coord.posY, coord.posZ) instanceof BlockTile) && !world.getBlock(coord.posX, coord.posY, coord.posZ).getMaterial().isReplaceable())
				return false;
		}
		return true;
	}
	
	public static boolean placeTiles(World world, EntityPlayer player, ArrayList<PreviewTile> previews, LittleStructure structure, int x, int y, int z, ItemStack stack, ArrayList<LittleTile> unplaceableTiles)
	{
		//if(!(world.getBlock(x, y, z) instanceof BlockTile))
			//if (!world.setBlock(x, y, z, LittleTiles.blockTile, 0, 3))
				//return false;
		
		HashMapList<ChunkCoordinates, PreviewTile> splitted = getSplittedTiles(previews, x, y, z);
		if(splitted == null)
			return false;
		
		/*int counting = 0;
		for (int i = 0; i < splitted.size(); i++) {
			counting += splitted.getValues(i).size();
		}
		
		System.out.println("Created " + counting + " of " + previews.size() + " tiles");*/
		ArrayList<ChunkCoordinates> coordsToCheck = null;
		if(structure != null)
		{
			coordsToCheck = splitted.getKeys();
		}else{
			coordsToCheck = new ArrayList<>();
			coordsToCheck.add(new ChunkCoordinates(x, y, z));
		}
		ArrayList<SoundType> soundsToBePlayed = new ArrayList<>();
		if(canPlaceTiles(world, splitted, coordsToCheck))
		{
			LittleTilePosition pos = null;
			
			for (int i = 0; i < splitted.size(); i++) {
				ChunkCoordinates coord = splitted.getKey(i);
				ArrayList<PreviewTile> placeTiles = splitted.getValues(i);
				boolean hascollideBlock = false;
				for (int j = 0; j < placeTiles.size(); j++) {
					if(placeTiles.get(j).needsCollisionTest())
					{
						hascollideBlock = true;
						break;
					}
				}
				if(hascollideBlock)
				{
					if(!(world.getBlock(coord.posX, coord.posY, coord.posZ) instanceof BlockTile) && world.getBlock(coord.posX, coord.posY, coord.posZ).getMaterial().isReplaceable())
						world.setBlock(coord.posX, coord.posY, coord.posZ, LittleTiles.blockTile, 0, 3);
					
					TileEntity te = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
					if(te instanceof TileEntityLittleTiles)
					{
						//int tiles = 0;
						TileEntityLittleTiles teLT = (TileEntityLittleTiles) te;
						
						for (int j = 0; j < placeTiles.size(); j++) {
							LittleTile LT = placeTiles.get(j).placeTile(player, stack, teLT, structure, unplaceableTiles);
							if(LT != null)
							{
								if(!soundsToBePlayed.contains(LT.getSound()))
									soundsToBePlayed.add(LT.getSound());
								if(structure != null)
								{
									if(pos == null)
									{
										structure.mainTile = LT;
										LT.isMainBlock = true;
										LT.updateCorner();
										pos = new LittleTilePosition(coord, LT.cornerVec.copy());
									}else
										LT.pos = pos;
								}
							}
						}
						
						if(structure != null)
							teLT.combineTiles(structure);
					}
					//System.out.println("Placed " + tiles + "/" + placeTiles.size());
				}//else
					//System.out.println("Couldn't create te at x=" + coord.posX + ",y=" + coord.posY + ",z=" + coord.posZ);
			}
			for (int i = 0; i < soundsToBePlayed.size(); i++) {
				world.playSoundEffect((double)((float)player.posX), (double)((float)player.posY), (double)((float)player.posZ), soundsToBePlayed.get(i).func_150496_b(), (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
			}
			return true;
		}
		return false;
	}
	
	public boolean placeBlockAt(EntityPlayer player, ItemStack stack, World world, Vec3 playerPos, Vec3 hitVec, PlacementHelper helper, int x, int y, int z, int side) //, ForgeDirection direction, ForgeDirection direction2)
    {
		ArrayList<PreviewTile> previews = helper.getPreviewTiles(stack, x, y, z, playerPos, hitVec, ForgeDirection.getOrientation(side), true); //, direction, direction2);
		
		LittleStructure structure = null;
		if(stack.getItem() instanceof ILittleTile)
		{
			structure = ((ILittleTile)stack.getItem()).getLittleStructure(stack);
		}else if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile){
			structure = ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittleStructure(stack);
		}
		
		if(structure != null)
		{
			structure.dropStack = stack.copy();
			structure.setTiles(new ArrayList<LittleTile>());
		}
		//System.out.println("Creating " + previews.size() + " tiles");
		
		ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		if(placeTiles(world, player, previews, structure, x, y, z, stack, unplaceableTiles))
		{			
			if(!player.capabilities.isCreativeMode)
			{
				player.inventory.mainInventory[player.inventory.currentItem].stackSize--;
				if(player.inventory.mainInventory[player.inventory.currentItem].stackSize == 0)
					player.inventory.mainInventory[player.inventory.currentItem] = null;
			}
			
			if(!world.isRemote)
			{
				for (int j = 0; j < unplaceableTiles.size(); j++) {
					if(!(unplaceableTiles.get(j) instanceof LittleTileBlock) && !ItemTileContainer.addBlock(player, ((LittleTileBlock)unplaceableTiles.get(j)).block, ((LittleTileBlock)unplaceableTiles.get(j)).meta, (float)((LittleTileBlock)unplaceableTiles.get(j)).getPercentVolume()))
						WorldUtils.dropItem(world, unplaceableTiles.get(j).getDrops(), x, y, z);
				}
			}
			
			/*TileEntity tileEntity = world.getTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				for (int i = 0; i < previews.size(); i++) {
					PreviewTile tile = previews.get(i);
					
				}
			}*/
		}
       return true;
    }

	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<LittleTilePreview>();
		previews.add(LittleTilePreview.getPreviewFromNBT(stack.stackTagCompound));
		return previews;
	}
	
	@Override
	public void rotateLittlePreview(ItemStack stack, ForgeDirection direction) {
		LittleTilePreview.rotatePreview(stack.stackTagCompound, direction);
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
			if(stack.stackTagCompound.hasKey("color"))
				cube.color = stack.stackTagCompound.getInteger("color");
			cubes.add(cube);
		}
		return cubes;
	}

	@Override
	public boolean hasBackground(ItemStack stack) {
		return false;
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return null;
	}

	@Override
	public void flipLittlePreview(ItemStack stack, ForgeDirection direction) {
		//No need to flip one single tile!
	}

}
