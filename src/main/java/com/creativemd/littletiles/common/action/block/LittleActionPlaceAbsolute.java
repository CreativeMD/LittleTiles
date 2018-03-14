package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class LittleActionPlaceAbsolute extends LittleAction {
	
	public LittleAbsolutePreviews previews;
	public PlacementMode mode;
	public LittleStructure structure;
	public boolean toVanilla;
	
	public LittleActionPlaceAbsolute(LittleAbsolutePreviews previews, LittleStructure structure, PlacementMode mode, boolean toVanilla) {
		super();
		this.previews = previews;
		this.mode = mode;
		this.structure = structure;
		this.toVanilla = toVanilla;
		checkMode();
	}
	
	public LittleActionPlaceAbsolute(LittleAbsolutePreviews previews, PlacementMode mode) {
		this(previews, null, mode, false);
	}
	
	public LittleActionPlaceAbsolute() {
		super();
	}
	
	public void checkMode()
	{
		if(structure != null && !mode.canPlaceStructures())
		{
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
		
		if(destroyed != null)
		{
			destroyed.convertToSmallest();
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, null, PlacementMode.normal, true));
		}	
			
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public LittleAbsolutePreviews destroyed;

	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		
		if(canDrainPreviews(player, previews))
		{
			ArrayList<PlacePreviewTile> placePreviews = new ArrayList<>();
			LittleTileVec zero = new LittleTileVec(0, 0, 0);
			for (LittleTilePreview preview : previews) {
				placePreviews.add(preview.getPlaceableTile(null, true, zero));
			}
			
			List<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			List<LittleTile> removedTiles = new ArrayList<LittleTile>();
			
			List<LittleTile> placedTiles = LittleActionPlaceRelative.placeTiles(player.world, player, previews.context, placePreviews, structure, mode, previews.pos, null, unplaceableTiles, removedTiles, EnumFacing.EAST);
			
			boxes = new LittleBoxes(previews.pos, LittleGridContext.get());
			if(placedTiles != null)
			{
				drainPreviews(player, getIngredientsPreviews(placedTiles));
				
				if(!player.world.isRemote)
				{
					addTilesToInventoryOrDrop(player, unplaceableTiles);
					addTilesToInventoryOrDrop(player, removedTiles);
				}
				
				for (LittleTile tile : placedTiles) {
					boxes.addBox(tile);
				}
				
				if(!removedTiles.isEmpty())
				{
					destroyed = new LittleAbsolutePreviews(previews.pos, previews.context);
					for (LittleTile tile : removedTiles) {
						destroyed.addTile(tile);
					}
				}
				
				if(toVanilla)
				{
					BlockPos lastPos = null;
					for (LittleTile tile : placedTiles) {
						if(tile.te.getPos() != lastPos)
						{
							lastPos = tile.te.getPos();
							tile.te.convertBlockToVanilla();
						}
					}
				}
			}
			
			return placedTiles != null;
		}
		return false;
	}

	@Override
	public void writeBytes(ByteBuf buf) {
		writeAbsolutePreviews(previews, buf);
		writePlacementMode(mode, buf);
		buf.writeBoolean(toVanilla);
		
		if(structure == null)
			buf.writeBoolean(false);
		else
		{
			buf.writeBoolean(true);
			NBTTagCompound structureNBT = new NBTTagCompound();
			structure.setTiles(null);
			structure.writeToNBT(structureNBT);
			structure.setTiles(new HashMapList<>());
			writeNBT(buf, structureNBT);
		}
	}

	@Override
	public void readBytes(ByteBuf buf) {
		previews = readAbsolutePreviews(buf);
		mode = readPlacementMode(buf);
		toVanilla = buf.readBoolean();
		
		if(buf.readBoolean())
		{
			structure = LittleStructure.createAndLoadStructure(readNBT(buf), null);
			structure.setTiles(new HashMapList<>());
		}
		
		checkMode();
	}

}
