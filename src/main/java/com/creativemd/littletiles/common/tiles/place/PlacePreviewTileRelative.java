package com.creativemd.littletiles.common.tiles.place;

import java.util.Collections;
import java.util.List;

import com.creativemd.littletiles.client.render.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.StructureTypeRelative;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileList;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PlacePreviewTileRelative extends PlacePreviewTile {
	
	public StructureTypeRelative relativeType;
	public StructureRelative relative;
	
	public PlacePreviewTileRelative(LittleTileBox box, LittlePreviews structure, StructureRelative relative, StructureTypeRelative relativeType) {
		super(box.copy(), null, structure);
		this.relative = relative;
		this.relativeType = relativeType;
	}
	
	@Override
	public boolean needsCollisionTest() {
		return false;
	}
	
	@Override
	public boolean requiresSplit() {
		return false;
	}
	
	@Override
	public PlacePreviewTile copy() {
		return new PlacePreviewTileRelative(box.copy(), this.structurePreview, relative, relativeType);
	}
	
	@Override
	public List<LittleRenderingCube> getPreviews(LittleGridContext context) {
		List<LittleRenderingCube> cubes = super.getPreviews(context);
		for (LittleRenderingCube cube : cubes) {
			cube.color = relativeType.annotation.color();
		}
		return cubes;
	}
	
	@Override
	public List<LittleTile> placeTile(EntityPlayer player, ItemStack stack, BlockPos pos, LittleGridContext context, TileEntityLittleTiles teLT, TileList list, List<LittleTile> unplaceableTiles, List<LittleTile> removedTiles, PlacementMode mode, EnumFacing facing, boolean requiresCollisionTest) {
		LittleStructure structure = structurePreview.getStructure();
		if (structure.getMainTile() == null && structure.selectMainTile())
			throw new RuntimeException("Invalid structure. Missing main tile!");
		
		relative.setBox(BlockPos.ORIGIN, box.copy(), context);
		relative.add(new LittleTilePos(pos, context).getRelative(structure.getMainTile().getAbsolutePos()));
		relativeType.setRelative(structure, relative);
		return Collections.EMPTY_LIST;
	}
	
}
