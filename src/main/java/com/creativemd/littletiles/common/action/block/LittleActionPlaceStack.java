package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tiles.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tiles.place.PlacePreview;
import com.creativemd.littletiles.common.tiles.place.PlacePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredient;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;
import com.creativemd.littletiles.common.utils.ingredients.LittleInventory;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PreviewResult;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;

public class LittleActionPlaceStack extends LittleAction {
	
	public PositionResult position;
	public boolean centered;
	public boolean fixed;
	public PlacementMode mode;
	public LittlePreviews previews;
	
	public LittlePlaceResult placedTiles;
	
	public LittleActionPlaceStack(LittlePreviews previews, PositionResult position, boolean centered, boolean fixed, PlacementMode mode) {
		super();
		this.position = position;
		this.centered = centered;
		this.fixed = fixed;
		this.mode = mode;
		this.previews = previews;
	}
	
	public LittleActionPlaceStack() {
		super();
	}
	
	public void checkMode(LittlePreviews previews) {
		if (previews.hasStructure() && !mode.canPlaceStructures()) {
			System.out.println("Using invalid mode for placing structure. mode=" + mode.name);
			this.mode = PlacementMode.getStructureDefault();
		}
	}
	
	public void checkMode(LittleStructure structure) {
		if (structure != null && !mode.canPlaceStructures()) {
			System.out.println("Using invalid mode for placing structure. mode=" + mode.name);
			this.mode = PlacementMode.getStructureDefault();
		}
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	@Override
	public LittleAction revert() {
		boxes.convertToSmallest();
		
		if (destroyed != null) {
			destroyed.convertToSmallest();
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, PlacementMode.normal, true));
		}
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public LittleBoxes boxes;
	public LittleAbsolutePreviews destroyed;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		ItemStack stack = player.getHeldItemMainhand();
		World world = player.world;
		
		if (!isAllowedToInteract(world, player, position.getPos(), true, EnumFacing.EAST)) {
			sendBlockResetToClient(world, (EntityPlayerMP) player, position.getPos());
			return false;
		}
		
		if (PlacementHelper.getLittleInterface(stack) != null) {
			LittlePlaceResult tiles = placeTile(player, stack, player.world, position, centered, fixed, mode);
			
			if (!player.world.isRemote) {
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
		writePreviews(previews, buf);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		this.position = PositionResult.readFromBytes(buf);
		this.centered = buf.readBoolean();
		this.fixed = buf.readBoolean();
		this.mode = readPlacementMode(buf);
		this.previews = readPreviews(buf);
	}
	
	public LittlePlaceResult placeTile(EntityPlayer player, ItemStack stack, World world, PositionResult position, boolean centered, boolean fixed, PlacementMode mode) throws LittleActionException {
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		checkMode(previews);
		if (previews.hasStructure())
			previews.getStructure().createTilesList();
		
		PreviewResult result = PlacementHelper.getPreviews(world, previews, iTile.getPreviewsContext(stack), stack, position, centered, fixed, false, mode);
		
		if (result == null)
			return null;
		
		List<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		List<LittleTile> removedTiles = new ArrayList<LittleTile>();
		
		ItemStack toPlace = stack.copy();
		
		LittleInventory inventory = new LittleInventory(player);
		
		if (needIngredients(player)) {
			if (!iTile.containsIngredients(stack))
				canTake(player, inventory, getIngredients(result.previews));
		}
		
		placedTiles = placeTiles(world, player, result.context, result.placePreviews, previews.getStructure(), mode, position.getPos(), toPlace, unplaceableTiles, removedTiles, position.facing);
		
		if (placedTiles != null) {
			boxes = placedTiles.placedBoxes;
			
			if (needIngredients(player)) {
				giveOrDrop(player, inventory, removedTiles);
				
				if (iTile.containsIngredients(stack)) {
					stack.shrink(1);
					giveOrDrop(player, inventory, unplaceableTiles);
				} else {
					LittleIngredients ingredients = LittleIngredient.extractStructureOnly(previews);
					ingredients.add(getIngredients(placedTiles.placedPreviews));
					take(player, inventory, ingredients);
				}
			}
			
			if (!removedTiles.isEmpty()) {
				destroyed = new LittleAbsolutePreviews(position.getPos(), result.context);
				for (LittleTile tile : removedTiles) {
					destroyed.addTile(tile);
				}
			}
		} else
			boxes = new LittleBoxes(position.getPos(), result.context);
		
		return placedTiles;
	}
	
	public static LittlePlaceResult placeTilesWithoutPlayer(World world, LittleGridContext context, HashMap<BlockPos, PlacePreviews> splitted, LittleStructure structure, PlacementMode mode, BlockPos pos, ItemStack stack, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, @Nullable EnumFacing facing) {
		
		try {
			return placeTiles(world, null, context, splitted, structure, mode, pos, stack, unplaceableTiles, removedTiles, facing);
		} catch (LittleActionException e) {
			return null;
		}
	}
	
	public static LittlePlaceResult placeTilesWithoutPlayer(World world, LittleGridContext context, List<PlacePreview> previews, LittleStructure structure, PlacementMode mode, BlockPos pos, ItemStack stack, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, @Nullable EnumFacing facing) {
		try {
			return placeTiles(world, null, context, previews, structure, mode, pos, stack, unplaceableTiles, removedTiles, facing);
		} catch (LittleActionException e) {
			return null;
		}
	}
	
	private static LittlePlaceResult placeTiles(World world, EntityPlayer player, LittleGridContext context, HashMap<BlockPos, PlacePreviews> splitted, LittleStructure parentStructure, PlacementMode mode, BlockPos pos, ItemStack stack, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, @Nullable EnumFacing facing) throws LittleActionException {
		if (splitted == null)
			return null;
		
		List<BlockPos> coordsToCheck = mode.getCoordsToCheck(splitted, pos);
		
		if (canPlaceTiles(player, world, splitted, coordsToCheck, mode)) {
			LittlePlaceResult placed = new LittlePlaceResult(pos, context, parentStructure);
			List<SoundType> soundsToBePlayed = new ArrayList<>();
			List<LastPlacedTile> lastPlacedTiles = new ArrayList<>(); // Used for structures, to be sure that this is the last thing which will be placed
			
			for (Entry<BlockPos, PlacePreviews> entry : splitted.entrySet()) {
				BlockPos coord = entry.getKey();
				PlacePreviews placeTiles = entry.getValue();
				boolean hascollideBlock = false;
				int i = 0;
				
				while (i < placeTiles.size()) {
					if (placeTiles.get(i).needsCollisionTest()) {
						hascollideBlock = true;
						i++;
					} else {
						lastPlacedTiles.add(new LastPlacedTile(placeTiles.get(i), coord, placeTiles.context));
						placeTiles.remove(i);
					}
				}
				if (hascollideBlock) {
					boolean requiresCollisionTest = true;
					if (!(world.getBlockState(coord).getBlock() instanceof BlockTile) && world.getBlockState(coord).getMaterial().isReplaceable()) {
						requiresCollisionTest = false;
						world.setBlockState(coord, BlockTile.getState(false, false));
					}
					
					TileEntityLittleTiles te = loadTe(player, world, coord, mode.shouldConvertBlock());
					if (te != null) {
						
						if (te.isEmpty())
							requiresCollisionTest = false;
						
						final boolean collsionTest = requiresCollisionTest;
						
						placeTiles.ensureBothAreEqual(te);
						
						te.updateTilesSecretly((x) -> {
							
							for (PlacePreview placeTile : placeTiles) {
								for (LittleTile LT : placeTile.placeTile(player, stack, coord, te.getContext(), te, x, unplaceableTiles, removedTiles, mode, facing, collsionTest)) {
									if (!soundsToBePlayed.contains(LT.getSound()))
										soundsToBePlayed.add(LT.getSound());
									
									if (placeTile.structurePreview != null) {
										if (!placeTile.structurePreview.getStructure().hasMainTile())
											placeTile.structurePreview.getStructure().setMainTile(LT);
										else {
											LT.connection = placeTile.structurePreview.getStructure().getStructureLink(LT);
											LT.connection.setLoadedStructure(placeTile.structurePreview.getStructure());
											placeTile.structurePreview.getStructure().add(LT);
										}
									}
									
									placed.addPlacedTile(LT);
								}
							}
						});
					}
				}
			}
			
			int j = 0;
			while (j < placed.tileEntities.size()) {
				TileEntityLittleTiles te = placed.tileEntities.get(j);
				if (parentStructure == null) {
					boolean changed = te.combineTiles();
					
					if (te.size() == 1 && te.convertBlockToVanilla()) {
						placed.tileEntities.remove(j); // Remove the last tileentity (the current one)
						continue;
					} else if (!changed)
						te.updateTiles();
				} else if (!te.combineTiles(parentStructure))
					te.updateTiles();
				j++;
			}
			
			for (LastPlacedTile lastPlacedTile : lastPlacedTiles) {
				for (LittleTile tile : lastPlacedTile.tile.placeTile(player, stack, lastPlacedTile.pos, lastPlacedTile.context, null, null, unplaceableTiles, removedTiles, mode, facing, true)) {
					if (tile != null)
						placed.addPlacedTile(tile);
				}
			}
			
			if (parentStructure != null) {
				if (parentStructure.getMainTile() == null)
					throw new LittleActionException("Missing maintile of structure. Placed " + placed.placedPreviews.size() + " tile(s).");
				
				parentStructure.placedStructure(stack);
			}
			
			for (int i = 0; i < soundsToBePlayed.size(); i++)
				world.playSound((EntityPlayer) null, pos, soundsToBePlayed.get(i).getPlaceSound(), SoundCategory.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
			
			return placed;
		}
		return null;
	}
	
	public static LittlePlaceResult placeTiles(World world, EntityPlayer player, LittleGridContext context, List<PlacePreview> previews, LittleStructure structure, PlacementMode mode, BlockPos pos, ItemStack stack, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, @Nullable EnumFacing facing) throws LittleActionException {
		if (player != null) {
			if (SpecialServerConfig.isPlaceLimited(player) && getVolume(context, previews) > SpecialServerConfig.maxPlaceBlocks)
				throw new SpecialServerConfig.NotAllowedToPlaceException();
			
			if (SpecialServerConfig.isTransparencyRestricted(player))
				for (PlacePreview placePreview : previews)
					isAllowedToPlacePreview(player, placePreview.preview);
		}
		
		HashMap<BlockPos, PlacePreviews> splitted = getSplittedTiles(context, previews, pos);
		
		if (player != null && !world.isRemote) {
			List<BlockSnapshot> snaps = new ArrayList<>();
			for (BlockPos snapPos : splitted.keySet()) {
				snaps.add(new BlockSnapshot(world, snapPos, BlockTile.getState(false, false)));
			}
			MultiPlaceEvent event = new MultiPlaceEvent(snaps, world.getBlockState(facing == null ? pos : pos.offset(facing)), player, EnumHand.MAIN_HAND);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				for (BlockPos snapPos : splitted.keySet())
					sendBlockResetToClient(world, (EntityPlayerMP) player, pos);
				return null;
			}
		}
		
		return placeTiles(world, player, context, splitted, structure, mode, pos, stack, unplaceableTiles, removedTiles, facing);
	}
	
	public static double getVolume(LittleGridContext context, List<PlacePreview> tiles) {
		double volume = 0;
		for (PlacePreview preview : tiles) {
			volume += preview.box.getPercentVolume(context);
		}
		return volume;
	}
	
	public static HashMap<BlockPos, PlacePreviews> getSplittedTiles(LittleGridContext context, List<PlacePreview> tiles, BlockPos pos) {
		HashMapList<BlockPos, PlacePreview> splitted = new HashMapList<BlockPos, PlacePreview>();
		for (int i = 0; i < tiles.size(); i++) {
			if (!tiles.get(i).split(context, splitted, pos))
				return null;
		}
		
		HashMap<BlockPos, PlacePreviews> previews = new HashMap<>();
		for (Entry<BlockPos, ArrayList<PlacePreview>> entry : splitted.entrySet()) {
			previews.put(entry.getKey(), new PlacePreviews(context, entry.getValue()));
		}
		return previews;
	}
	
	public static boolean canPlaceTiles(EntityPlayer player, World world, HashMap<BlockPos, PlacePreviews> splitted, List<BlockPos> coordsToCheck, PlacementMode mode) {
		return canPlaceTiles(player, world, splitted, coordsToCheck, mode, null, false);
	}
	
	public static boolean canPlaceTiles(EntityPlayer player, World world, HashMap<BlockPos, PlacePreviews> splitted, List<BlockPos> coordsToCheck, PlacementMode mode, Predicate<LittleTile> predicate, boolean includeWorldBoundaries) {
		for (BlockPos pos : splitted.keySet()) {
			if (!isAllowedToInteract(world, player, pos, true, EnumFacing.EAST)) {
				sendBlockResetToClient(world, (EntityPlayerMP) player, pos);
				return false;
			}
		}
		
		if (coordsToCheck != null) {
			for (BlockPos pos : coordsToCheck) {
				PlacePreviews tiles = splitted.get(pos);
				
				if (tiles == null || tiles.isEmpty())
					continue;
				
				boolean needsCollisionCheck = false;
				for (int j = 0; j < tiles.size(); j++) {
					if (tiles.get(j).needsCollisionTest()) {
						needsCollisionCheck = true;
						break;
					}
				}
				
				if (!needsCollisionCheck)
					continue;
				
				if (includeWorldBoundaries && (pos.getY() < 0 || pos.getY() >= 256))
					return false;
				
				TileEntityLittleTiles te = loadTe(player, world, pos, false);
				if (te != null) {
					LittleGridContext contextBefore = te.getContext();
					tiles.ensureBothAreEqual(te);
					
					for (int j = 0; j < tiles.size(); j++)
						if (tiles.get(j).needsCollisionTest())
							if (mode.checkAll()) {
								if (!te.isSpaceForLittleTile(tiles.get(j).box, predicate)) {
									if (te.getContext() != contextBefore)
										te.convertTo(contextBefore);
									return false;
								}
							} else if (!te.isSpaceForLittleTileStructure(tiles.get(j).box, predicate)) {
								if (te.getContext() != contextBefore)
									te.convertTo(contextBefore);
								return false;
							}
						
					// tiles.convertToSmallest();
				} else {
					IBlockState state = world.getBlockState(pos);
					if (state.getMaterial().isReplaceable())
						return false;
					if (!(state.getBlock() instanceof BlockTile))
						if (mode.checkAll() || !(isBlockValid(state) && canConvertBlock(player, world, pos, state)))
							return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
		if (placedTiles == null)
			return null;
		return new LittleActionPlaceAbsolute(placedTiles.placedPreviews.copy(), mode);
	}
	
	public static class LastPlacedTile {
		
		public final PlacePreview tile;
		public final BlockPos pos;
		public final LittleGridContext context;
		
		public LastPlacedTile(PlacePreview tile, BlockPos pos, LittleGridContext context) {
			this.tile = tile;
			this.pos = pos;
			this.context = context;
		}
		
	}
	
	public static class LittlePlaceResult {
		
		public final LittleAbsolutePreviews placedPreviews;
		public final LittleBoxes placedBoxes;
		private BlockPos lastPos = null;
		public final List<TileEntityLittleTiles> tileEntities = new ArrayList<>();
		public final LittleStructure parentStructure;
		
		public LittlePlaceResult(BlockPos pos, LittleGridContext context, LittleStructure parentStructure) {
			this.placedPreviews = new LittleAbsolutePreviews(pos, context);
			this.placedBoxes = new LittleBoxes(pos, context);
			this.parentStructure = parentStructure;
		}
		
		public void addPlacedTile(LittleTile tile) {
			if (lastPos == null || !lastPos.equals(tile.te.getPos())) {
				lastPos = tile.te.getPos();
				tileEntities.add(tile.te);
			}
			placedPreviews.addTile(tile);
			placedBoxes.addBox(tile);
		}
		
	}
	
}
