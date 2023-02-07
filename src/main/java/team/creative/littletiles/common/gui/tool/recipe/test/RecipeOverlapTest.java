package team.creative.littletiles.common.gui.tool.recipe.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.type.itr.ArrayIterator;
import team.creative.creativecore.common.util.type.itr.SingleIterator;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.collection.LittleCollection;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipe;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.vec.LittleVec;

public class RecipeOverlapTest extends RecipeTestModule {
    
    public static void removeOverlap(GuiTreeItemStructure item, LittleBoxesNoOverlap boxes) {
        List<LittleBox> cutter = boxes.all();
        List<LittleBox> cutout = new ArrayList<>();
        for (Iterator<LittleTile> iterator = item.group.iterator(); iterator.hasNext();) {
            LittleTile tile = iterator.next();
            tile.cutOut(cutter, cutout, null);
            if (tile.isEmpty())
                iterator.remove();
        }
        
        if (item.group.isEmpty())
            item.recipe.removeItem(item);
    }
    
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
        if (overlapped == null)
            overlapped = new HashMap<>();
        
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
                    results.reportError(new SelfOverlapError(item, entry.getValue()));
                else
                    results.reportError(new OverlapError(item, entry.getKey(), entry.getValue()));
            }
        }
        
        overlapped = null;
    }
    
    @Override
    public void endTest(GuiRecipe recipe, RecipeTestResults results) {
        blocks = null;
    }
    
    public class RecipeOverlayTestBlock implements IGridBased {
        
        public HashMap<GuiTreeItemStructure, List<LittleBox>> structureBoxes = new HashMap<>();
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
    
    public static class SelfOverlapError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private final LittleBoxesNoOverlap boxes;
        
        public SelfOverlapError(GuiTreeItemStructure structure, LittleBoxesNoOverlap boxes) {
            this.structure = structure;
            this.boxes = boxes;
        }
        
        @Override
        public Component header() {
            return GuiControl.translatable("gui.recipe.test.overlap.self.title", structure.getTitle());
        }
        
        @Override
        public Component description() {
            int volume = boxes.littleVolume();
            if (volume >= boxes.grid.count3d)
                return GuiControl.translatable("gui.recipe.test.overlap.desc.large", TooltipUtils.print(boxes.grid.pixelVolume * volume));
            return GuiControl.translatable("gui.recipe.test.overlap.desc.small", TooltipUtils.print(volume), boxes.grid);
        }
        
        @Override
        public Component tooltip(GuiTreeItemStructure structure) {
            return header();
        }
        
        @Override
        public Iterator<GuiTreeItemStructure> iterator() {
            return new SingleIterator<>(structure);
        }
        
        private void add(LittleElement element, LittleBox box, MutableBlockPos pos, LittleGrid grid, HashMap<BlockPos, LittleCollection> blocks) {
            LittleCollection collection = blocks.get(pos);
            if (collection == null)
                blocks.put(pos.immutable(), collection = new LittleCollection());
            final Iterator<LittleBox> itr = collection.boxes();
            for (LittleBox placedBox : (Iterable<LittleBox>) () -> itr) {
                if (LittleBox.intersectsWith(box, placedBox)) {
                    List<LittleBox> left = box.cutOut(placedBox, null);
                    if (!left.isEmpty())
                        for (LittleBox leftBox : left)
                            add(element, leftBox, pos, grid, blocks);
                    return;
                }
            }
            
            collection.add(element, box);
        }
        
        @Override
        public void create(GuiRecipe recipe, GuiParent parent, Runnable refresh) {
            parent.add(new GuiButton("fix", x -> {
                LittleGroup group = structure.group;
                LittleGrid grid = group.getGrid();
                HashMap<BlockPos, LittleCollection> blocks = new HashMap<>();
                
                MutableBlockPos pos = new MutableBlockPos();
                for (LittleTile tile : group)
                    for (LittleBox toAdd : tile) {
                        toAdd.setMinPos(pos, grid);
                        toAdd.splitIterator(grid, pos, LittleVec.ZERO, (blockPos, littleBox) -> add(tile, littleBox, blockPos, grid, blocks));
                    }
                
                group = new LittleGroup(group.getStructureTag(), Collections.EMPTY_LIST);
                LittleVec vec = new LittleVec(0, 0, 0);
                for (Entry<BlockPos, LittleCollection> entry : blocks.entrySet()) {
                    vec.set(grid, entry.getKey());
                    for (LittleTile tile : entry.getValue())
                        for (LittleBox box : tile) {
                            box.add(vec);
                            group.addTile(grid, tile);
                        }
                    
                }
                group.combine();
                structure.group = group;
                
                refresh.run();
            }).setTranslate("gui.recipe.test.overlap.fix"));
        }
    }
    
    public static class OverlapError extends RecipeTestError {
        
        private final GuiTreeItemStructure structure;
        private final GuiTreeItemStructure structure2;
        private final LittleBoxesNoOverlap boxes;
        
        public OverlapError(GuiTreeItemStructure structure, GuiTreeItemStructure structure2, LittleBoxesNoOverlap boxes) {
            this.structure = structure;
            this.structure2 = structure2;
            this.boxes = boxes;
        }
        
        @Override
        public Component header() {
            return GuiControl.translatable("gui.recipe.test.overlap.title", structure.getTitle(), structure2.getTitle());
        }
        
        @Override
        public Component description() {
            int volume = boxes.littleVolume();
            if (volume >= boxes.grid.count3d)
                return GuiControl.translatable("gui.recipe.test.overlap.desc.large", TooltipUtils.print(boxes.grid.pixelVolume * volume));
            return GuiControl.translatable("gui.recipe.test.overlap.desc.small", TooltipUtils.print(volume), boxes.grid);
        }
        
        @Override
        public Iterator<GuiTreeItemStructure> iterator() {
            return new ArrayIterator<>(structure, structure2);
        }
        
        @Override
        public Component tooltip(GuiTreeItemStructure structure) {
            return GuiControl.translatable("gui.recipe.test.overlap.tooltip", structure == this.structure ? structure2.getTitle() : this.structure.getTitle());
        }
        
        @Override
        public void create(GuiRecipe recipe, GuiParent parent, Runnable refresh) {
            parent.add(new GuiLabel("remove").setTranslate("gui.recipe.test.overlap.remove"));;
            parent.add(new GuiButton("remove", x -> {
                removeOverlap(structure, boxes);
                refresh.run();
            }).setTitle(Component.literal(this.structure.getTitle())));
            parent.add(new GuiButton("remove2", x -> {
                removeOverlap(structure2, boxes);
                refresh.run();
            }).setTitle(Component.literal(this.structure2.getTitle())));
            parent.add(new GuiButton("move", x -> recipe.OPEN_MOVE.open(new CompoundTag()).init(recipe)).setTranslate("gui.recipe.test.overlap.move"));
        }
        
    }
    
}
