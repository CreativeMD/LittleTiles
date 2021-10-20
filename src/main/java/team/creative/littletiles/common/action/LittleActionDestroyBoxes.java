package team.creative.littletiles.common.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.action.LittleActionDestroy.StructurePreview;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETiles.BlockEntityInteractor;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.ParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleActionDestroyBoxes extends LittleActionBoxes {
    
    public LittleActionDestroyBoxes(Level level, LittleBoxes boxes) {
        super(level, boxes);
    }
    
    public LittleActionDestroyBoxes(UUID levelUUID, LittleBoxes boxes) {
        super(levelUUID, boxes);
    }
    
    public LittleActionDestroyBoxes() {}
    
    public List<StructurePreview> destroyedStructures;
    public LittleGroupAbsolute previews;
    
    private boolean containsStructure(LittleStructure structure) {
        for (StructurePreview structurePreview : destroyedStructures) {
            if (structurePreview.structure == structure)
                return true;
        }
        return false;
    }
    
    public boolean shouldSkipTile(IParentCollection parent, LittleTile tile) {
        return false;
    }
    
    public boolean doneSomething;
    
    public LittleIngredients action(Player player, BETiles be, List<LittleBox> boxes, boolean simulate, LittleGrid grid) {
        doneSomething = false;
        
        if (previews == null)
            previews = new LittleGroupAbsolute(be.getBlockPos());
        
        LittleIngredients ingredients = new LittleIngredients();
        
        List<LittleTile> placedTiles = new ArrayList<>();
        List<LittleTile> destroyedTiles = new ArrayList<>();
        
        for (IParentCollection parent : be.groups()) {
            if (parent.isStructure()) {
                if (simulate)
                    continue;
                
                boolean intersects = false;
                outer_loop: for (LittleTile tile : parent)
                    for (int j = 0; j < boxes.size(); j++)
                        if (tile.intersectsWith(boxes.get(j))) {
                            intersects = true;
                            break outer_loop;
                        }
                    
                if (!intersects)
                    continue;
                
                try {
                    LittleStructure structure = parent.getStructure();
                    if (!containsStructure(structure))
                        destroyedStructures.add(new StructurePreview(structure));
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                
            } else {
                for (LittleTile tile : parent) {
                    
                    if (shouldSkipTile(parent, tile))
                        continue;
                    
                    LittleBox intersecting = null;
                    boolean intersects = false;
                    for (int j = 0; j < boxes.size(); j++) {
                        if (tile.intersectsWith(boxes.get(j))) {
                            intersects = true;
                            intersecting = boxes.get(j);
                            break;
                        }
                    }
                    
                    if (!intersects)
                        continue;
                    
                    doneSomething = true;
                    if (!tile.equalsBox(intersecting)) {
                        double volume = 0;
                        LittlePreview preview = tile.getPreviewTile();
                        
                        List<LittleBox> cutout = new ArrayList<>();
                        LittleBoxReturnedVolume returnedVolume = new LittleBoxReturnedVolume();
                        List<LittleBox> newBoxes = tile.cutOut(boxes, cutout, returnedVolume);
                        
                        if (newBoxes != null) {
                            if (!simulate) {
                                for (int i = 0; i < newBoxes.size(); i++) {
                                    LittleTile newTile = tile.copy();
                                    newTile.setBox(newBoxes.get(i));
                                    placedTiles.add(newTile);
                                }
                                
                                destroyedTiles.add(tile);
                                
                            }
                            
                            for (int l = 0; l < cutout.size(); l++) {
                                volume += cutout.get(l).getPercentVolume(context);
                                if (!simulate) {
                                    LittlePreview preview2 = preview.copy();
                                    preview2.box = cutout.get(l).copy();
                                    previews.addPreview(te.getPos(), preview2, context);
                                }
                            }
                        }
                        
                        if (volume > 0)
                            ingredients.add(getIngredients(preview, volume));
                        if (returnedVolume.has())
                            ingredients.add(getIngredients(preview, returnedVolume.getPercentVolume(context)));
                    } else {
                        ingredients.add(getIngredients(parent, tile));
                        
                        if (!simulate) {
                            previews.addTile(parent, tile);
                            destroyedTiles.add(tile);
                        }
                    }
                }
            }
        }
        
        if (!simulate) {
            for (StructurePreview structure : destroyedStructures)
                try {
                    if (!structure.structure.mainBlock.isRemoved())
                        structure.structure.onLittleTileDestroy();
                } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            be.updateTiles(x -> {
                ParentCollection parent = x.noneStructureTiles();
                parent.removeAll(destroyedTiles);
                parent.addAll(placedTiles);
            });
        }
        
        return ingredients;
        
    }
    
    @Override
    public void action(Level world, Player player, BlockPos pos, BlockState state, List<LittleBox> boxes, LittleGrid grid) throws LittleActionException {
        fireBlockBreakEvent(world, pos, player);
        
        BlockEntity blockEntity = loadBE(player, world, pos, null, true, 0);
        
        if (blockEntity instanceof BETiles) {
            BETiles be = (BETiles) blockEntity;
            
            if (grid != be.getGrid()) {
                if (grid.count < be.getGrid().count) {
                    for (LittleBox box : boxes)
                        box.convertTo(grid, be.getGrid());
                    grid = be.getGrid();
                } else
                    be.convertTo(grid);
            }
            
            if (checkAndGive(player, new LittleInventory(player), action(player, be, boxes, true, grid)))
                action(player, be, boxes, false, grid);
            
            be.combineTiles();
            
            if (!doneSomething)
                be.convertBlockToVanilla();
        }
    }
    
    @Override
    public boolean action(Player player) throws LittleActionException {
        destroyedStructures = new ArrayList<>();
        return super.action(player);
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(Player player) {
        boolean additionalPreviews = previews != null && previews.size() > 0;
        LittleAction[] actions = new LittleAction[(additionalPreviews ? 1 : 0) + destroyedStructures.size()];
        if (additionalPreviews) {
            previews.convertToSmallest();
            actions[0] = new LittleActionPlaceAbsolute(previews, PlacementMode.fill, true);
        }
        for (int i = 0; i < destroyedStructures.size(); i++)
            actions[(additionalPreviews ? 1 : 0) + i] = destroyedStructures.get(i).getPlaceAction();
        return new LittleActions(actions);
    }
    
    public static class LittleActionDestroyBoxesFiltered extends LittleActionDestroyBoxes {
        
        public BiFilter<IParentCollection, LittleTile> filter;
        
        public LittleActionDestroyBoxesFiltered(Level level, LittleBoxes boxes, BiFilter<IParentCollection, LittleTile> filter) {
            super(level, boxes);
            this.filter = filter;
        }
        
        public LittleActionDestroyBoxesFiltered() {
            
        }
        
        @Override
        public boolean shouldSkipTile(IParentCollection parent, LittleTile tile) {
            return !filter.is(parent, tile);
        }
        
    }
    
    public static List<LittleTile> removeBox(BETiles be, LittleGrid grid, LittleBox toCut, boolean update) {
        if (grid != be.getGrid()) {
            if (grid.count > be.getGrid().count)
                be.convertTo(grid);
            else {
                toCut.convertTo(grid, be.getGrid());
                grid = be.getGrid();
            }
        }
        
        List<LittleTile> removed = new ArrayList<>();
        
        Consumer<BlockEntityInteractor> consumer = x -> {
            List<LittleTile> toAdd = new ArrayList<>();
            for (LittleTile tile : x.noneStructureTiles()) {
                
                if (!tile.intersectsWith(toCut))
                    continue;
                
                x.noneStructureTiles().remove(tile);
                
                if (!tile.equalsBox(toCut)) {
                    double volume = 0;
                    LittlePreview preview = tile.getPreviewTile();
                    
                    List<LittleBox> cutout = new ArrayList<>();
                    List<LittleBox> boxes = new ArrayList<>();
                    boxes.add(toCut);
                    LittleBoxReturnedVolume returnedVolume = new LittleBoxReturnedVolume();
                    List<LittleBox> newBoxes = tile.cutOut(boxes, cutout, returnedVolume);
                    
                    if (newBoxes != null) {
                        for (LittleBox box : newBoxes) {
                            LittleTile copy = tile.copy();
                            copy.setBox(box);
                            toAdd.add(copy);
                        }
                        
                        for (LittleBox box : cutout) {
                            LittleTile copy = tile.copy();
                            copy.setBox(box);
                            removed.add(copy);
                        }
                        
                        if (returnedVolume.has())
                            removed.add(returnedVolume.createFakeTile(tile));
                    }
                } else
                    removed.add(tile);
            }
            
            x.noneStructureTiles().addAll(toAdd);
        };
        
        if (update)
            be.updateTiles(consumer);
        else
            be.updateTilesSecretly(consumer);
        return removed;
    }
    
    public static List<LittleTile> removeBoxes(BETiles be, LittleGrid grid, List<LittleBox> boxes) {
        if (grid != be.getGrid()) {
            if (grid.count > be.getGrid().count)
                be.convertTo(grid);
            else {
                for (LittleBox box : boxes)
                    box.convertTo(grid, be.getGrid());
                grid = be.getGrid();
            }
        }
        List<LittleTile> removed = new ArrayList<>();
        be.updateTiles(x -> {
            List<LittleTile> toAdd = new ArrayList<>();
            for (Iterator<LittleTile> iterator = x.noneStructureTiles().iterator(); iterator.hasNext();) {
                LittleTile tile = iterator.next();
                
                LittleBox intersecting = null;
                boolean intersects = false;
                for (int j = 0; j < boxes.size(); j++) {
                    if (tile.intersectsWith(boxes.get(j))) {
                        intersects = true;
                        intersecting = boxes.get(j);
                        break;
                    }
                }
                
                if (!intersects)
                    continue;
                
                iterator.remove();
                
                if (!tile.equalsBox(intersecting)) {
                    double volume = 0;
                    LittlePreview preview = tile.getPreviewTile();
                    
                    List<LittleBox> cutout = new ArrayList<>();
                    LittleBoxReturnedVolume returnedVolume = new LittleBoxReturnedVolume();
                    List<LittleBox> newBoxes = tile.cutOut(boxes, cutout, returnedVolume);
                    
                    if (newBoxes != null) {
                        for (LittleBox box : newBoxes) {
                            LittleTile copy = tile.copy();
                            copy.setBox(box);
                            toAdd.add(copy);
                        }
                        
                        for (LittleBox box : cutout) {
                            LittleTile copy = tile.copy();
                            copy.setBox(box);
                            removed.add(copy);
                        }
                        
                        if (returnedVolume.has())
                            removed.add(returnedVolume.createFakeTile(tile));
                    }
                } else
                    removed.add(tile);
            }
            x.noneStructureTiles().addAll(toAdd);
        });
        
        return removed;
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        return assignFlip(new LittleActionDestroyBoxes(), axis, box);
    }
}
