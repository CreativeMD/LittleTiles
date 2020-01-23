package com.creativemd.littletiles.common.tile.combine;

import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;

public class StructureCombiner extends AdvancedCombiner<LittleTile> {
	
	public StructureCombiner(List<LittleTile> tiles, LittleStructure structure) {
		super(tiles);
		this.structure = structure;
	}
	
	protected LittleStructure structure;
	protected boolean modifiedMainTile = false;
	
	@Override
	public boolean combine() {
		if (!structure.load())
			return false;
		
		boolean changed = super.combine();
		if (modifiedMainTile)
			structure.selectMainTile();
		return changed;
	}
	
	@Override
	public void onCombined(LittleTile first, LittleTile second) {
		super.onCombined(first, second);
		
		if (!first.connection.isLink() || !second.connection.isLink())
			modifiedMainTile = true;
	}
	
	@Override
	protected boolean shouldScan(LittleTile tile) {
		return tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) == structure;
	}
	
	@Override
	protected boolean canCutOut(LittleTile tile, LittleTile toCombine) {
		return tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) == structure && tile.canCombine(toCombine);
	}
	
	@Override
	public void addCuttedTile(LittleTile cutTile) {
		super.addCuttedTile(cutTile);
		structure.add(cutTile);
	}
	
	@Override
	protected void removeBox(int index) {
		structure.remove(tiles.get(index));
		super.removeBox(index);
	}
}
