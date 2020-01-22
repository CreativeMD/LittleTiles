package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack.LittlePlaceResult;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tiles.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tiles.math.vec.LittleVec;
import com.creativemd.littletiles.common.tiles.place.PlacePreview;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredient;
import com.creativemd.littletiles.common.utils.ingredients.LittleIngredients;
import com.creativemd.littletiles.common.utils.ingredients.LittleInventory;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public class LittleActionPlaceAbsolute extends LittleAction {
	
	public LittleAbsolutePreviews previews;
	public PlacementMode mode;
	public boolean toVanilla;
	
	public LittleActionPlaceAbsolute(LittleAbsolutePreviews previews, PlacementMode mode, boolean toVanilla) {
		super();
		this.previews = previews;
		this.mode = mode;
		this.toVanilla = toVanilla;
		checkMode();
	}
	
	public LittleActionPlaceAbsolute(LittleAbsolutePreviews previews, PlacementMode mode) {
		this(previews, mode, false);
	}
	
	public LittleActionPlaceAbsolute() {
		super();
	}
	
	public void checkMode() {
		if (previews != null && previews.hasStructure() && !mode.canPlaceStructures()) {
			System.out.println("Using invalid mode for placing structure. mode=" + mode.name);
			this.mode = PlacementMode.getStructureDefault();
		}
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	public LittleBoxes boxes;
	
	@Override
	public LittleAction revert() {
		boxes.convertToSmallest();
		
		if (destroyed != null) {
			destroyed.convertToSmallest();
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, PlacementMode.normal, true));
		}
		
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public LittleAbsolutePreviews destroyed;
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		LittleInventory inventory = new LittleInventory(player);
		if (canDrainIngredientsBeforePlacing(player, inventory)) {
			List<PlacePreview> placePreviews = new ArrayList<>();
			previews.getPlacePreviews(placePreviews, null, true, LittleVec.ZERO);
			
			List<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			List<LittleTile> removedTiles = new ArrayList<LittleTile>();
			
			LittlePlaceResult placedTiles = LittleActionPlaceStack.placeTiles(player.world, player, previews.context, placePreviews, previews.getStructure(), mode, previews.pos, null, unplaceableTiles, removedTiles, EnumFacing.EAST);
			
			if (placedTiles != null) {
				boxes = placedTiles.placedBoxes;
				
				drainIngredientsAfterPlacing(player, inventory, placedTiles, previews);
				
				if (!player.world.isRemote) {
					giveOrDrop(player, inventory, unplaceableTiles);
					giveOrDrop(player, inventory, removedTiles);
				}
				
				if (!removedTiles.isEmpty()) {
					destroyed = new LittleAbsolutePreviews(previews.pos, previews.context);
					for (LittleTile tile : removedTiles) {
						destroyed.addTile(tile);
					}
				}
				
				if (toVanilla) {
					for (TileEntityLittleTiles te : placedTiles.tileEntities) {
						te.convertBlockToVanilla();
					}
				}
			} else
				boxes = new LittleBoxes(previews.pos, LittleGridContext.get());
			
			return placedTiles != null;
		}
		return false;
	}
	
	protected boolean canDrainIngredientsBeforePlacing(EntityPlayer player, LittleInventory inventory) throws LittleActionException {
		return canTake(player, inventory, getIngredients(previews));
	}
	
	protected void drainIngredientsAfterPlacing(EntityPlayer player, LittleInventory inventory, LittlePlaceResult placedTiles, LittlePreviews previews) throws LittleActionException {
		LittleIngredients ingredients = LittleIngredient.extractStructureOnly(previews);
		ingredients.add(getIngredients(placedTiles.placedPreviews));
		take(player, inventory, ingredients);
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePreviews(previews, buf);
		writePlacementMode(mode, buf);
		buf.writeBoolean(toVanilla);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		previews = (LittleAbsolutePreviews) readPreviews(buf);
		mode = readPlacementMode(buf);
		toVanilla = buf.readBoolean();
		
		checkMode();
	}
	
	@Override
	public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
		LittleAbsolutePreviews newPreviews = previews.copy();
		
		if (newPreviews.context != box.context)
			if (newPreviews.context.size > box.context.size)
				box.convertTo(newPreviews.context);
			else
				newPreviews.convertTo(box.context);
			
		newPreviews.flipPreviews(axis, box.getDoubledCenter(newPreviews.pos));
		
		newPreviews.convertToSmallest();
		box.convertToSmallest();
		return new LittleActionPlaceAbsolute(newPreviews, mode, toVanilla);
	}
	
	public static class LittleActionPlaceAbsolutePremade extends LittleActionPlaceAbsolute {
		
		public LittleActionPlaceAbsolutePremade(LittleAbsolutePreviews previews, PlacementMode mode, boolean toVanilla) {
			super(previews, mode, toVanilla);
		}
		
		public LittleActionPlaceAbsolutePremade() {
			super();
		}
		
		@Override
		protected void drainIngredientsAfterPlacing(EntityPlayer player, LittleInventory inventory, LittlePlaceResult placedTiles, LittlePreviews previews) throws LittleActionException {
			take(player, inventory, LittleStructurePremade.getStructurePremadeEntry(previews.getStructure().type.id).stack);
		}
		
		@Override
		protected boolean canDrainIngredientsBeforePlacing(EntityPlayer player, LittleInventory inventory) throws LittleActionException {
			LittleStructurePremadeEntry entry = LittleStructurePremade.getStructurePremadeEntry(previews.getStructure().type.id);
			
			try {
				inventory.startSimulation();
				return take(player, inventory, entry.stack) && entry.arePreviewsEqual(previews);
			} finally {
				inventory.stopSimulation();
			}
		}
		
		@Override
		public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
			LittleAbsolutePreviews newPreviews = previews.copy();
			
			if (newPreviews.context != box.context)
				if (newPreviews.context.size > box.context.size)
					box.convertTo(newPreviews.context);
				else
					newPreviews.convertTo(box.context);
				
			newPreviews.flipPreviews(axis, box.getDoubledCenter(newPreviews.pos));
			
			newPreviews.convertToSmallest();
			box.convertToSmallest();
			return new LittleActionPlaceAbsolutePremade(newPreviews, mode, toVanilla);
		}
	}
	
}
