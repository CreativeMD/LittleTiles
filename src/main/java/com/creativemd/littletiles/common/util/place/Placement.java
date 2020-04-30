package com.creativemd.littletiles.common.util.place;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig.NotAllowedToPlaceException;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.MultiPlaceEvent;

public class Placement {
	
	public final EntityPlayer player;
	public final World world;
	public final PlacementMode mode;
	public final BlockPos pos;
	public final EnumFacing facing;
	public final LittlePreviews previews;
	public final LinkedHashMap<BlockPos, PlacementBlock> blocks = new LinkedHashMap<>();
	public final PlacementStructurePreview origin;
	public final List<PlacementStructurePreview> structures = new ArrayList<>();
	
	public final List<LittleTile> removedTiles = new ArrayList<>();
	public final List<LittleTile> unplaceableTiles = new ArrayList<>();
	public final List<SoundType> soundsToBePlayed = new ArrayList<>();
	
	protected ItemStack stack;
	protected boolean ignoreWorldBoundaries = true;
	protected Predicate<LittleTile> predicate;
	
	public Placement(EntityPlayer player, PlacementPreview preview) {
		this.player = player;
		this.world = preview.world;
		this.mode = preview.mode;
		this.pos = preview.pos;
		this.facing = preview.facing;
		this.previews = preview.previews;
		this.origin = createStructureTree(null, preview.previews);
		
		createPreviews(origin, preview.inBlockOffset, preview.pos);
		
		for (PlacementBlock block : blocks.values())
			block.convertToSmallest();
	}
	
	public Placement setIgnoreWorldBoundaries(boolean value) {
		this.ignoreWorldBoundaries = value;
		return this;
	}
	
	public Placement setPredicate(Predicate<LittleTile> predicate) {
		this.predicate = predicate;
		return this;
	}
	
	public Placement setStack(ItemStack stack) {
		this.stack = stack;
		return this;
	}
	
	public boolean canPlace() {
		for (BlockPos pos : blocks.keySet()) {
			if (!LittleAction.isAllowedToInteract(world, player, pos, true, EnumFacing.EAST)) {
				LittleAction.sendBlockResetToClient(world, (EntityPlayerMP) player, pos);
				return false;
			}
		}
		
		List<BlockPos> coordsToCheck = mode.getCoordsToCheck(blocks.keySet(), pos);
		if (coordsToCheck != null) {
			for (BlockPos pos : coordsToCheck) {
				PlacementBlock block = blocks.get(pos);
				
				if (block == null)
					continue;
				
				if (!block.canPlace())
					return false;
			}
		}
		return true;
	}
	
	public PlacementResult place() throws LittleActionException {
		if (player != null && !world.isRemote) {
			if (player != null) {
				if (LittleTiles.CONFIG.isPlaceLimited(player) && previews.getVolumeIncludingChildren() > LittleTiles.CONFIG.survival.maxPlaceBlocks)
					throw new NotAllowedToPlaceException();
				
				if (LittleTiles.CONFIG.isTransparencyRestricted(player))
					for (LittlePreview preview : previews)
						LittleAction.isAllowedToPlacePreview(player, preview);
			}
			
			List<BlockSnapshot> snaps = new ArrayList<>();
			for (BlockPos snapPos : blocks.keySet())
				snaps.add(new BlockSnapshot(world, snapPos, BlockTile.getState(false, false)));
			
			MultiPlaceEvent event = new MultiPlaceEvent(snaps, world.getBlockState(facing == null ? pos : pos.offset(facing)), player, EnumHand.MAIN_HAND);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				for (BlockPos snapPos : blocks.keySet())
					LittleAction.sendBlockResetToClient(world, (EntityPlayerMP) player, pos);
				return null;
			}
		}
		if (canPlace())
			return placeTiles();
		return null;
	}
	
	public PlacementResult tryPlace() {
		try {
			return place();
		} catch (LittleActionException e) {
			return null;
		}
	}
	
	protected PlacementResult placeTiles() throws LittleActionException {
		PlacementResult result = new PlacementResult(pos, origin.structure);
		
		for (PlacementBlock block : blocks.values())
			block.place(result);
		
		HashSet<BlockPos> blocksToUpdate = new HashSet<>(blocks.keySet());
		
		for (Iterator iterator = blocks.values().iterator(); iterator.hasNext();) {
			PlacementBlock block = (PlacementBlock) iterator.next();
			if (block.combineTilesSecretly()) {
				result.tileEntities.remove(block.cached);
				iterator.remove();
			}
		}
		
		for (PlacementBlock block : blocks.values())
			block.placeLate();
		
		if (origin.structure != null) {
			if (origin.structure.getMainTile() == null)
				throw new LittleActionException("Missing maintile of structure. Placed " + result.placedPreviews.size() + " tile(s).");
			notifyStructurePlaced();
		}
		
		constructStructureRelations();
		
		HashSet<BlockPos> blocksToNotify = new HashSet<>();
		for (BlockPos pos : blocksToUpdate) {
			for (int i = 0; i < 6; i++) {
				BlockPos neighbour = pos.offset(EnumFacing.VALUES[i]);
				if (!blocksToNotify.contains(neighbour) && !blocksToUpdate.contains(neighbour))
					blocksToNotify.add(neighbour);
			}
			
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityLittleTiles)
				((TileEntityLittleTiles) te).updateTiles(false);
			world.getBlockState(pos).neighborChanged(world, pos, LittleTiles.blockTileNoTicking, this.pos);
		}
		
		for (BlockPos pos : blocksToNotify)
			world.getBlockState(pos).neighborChanged(world, pos, LittleTiles.blockTileNoTicking, this.pos);
		
		for (int i = 0; i < soundsToBePlayed.size(); i++)
			world.playSound((EntityPlayer) null, pos, soundsToBePlayed.get(i).getPlaceSound(), SoundCategory.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
		
		return result;
	}
	
	public void notifyStructurePlaced() {
		origin.structure.placedStructure(stack);
	}
	
	public void constructStructureRelations() {
		updateRelations(origin);
	}
	
	private void updateRelations(PlacementStructurePreview preview) {
		for (int i = 0; i < preview.children.size(); i++) {
			PlacementStructurePreview child = preview.children.get(i);
			if (preview.structure != null && child.structure != null) {
				preview.structure.updateChildConnection(i, child.structure);
				child.structure.updateParentConnection(i, preview.structure);
			}
			
			updateRelations(child);
		}
	}
	
	public PlacementBlock getorCreateBlock(BlockPos pos) {
		PlacementBlock block = blocks.get(pos);
		if (block == null) {
			block = new PlacementBlock(pos, previews.getContext());
			blocks.put(pos, block);
		}
		return block;
	}
	
	private PlacementStructurePreview createStructureTree(PlacementStructurePreview parent, LittlePreviews previews) {
		PlacementStructurePreview structure = new PlacementStructurePreview(parent, previews.createStructure(), previews instanceof LittlePreviews ? (LittlePreviews) previews : null);
		
		for (LittlePreviews child : previews.getChildren())
			structure.addChild(createStructureTree(structure, child));
		
		return structure;
	}
	
	private void createPreviews(PlacementStructurePreview current, LittleVec inBlockOffset, BlockPos pos) {
		
		if (current.previews != null) {
			HashMapList<BlockPos, PlacePreview> splitted = new HashMapList<BlockPos, PlacePreview>();
			for (PlacePreview pp : current.previews.getPlacePreviews(inBlockOffset))
				pp.split(current.previews.getContext(), splitted, pos);
			
			for (Entry<BlockPos, ArrayList<PlacePreview>> entry : splitted.entrySet())
				getorCreateBlock(entry.getKey()).addPlacePreviews(current.index, entry.getValue());
		}
		
		for (PlacementStructurePreview child : current.children)
			createPreviews(child, inBlockOffset, pos);
	}
	
	public class PlacementBlock implements IGridBased {
		
		private final BlockPos pos;
		private TileEntityLittleTiles cached;
		private LittleGridContext context;
		private final List<PlacePreview>[] previews;
		private final List<PlacePreview>[] latePreviews;
		
		public PlacementBlock(BlockPos pos, LittleGridContext context) {
			this.pos = pos;
			this.context = context;
			previews = new List[structures.size()];
			latePreviews = new List[structures.size()];
		}
		
		@Override
		public LittleGridContext getContext() {
			return context;
		}
		
		public void addPlacePreviews(int index, List<PlacePreview> previews) {
			List<PlacePreview> list = this.previews[index];
			if (list == null)
				this.previews[index] = previews;
			else
				list.addAll(previews);
		}
		
		@Override
		public void convertTo(LittleGridContext to) {
			for (int i = 0; i < previews.length; i++)
				if (previews[i] != null)
					for (PlacePreview preview : previews[i])
						preview.convertTo(this.context, to);
					
			this.context = to;
		}
		
		@Override
		public void convertToSmallest() {
			int size = LittleGridContext.minSize;
			for (int i = 0; i < previews.length; i++)
				if (previews[i] != null)
					for (PlacePreview preview : previews[i])
						size = Math.max(size, preview.getSmallestContext(context));
					
			if (size < context.size)
				convertTo(LittleGridContext.get(size));
		}
		
		private boolean needsCollisionTest() {
			for (int i = 0; i < previews.length; i++)
				if (previews[i] != null)
					for (PlacePreview preview : previews[i])
						if (preview.needsCollisionTest())
							return true;
			return false;
		}
		
		public boolean canPlace() {
			if (!needsCollisionTest())
				return true;
			
			if (!ignoreWorldBoundaries && (pos.getY() < 0 || pos.getY() >= 256))
				return false;
			
			TileEntityLittleTiles te = LittleAction.loadTe(player, world, pos, false);
			if (te != null) {
				LittleGridContext contextBefore = te.getContext();
				te.forceContext(this);
				
				for (int i = 0; i < previews.length; i++)
					if (previews[i] != null)
						for (PlacePreview preview : previews[i])
							if (preview.needsCollisionTest())
								if (mode.checkAll()) {
									if (!te.isSpaceForLittleTile(preview.box, predicate)) {
										if (te.getContext() != contextBefore)
											te.convertTo(contextBefore);
										return false;
									}
								} else if (!te.isSpaceForLittleTileStructure(preview.box, predicate)) {
									if (te.getContext() != contextBefore)
										te.convertTo(contextBefore);
									return false;
								}
							
				cached = te;
				return true;
			}
			
			IBlockState state = world.getBlockState(pos);
			if (state.getMaterial().isReplaceable())
				return true;
			else if (mode.checkAll() || !(LittleAction.isBlockValid(state) && LittleAction.canConvertBlock(player, world, pos, state)))
				return false;
			
			return true;
		}
		
		public boolean combineTilesSecretly() {
			if (cached == null)
				return false;
			if (hasStructure()) {
				for (int i = 0; i < previews.length; i++)
					if (previews[i] != null && structures.get(i).structure != null)
						cached.combineTilesSecretly(structures.get(i).structure);
				return false;
			}
			
			cached.combineTilesSecretly();
			if (cached.size() == 1 && cached.convertBlockToVanilla())
				return true;
			return false;
		}
		
		public boolean hasStructure() {
			for (int i = 0; i < previews.length; i++)
				if (previews[i] != null && structures.get(i).structure != null)
					return true;
			return false;
		}
		
		public void place(PlacementResult result) {
			boolean hascollideBlock = false;
			for (int i = 0; i < previews.length; i++)
				if (previews[i] != null)
					for (Iterator<PlacePreview> iterator = previews[i].iterator(); iterator.hasNext();) {
						PlacePreview preview = iterator.next();
						if (preview.needsCollisionTest())
							hascollideBlock = true;
						else {
							if (latePreviews[i] == null)
								latePreviews[i] = new ArrayList<>();
							latePreviews[i].add(preview);
							iterator.remove();
						}
					}
				
			if (hascollideBlock) {
				boolean requiresCollisionTest = true;
				if (cached == null) {
					if (!(world.getBlockState(pos).getBlock() instanceof BlockTile) && world.getBlockState(pos).getMaterial().isReplaceable()) {
						requiresCollisionTest = false;
						world.setBlockState(pos, BlockTile.getState(false, false));
					}
					
					cached = LittleAction.loadTe(player, world, pos, mode.shouldConvertBlock());
				}
				
				if (cached != null) {
					
					if (cached.isEmpty())
						requiresCollisionTest = false;
					
					final boolean collsionTest = requiresCollisionTest;
					
					cached.forceContext(this);
					
					cached.updateTilesSecretly((x) -> {
						
						for (int i = 0; i < previews.length; i++) {
							if (previews[i] == null)
								continue;
							PlacementStructurePreview structure = structures.get(i);
							for (PlacePreview preview : previews[i]) {
								for (LittleTile LT : preview.placeTile(player, pos, cached.getContext(), cached, x, unplaceableTiles, removedTiles, mode, facing, collsionTest, structure.structure)) {
									if (!soundsToBePlayed.contains(LT.getSound()))
										soundsToBePlayed.add(LT.getSound());
									
									if (structure.structure != null) {
										if (structure.structure.getMainTile() == null)
											structure.structure.setMainTile(LT);
										else {
											LT.connection = structure.structure.getStructureLink(LT);
											LT.connection.setLoadedStructure(structure.structure);
											structure.structure.add(LT);
										}
									}
									
									LT.place(x);
									LT.placed(player, facing);
									
									result.addPlacedTile(LT);
								}
							}
						}
					});
				}
			}
		}
		
		public void placeLate() {
			for (int i = 0; i < latePreviews.length; i++)
				if (latePreviews[i] == null)
					continue;
				else
					for (PlacePreview preview : latePreviews[i])
						preview.placeTile(player, pos, context, cached, null, unplaceableTiles, removedTiles, mode, facing, false, structures.get(i).structure);
		}
	}
	
	public class PlacementStructurePreview {
		
		public final LittleStructure structure;
		public final LittlePreviews previews;
		public final PlacementStructurePreview parent;
		public final int index;
		
		public PlacementStructurePreview(PlacementStructurePreview parent, LittleStructure structure, LittlePreviews previews) {
			this.index = structures.size();
			structures.add(this);
			
			this.parent = parent;
			this.structure = structure;
			this.previews = previews;
		}
		
		List<PlacementStructurePreview> children = new ArrayList<>();
		
		public void addChild(PlacementStructurePreview child) {
			children.add(child);
		}
		
	}
	
}