package com.creativemd.littletiles.common.tile.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PlacePreview {
	
	public LittleBox box;
	public LittlePreview preview;
	
	public PlacePreview(LittleBox box, LittlePreview preview) {
		this.box = box;
		this.preview = preview;
	}
	
	/** NEEDS TO BE OVERRIDEN! ALWAYS! **/
	public PlacePreview copy() {
		return new PlacePreview(box.copy(), preview.copy());
	}
	
	/** If false it will be placed after all regular tiles have been placed **/
	public boolean needsCollisionTest() {
		return true;
	}
	
	public List<LittleRenderingCube> getPreviews(LittleGridContext context) {
		ArrayList<LittleRenderingCube> previews = new ArrayList<>();
		previews.add(box.getRenderingCube(context, null, 0));
		return previews;
	}
	
	public List<LittleTile> placeTile(@Nullable EntityPlayer player, BlockPos pos, LittleGridContext context, TileEntityLittleTiles te, TileList list, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, @Nullable EnumFacing facing, boolean requiresCollisionTest, LittleStructure structure) {
		LittleTile LT = preview.getLittleTile(te);
		
		if (LT == null)
			return Collections.EMPTY_LIST;
		
		LT.box = box.copy();
		return mode.placeTile(te, LT, unplaceableTiles, removedTiles, requiresCollisionTest);
	}
	
	public PlacePreview copyWithBox(LittleBox box) {
		PlacePreview tile = this.copy();
		tile.box = box;
		return tile;
	}
	
	public boolean split(LittleGridContext context, HashMapList<BlockPos, PlacePreview> tiles, BlockPos pos) {
		if (!requiresSplit()) {
			tiles.add(pos, this);
			return true;
		}
		
		HashMapList<BlockPos, LittleBox> boxes = new HashMapList<>();
		this.box.split(context, pos, boxes);
		for (Entry<BlockPos, ArrayList<LittleBox>> entry : boxes.entrySet()) {
			for (LittleBox box : entry.getValue()) {
				tiles.add(entry.getKey(), this.copyWithBox(box));
			}
		}
		
		return true;
	}
	
	public boolean requiresSplit() {
		return true;
	}
	
	public void add(LittleVec vec) {
		box.add(vec);
	}
	
	public void convertTo(LittleGridContext context, LittleGridContext to) {
		box.convertTo(context, to);
	}
	
	public int getSmallestContext(LittleGridContext context) {
		return box.getSmallestContext(context);
	}
	
}
