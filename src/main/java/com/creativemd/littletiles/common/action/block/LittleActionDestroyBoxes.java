package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.creativemd.creativecore.common.utils.mc.WorldUtils;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy.StructurePreview;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleAbsoluteBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxReturnedVolume;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.ParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittleAbsolutePreviews;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles.TileEntityInteractor;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LittleActionDestroyBoxes extends LittleActionBoxes {
    
    public LittleActionDestroyBoxes(LittleBoxes boxes) {
        super(boxes);
    }
    
    public LittleActionDestroyBoxes() {
        
    }
    
    public List<StructurePreview> destroyedStructures;
    public LittleAbsolutePreviews previews;
    
    private boolean containsStructure(LittleStructure structure) {
        for (StructurePreview structurePreview : destroyedStructures) {
            if (structurePreview.structure == structure)
                return true;
        }
        return false;
    }
    
    public boolean shouldSkipTile(IParentTileList parent, LittleTile tile) {
        return false;
    }
    
    public boolean doneSomething;
    
    public LittleIngredients action(EntityPlayer player, TileEntityLittleTiles te, List<LittleBox> boxes, boolean simulate, LittleGridContext context) {
        doneSomething = false;
        
        if (previews == null)
            previews = new LittleAbsolutePreviews(te.getPos(), context);
        
        LittleIngredients ingredients = new LittleIngredients();
        
        List<LittleTile> placedTiles = new ArrayList<>();
        List<LittleTile> destroyedTiles = new ArrayList<>();
        
        for (IParentTileList parent : te.groups()) {
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
            te.updateTiles(x -> {
                ParentTileList parent = x.noneStructureTiles();
                parent.removeAll(destroyedTiles);
                parent.addAll(placedTiles);
            });
        }
        
        return ingredients;
        
    }
    
    @Override
    public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleBox> boxes, LittleGridContext context) throws LittleActionException {
        fireBlockBreakEvent(world, pos, player);
        
        TileEntity tileEntity = loadTe(player, world, pos, null, true, 0);
        
        if (tileEntity instanceof TileEntityLittleTiles) {
            TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
            
            if (context != te.getContext()) {
                if (context.size < te.getContext().size) {
                    for (LittleBox box : boxes)
                        box.convertTo(context, te.getContext());
                    context = te.getContext();
                } else
                    te.convertTo(context);
            }
            
            if (checkAndGive(player, new LittleInventory(player), action(player, (TileEntityLittleTiles) tileEntity, boxes, true, context)))
                action(player, (TileEntityLittleTiles) tileEntity, boxes, false, context);
            
            ((TileEntityLittleTiles) tileEntity).combineTiles();
            
            if (!doneSomething)
                ((TileEntityLittleTiles) tileEntity).convertBlockToVanilla();
        }
    }
    
    @Override
    protected boolean action(EntityPlayer player) throws LittleActionException {
        destroyedStructures = new ArrayList<>();
        return super.action(player);
    }
    
    @Override
    public void actionDone(EntityPlayer player, World world) {
        for (StructurePreview structure : destroyedStructures) {
            try {
                if (!structure.structure.mainBlock.isRemoved()) {
                    if (needIngredients(player) && !world.isRemote) {
                        ItemStack stack = structure.structure.getStructureDrop();
                        if (!stack.isEmpty() && !player.addItemStackToInventory(stack))
                            WorldUtils.dropItem(player, stack);
                    }
                    structure.structure.onLittleTileDestroy();
                }
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) {
        boolean additionalPreviews = previews != null && previews.size() > 0;
        LittleAction[] actions = new LittleAction[(additionalPreviews ? 1 : 0) + destroyedStructures.size()];
        if (additionalPreviews) {
            previews.convertToSmallest();
            actions[0] = new LittleActionPlaceAbsolute(previews, PlacementMode.fill, true);
        }
        for (int i = 0; i < destroyedStructures.size(); i++)
            actions[(additionalPreviews ? 1 : 0) + i] = destroyedStructures.get(i).getPlaceAction();
        return new LittleActionCombined(actions);
    }
    
    public static class LittleActionDestroyBoxesFiltered extends LittleActionDestroyBoxes {
        
        public TileSelector selector;
        
        public LittleActionDestroyBoxesFiltered(LittleBoxes boxes, TileSelector selector) {
            super(boxes);
            this.selector = selector;
        }
        
        public LittleActionDestroyBoxesFiltered() {
            
        }
        
        @Override
        public void writeBytes(ByteBuf buf) {
            super.writeBytes(buf);
            writeSelector(selector, buf);
        }
        
        @Override
        public void readBytes(ByteBuf buf) {
            super.readBytes(buf);
            selector = readSelector(buf);
        }
        
        @Override
        public boolean shouldSkipTile(IParentTileList parent, LittleTile tile) {
            return !selector.is(parent, tile);
        }
        
    }
    
    public static List<LittleTile> removeBox(TileEntityLittleTiles te, LittleGridContext context, LittleBox toCut, boolean update, LittleBoxReturnedVolume returnedVolume) {
        if (context != te.getContext()) {
            if (context.size > te.getContext().size)
                te.convertTo(context);
            else {
                toCut.convertTo(context, te.getContext());
                context = te.getContext();
            }
        }
        
        List<LittleTile> removed = new ArrayList<>();
        
        Consumer<TileEntityInteractor> consumer = x -> {
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
                    }
                } else
                    removed.add(tile);
            }
            
            x.noneStructureTiles().addAll(toAdd);
        };
        
        if (update)
            te.updateTiles(consumer);
        else
            te.updateTilesSecretly(consumer);
        return removed;
    }
    
    public static List<LittleTile> removeBoxes(TileEntityLittleTiles te, LittleGridContext context, List<LittleBox> boxes) {
        if (context != te.getContext()) {
            if (context.size > te.getContext().size)
                te.convertTo(context);
            else {
                for (LittleBox box : boxes) {
                    box.convertTo(context, te.getContext());
                }
                context = te.getContext();
            }
        }
        List<LittleTile> removed = new ArrayList<>();
        te.updateTiles(x -> {
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
    public LittleAction flip(Axis axis, LittleAbsoluteBox box) {
        return assignFlip(new LittleActionDestroyBoxes(), axis, box);
    }
}
