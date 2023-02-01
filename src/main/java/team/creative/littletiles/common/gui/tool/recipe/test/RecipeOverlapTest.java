package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import team.creative.littletiles.common.block.little.tile.LittleTile;
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
    
    private void addOverlay(GuiTreeItemStructure other) {
        LittleBoxesNoOverlap boxes = overlapped.get(other);
        if (boxes == null)
            overlapped.put(other, boxes = new LittleBoxesNoOverlap(BlockPos.ZERO, other.group.getGrid()));
    }
    
    @Override
    public void test(GuiTreeItemStructure item, RecipeTestResults results) {
        MutableBlockPos pos = new MutableBlockPos();
        LittleGrid grid = item.group.getGrid();
        for (LittleTile tile : item.group)
            for (LittleBox box : tile)
                box.splitIterator(grid, pos, LittleVec.ZERO, (x, y) -> getOrCreate(x).add(item, y, results));
            
        overlapped = null;
        
    }
    
    @Override
    public void endTest(GuiRecipe recipe, RecipeTestResults results) {
        blocks = null;
    }
    
    public class RecipeOverlayTestBlock {
        
        public HashMap<GuiTreeItemStructure, List<LittleBox>> structureBoxes;
        private LittleGrid grid;
        
        public RecipeOverlayTestBlock(LittleGrid grid) {
            this.grid = grid;
        }
        
        public void add(GuiTreeItemStructure item, LittleBox box) {
            for (Entry<GuiTreeItemStructure, List<LittleBox>> entry : structureBoxes.entrySet()) {
                for (LittleBox other : entry.getValue()) {
                    if (LittleBox.intersectsWith(box, other)) {
                        
                    }
                }
            }
        }
        
    }
    
    public static class SelfOverlayError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private LittleBoxesNoOverlap boxes;
        
        public SelfOverlayError(GuiTreeItemStructure structure) {
            this.structure = structure;
        }
        
    }
    
    public static class OverlayError extends RecipeTestError {
        
        private GuiTreeItemStructure structure;
        private GuiTreeItemStructure structure2;
        
        private LittleBoxesNoOverlap boxes;
        
    }
    
}
