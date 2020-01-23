package com.creativemd.littletiles.common.tile.combine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;

public class AdvancedCombiner<T extends ICombinable> extends BasicCombiner {
	
	protected List<T> tiles;
	protected T currentTile;
	
	public AdvancedCombiner(List<T> tiles) {
		super(new ArrayList<>());
		setCombinables(tiles);
	}
	
	public void setCombinables(List<T> tiles) {
		boxes.clear();
		for (T tile : tiles) {
			boxes.add(tile.getBox());
		}
		this.tiles = tiles;
	}
	
	public List<T> getCombinables() {
		return tiles;
	}
	
	public void onCombined(T first, T second) {
		
	}
	
	protected boolean shouldScan(T tile) {
		return !tiles.get(i).isChildOfStructure();
	}
	
	@Override
	public boolean combine() {
		int sizeBefore = tiles.size();
		modified = true;
		while (modified) {
			modified = false;
			
			i = 0;
			while (i < tiles.size()) {
				if (!shouldScan(tiles.get(i))) {
					i++;
					continue;
				}
				
				j = 0;
				while (j < tiles.size()) {
					if (!shouldScan(tiles.get(j))) {
						j++;
						continue;
					}
					
					if (i != j && tiles.get(i).canCombine(tiles.get(j))) {
						this.currentTile = tiles.get(i);
						LittleBox box = tiles.get(i).getBox().combineBoxes(tiles.get(j).getBox(), this);
						if (box != null) {
							onCombined(tiles.get(i), tiles.get(j));
							tiles.get(i).setBox(box);
							tiles.get(i).combine(tiles.get(j));
							tiles.remove(j);
							boxes.set(i, box);
							boxes.remove(j);
							if (i > j)
								i--;
							modified = true;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}
		boolean changed = sizeBefore != tiles.size();
		this.tiles = null;
		this.currentTile = null;
		return changed;
	}
	
	public void addCuttedTile(T cutTile) {
		tiles.add(cutTile);
	}
	
	protected boolean canCutOut(T tile, T searching) {
		return !tile.isChildOfStructure() && tile.canCombine(searching);
	}
	
	public boolean cutOut(LittleBox searching, T toCombine) {
		boolean intersects = false;
		for (T tile : tiles) {
			if (tile.getBox().containsBox(searching)) {
				boolean canBeCombined = canCutOut(tile, toCombine);
				
				if (canBeCombined && searching.getClass() == tile.getBox().getClass()) {
					List<LittleBox> cutOut = tile.getBox().cutOut(searching);
					if (cutOut != null) {
						boxes.addAll(cutOut);
						for (LittleBox cutBox : cutOut) {
							T cutTile = (T) toCombine.copy();
							cutTile.setBox(cutBox);
							addCuttedTile(cutTile);
						}
					}
					removeBox(tile.getBox());
					return true;
				}
			} else if (LittleBox.intersectsWith(tile.getBox(), searching)) {
				intersects = true;
				break;
			}
		}
		
		if (intersects) {
			LittleVec size = searching.getSize();
			boolean[][][] filled = new boolean[size.x][size.y][size.z];
			
			for (Iterator<T> iterator = tiles.iterator(); iterator.hasNext();) {
				T tile = iterator.next();
				
				if (LittleBox.intersectsWith(tile.getBox(), searching)) {
					boolean canBeCombined = tile.canCombine(toCombine);
					
					if (canBeCombined && searching.getClass() == tile.getBox().getClass()) {
						tile.fillInSpace(searching, filled);
					}
				}
			}
			
			for (int x = 0; x < filled.length; x++) {
				for (int y = 0; y < filled[x].length; y++) {
					for (int z = 0; z < filled[x][y].length; z++) {
						if (!filled[x][y][z])
							return false;
					}
				}
			}
			
			int i = 0;
			while (i < tiles.size()) {
				T tile = tiles.get(i);
				
				if (LittleBox.intersectsWith(tile.getBox(), searching)) {
					boolean canBeCombined = canCutOut(tile, toCombine);
					
					if (canBeCombined && searching.getClass() == tile.getBox().getClass()) {
						List<LittleBox> cutOut = tile.getBox().cutOut(searching);
						if (cutOut != null) {
							boxes.addAll(cutOut);
							for (LittleBox cutBox : cutOut) {
								T cutTile = (T) toCombine.copy();
								cutTile.setBox(cutBox);
								addCuttedTile(cutTile);
							}
						}
						removeBox(tile.getBox());
						continue;
					}
				}
				i++;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean cutOut(LittleBox searching) {
		if (currentTile != null)
			return cutOut(searching, currentTile);
		
		return super.cutOut(searching);
	}
	
	@Override
	protected void removeBox(int index) {
		super.removeBox(index);
		tiles.remove(index);
	}
}