package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.PlacementHelper;
import com.creativemd.littletiles.common.tiles.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PreviewResult;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileCoord;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
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
import net.minecraft.util.NonNullList;
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
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(!worldIn.isRemote)
			return EnumActionResult.FAIL;
		
		PositionResult position = PreviewRenderer.markedPosition != null ? PreviewRenderer.markedPosition : PlacementHelper.getPosition(worldIn, Minecraft.getMinecraft().objectMouseOver);
		
		ItemStack stack = player.getHeldItem(hand);
		
		if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack))
        {
            
            if(worldIn.isRemote)
    			PacketHandler.sendPacketToServer(new LittlePlacePacket(position, PreviewRenderer.isCentered(player), PreviewRenderer.isFixed(player), GuiScreen.isCtrlKeyDown()));
    		
            if(placeBlockAt(player, stack, worldIn, position, PreviewRenderer.isCentered(player), PreviewRenderer.isFixed(player), GuiScreen.isCtrlKeyDown()))
	            PreviewRenderer.markedPosition = null;

            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        
    }
	
	public static HashMapList<BlockPos, PlacePreviewTile> getSplittedTiles(List<PlacePreviewTile> tiles, BlockPos pos)
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
	
	public static boolean placeTiles(World world, EntityPlayer player, List<PlacePreviewTile> previews, LittleStructure structure, BlockPos pos, ItemStack stack, ArrayList<LittleTile> unplaceableTiles, boolean forced, EnumFacing facing)
	{		
		HashMapList<BlockPos, PlacePreviewTile> splitted = getSplittedTiles(previews, pos);
		if(splitted == null)
			return false;
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
					boolean requiresCollisionTest = true;
					if(!(world.getBlockState(coord).getBlock() instanceof BlockTile) && world.getBlockState(coord).getMaterial().isReplaceable())
					{
						requiresCollisionTest = false;
						world.setBlockState(coord, LittleTiles.blockTile.getDefaultState());
					}
					
					TileEntity te = world.getTileEntity(coord);
					if(te instanceof TileEntityLittleTiles)
					{
						TileEntityLittleTiles teLT = (TileEntityLittleTiles) te;
						teLT.preventUpdate = true;
						for (int j = 0; j < placeTiles.size(); j++) {
							LittleTile LT = placeTiles.get(j).placeTile(player, stack, coord, teLT, structure, unplaceableTiles, forced, facing, requiresCollisionTest);
							if(LT != null && (structure == null || structure.shouldPlaceTile(LT)))
							{
								if(!soundsToBePlayed.contains(LT.getSound()))
									soundsToBePlayed.add(LT.getSound());
								if(structure != null)
								{
									if(!structure.hasMainTile())
									{
										structure.setMainTile(LT);
									}else
										LT.coord = structure.getMainTileCoord(LT);
								}
								LT.isAllowedToSearchForStructure = false;
							}
						}
						
						teLT.preventUpdate = false;
						teLT.updateTiles();
					}
				}
			}
			
			for (int j = 0; j < lastPlacedTiles.size(); j++) {
				lastPlacedTiles.get(j).tile.placeTile(player, stack, lastPlacedTiles.get(j).pos, null, structure, unplaceableTiles, forced, facing, true);
			}
			
			if(structure != null)
			{
				structure.setMainTile(structure.getMainTile());
				for (Iterator<LittleTile> iterator = structure.getTiles(); iterator.hasNext();) {
					LittleTile tile = iterator.next();
					tile.isAllowedToSearchForStructure = true;
				}
				structure.combineTiles();
			}
			
			for (int i = 0; i < soundsToBePlayed.size(); i++) {
				world.playSound((EntityPlayer)null, pos, soundsToBePlayed.get(i).getPlaceSound(), SoundCategory.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
			}
			
			return true;
		}
		return false;
	}
	
	public boolean placeBlockAt(EntityPlayer player, ItemStack stack, World world, PositionResult position, boolean centered, boolean fixed, boolean forced)
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
			structure.setTiles(new HashMapList<>());
			forced = false;
		}
		
		PreviewResult result = PlacementHelper.getPreviews(world, stack, position, centered, fixed, false);
		
		ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		if(placeTiles(world, player, result.previews, structure, position.pos, stack, unplaceableTiles, forced, position.facing))
		{			
			if(!player.capabilities.isCreativeMode)
			{
				player.inventory.getCurrentItem().shrink(1);
				if(player.inventory.getCurrentItem().isEmpty())
					player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
			}
			
			if(!world.isRemote)
			{
				for (int j = 0; j < unplaceableTiles.size(); j++) {
					if(!(unplaceableTiles.get(j) instanceof LittleTileBlock) && !ItemTileContainer.addBlock(player, ((LittleTileBlock)unplaceableTiles.get(j)).getBlock(), ((LittleTileBlock)unplaceableTiles.get(j)).getMeta(), (float)((LittleTileBlock)unplaceableTiles.get(j)).getPercentVolume()))
						WorldUtils.dropItem(world, unplaceableTiles.get(j).getDrops(), position.pos);
				}
			}
			return true;
		}
       return false;
    }
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<LittleTilePreview>();
		previews.add(LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound()));
		return previews;
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews) {
		if(previews.size() > 0)
		{
			LittleTilePreview preview = previews.get(0);
			NBTTagCompound nbt = preview.getTileData().copy();
			
			preview.size.writeToNBT("size", nbt);
			
			if(preview.isCustomPreview() && !preview.getTypeID().equals(""))
				nbt.setString("type", preview.getTypeID());
			
			stack.setTagCompound(nbt);
		}else
			stack.setTagCompound(new NBTTagCompound());
	}
	
	public static ItemStack getStackFromPreview(LittleTilePreview preview)
	{
		ItemStack stack = new ItemStack(LittleTiles.blockTile);
		NBTTagCompound nbt = preview.getTileData().copy();
		
		preview.size.writeToNBT("size", nbt);
		
		if(preview.isCustomPreview() && !preview.getTypeID().equals(""))
			nbt.setString("type", preview.getTypeID());
		
		stack.setTagCompound(nbt);
		return stack;
	}
	
	public static ArrayList<RenderCubeObject> getItemRenderingCubes(ItemStack stack) {
		ArrayList<RenderCubeObject> cubes = new ArrayList<RenderCubeObject>();
		if(stack != null && stack.hasTagCompound())
		{
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
