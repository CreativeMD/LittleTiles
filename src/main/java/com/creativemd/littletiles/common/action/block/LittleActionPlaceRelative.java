package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PreviewResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleActionPlaceRelative extends LittleAction {
	
	public PositionResult position;
	public boolean centered;
	public boolean fixed;
	public PlacementMode mode;
	
	public LittleActionPlaceRelative(PositionResult position, boolean centered, boolean fixed, PlacementMode mode) {
		super();
		this.position = position;
		this.centered = centered;
		this.fixed = fixed;
		this.mode = mode;
	}
	
	public LittleActionPlaceRelative() {
		super();
	}

	@Override
	public boolean canBeReverted() {
		return true;
	}

	@Override
	public LittleAction revert() {
		boxes.convertToSmallest();
		
		if(destroyed != null)
		{
			destroyed.convertToSmallest();
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, null, PlacementMode.normal, true));
		}
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public LittleBoxes boxes;
	public LittleAbsolutePreviews destroyed;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		ItemStack stack = player.getHeldItemMainhand();
		
		if(!isAllowedToInteract(player, position.pos, true, EnumFacing.EAST))
		{
			IBlockState state = player.world.getBlockState(position.pos);
			player.world.notifyBlockUpdate(position.pos, state, state, 3);
			return false;
		}
		
		if(PlacementHelper.isLittleBlock(stack))
		{
			List<LittleTile> tiles = placeTile(player, stack, player.world, position, centered, fixed, mode);
			
			if(!player.world.isRemote)
			{
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
				playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
			}
			return tiles != null;
		}
		return false;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		position.writeToBytes(buf);
		buf.writeBoolean(centered);
		buf.writeBoolean(fixed);
		writePlacementMode(mode, buf);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		this.position = PositionResult.readFromBytes(buf);
		this.centered = buf.readBoolean();
		this.fixed = buf.readBoolean();
		this.mode = readPlacementMode(buf);
	}
	
	public List<LittleTile> placeTile(EntityPlayer player, ItemStack stack, World world, PositionResult position, boolean centered, boolean fixed, PlacementMode mode) throws LittleActionException
    {
		LittleStructure structure = null;
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		structure = iTile.getLittleStructure(stack);
		
		if(structure != null)
		{
			structure.setTiles(new HashMapList<>());
			if(!mode.canPlaceStructures())
			{
				System.out.println("Using invalid mode for placing structure. mode=" + mode.name);
				mode = PlacementMode.getStructureDefault();
			}
		}
		
		PreviewResult result = PlacementHelper.getPreviews(world, stack, position, centered, fixed, false, false, mode);
		
		if(result == null)
		{
			boxes = new LittleBoxes(position.pos, position.getContext());
			return new ArrayList<>();
		}
		
		List<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		List<LittleTile> removedTiles = new ArrayList<LittleTile>();
		
		ItemStack toPlace = stack.copy();
		
		if(needIngredients(player))
		{
			if(iTile.containsIngredients(stack))
			{
				stack.shrink(1);
			}else
				canDrainPreviews(player, result.previews);
		}
			
		List<LittleTile> placedTiles = placeTiles(world, player, result.context, result.placePreviews, structure, mode, position.pos, toPlace, unplaceableTiles, removedTiles, position.facing);
		
		boxes = new LittleBoxes(position.pos, result.context);
		if(placedTiles != null)
		{
			if(!world.isRemote)
			{
				addTilesToInventoryOrDrop(player, unplaceableTiles);
				addTilesToInventoryOrDrop(player, removedTiles);
			}
			
			for (LittleTile tile : placedTiles) {
				boxes.addBox(tile);
			}
			
			if(!removedTiles.isEmpty())
			{
				destroyed = new LittleAbsolutePreviews(position.pos, result.context);
				for (LittleTile tile : removedTiles) {
					destroyed.addTile(tile);
				}
			}
		}
			
		return placedTiles;
    }
	
	public static List<LittleTile> placeTilesWithoutPlayer(World world, LittleGridContext context, List<PlacePreviewTile> previews, LittleStructure structure, PlacementMode mode, BlockPos pos, ItemStack stack, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, @Nullable EnumFacing facing)
	{
		try {
			return placeTiles(world, null, context, previews, structure, mode, pos, stack, unplaceableTiles, removedTiles, facing);
		} catch (LittleActionException e) {
			return null;
		}
	}
	
	public static List<LittleTile> placeTiles(World world, EntityPlayer player, LittleGridContext context, List<PlacePreviewTile> previews, LittleStructure structure, PlacementMode mode, BlockPos pos, ItemStack stack, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, @Nullable EnumFacing facing) throws LittleActionException
	{
		if(player != null)
		{
			if(SpecialServerConfig.isPlaceLimited(player) && getVolume(context, previews) > SpecialServerConfig.maxPlaceBlocks)
				throw new SpecialServerConfig.NotAllowedToPlaceException();
		}
		
		HashMap<BlockPos, PlacePreviews> splitted = getSplittedTiles(context, previews, pos);
		if(splitted == null)
			return null;
		
		List<BlockPos> coordsToCheck = mode.getCoordsToCheck(splitted, pos);	
		
		if(canPlaceTiles(player, world, splitted, coordsToCheck, mode))
		{
			List<LittleTile> placed = new ArrayList<>();
			List<SoundType> soundsToBePlayed = new ArrayList<>();
			List<LastPlacedTile> lastPlacedTiles = new ArrayList<>(); //Used in structures, to be sure that this is the last thing which will be placed
			
			for (Entry<BlockPos, PlacePreviews> entry : splitted.entrySet()) {
				BlockPos coord = entry.getKey();
				PlacePreviews placeTiles = entry.getValue();
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
					
					TileEntityLittleTiles te = loadTe(player, world, coord, mode.shouldConvertBlock());
					if(te != null)
					{
						te.preventUpdate = true;
						
						placeTiles.ensureBothAreEqual(te);
						
						for (int j = 0; j < placeTiles.size(); j++) {
							for (LittleTile LT : placeTiles.get(j).placeTile(player, stack, coord, te, structure, unplaceableTiles, removedTiles, mode, facing, requiresCollisionTest)) {
								if(structure == null || structure.shouldPlaceTile(LT))
								{
									if(!soundsToBePlayed.contains(LT.getSound()))
										soundsToBePlayed.add(LT.getSound());
									if(structure != null)
									{
										if(!structure.hasMainTile())
											structure.setMainTile(LT);
										else
											LT.coord = structure.getMainTileCoord(LT);
									}
									LT.isAllowedToSearchForStructure = false;
									placed.add(LT);
								}
							}
						}
						
						te.preventUpdate = false;
						te.updateTiles();
					}
				}
			}
			
			for (int j = 0; j < lastPlacedTiles.size(); j++) {
				for (LittleTile tile : lastPlacedTiles.get(j).tile.placeTile(player, stack, lastPlacedTiles.get(j).pos, null, structure, unplaceableTiles, removedTiles, mode, facing, true))
				{
					if(tile != null)
						placed.add(tile);
				}
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
			
			return placed;
		}
		return null;
	}
	
	public static double getVolume(LittleGridContext context, List<PlacePreviewTile> tiles)
	{
		double volume = 0;
		for (PlacePreviewTile preview : tiles) {
			volume += preview.box.getPercentVolume(context);
		}
		return volume;
	}
	
	public static HashMap<BlockPos, PlacePreviews> getSplittedTiles(LittleGridContext context, List<PlacePreviewTile> tiles, BlockPos pos)
	{
		HashMapList<BlockPos, PlacePreviewTile> splitted = new HashMapList<BlockPos, PlacePreviewTile>();
		for (int i = 0; i < tiles.size(); i++) {
			if(!tiles.get(i).split(context, splitted, pos))
				return null;
		}
		
		HashMap<BlockPos, PlacePreviews> previews = new HashMap<>();
		for (Entry<BlockPos, ArrayList<PlacePreviewTile>> entry : splitted.entrySet()) {
			previews.put(entry.getKey(), new PlacePreviews(context, entry.getValue()));
		}
		return previews;
	}
	
	public static boolean canPlaceTiles(EntityPlayer player, World world, HashMap<BlockPos, PlacePreviews> splitted, List<BlockPos> coordsToCheck, PlacementMode mode)
	{
		for (BlockPos pos : splitted.keySet()) {
			if(!isAllowedToInteract(player, pos, true, EnumFacing.EAST))
			{
				IBlockState state = player.world.getBlockState(pos);
				player.world.notifyBlockUpdate(pos, state, state, 3);
				return false;
			}
		}
		
		if(coordsToCheck != null)
		{
			for (BlockPos pos : coordsToCheck) {
				PlacePreviews tiles = splitted.get(pos);
				
				if(tiles == null)
					continue;
				
				boolean needsCollisionCheck = false;
				for (int j = 0; j < tiles.size(); j++)
				{
					if(tiles.get(j).needsCollisionTest())
					{
						needsCollisionCheck = true;
						break;
					}
				}
				
				if(!needsCollisionCheck)
					continue;
				
				TileEntityLittleTiles te = loadTe(player, world, pos, false);
				if(te != null)
				{
					tiles.ensureBothAreEqual(te);
					
					for (int j = 0; j < tiles.size(); j++)
						if(tiles.get(j).needsCollisionTest())
							if(mode.checkAll())
							{
								if(!te.isSpaceForLittleTile(tiles.get(j).box))
								{
									te.convertToSmallest();
									return false;
								}
							}else
								if(!te.isSpaceForLittleTileStructure(tiles.get(j).box))
								{
									te.convertToSmallest();
									return false;
								}
				}else{
					IBlockState state = world.getBlockState(pos);
					if(!(state.getBlock() instanceof BlockTile) && !state.getMaterial().isReplaceable())
						if(mode.checkAll() || !(isBlockValid(state.getBlock()) && canConvertBlock(player, world, pos, state)))
							return false;
				}
			}
		}
		return true;
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
