package com.creativemd.littletiles.common.tiles.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.client.render.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlacePreviewTile {
	
	public static final Vec3d white = new Vec3d(1, 1, 1);
	
	public LittleTileBox box;
	public LittleTilePreview preview;
	
	public LittlePreviews structurePreview;
	
	public PlacePreviewTile(LittleTileBox box, LittleTilePreview preview, LittlePreviews previews) {
		this.box = box;
		this.preview = preview;
		if (previews != null && previews.hasStructure())
			this.structurePreview = previews;
	}
	
	/** NEEDS TO BE OVERRIDEN! ALWAYS! **/
	public PlacePreviewTile copy() {
		return new PlacePreviewTile(box.copy(), preview.copy(), structurePreview);
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
	
	public List<LittleTile> placeTile(@Nullable EntityPlayer player, @Nullable ItemStack stack, BlockPos pos, LittleGridContext context, TileEntityLittleTiles te, TileList list, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, @Nullable EnumFacing facing, boolean requiresCollisionTest) {
		LittleTile LT = preview.getLittleTile(te);
		
		if (LT == null)
			return Collections.EMPTY_LIST;
		
		LT.box = box.copy();
		
		List<LittleTile> tiles = mode.placeTile(te, LT, unplaceableTiles, removedTiles, requiresCollisionTest);
		
		for (LittleTile tile : tiles) {
			tile.place(list);
			tile.onPlaced(player, stack, facing);
		}
		
		return tiles;
	}
	
	public PlacePreviewTile copyWithBox(LittleTileBox box) {
		PlacePreviewTile tile = this.copy();
		tile.box = box;
		return tile;
	}
	
	public boolean split(LittleGridContext context, HashMapList<BlockPos, PlacePreviewTile> tiles, BlockPos pos) {
		if (!requiresSplit()) {
			tiles.add(pos, this);
			return true;
		}
		
		if (canSplit() && box.needsMultipleBlocks(context))
			return false;
		
		HashMapList<BlockPos, LittleTileBox> boxes = new HashMapList<>();
		this.box.split(context, pos, boxes);
		for (Entry<BlockPos, ArrayList<LittleTileBox>> entry : boxes.entrySet()) {
			for (LittleTileBox box : entry.getValue()) {
				tiles.add(entry.getKey(), this.copyWithBox(box));
			}
		}
		
		return true;
	}
	
	public boolean requiresSplit() {
		return true;
	}
	
	public boolean canSplit() {
		return preview != null && !preview.canSplit;
	}
	
	public void add(LittleTileVec vec) {
		box.add(vec);
	}
	
	public void convertTo(LittleGridContext context, LittleGridContext to) {
		box.convertTo(context, to);
	}
	
	public int getSmallestContext(LittleGridContext context) {
		return box.getSmallestContext(context);
	}
	
}
