package team.creative.littletiles.common.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.LevelUtils;
import team.creative.littletiles.common.action.LittleActionDestroy.StructurePreview;
import team.creative.littletiles.common.action.LittleActionPlace.PlaceAction;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETiles.BlockEntityInteractor;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.collection.LittleCollection;
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
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleActionDestroyBoxes extends LittleActionBoxes {
    
    public transient boolean doneSomething;
    public transient List<StructurePreview> destroyedStructures;
    public transient LittleGroupAbsolute destroyed;
    
    public LittleActionDestroyBoxes(Level level, LittleBoxes boxes) {
        super(level, boxes);
    }
    
    public LittleActionDestroyBoxes(UUID levelUUID, LittleBoxes boxes) {
        super(levelUUID, boxes);
    }
    
    public LittleActionDestroyBoxes() {}
    
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
    
    public LittleIngredients action(Player player, BETiles be, List<LittleBox> boxes, boolean simulate, LittleGrid grid) {
        doneSomething = false;
        
        if (destroyed == null)
            destroyed = new LittleGroupAbsolute(be.getBlockPos());
        
        LittleIngredients ingredients = new LittleIngredients();
        
        LittleCollection toPlace = new LittleCollection();
        LittleCollection toRemove = new LittleCollection();
        
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
                for (LittleTile element : parent) {
                    
                    if (shouldSkipTile(parent, element))
                        continue;
                    
                    for (LittleBox box : element) {
                        LittleBox intersecting = null;
                        boolean intersects = false;
                        for (int j = 0; j < boxes.size(); j++) {
                            if (LittleBox.intersectsWith(box, boxes.get(j))) {
                                intersects = true;
                                intersecting = boxes.get(j);
                                break;
                            }
                        }
                        
                        if (!intersects)
                            continue;
                        
                        doneSomething = true;
                        
                        if (!box.equals(intersecting)) {
                            double volume = 0;
                            
                            List<LittleBox> cutout = new ArrayList<>();
                            LittleBoxReturnedVolume returnedVolume = new LittleBoxReturnedVolume();
                            List<LittleBox> newBoxes = box.cutOut(grid, boxes, cutout, returnedVolume);
                            
                            if (newBoxes != null) {
                                if (!simulate) {
                                    toPlace.add(element, newBoxes);
                                    toRemove.add(element, box);
                                }
                                
                                for (int l = 0; l < cutout.size(); l++) {
                                    volume += cutout.get(l).getPercentVolume(grid);
                                    if (!simulate)
                                        destroyed.add(parent, element, cutout.get(l));
                                }
                            }
                            
                            if (volume > 0)
                                ingredients.add(getIngredients(element, volume));
                            if (returnedVolume.has())
                                ingredients.add(getIngredients(element, returnedVolume.getPercentVolume(grid)));
                        } else {
                            ingredients.add(getIngredients(parent, element, box));
                            
                            if (!simulate) {
                                toRemove.add(element, intersecting);
                                destroyed.add(parent, element, intersecting);
                            }
                        }
                    }
                }
            }
        }
        
        if (!simulate) {
            be.updateTiles(x -> {
                ParentCollection parent = x.noneStructureTiles();
                parent.removeAll(toRemove);
                parent.addAll(toPlace);
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
            
            be.combineAllTiles(false); // Does not need to be optimised. Chance of this having an effect is a lot less.
            
            if (!doneSomething)
                be.convertBlockToVanilla();
        }
    }
    
    @Override
    public Boolean action(Player player) throws LittleActionException {
        destroyedStructures = new ArrayList<>();
        return super.action(player);
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(Player player) {
        boolean additionalPreviews = destroyed != null && !destroyed.isEmpty();
        LittleAction[] actions = new LittleAction[(additionalPreviews ? 1 : 0) + destroyedStructures.size()];
        if (additionalPreviews) {
            destroyed.convertToSmallest();
            actions[0] = new LittleActionPlace(PlaceAction.ABSOLUTE, PlacementPreview.load(levelUUID, PlacementMode.FILL, destroyed, Facing.EAST));
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
        
        public LittleActionDestroyBoxesFiltered() {}
        
        @Override
        public boolean shouldSkipTile(IParentCollection parent, LittleTile tile) {
            return !filter.is(parent, tile);
        }
        
    }
    
    public static LittleCollection removeBox(BETiles be, LittleGrid grid, LittleBox toCut, boolean update, LittleBoxReturnedVolume returnedVolume) {
        if (grid != be.getGrid()) {
            if (grid.count > be.getGrid().count)
                be.convertTo(grid);
            else {
                toCut.convertTo(grid, be.getGrid());
                grid = be.getGrid();
            }
        }
        
        final LittleGrid usedGrid = grid;
        
        LittleCollection removed = new LittleCollection();
        
        Consumer<BlockEntityInteractor> consumer = x -> {
            LittleCollection toAdd = new LittleCollection();
            for (LittleTile element : x.noneStructureTiles()) {
                
                for (LittleBox box : element) {
                    
                    if (!LittleBox.intersectsWith(box, toCut))
                        continue;
                    
                    x.noneStructureTiles().remove(element, box);
                    
                    if (!box.equals(toCut)) {
                        List<LittleBox> cutout = new ArrayList<>();
                        List<LittleBox> boxes = new ArrayList<>();
                        boxes.add(toCut);
                        List<LittleBox> newBoxes = box.cutOut(usedGrid, boxes, cutout, null);
                        
                        if (newBoxes != null) {
                            toAdd.add(element, newBoxes);
                            removed.add(element, cutout);
                        }
                    } else
                        removed.add(element, box);
                }
            }
            x.noneStructureTiles().removeAll(removed);
            x.noneStructureTiles().addAll(toAdd);
        };
        
        if (update)
            be.updateTiles(consumer);
        else
            be.updateTilesSecretly(consumer);
        return removed;
    }
    
    public static LittleCollection removeBoxes(BETiles be, LittleGrid grid, List<LittleBox> boxes) {
        if (grid != be.getGrid()) {
            if (grid.count > be.getGrid().count)
                be.convertTo(grid);
            else {
                for (LittleBox box : boxes)
                    box.convertTo(grid, be.getGrid());
                grid = be.getGrid();
            }
        }
        
        final LittleGrid usedGrid = grid;
        LittleCollection removed = new LittleCollection();
        be.updateTiles(x -> {
            LittleCollection toAdd = new LittleCollection();
            for (LittleTile element : x.noneStructureTiles()) {
                for (LittleBox box : element) {
                    LittleBox intersecting = null;
                    boolean intersects = false;
                    for (int j = 0; j < boxes.size(); j++) {
                        if (LittleBox.intersectsWith(box, boxes.get(j))) {
                            intersects = true;
                            intersecting = boxes.get(j);
                            break;
                        }
                    }
                    
                    if (!intersects)
                        continue;
                    
                    x.noneStructureTiles().remove(element, box);
                    
                    if (!box.equals(intersecting)) {
                        List<LittleBox> cutout = new ArrayList<>();
                        List<LittleBox> newBoxes = box.cutOut(usedGrid, boxes, cutout, null);
                        
                        if (newBoxes != null) {
                            toAdd.add(element, newBoxes);
                            removed.add(element, cutout);
                        }
                    } else
                        removed.add(element, box);
                }
            }
            
            x.noneStructureTiles().removeAll(removed);
            x.noneStructureTiles().addAll(toAdd);
        });
        
        return removed;
    }
    
    @Override
    public void actionDone(Level level, Player player) {
        for (StructurePreview structure : destroyedStructures) {
            try {
                if (!structure.structure.mainBlock.isRemoved()) {
                    if (needIngredients(player) && !level.isClientSide) {
                        ItemStack stack = structure.structure.getStructureDrop();
                        if (!stack.isEmpty() && !player.addItem(stack))
                            LevelUtils.dropItem(player, stack);
                    }
                    structure.structure.tileDestroyed();
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public LittleAction mirror(Axis axis, LittleBoxAbsolute box) {
        return assignMirror(new LittleActionDestroyBoxes(), axis, box);
    }
}
