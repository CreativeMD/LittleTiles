package com.creativemd.littletiles.common.tiles.combine;

import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;

public class StructureCombiner extends AdvancedCombiner<LittleTile> {
	
	public StructureCombiner(List<LittleTile> tiles, LittleStructure structure) {
		super(tiles);
		this.structure = structure;
	}
	
	protected LittleStructure structure;
	protected boolean modifiedMainTile = false;
	
	@Override
	public void combine() {
		if (!structure.hasLoaded())
			return;
		
		super.combine();
		if (modifiedMainTile)
			structure.selectMainTile();
	}
	
	@Override
	public void onCombined(LittleTile first, LittleTile second) {
		super.onCombined(first, second);
		if (first.isMainBlock || second.isMainBlock)
			modifiedMainTile = true;
	}
	
	@Override
	protected boolean shouldScan(LittleTile tile) {
		return tile.isChildOfStructure() && tile.structure == structure;
	}
	
	@Override
	protected boolean canCutOut(LittleTile tile, LittleTile toCombine) {
		return tile.isChildOfStructure() && tile.structure == structure && tile.canCombine(toCombine);
	}
	
	@Override
	public void addCuttedTile(LittleTile cutTile) {
		super.addCuttedTile(cutTile);
		structure.addTile(cutTile);
	}
	
	@Override
	protected void removeBox(int index) {
		structure.removeTile(tiles.get(index));
		super.removeBox(index);
	}
}
