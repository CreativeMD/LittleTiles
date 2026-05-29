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
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.exception.AreaTooLarge;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class SelectionBuilder {
    
    public final SelectionParameters selection;
    private final List<LittleStructure> structures;
    private final List<LittleGroup> children = new ArrayList<>();
    private final LittleGroup previews = new LittleGroup();
    private final MutableBlockPos temp = new MutableBlockPos();
    private BlockPos pos;
    
    public SelectionBuilder(SelectionParameters selection) throws AreaTooLarge {
        this.selection = selection;
        this.structures = selection.rememberStructure() ? new ArrayList<>() : null;
    }
    
    public void scanLevel(LevelAccessor level, ABB bb) throws LittleActionException {
        scanLevel(level, Mth.floor(bb.minX), Mth.floor(bb.minY), Mth.floor(bb.minZ), Mth.ceil(bb.maxX), Mth.ceil(bb.maxY), Mth.ceil(bb.maxZ), null);
    }
    
    public void scanLevel(LevelAccessor level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, @Nullable LittleBoxAbsolute box) throws LittleActionException {
        pos = new BlockPos(minX, minY, minZ);
        for (int posX = minX; posX <= maxX; posX++) {
            for (int posY = minY; posY <= maxY; posY++) {
                for (int posZ = minZ; posZ <= maxZ; posZ++) {
                    temp.set(posX, posY, posZ);
                    
                    if (selection.includeBE()) {
                        BlockEntity blockEntity = level.getBlockEntity(temp);
                        
                        if (selection.includeLT() && blockEntity instanceof BETiles be) {
                            Runnable run = () -> {
                                LittleBox intersect = box != null ? box.extractSimple(temp) : null;
                                if (box != null && intersect == null)
                                    return;
                                
                                for (IParentCollection parent : be.groups())
                                    if (selection.rememberStructure() && parent.isStructure()) {
                                        try {
                                            if (box != null && !parent.intersectsWith(intersect))
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
                                            if (box == null)
                                                tile = tile.copy();
                                            else {
                                                List<LittleBox> intersecting = new ArrayList<>();
                                                for (LittleBox tileBox : tile) {
                                                    if (!LittleBox.intersectsWith(tileBox, intersect))
                                                        continue;
                                                    var overlap = tileBox.intersectionExact(parent.getGrid(), intersect, null);
                                                    if (overlap != null)
                                                        intersecting.add(overlap);
                                                }
                                                if (intersecting.isEmpty())
                                                    continue;
                                                tile = tile.copy(intersecting);
                                            }
                                            
                                            tile.move(new LittleVec(parent.getGrid().toGrid(temp.getX() - minX), parent.getGrid().toGrid(temp.getY() - minY), parent.getGrid()
                                                    .toGrid(temp.getZ() - minZ)));
                                            previews.add(parent.getGrid(), tile, tile);
                                        }
                            };
                            
                            if (box != null)
                                be.sameGrid(box, run);
                            else
                                run.run();
                            continue;
                        }
                        
                        if (selection.includeCB()) {
                            LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(blockEntity);
                            if (specialPreviews != null) {
                                if (box != null) {
                                    LittleGroup otherPreviews = new LittleGroup();
                                    final var cBPreviews = specialPreviews;
                                    specialPreviews.sameGrid(box, () -> {
                                        LittleBox intersect = box != null ? box.extractSimple(temp) : null;
                                        if (box != null && intersect == null)
                                            return;
                                        
                                        for (LittleTile tile : otherPreviews) {
                                            List<LittleBox> intersecting = new ArrayList<>();
                                            for (LittleBox tileBox : tile) {
                                                if (!LittleBox.intersectsWith(tileBox, intersect))
                                                    continue;
                                                var overlap = tileBox.intersection(intersect);
                                                if (overlap != null)
                                                    intersecting.add(tileBox);
                                            }
                                            if (!otherPreviews.isEmpty())
                                                otherPreviews.add(cBPreviews.getGrid(), tile, intersecting);
                                        }
                                    });
                                    specialPreviews = otherPreviews;
                                }
                                specialPreviews.move(new LittleVecGrid(new LittleVec(previews.getGrid().toGrid(temp.getX() - minX), previews.getGrid().toGrid(temp
                                        .getY() - minY), previews.getGrid().toGrid(temp.getZ() - minZ)), previews.getGrid()));
                                previews.add(specialPreviews);
                                continue;
                            }
                        }
                    }
                    
                    if (selection.includeVanilla()) {
                        BlockState state = level.getBlockState(temp);
                        if (LittleAction.isBlockValid(state)) {
                            LittleBox blockBox = box != null ? box.extractSimple(temp) : previews.getGrid().box();
                            if (blockBox == null)
                                continue;
                            blockBox.add((posX - minX) * previews.getGrid().count, (posY - minY) * previews.getGrid().count, (posZ - minZ) * previews.getGrid().count);
                            previews.add(previews.getGrid(), new LittleElement(state, ColorUtils.WHITE), blockBox);
                        }
                    }
                }
            }
        }
    }
    
    public void addBoxes(LevelAccessor level, BlockPos pos, LittleBoxes boxes) {
        if (this.pos == null)
            this.pos = pos;
        
        int difX = pos.getX() - this.pos.getX();
        int difY = pos.getY() - this.pos.getY();
        int difZ = pos.getZ() - this.pos.getZ();
        
        if (selection.includeBE()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            
            if (selection.includeLT() && blockEntity instanceof BETiles be) {
                be.sameGrid(boxes, () -> {
                    for (IParentCollection parent : be.groups())
                        if (selection.rememberStructure() && parent.isStructure()) {
                            try {
                                if (!parent.intersectsWith(boxes.all()))
                                    return;
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
                                List<LittleBox> cutout = new ArrayList<>();
                                for (LittleBox box : tile)
                                    box.cutOut(parent.getGrid(), boxes.all(), cutout, null);
                                if (cutout.isEmpty())
                                    continue;
                                tile = tile.copy(cutout);
                                tile.move(new LittleVec(parent.getGrid().toGrid(difX), parent.getGrid().toGrid(difY), parent.getGrid().toGrid(difZ)));
                                previews.add(parent.getGrid(), tile, tile);
                            }
                });
                return;
            }
            
            if (selection.includeCB()) {
                LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(blockEntity);
                if (specialPreviews != null) {
                    LittleGroup otherPreviews = new LittleGroup();
                    final var cBPreviews = specialPreviews;
                    specialPreviews.sameGrid(boxes, () -> {
                        for (LittleTile tile : otherPreviews) {
                            List<LittleBox> cutout = new ArrayList<>();
                            for (LittleBox box : tile)
                                box.cutOut(boxes.getGrid(), boxes.all(), cutout, null);
                            if (cutout.isEmpty())
                                continue;
                            if (!otherPreviews.isEmpty())
                                otherPreviews.add(cBPreviews.getGrid(), tile, cutout);
                        }
                    });
                    specialPreviews = otherPreviews;
                    specialPreviews.move(new LittleVecGrid(new LittleVec(boxes.getGrid().toGrid(difX), boxes.getGrid().toGrid(difY), boxes.getGrid().toGrid(difZ)), boxes
                            .getGrid()));
                    previews.add(specialPreviews);
                    return;
                }
            }
        }
        
        if (selection.includeVanilla()) {
            BlockState state = level.getBlockState(pos);
            if (LittleAction.isBlockValid(state)) {
                List<LittleBox> result = new ArrayList<>();
                LittleVec offset = new LittleVec(difX * previews.getGrid().count, difY * previews.getGrid().count, difZ * previews.getGrid().count);
                for (LittleBox box : boxes.all()) {
                    box = box.copy();
                    box.add(offset);
                    result.add(box);
                }
                previews.add(previews.getGrid(), new LittleElement(state, ColorUtils.WHITE), result);
            }
        }
    }
    
    public LittleGroup build() {
        if (children.isEmpty())
            return previews;
        List<LittleGroup> newChildren = new ArrayList<>();
        for (LittleGroup child : previews.children.children())
            newChildren.add(child);
        newChildren.addAll(children);
        return new LittleGroup(previews, newChildren);
    }
}
