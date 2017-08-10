package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.PlacementHelper;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.tiles.PlacementHelper.PreviewResult;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;

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
	public boolean forced;
	
	public LittleActionPlaceRelative(PositionResult position, boolean centered, boolean fixed, boolean forced) {
		super();
		this.position = position;
		this.centered = centered;
		this.fixed = fixed;
		this.forced = forced;
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
		// TODO Auto-generated method stub
		return null;
	}
	
	public LittleStructure placedStructure;
	public List<LittleTile> placedTiles;

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
			List<LittleTile> tiles = placeTile(player, stack, player.world, position, centered, fixed, forced);
			
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
		buf.writeBoolean(forced);
	}

	@Override
	public void readBytes(ByteBuf buf) {
		this.position = PositionResult.readFromBytes(buf);
		this.centered = buf.readBoolean();
		this.fixed = buf.readBoolean();
		this.forced = buf.readBoolean();
	}
	
	public List<LittleTile> placeTile(EntityPlayer player, ItemStack stack, World world, PositionResult position, boolean centered, boolean fixed, boolean forced) throws LittleActionException
    {
		LittleStructure structure = null;
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		structure = iTile.getLittleStructure(stack);
		
		if(structure != null)
		{
			structure.setTiles(new HashMapList<>());
			forced = false;
		}
		
		PreviewResult result = PlacementHelper.getPreviews(world, stack, position, centered, fixed, false);
		
		ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		
		if(!iTile.drainIngredients(player, stack, result.previews))
			return null;
			
		placedTiles = placeTiles(world, player, result.previews, structure, position.pos, stack, unplaceableTiles, forced, position.facing);
		placedStructure = structure;
		
		if(placedTiles != null)
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
					addTileToInventory(player, unplaceableTiles.get(j));
				}
			}
			return placedTiles;
		}
       return placedTiles;
    }
	
	public static List<LittleTile> placeTiles(World world, EntityPlayer player, List<PlacePreviewTile> previews, LittleStructure structure, BlockPos pos, ItemStack stack, ArrayList<LittleTile> unplaceableTiles, boolean forced, EnumFacing facing)
	{		
		HashMapList<BlockPos, PlacePreviewTile> splitted = getSplittedTiles(previews, pos);
		if(splitted == null)
			return null;
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
		
		
		if(canPlaceTiles(player, world, splitted, coordsToCheck, forced))
		{
			List<LittleTile> placed = new ArrayList<>();
			ArrayList<SoundType> soundsToBePlayed = new ArrayList<>();
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
								placed.add(LT);
							}
						}
						
						teLT.preventUpdate = false;
						teLT.updateTiles();
					}
				}
			}
			
			for (int j = 0; j < lastPlacedTiles.size(); j++) {
				LittleTile tile = lastPlacedTiles.get(j).tile.placeTile(player, stack, lastPlacedTiles.get(j).pos, null, structure, unplaceableTiles, forced, facing, true);
				if(tile != null)
					placed.add(tile);
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
	
	public static HashMapList<BlockPos, PlacePreviewTile> getSplittedTiles(List<PlacePreviewTile> tiles, BlockPos pos)
	{
		HashMapList<BlockPos, PlacePreviewTile> splitted = new HashMapList<BlockPos, PlacePreviewTile>();
		for (int i = 0; i < tiles.size(); i++) {
			if(!tiles.get(i).split(splitted, pos))
				return null;
		}
		return splitted;
	}
	
	public static boolean canPlaceTiles(EntityPlayer player, World world, HashMapList<BlockPos, PlacePreviewTile> splitted, ArrayList<BlockPos> coordsToCheck, boolean forced)
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
			
			if(!isAllowedToInteract(player, pos, true, EnumFacing.EAST))
			{
				IBlockState state = player.world.getBlockState(pos);
				player.world.notifyBlockUpdate(pos, state, state, 3);
				return false;
			}
			
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
	
	public static class LastPlacedTile {
		
		public final PlacePreviewTile tile;
		public final BlockPos pos;
		
		
		public LastPlacedTile(PlacePreviewTile tile, BlockPos pos) {
			this.tile = tile;
			this.pos = pos;
		}
		
	}	

}
