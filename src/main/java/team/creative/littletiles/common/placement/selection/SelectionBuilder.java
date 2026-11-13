package team.creative.littletiles.common.placement.selection;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class SelectionBuilder {
    
    private final List<SelectionEntry> selections = new ArrayList<>();
    
    public void scanLevel(LevelAccessor level, ABB bb) throws LittleActionException {
        scanLevel(level, Mth.floor(bb.minX), Mth.floor(bb.minY), Mth.floor(bb.minZ), Mth.ceil(bb.maxX), Mth.ceil(bb.maxY), Mth.ceil(bb.maxZ), null);
    }
    
    public void scanLevel(LevelAccessor level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @Nullable LittleBoxAbsolute box) throws LittleActionException {
        selections.add(new SelectionArea(level, minX, minY, minZ, maxX, maxY, maxZ, box));
    }
    
    public void addBoxes(LevelAccessor level, BlockPos pos, LittleBoxes boxes) {
        selections.add(new SelectionBlock(level, pos, boxes));
    }
    
    public BlockPos firstPos() {
        return selections.getFirst().pos();
    }
    
    public void eachBlock(SelectionBlockConsumer consumer) {
        for (SelectionEntry entry : selections)
            entry.consume(consumer);
    }
    
    public void eachBE(SelectionBEConsumer consumer) {
        eachBlock((level, pos, boxes) -> {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BETiles be)
                consumer.consume(be, boxes);
        });
    }
    
    public LittleGroup build(final SelectionParameters selection) {
        final List<LittleGroup> children = new ArrayList<>();
        final LittleGroup previews = new LittleGroup();
        final BlockPos basePos = firstPos();
        final List<LittleStructure> structures = selection.rememberStructure() ? new ArrayList<>() : null;
        
        eachBlock((level, pos, boxes) -> {
            int difX = pos.getX() - basePos.getX();
            int difY = pos.getY() - basePos.getY();
            int difZ = pos.getZ() - basePos.getZ();
            
            if (selection.includeBE()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                
                if (selection.includeLT() && blockEntity instanceof BETiles be) {
                    Runnable extractTiles = () -> {
                        for (IParentCollection parent : be.groups())
                            if (selection.rememberStructure() && parent.isStructure()) {
                                try {
                                    if (boxes != null && !parent.intersectsWith(boxes.all()))
                                        continue;
                                    LittleStructure structure = parent.getStructure();
                                    while (structure.getParent() != null)
                                        structure = structure.getParent().getStructure();
                                    structure.checkConnections();
                                    if (!structures.contains(structure)) {
                                        children.add(structure.getPreviews(pos));
                                        structures.add(structure);
                                    }
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                    continue;
                                }
                                
                            } else
                                for (LittleTile tile : parent) {
                                    if (boxes != null) {
                                        List<LittleBox> cutout = new ArrayList<>();
                                        for (LittleBox box : tile)
                                            box.cutOut(parent.getGrid(), boxes.all(), cutout, null);
                                        if (cutout.isEmpty())
                                            continue;
                                        tile = tile.copy(cutout);
                                    } else
                                        tile = tile.copy();
                                    
                                    tile.move(new LittleVec(parent.getGrid(), difX, difY, difZ));
                                    previews.add(parent.getGrid(), tile, tile);
                                }
                    };
                    
                    if (boxes != null)
                        be.sameGrid(boxes, extractTiles);
                    else
                        extractTiles.run();
                } else if (selection.includeCB()) {
                    final LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(blockEntity);
                    if (specialPreviews != null) {
                        LittleGroup extractPreviews = new LittleGroup();
                        Runnable extractTiles = () -> {
                            for (LittleTile tile : specialPreviews) {
                                Iterable<LittleBox> extracted = null;
                                if (boxes != null) {
                                    List<LittleBox> cutout = new ArrayList<>();
                                    for (LittleBox box : tile)
                                        box.cutOut(boxes.getGrid(), boxes.all(), cutout, null);
                                    if (cutout.isEmpty())
                                        continue;
                                    extracted = cutout;
                                } else
                                    extracted = tile.copy();
                                extractPreviews.add(specialPreviews.getGrid(), tile, extracted);
                            }
                        };
                        
                        if (boxes != null)
                            specialPreviews.sameGrid(boxes, extractTiles);
                        else
                            extractTiles.run();
                        extractPreviews.move(new LittleVecGrid(new LittleVec(extractPreviews.getGrid(), difX, difY, difZ), extractPreviews.getGrid()));
                        previews.add(extractPreviews);
                    }
                }
            } else if (selection.includeVanilla()) {
                BlockState state = level.getBlockState(pos);
                if (LittleAction.isBlockValid(state)) {
                    List<LittleBox> result = new ArrayList<>();
                    LittleVec offset = new LittleVec(previews.getGrid(), difX, difY, difZ);
                    if (boxes != null)
                        for (LittleBox box : boxes.all()) {
                            box = box.copy();
                            box.add(offset);
                            result.add(box);
                        }
                    else
                        result.add(LittleBox.of(previews.getGrid()));
                    previews.add(previews.getGrid(), new LittleElement(state, ColorUtils.WHITE), result);
                }
            }
        });
        
        if (children.isEmpty())
            return previews;
        List<LittleGroup> newChildren = new ArrayList<>();
        for (LittleGroup child : previews.children.children())
            newChildren.add(child);
        newChildren.addAll(children);
        return new LittleGroup(previews, newChildren);
    }
    
    public LittleGroupAbsolute build(BiFilter<IParentCollection, LittleTile> filter) {
        var group = new LittleGroupAbsolute(firstPos());
        eachBE((be, boxes) -> {
            Runnable extractTiles = () -> {
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
                    if (filter.is(pair.key, pair.value)) {
                        LittleTile tile = pair.value;
                        if (boxes != null) {
                            List<LittleBox> cutout = new ArrayList<>();
                            for (LittleBox box : tile)
                                box.cutOut(be.getGrid(), boxes.all(), cutout, null);
                            if (cutout.isEmpty())
                                continue;
                            tile = tile.copy(cutout);
                        } else
                            tile = tile.copy();
                        
                        group.addFast(pair.key, tile);
                    }
                }
            };
            if (boxes != null)
                be.sameGrid(boxes, extractTiles);
            else
                extractTiles.run();
        });
        group.convertToSmallest();
        return group;
    }
    
    @FunctionalInterface
    private static interface SelectionBlockConsumer {
        
        public void consume(LevelAccessor level, BlockPos pos, @Nullable LittleBoxes boxes);
        
    }
    
    @FunctionalInterface
    private static interface SelectionBEConsumer {
        
        public void consume(BETiles be, @Nullable LittleBoxes boxes);
        
    }
    
    private static abstract class SelectionEntry {
        
        public final LevelAccessor level;
        
        public SelectionEntry(LevelAccessor level) {
            this.level = level;
        }
        
        public abstract BlockPos pos();
        
        public abstract void consume(SelectionBlockConsumer consumer);
        
    }
    
    private static class SelectionBlock extends SelectionEntry {
        
        public final BlockPos pos;
        public final LittleBoxes boxes;
        
        public SelectionBlock(LevelAccessor level, BlockPos pos, LittleBoxes boxes) {
            super(level);
            this.pos = pos;
            this.boxes = boxes;
        }
        
        @Override
        public BlockPos pos() {
            return pos;
        }
        
        @Override
        public void consume(SelectionBlockConsumer consumer) {
            consumer.consume(level, pos, boxes);
        }
    }
    
    private static class SelectionArea extends SelectionEntry {
        
        public final int minX;
        public final int minY;
        public final int minZ;
        public final int maxX;
        public final int maxY;
        public final int maxZ;
        @Nullable
        public final LittleBoxAbsolute box;
        
        public SelectionArea(LevelAccessor level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @Nullable LittleBoxAbsolute box) {
            super(level);
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.box = box;
        }
        
        @Override
        public BlockPos pos() {
            return new BlockPos(minX, minY, minZ);
        }
        
        @Override
        public void consume(SelectionBlockConsumer consumer) {
            MutableBlockPos temp = new MutableBlockPos();
            for (int posX = minX; posX <= maxX; posX++) {
                for (int posY = minY; posY <= maxY; posY++) {
                    for (int posZ = minZ; posZ <= maxZ; posZ++) {
                        temp.set(posX, posY, posZ);
                        LittleBoxes boxes = null;
                        if (box != null) {
                            boxes = new LittleBoxesSimple(temp.immutable(), box.grid);
                            boxes.add(box.extractSimple(temp));
                        }
                        consumer.consume(level, temp, boxes);
                    }
                }
            }
        }
    }
}
