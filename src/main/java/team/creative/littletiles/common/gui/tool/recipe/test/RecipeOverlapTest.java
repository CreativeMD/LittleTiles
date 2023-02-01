package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.vec.LittleVec;

public class RecipeOverlapTest extends RecipeTestModule {
    
    private HashMap<BlockPos, RecipeOverlayTestBlock> blocks;
    private HashMap<GuiTreeItemStructure, LittleBoxesNoOverlap> overlapped;
    
    @Override
    public void startTest(GuiRecipe recipe, RecipeTestResults results) {
        blocks = new HashMap<>();
    }
    
    private RecipeOverlayTestBlock getOrCreate(BlockPos pos) {
        RecipeOverlayTestBlock block = blocks.get(pos);
        if (block == null)
            blocks.put(pos.immutable(), block = new RecipeOverlayTestBlock());
        return block;
    }
    
    private void addOverlay(GuiTreeItemStructure other, BlockPos pos, LittleGrid grid, LittleBox box) {
        LittleBoxesNoOverlap boxes = overlapped.get(other);
        if (boxes == null)
            overlapped.put(other, boxes = new LittleBoxesNoOverlap(BlockPos.ZERO, grid));
        boxes.addBox(grid, pos, box);
    }
    
    @Override
    public void test(GuiTreeItemStructure item, RecipeTestResults results) {
        overlapped = null;
        
        MutableBlockPos pos = new MutableBlockPos();
        LittleGrid grid = item.group.getGrid();
        for (LittleTile tile : item.group)
            for (LittleBox box : tile)
                box.splitIterator(grid, pos, LittleVec.ZERO, (x, y) -> getOrCreate(x).add(item, x, grid, y));
            
        if (overlapped != null) {
            for (Entry<GuiTreeItemStructure, LittleBoxesNoOverlap> entry : overlapped.entrySet()) {
                if (entry.getKey() == item)
                    results.reportError(new SelfOverlayError(item, entry.getValue()));
                else
                    results.reportError(new OverlayError(item, entry.getKey(), entry.getValue()));
            }
        }
        
        overlapped = null;
        
    }
    
    @Override
    public void endTest(GuiRecipe recipe, RecipeTestResults results) {
        blocks = null;
    }
    
    public class RecipeOverlayTestBlock implements IGridBased {
        
        public HashMap<GuiTreeItemStructure, List<LittleBox>> structureBoxes;
        private LittleGrid grid = LittleGrid.min();
        
        public void add(GuiTreeItemStructure item, BlockPos pos, LittleGrid grid, LittleBox box) {
            if (grid.count > this.grid.count)
                convertTo(grid);
            else if (grid.count < this.grid.count)
                box.convertTo(grid, this.grid);
            
            for (Entry<GuiTreeItemStructure, List<LittleBox>> entry : structureBoxes.entrySet()) {
                for (LittleBox other : entry.getValue()) {
                    if (LittleBox.intersectsWith(box, other)) {
                        LittleBox intersecting = box.intersection(other);
                        addOverlay(entry.getKey(), pos, grid, intersecting);
                    }
                }
            }
            
            List<LittleBox> boxes = structureBoxes.get(item);
            if (boxes == null)
                structureBoxes.put(item, boxes = new ArrayList<>());
            boxes.add(box);
        }
        
        @Override
        public LittleGrid getGrid() {
            return grid;
        }
        
        @Override
        public void convertTo(LittleGrid to) {
            for (List<LittleBox> boxes : structureBoxes.values())
                for (LittleBox box : boxes)
                    box.convertTo(grid, to);
            this.grid = to;
        }
        
        @Override
        public int getSmallest() {
            int smallest = 0;
            for (List<LittleBox> boxes : structureBoxes.values())
                for (LittleBox box : boxes)
                    smallest = Math.max(smallest, box.getSmallest(grid));
            return smallest;
        }
        
    }
    
    public static class SelfOverlayError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private final LittleBoxesNoOverlap boxes;
        
        public SelfOverlayError(GuiTreeItemStructure structure, LittleBoxesNoOverlap boxes) {
            this.structure = structure;
            this.boxes = boxes;
        }
        
    }
    
    public static class OverlayError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private final GuiTreeItemStructure structure2;
        private final LittleBoxesNoOverlap boxes;
        
        public OverlayError(GuiTreeItemStructure structure, GuiTreeItemStructure structure2, LittleBoxesNoOverlap boxes) {
            this.structure = structure;
            this.structure2 = structure2;
            this.boxes = boxes;
        }
        
    }
    
}
