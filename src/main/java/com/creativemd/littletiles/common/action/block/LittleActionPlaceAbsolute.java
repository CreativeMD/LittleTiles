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
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class LittleActionPlaceAbsolute extends LittleAction {
	
	public List<LittleTilePreview> previews;
	public PlacementMode mode;
	public LittleStructure structure;
	public boolean toVanilla;
	
	public LittleActionPlaceAbsolute(List<LittleTilePreview> previews, LittleStructure structure, PlacementMode mode, boolean toVanilla) {
		super();
		this.previews = previews;
		this.mode = mode;
		this.structure = structure;
		this.toVanilla = toVanilla;
		checkMode();
	}
	
	public LittleActionPlaceAbsolute(List<LittleTilePreview> previews, PlacementMode mode) {
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
	
	public List<LittleTileBox> boxes;

	@Override
	public LittleAction revert() {
		if(destroyed != null)
			return new LittleActionCombined(new LittleActionDestroyBoxes(boxes), new LittleActionPlaceAbsolute(destroyed, null, PlacementMode.normal, true));
		return new LittleActionDestroyBoxes(boxes);
	}
	
	public List<LittleTile> placedTiles;
	public List<LittleTilePreview> destroyed;

	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		
		if(drainPreviews(player, previews))
		{
			BlockPos pos = previews.get(0).box.getMinVec().getBlockPos();
			LittleTileVec offset = new LittleTileVec(pos);
			LittleTileVec zero = new LittleTileVec(0, 0, 0);
			ArrayList<PlacePreviewTile> placePreviews = new ArrayList<>();
			boxes = new ArrayList<>();
			
			for (LittleTilePreview preview : previews) {
				preview = preview.copy();
				preview.box.subOffset(offset);
				placePreviews.add(preview.getPlaceableTile(null, true, offset));
			}
			
			List<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
			List<LittleTile> removedTiles = new ArrayList<LittleTile>();
			
			placedTiles = LittleActionPlaceRelative.placeTiles(player.world, player, placePreviews, structure, mode, pos, null, unplaceableTiles, removedTiles, EnumFacing.EAST);
			
			if(placedTiles != null)
			{
				if(!player.world.isRemote)
				{
					addTilesToInventoryOrDrop(player, unplaceableTiles);
					addTilesToInventoryOrDrop(player, removedTiles);
				}
				
				for (LittleTile tile : placedTiles) {
					LittleTileBox box = tile.box.copy();
					box.addOffset(tile.te.getPos());
					boxes.add(box);
				}
				
				if(!removedTiles.isEmpty())
				{
					destroyed = new ArrayList<>();
					for (LittleTile tile : removedTiles) {
						LittleTilePreview preview = tile.getPreviewTile();
						preview.box.addOffset(tile.te.getPos());
						destroyed.add(preview);
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
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("list", LittleNBTCompressionTools.writePreviews(previews));
		writeNBT(buf, nbt);
		writeString(buf, mode.name);
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
		previews = LittleNBTCompressionTools.readPreviews(readNBT(buf).getTagList("list", 10));
		mode = PlacementMode.getModeOrDefault(readString(buf));
		toVanilla = buf.readBoolean();
		
		if(buf.readBoolean())
		{
			structure = LittleStructure.createAndLoadStructure(readNBT(buf), null);
			structure.setTiles(new HashMapList<>());
		}
		
		checkMode();
	}

}
