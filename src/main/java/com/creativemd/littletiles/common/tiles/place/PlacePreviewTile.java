package com.creativemd.littletiles.common.tiles.place;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.common.utils.ColoredCube;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.combine.BasicCombiner;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.jcraft.jorbis.Block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlacePreviewTile {
	
	public static final Vec3d white = new Vec3d(1, 1, 1);

	public LittleTileBox box;
	public LittleTilePreview preview;
	
	public PlacePreviewTile(LittleTileBox box, LittleTilePreview preview)
	{
		this.box = box;
		this.preview = preview;
	}
	
	/**NEEDS TO BE OVERRIDEN! ALWAYS!**/
	public PlacePreviewTile copy()
	{
		return new PlacePreviewTile(box.copy(), preview.copy());
	}
	
	/**If false it will be placed after all regular tiles have been placed**/
	public boolean needsCollisionTest()
	{
		return true;
	}
	
	public List<LittleRenderingCube> getPreviews()
	{
		ArrayList<LittleRenderingCube> previews = new ArrayList<>();
		previews.add(box.getRenderingCube(null, 0));
		return previews;
	}
	
	public List<LittleTile> placeTile(@Nullable EntityPlayer player, @Nullable ItemStack stack, BlockPos pos, TileEntityLittleTiles te, LittleStructure structure, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, @Nullable EnumFacing facing, boolean requiresCollisionTest)
	{
		LittleTile LT = preview.getLittleTile(te);
		
		if(LT == null)
			return Collections.EMPTY_LIST;
		
		LT.box = box.copy();
		
		List<LittleTile> tiles = mode.placeTile(te, LT, unplaceableTiles, removedTiles, requiresCollisionTest);
		
		for (LittleTile tile : tiles) {
			if(structure != null)
			{
				tile.isStructureBlock = true;
				tile.structure = structure;
				structure.addTile(tile);
			}
			tile.place();
			tile.onPlaced(player, stack, facing);
		}
		
		return tiles;
	}
	
	public boolean split(HashMapList<BlockPos, PlacePreviewTile> tiles, BlockPos pos)
	{		
		if(preview != null && !preview.canSplit && box.needsMultipleBlocks())
			return false;
		
		HashMapList<BlockPos, LittleTileBox> boxes = new HashMapList<>();
		this.box.split(boxes);
		for (Entry<BlockPos, ArrayList<LittleTileBox>> entry : boxes.entrySet()) {
			for (LittleTileBox box : entry.getValue()) {
				PlacePreviewTile tile = this.copy();
				tile.box = box;
				tiles.add(entry.getKey().add(pos), tile);
			}
		}
		
		return true;
	}
	
}
