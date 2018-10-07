package com.creativemd.littletiles.common.tiles.combine;

import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;

public class BasicCombiner {
	
	public static void combineBoxes(List<LittleTileBox> boxes) {
		new BasicCombiner(boxes).combine();
	}
	
	public static void combineTiles(List<LittleTile> tiles) {
		new AdvancedCombiner<>(tiles).combine();
	}
	
	public static void combineTiles(List<LittleTile> tiles, LittleStructure structure) {
		new StructureCombiner(tiles, structure).combine();
	}
	
	public static void combinePreviews(List<LittleTilePreview> previews) {
		new AdvancedCombiner<>(previews).combine();
	}
	
	protected List<LittleTileBox> boxes;
	protected int i;
	protected int j;
	protected boolean modified;
	
	public BasicCombiner(List<LittleTileBox> boxes) {
		this.boxes = boxes;
	}
	
	public void set(List<LittleTileBox> boxes) {
		if (getClass() != BasicCombiner.class)
			throw new RuntimeException("Illegal action! Boxes cannot be set for advanced combiners!");
		
		this.boxes = boxes;
	}
	
	public List<LittleTileBox> getBoxes() {
		return boxes;
	}
	
	public void combine() {
		modified = true;
		while (modified) {
			modified = false;
			i = 0;
			while (i < boxes.size()) {
				j = 0;
				while (j < boxes.size()) {
					if (i != j) {
						LittleTileBox box = boxes.get(i).combineBoxes(boxes.get(j), this);
						if (box != null) {
							boxes.set(i, box);
							boxes.remove(j);
							modified = true;
							if (i > j)
								i--;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}
		this.boxes = null;
	}
	
	public boolean cutOut(LittleTileBox searching) {
		boolean intersects = false;
		for (LittleTileBox box : boxes) {
			if (searching.getClass() == box.getClass()) {
				if (box.containsBox(searching)) {
					List<LittleTileBox> cutOut = box.cutOut(searching);
					if (cutOut != null)
						boxes.addAll(cutOut);
					removeBox(box);
					return true;
				} else if (LittleTileBox.intersectsWith(box, searching)) {
					intersects = true;
					break;
				}
			}
		}
		
		if (intersects) {
			LittleTileSize size = searching.getSize();
			boolean[][][] filled = new boolean[size.sizeX][size.sizeY][size.sizeZ];
			
			for (Iterator<LittleTileBox> iterator = boxes.iterator(); iterator.hasNext();) {
				LittleTileBox box = iterator.next();
				
				if (LittleTileBox.intersectsWith(box, searching) && searching.getClass() == box.getClass())
					box.fillInSpace(searching, filled);
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
			while (i < boxes.size()) {
				LittleTileBox box = boxes.get(i);
				
				if (LittleTileBox.intersectsWith(box, searching) && searching.getClass() == box.getClass()) {
					List<LittleTileBox> cutOut = box.cutOut(searching);
					if (cutOut != null)
						boxes.addAll(cutOut);
					removeBox(box);
					continue;
				}
				i++;
			}
			
			return true;
		}
		
		return false;
	}
	
	public void removeBox(LittleTileBox box) {
		int index = boxes.indexOf(box);
		if (index != -1) {
			if (i > index)
				i--;
			if (j > index)
				j--;
			modified = true;
			removeBox(index);
		}
	}
	
	protected void removeBox(int index) {
		boxes.remove(index);
	}
}