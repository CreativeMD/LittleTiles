package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceRelative.LittlePlaceResult;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class LittleActionPlaceAbsolute extends LittleAction {

	public LittleAbsolutePreviews previews;
	public PlacementMode mode;
	public LittleStructure structure;
	public NBTTagCompound structureNBT;
	public boolean toVanilla;

	public LittleActionPlaceAbsolute(LittleAbsolutePreviews previews, LittleStructure structure, PlacementMode mode, boolean toVanilla) {
		super();
		this.previews = previews;
		this.mode = mode;
		this.structure = structure;
		if (structure != null) {
			this.structureNBT = new NBTTagCompound();
			this.structure.setTiles(null);
			this.structure.writeToNBT(structureNBT);
			this.structure.setTiles(new HashMapList<>());
		}
		this.toVanilla = toVanilla;
		checkMode();
	}

	public LittleActionPlaceAbsolute(LittleAbsolutePreviews previews, PlacementMode mode) {
		this(previews, null, mode, false);
	}

	public LittleActionPlaceAbsolute() {
		super();
	}

	public void checkMode() {
		if (structure != null && !mode.canPlaceStructures()) {
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
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, null, PlacementMode.normal, true));
		}

		return new LittleActionDestroyBoxes(boxes);
	}

	public LittleAbsolutePreviews destroyed;

	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {

		if (canDrainIngredientsBeforePlacing(player)) {
			ArrayList<PlacePreviewTile> placePreviews = new ArrayList<>();
			for (LittleTilePreview preview : previews) {
				placePreviews.add(preview.getPlaceableTile(null, true, LittleTileVec.ZERO));
			}

			if (structure != null) {
				previews.ensureContext(structure.getMinContext());

				for (PlacePreviewTile preview : structure.getSpecialTiles(previews.context)) {
					placePreviews.add(preview);
				}
			}

			List<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			List<LittleTile> removedTiles = new ArrayList<LittleTile>();

			LittlePlaceResult placedTiles = LittleActionPlaceRelative.placeTiles(player.world, player, previews.context, placePreviews, structure, mode, previews.pos, null, unplaceableTiles, removedTiles, EnumFacing.EAST);

			if (placedTiles != null) {
				boxes = placedTiles.placedBoxes;

				drainIngredientsAfterPlacing(player, placedTiles);

				if (!player.world.isRemote) {
					addTilesToInventoryOrDrop(player, unplaceableTiles);
					addTilesToInventoryOrDrop(player, removedTiles);
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

	protected boolean canDrainIngredientsBeforePlacing(EntityPlayer player) throws LittleActionException {
		return canDrainPreviews(player, previews);
	}

	protected void drainIngredientsAfterPlacing(EntityPlayer player, LittlePlaceResult placedTiles) throws LittleActionException {
		drainPreviews(player, placedTiles.placedPreviews);
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writeAbsolutePreviews(previews, buf);
		writePlacementMode(mode, buf);
		buf.writeBoolean(toVanilla);

		if (structure == null)
			buf.writeBoolean(false);
		else {
			buf.writeBoolean(true);
			writeNBT(buf, structureNBT);
		}
	}

	@Override
	public void readBytes(ByteBuf buf) {
		previews = readAbsolutePreviews(buf);
		mode = readPlacementMode(buf);
		toVanilla = buf.readBoolean();

		if (buf.readBoolean()) {
			structure = LittleStructure.createAndLoadStructure(readNBT(buf), null);
			structure.setTiles(new HashMapList<>());
		}

		checkMode();
	}

	public static class LittleActionPlaceAbsolutePremade extends LittleActionPlaceAbsolute {

		public LittleActionPlaceAbsolutePremade(LittleAbsolutePreviews previews, LittleStructure structure, PlacementMode mode, boolean toVanilla) {
			super(previews, structure, mode, toVanilla);
		}

		public LittleActionPlaceAbsolutePremade() {
			super();
		}

		@Override
		protected void drainIngredientsAfterPlacing(EntityPlayer player, LittlePlaceResult placedTiles) throws LittleActionException {
			drainPremadeItemStack(player, LittleStructurePremade.getStructurePremadeEntry(structure.structureID).stack);
		}

		@Override
		protected boolean canDrainIngredientsBeforePlacing(EntityPlayer player) throws LittleActionException {
			LittleStructurePremadeEntry entry = LittleStructurePremade.getStructurePremadeEntry(structure.structureID);
			return canDrainPremadeItemStack(player, entry.stack) && entry.arePreviewsEqual(previews);
		}

	}

}
