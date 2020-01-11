package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy.StructurePreview;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;
import com.creativemd.littletiles.common.utils.ingredients.LittleInventory;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.selection.selector.TileSelector;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class LittleActionDestroyBoxes extends LittleActionBoxes {
	
	public LittleActionDestroyBoxes(LittleBoxes boxes) {
		super(boxes);
	}
	
	public LittleActionDestroyBoxes() {
		
	}
	
	public List<StructurePreview> destroyedStructures;
	public LittleAbsolutePreviews previews;
	
	private boolean containsStructure(LittleStructure structure) {
		for (StructurePreview structurePreview : destroyedStructures) {
			if (structurePreview.structure == structure)
				return true;
		}
		return false;
	}
	
	public boolean shouldSkipTile(LittleTile tile) {
		return false;
	}
	
	public boolean doneSomething;
	
	public LittleIngredients action(EntityPlayer player, TileEntityLittleTiles te, List<LittleTileBox> boxes, boolean simulate, LittleGridContext context) {
		doneSomething = false;
		
		if (previews == null)
			previews = new LittleAbsolutePreviews(te.getPos(), context);
		
		LittleIngredients ingredients = new LittleIngredients();
		
		Consumer<TileList> consumer = x -> {
			
			for (LittleTile tile : te) {
				
				if (shouldSkipTile(tile))
					continue;
				
				LittleTileBox intersecting = null;
				boolean intersects = false;
				for (int j = 0; j < boxes.size(); j++) {
					if (tile.intersectsWith(boxes.get(j))) {
						intersects = true;
						intersecting = boxes.get(j);
						break;
					}
				}
				
				if (!intersects)
					continue;
				
				doneSomething = true;
				if (!tile.isChildOfStructure() && tile.canBeSplitted() && !tile.equalsBox(intersecting)) {
					double volume = 0;
					LittleTilePreview preview = tile.getPreviewTile();
					
					List<LittleTileBox> cutout = new ArrayList<>();
					List<LittleTileBox> newBoxes = tile.cutOut(boxes, cutout);
					
					if (newBoxes != null) {
						if (!simulate) {
							for (int i = 0; i < newBoxes.size(); i++) {
								LittleTile newTile = tile.copy();
								newTile.box = newBoxes.get(i);
								newTile.place(x);
							}
							
							tile.destroy(x);
						}
						
						for (int l = 0; l < cutout.size(); l++) {
							volume += cutout.get(l).getPercentVolume(context);
							if (!simulate) {
								LittleTilePreview preview2 = preview.copy();
								preview2.box = cutout.get(l).copy();
								previews.addPreview(te.getPos(), preview2, te.getContext());
							}
						}
					}
					
					if (volume > 0)
						ingredients.add(getIngredients(preview, volume));
				} else {
					if (!tile.isChildOfStructure())
						ingredients.add(getIngredients(tile));
					
					if (!simulate) {
						if (tile.isChildOfStructure()) {
							LittleStructure structure;
							if (tile.isConnectedToStructure() && (structure = tile.connection.getStructure(te.getWorld())).load() && !containsStructure(structure)) {
								destroyedStructures.add(new StructurePreview(structure));
								ItemStack drop = structure.getStructureDrop();
								if (needIngredients(player) && !player.world.isRemote && !InventoryUtils.addItemStackToInventory(player.inventory, drop))
									WorldUtils.dropItem(player.world, drop, tile.te.getPos());
								
								tile.destroy(x);
							}
						} else {
							previews.addTile(tile);
							tile.destroy(x);
						}
					}
					
				}
			}
		};
		if (simulate)
			te.updateTilesSecretly(consumer);
		else
			te.updateTiles(consumer);
		
		return ingredients;
	}
	
	@Override
	public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes, LittleGridContext context) throws LittleActionException {
		TileEntity tileEntity = loadTe(player, world, pos, true);
		
		if (tileEntity instanceof TileEntityLittleTiles) {
			if (!world.isRemote) {
				BreakEvent event = new BreakEvent(world, tileEntity.getPos(), ((TileEntityLittleTiles) tileEntity).getBlockTileState(), player);
				MinecraftForge.EVENT_BUS.post(event);
				if (event.isCanceled()) {
					sendBlockResetToClient(world, (EntityPlayerMP) player, (TileEntityLittleTiles) tileEntity);
					return;
				}
			}
			
			((TileEntityLittleTiles) tileEntity).ensureMinContext(context);
			
			if (context != ((TileEntityLittleTiles) tileEntity).getContext()) {
				for (LittleTileBox box : boxes) {
					box.convertTo(context, ((TileEntityLittleTiles) tileEntity).getContext());
				}
				context = ((TileEntityLittleTiles) tileEntity).getContext();
			}
			
			if (checkAndGive(player, new LittleInventory(player), action(player, (TileEntityLittleTiles) tileEntity, boxes, true, context)))
				action(player, (TileEntityLittleTiles) tileEntity, boxes, false, context);
			
			((TileEntityLittleTiles) tileEntity).combineTiles();
			
			if (!doneSomething)
				((TileEntityLittleTiles) tileEntity).convertBlockToVanilla();
		}
	}
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		destroyedStructures = new ArrayList<>();
		return super.action(player);
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	@Override
	public LittleAction revert() {
		boolean additionalPreviews = previews != null && previews.size() > 0;
		LittleAction[] actions = new LittleAction[(additionalPreviews ? 1 : 0) + destroyedStructures.size()];
		if (additionalPreviews) {
			previews.convertToSmallest();
			actions[0] = new LittleActionPlaceAbsolute(previews, PlacementMode.fill, true);
		}
		for (int i = 0; i < destroyedStructures.size(); i++) {
			actions[additionalPreviews ? 1 : 0 + i] = destroyedStructures.get(i).getPlaceAction();
		}
		return new LittleActionCombined(actions);
	}
	
	public static class LittleActionDestroyBoxesFiltered extends LittleActionDestroyBoxes {
		
		public TileSelector selector;
		
		public LittleActionDestroyBoxesFiltered(LittleBoxes boxes, TileSelector selector) {
			super(boxes);
			this.selector = selector;
		}
		
		public LittleActionDestroyBoxesFiltered() {
			
		}
		
		@Override
		public void writeBytes(ByteBuf buf) {
			super.writeBytes(buf);
			writeSelector(selector, buf);
		}
		
		@Override
		public void readBytes(ByteBuf buf) {
			super.readBytes(buf);
			selector = readSelector(buf);
		}
		
		@Override
		public boolean shouldSkipTile(LittleTile tile) {
			return !selector.is(tile);
		}
		
	}
	
	public static List<LittleTile> removeBox(TileEntityLittleTiles te, LittleGridContext context, LittleTileBox toCut, boolean update) {
		if (context != te.getContext()) {
			if (context.size > te.getContext().size)
				te.convertTo(context);
			else {
				toCut.convertTo(context, te.getContext());
				context = te.getContext();
			}
		}
		
		List<LittleTile> removed = new ArrayList<>();
		
		Consumer<TileList> consumer = x -> {
			for (LittleTile tile : te) {
				
				if (!tile.intersectsWith(toCut) || tile.isChildOfStructure() || !tile.canBeSplitted())
					continue;
				
				tile.destroy(x);
				
				if (!tile.equalsBox(toCut)) {
					double volume = 0;
					LittleTilePreview preview = tile.getPreviewTile();
					
					List<LittleTileBox> cutout = new ArrayList<>();
					List<LittleTileBox> boxes = new ArrayList<>();
					boxes.add(toCut);
					List<LittleTileBox> newBoxes = tile.cutOut(boxes, cutout);
					
					if (newBoxes != null) {
						for (LittleTileBox box : newBoxes) {
							LittleTile copy = tile.copy();
							copy.box = box;
							copy.place(x);
						}
						
						for (LittleTileBox box : cutout) {
							LittleTile copy = tile.copy();
							copy.box = box;
							removed.add(copy);
						}
					}
				} else
					removed.add(tile);
			}
		};
		
		if (update)
			te.updateTiles(consumer);
		else
			te.updateTilesSecretly(consumer);
		return removed;
	}
	
	public static List<LittleTile> removeBoxes(TileEntityLittleTiles te, LittleGridContext context, List<LittleTileBox> boxes) {
		if (context != te.getContext()) {
			if (context.size > te.getContext().size)
				te.convertTo(context);
			else {
				for (LittleTileBox box : boxes) {
					box.convertTo(context, te.getContext());
				}
				context = te.getContext();
			}
		}
		List<LittleTile> removed = new ArrayList<>();
		te.updateTiles(x -> {
			for (LittleTile tile : te) {
				
				LittleTileBox intersecting = null;
				boolean intersects = false;
				for (int j = 0; j < boxes.size(); j++) {
					if (tile.intersectsWith(boxes.get(j))) {
						intersects = true;
						intersecting = boxes.get(j);
						break;
					}
				}
				
				if (!intersects)
					continue;
				
				if (tile.isChildOfStructure() || !tile.canBeSplitted())
					continue;
				
				tile.destroy(x);
				
				if (!tile.equalsBox(intersecting)) {
					double volume = 0;
					LittleTilePreview preview = tile.getPreviewTile();
					
					List<LittleTileBox> cutout = new ArrayList<>();
					List<LittleTileBox> newBoxes = tile.cutOut(boxes, cutout);
					
					if (newBoxes != null) {
						for (LittleTileBox box : newBoxes) {
							LittleTile copy = tile.copy();
							copy.box = box;
							copy.place(x);
						}
						
						for (LittleTileBox box : cutout) {
							LittleTile copy = tile.copy();
							copy.box = box;
							removed.add(copy);
						}
					}
				} else
					removed.add(tile);
			}
		});
		
		return removed;
	}
}
