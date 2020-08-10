package com.creativemd.littletiles.common.tile.combine;

import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;

public class BasicCombiner {
	
	public static boolean combineBoxes(List<LittleBox> boxes) {
		return new BasicCombiner(boxes).combine();
	}
	
	public static boolean combineTiles(List<LittleTile> tiles) {
		return new AdvancedCombiner<>(tiles).combine();
	}
	
	public static boolean combinePreviews(List<LittlePreview> previews) {
		return new AdvancedCombiner<>(previews).combine();
	}
	
	protected List<LittleBox> boxes;
	protected int i;
	protected int j;
	protected boolean modified;
	
	public BasicCombiner(List<LittleBox> boxes) {
		this.boxes = boxes;
	}
	
	public void set(List<LittleBox> boxes) {
		if (getClass() != BasicCombiner.class)
			throw new RuntimeException("Illegal action! Boxes cannot be set for advanced combiners!");
		
		this.boxes = boxes;
	}
	
	public List<LittleBox> getBoxes() {
		return boxes;
	}
	
	public boolean combine() {
		int sizeBefore = boxes.size();
		modified = true;
		while (modified) {
			modified = false;
			i = 0;
			while (i < boxes.size()) {
				j = 0;
				while (j < boxes.size()) {
					if (i != j) {
						LittleBox box = boxes.get(i).combineBoxes(boxes.get(j), this);
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
		boolean changed = sizeBefore != boxes.size();
		this.boxes = null;
		return changed;
	}
	
	public boolean cutOut(LittleBox searching) {
		boolean intersects = false;
		for (LittleBox box : boxes) {
			if (searching.getClass() == box.getClass()) {
				if (box.containsBox(searching)) {
					List<LittleBox> cutOut = box.cutOut(searching);
					if (cutOut != null)
						boxes.addAll(cutOut);
					removeBox(box);
					return true;
				} else if (LittleBox.intersectsWith(box, searching)) {
					intersects = true;
					break;
				}
			}
		}
		
		if (intersects) {
			LittleVec size = searching.getSize();
			boolean[][][] filled = new boolean[size.x][size.y][size.z];
			
			for (Iterator<LittleBox> iterator = boxes.iterator(); iterator.hasNext();) {
				LittleBox box = iterator.next();
				
				if (LittleBox.intersectsWith(box, searching) && searching.getClass() == box.getClass())
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
				LittleBox box = boxes.get(i);
				
				if (LittleBox.intersectsWith(box, searching) && searching.getClass() == box.getClass()) {
					List<LittleBox> cutOut = box.cutOut(searching);
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
	
	public void removeBox(LittleBox box) {
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