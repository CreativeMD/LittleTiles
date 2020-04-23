package com.creativemd.littletiles.common.action.block;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.place.Placement;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementResult;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
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
			Placement placement = new Placement(player, PlacementHelper.getAbsolutePreviews(player.world, previews, previews.pos, mode));
			PlacementResult placedTiles = placement.place();
			
			if (placedTiles != null) {
				boxes = placedTiles.placedBoxes;
				
				drainIngredientsAfterPlacing(player, inventory, placedTiles, previews);
				
				if (!player.world.isRemote) {
					giveOrDrop(player, inventory, placement.unplaceableTiles);
					giveOrDrop(player, inventory, placement.removedTiles);
				}
				
				if (!placement.removedTiles.isEmpty()) {
					destroyed = new LittleAbsolutePreviews(previews.pos, previews.getContext());
					for (LittleTile tile : placement.removedTiles)
						destroyed.addTile(tile);
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
	
	protected void drainIngredientsAfterPlacing(EntityPlayer player, LittleInventory inventory, PlacementResult placedTiles, LittlePreviews previews) throws LittleActionException {
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
		
		if (newPreviews.getContext() != box.context)
			if (newPreviews.getContext().size > box.context.size)
				box.convertTo(newPreviews.getContext());
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
		protected void drainIngredientsAfterPlacing(EntityPlayer player, LittleInventory inventory, PlacementResult placedTiles, LittlePreviews previews) throws LittleActionException {
			take(player, inventory, LittleStructurePremade.getStructurePremadeEntry(previews.getStructureId()).stack);
		}
		
		@Override
		protected boolean canDrainIngredientsBeforePlacing(EntityPlayer player, LittleInventory inventory) throws LittleActionException {
			LittleStructurePremadeEntry entry = LittleStructurePremade.getStructurePremadeEntry(previews.getStructureId());
			
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
			
			if (newPreviews.getContext() != box.context)
				if (newPreviews.getContext().size > box.context.size)
					box.convertTo(newPreviews.getContext());
				else
					newPreviews.convertTo(box.context);
				
			newPreviews.flipPreviews(axis, box.getDoubledCenter(newPreviews.pos));
			
			newPreviews.convertToSmallest();
			box.convertToSmallest();
			return new LittleActionPlaceAbsolutePremade(newPreviews, mode, toVanilla);
		}
	}
	
}
