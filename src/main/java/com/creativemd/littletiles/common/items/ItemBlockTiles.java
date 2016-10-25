package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.RenderCubeObject;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PlacePreviewTile;
import com.ibm.icu.impl.duration.impl.DataRecord.EUnitVariant;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTiles extends ItemBlock implements ILittleTile, ICreativeRendered{

	public ItemBlockTiles(Block block, ResourceLocation location) {
		super(block);
		setUnlocalizedName(location.getResourcePath());
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack)
    {
		String result = super.getItemStackDisplayName(stack);
		//LittleTile tile = getLittleTile(Minecraft.getMinecraft().theWorld, stack);
		if(stack.hasTagCompound())
		{
			LittleTileSize size = new LittleTileSize("size", stack.getTagCompound());
			result += " (x=" + size.sizeX + ",y=" + size.sizeY + "z=" + size.sizeZ + ")";
		}
		return result;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName(ItemStack stack)
    {
		if(stack.hasTagCompound())
		{
			Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
			if(block != null && !(block instanceof BlockAir))
				return new ItemStack(block, 1, stack.getTagCompound().getInteger("meta")).getUnlocalizedName();
		}
        return super.getUnlocalizedName(stack);
    }
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(!worldIn.isRemote)
			return EnumActionResult.FAIL;
		
		PlacementHelper helper = PlacementHelper.getInstance(playerIn);
		
		RayTraceResult moving = Minecraft.getMinecraft().objectMouseOver;
		if(PreviewRenderer.markedHit != null)
			moving = PreviewRenderer.markedHit;
		
		//LittleTileSize size = helper.size;
		//size.rotateSize(PreviewRenderer.direction);
		//size.rotateSize(PreviewRenderer.direction2);
		//Vec3 center = helper.getCenterPos(size);
		
		/*x = moving.blockX;
		y = moving.blockY;
		z = moving.blockZ;*/
		
		facing = moving.sideHit;
		
		if(PreviewRenderer.markedHit == null)
		{
			if(!helper.canBePlacedInside(pos, moving.hitVec, facing))
			{
				pos = pos.offset(facing);
			}
		}else
			pos = moving.getBlockPos().offset(facing);
		if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos, facing, stack)) //&& worldIn.canBlockBePlaced(this.block, pos, false, facing, (Entity)null, stack))
        {
            /*int i = this.getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, i, playerIn);*/
            
            if(worldIn.isRemote)
    			PacketHandler.sendPacketToServer(new LittlePlacePacket(/*stack,*/ playerIn.getPositionEyes(TickUtils.getPartialTickTime()), moving.hitVec, pos, facing, PreviewRenderer.markedHit != null, GuiScreen.isCtrlKeyDown())); //, RotationUtils.getIndex(PreviewRenderer.direction), RotationUtils.getIndex(PreviewRenderer.direction2)));
    		
            if(placeBlockAt(playerIn, stack, worldIn, playerIn.getPositionEyes(TickUtils.getPartialTickTime()), moving.hitVec, helper, pos, facing, PreviewRenderer.markedHit != null, GuiScreen.isCtrlKeyDown())) //, PreviewRenderer.direction, PreviewRenderer.direction2);
	            PreviewRenderer.markedHit = null;
            
            /*if (placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                SoundType soundtype = this.block.getSoundType();
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                --stack.stackSize;
            }*/

            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item stack, CreativeTabs tab, List list)
    {
        
    }

	
	/*@Override
	@SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack)
    {
		Block block = world.getBlockState(x, y, z);
		
		Minecraft mc = Minecraft.getMinecraft();
		
		MovingObjectPosition moving = Minecraft.getMinecraft().objectMouseOver;
		if(PreviewRenderer.markedHit != null)
			moving = PreviewRenderer.markedHit;
		
		x = moving.blockX;
		y = moving.blockY;
		z = moving.blockZ;
		
		
		
        if (block == Blocks.snow_layer)
        {
            side = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z) && !PlacementHelper.getInstance(player).canBePlacedInside(x, y, z, moving.hitVec, ForgeDirection.getOrientation(side)))
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
        block = world.getBlockState(x, y, z);
        //helper.posX = x;
        //helper.posY = y;
        //helper.posZ = z;
        return block.isReplaceable(world, x, y, z) || PlacementHelper.getInstance(player).canBePlacedInsideBlock(x, y, z);
    }*/
	
	public static HashMapList<BlockPos, PlacePreviewTile> getSplittedTiles(ArrayList<PlacePreviewTile> tiles, BlockPos pos)
	{
		HashMapList<BlockPos, PlacePreviewTile> splitted = new HashMapList<BlockPos, PlacePreviewTile>();
		for (int i = 0; i < tiles.size(); i++) {
			if(!tiles.get(i).split(splitted, pos))
				return null;
		}
		return splitted;
	}
	
	public static boolean canPlaceTiles(World world, HashMapList<BlockPos, PlacePreviewTile> splitted, ArrayList<BlockPos> coordsToCheck, boolean forced)
	{
		for (BlockPos pos : coordsToCheck) {
			ArrayList<PlacePreviewTile> tiles = splitted.getValues(pos);
			boolean needsCollisionCheck = false;
			if(tiles != null)
			{
				for (int j = 0; j < tiles.size(); j++)
					if(tiles.get(j).needsCollisionTest())
					{
						needsCollisionCheck = true;
						break;
					}
			}
			if(!needsCollisionCheck)
				continue;
			TileEntity mainTile = world.getTileEntity(pos);
			if(mainTile instanceof TileEntityLittleTiles)
			{
				if(forced)
					return true;
				if(tiles != null)
				{
					for (int j = 0; j < tiles.size(); j++)
						if(tiles.get(j).needsCollisionTest() && !((TileEntityLittleTiles) mainTile).isSpaceForLittleTile(tiles.get(j).box))
							return false;
				}
			}else{
				IBlockState state = world.getBlockState(pos);
				if(forced){
					if(state.getBlock() instanceof BlockTile || state.getMaterial().isReplaceable())
						return true;
				}else if(!(state.getBlock() instanceof BlockTile) && !state.getMaterial().isReplaceable())
					return false;
			}
		}
		
		if(forced)
			return false;
		return true;
	}
	
	public static boolean placeTiles(World world, EntityPlayer player, ArrayList<PlacePreviewTile> previews, LittleStructure structure, BlockPos pos, ItemStack stack, ArrayList<LittleTile> unplaceableTiles, boolean forced, EnumFacing facing)
	{
		//if(!(world.getBlock(x, y, z) instanceof BlockTile))
			//if (!world.setBlock(x, y, z, LittleTiles.blockTile, 0, 3))
				//return false;
		
		HashMapList<BlockPos, PlacePreviewTile> splitted = getSplittedTiles(previews, pos);
		if(splitted == null)
			return false;
		
		/*int counting = 0;
		for (int i = 0; i < splitted.size(); i++) {
			counting += splitted.getValues(i).size();
		}
		
		System.out.println("Created " + counting + " of " + previews.size() + " tiles");*/
		ArrayList<BlockPos> coordsToCheck = new ArrayList<BlockPos>();
		if(structure != null)
		{
			coordsToCheck.addAll(splitted.getKeys());
		}else{
			if(forced)
				coordsToCheck.addAll(splitted.getKeys());
			else
				coordsToCheck.add(pos);
		}
		ArrayList<SoundType> soundsToBePlayed = new ArrayList<>();
		if(canPlaceTiles(world, splitted, coordsToCheck, forced))
		{
			LittleTilePosition littlePos = null;
			//LittleTileCoord pos = null;
			
			ArrayList<LastPlacedTile> lastPlacedTiles = new ArrayList<>(); //Used in structures, to be sure that this is the last thing which will be placed
			
			for (BlockPos coord : splitted.getKeys()) {
				ArrayList<PlacePreviewTile> placeTiles = splitted.getValues(coord);
				boolean hascollideBlock = false;
				int i = 0;
				
				while(i < placeTiles.size()){
					if(placeTiles.get(i).needsCollisionTest())
					{
						hascollideBlock = true;
						i++;
					}
					else{
						lastPlacedTiles.add(new LastPlacedTile(placeTiles.get(i), coord));
						placeTiles.remove(i);
					}
				}
				if(hascollideBlock)
				{
					if(!(world.getBlockState(coord).getBlock() instanceof BlockTile) && world.getBlockState(coord).getMaterial().isReplaceable())
						world.setBlockState(coord, LittleTiles.blockTile.getDefaultState());
					
					TileEntity te = world.getTileEntity(coord);
					if(te instanceof TileEntityLittleTiles)
					{
						//int tiles = 0;
						TileEntityLittleTiles teLT = (TileEntityLittleTiles) te;
						teLT.preventUpdate = true;
						for (int j = 0; j < placeTiles.size(); j++) {
							LittleTile LT = placeTiles.get(j).placeTile(player, stack, coord, teLT, structure, unplaceableTiles, forced, facing);
							if(LT != null)
							{
								if(!soundsToBePlayed.contains(LT.getSound()))
									soundsToBePlayed.add(LT.getSound());
								if(structure != null)
								{
									if(littlePos == null)
									{
										structure.setMainTile(LT);
										littlePos = new LittleTilePosition(coord, LT.cornerVec);
									}else
										LT.coord = new LittleTileCoord(teLT, littlePos.coord, littlePos.position);
								}
							}
						}
						
						//if(structure != null)
							//teLT.combineTiles(structure);
						
						teLT.preventUpdate = false;
						teLT.updateTiles();
					}
					//System.out.println("Placed " + tiles + "/" + placeTiles.size());
				}
			}
			
			for (int j = 0; j < lastPlacedTiles.size(); j++) {
				lastPlacedTiles.get(j).tile.placeTile(player, stack, lastPlacedTiles.get(j).pos, null, structure, unplaceableTiles, forced, facing);
			}
			
			if(structure != null)
				structure.combineTiles();
			for (int i = 0; i < soundsToBePlayed.size(); i++) {
				world.playSound((EntityPlayer)null, pos, soundsToBePlayed.get(i).getPlaceSound(), SoundCategory.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
			}
			return true;
		}
		return false;
	}
	
	public boolean placeBlockAt(EntityPlayer player, ItemStack stack, World world, Vec3d playerPos, Vec3d hitVec, PlacementHelper helper, BlockPos pos, EnumFacing facing, boolean customPlacement, boolean forced) //, ForgeDirection direction, ForgeDirection direction2)
    {
		LittleStructure structure = null;
		if(stack.getItem() instanceof ILittleTile)
		{
			structure = ((ILittleTile)stack.getItem()).getLittleStructure(stack);
		}else if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile){
			structure = ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittleStructure(stack);
		}
		
		if(structure != null)
		{
			//structure.dropStack = stack.copy();
			structure.setTiles(new ArrayList<LittleTile>());
			forced = false;
		}
		
		ArrayList<PlacePreviewTile> previews = helper.getPreviewTiles(stack, pos, playerPos, hitVec, facing, customPlacement, true); //, direction, direction2);
		
		//System.out.println("Creating " + previews.size() + " tiles");
		
		ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		if(placeTiles(world, player, previews, structure, pos, stack, unplaceableTiles, forced, facing))
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
						WorldUtils.dropItem(world, unplaceableTiles.get(j).getDrops(), pos);
				}
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
       return false;
    }

	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<LittleTilePreview>();
		previews.add(LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound()));
		return previews;
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, ArrayList<LittleTilePreview> previews) {
		stack.setTagCompound(new NBTTagCompound());
		if(previews.size() > 0)
			previews.get(0).writeToNBT(stack.getTagCompound());
	}
	
	public static ArrayList<RenderCubeObject> getItemRenderingCubes(ItemStack stack) {
		ArrayList<RenderCubeObject> cubes = new ArrayList<RenderCubeObject>();
		Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
		int meta = stack.getTagCompound().getInteger("meta");
		LittleTileSize size = new LittleTileSize("size", stack.getTagCompound());
		if(!(block instanceof BlockAir))
		{
			RenderCubeObject cube = new RenderCubeObject(new LittleTileBox(new LittleTileVec(LittleTile.halfGridSize, LittleTile.halfGridSize, LittleTile.halfGridSize), size).getCube(), block, meta);
			//cube.block = block;
			//cube.meta = meta;
			if(stack.getTagCompound().hasKey("color"))
				cube.color = stack.getTagCompound().getInteger("color");
			cubes.add(cube);
		}
		return cubes;
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return null;
	}

	@Override
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if(stack != null)
			return getItemRenderingCubes(stack);
		return new ArrayList<>();
	}
	
	public static class LastPlacedTile {
		
		public final PlacePreviewTile tile;
		public final BlockPos pos;
		
		
		public LastPlacedTile(PlacePreviewTile tile, BlockPos pos) {
			this.tile = tile;
			this.pos = pos;
		}
		
	}

}
