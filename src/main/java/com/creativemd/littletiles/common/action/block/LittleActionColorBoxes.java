package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.LittleTilesConfig.NotAllowedToPlaceColorException;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles.TileEntityInteractor;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.BlockIngredientEntry;
import com.creativemd.littletiles.common.util.ingredient.ColorIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.action.LittleActionBoxes;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.math.box.volume.LittleBoxReturnedVolume;

public class LittleActionColorBoxes extends LittleActionBoxes {
    
    public int color;
    public boolean toVanilla;
    
    public LittleActionColorBoxes(LittleBoxes boxes, int color, boolean toVanilla) {
        super(boxes);
        this.color = color;
        this.toVanilla = toVanilla;
    }
    
    public LittleActionColorBoxes() {
        
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        super.writeBytes(buf);
        buf.writeInt(color);
        buf.writeBoolean(toVanilla);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        super.readBytes(buf);
        color = buf.readInt();
        toVanilla = buf.readBoolean();
    }
    
    public HashMapList<Integer, LittleBoxes> revertList;
    
    public void addRevert(int color, BlockPos pos, LittleGridContext context, List<LittleBox> boxes) {
        LittleBoxes newBoxes = new LittleBoxesSimple(pos, context);
        for (LittleBox box : boxes)
            newBoxes.add(box.copy());
        revertList.add(color, newBoxes);
    }
    
    public boolean shouldSkipTile(IParentTileList parent, LittleTile tile) {
        return false;
    }
    
    public boolean doneSomething;
    
    private double colorVolume;
    
    public ColorIngredient action(TileEntityLittleTiles te, List<LittleBox> boxes, ColorIngredient gained, boolean simulate, LittleGridContext context) {
        doneSomething = false;
        colorVolume = 0;
        
        Consumer<TileEntityInteractor> consumer = x -> {
            structure_loop: for (IParentTileList parent : te.groups()) {
                
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
                    
                    if (!intersects || !(tile.getClass() == LittleTile.class || tile instanceof LittleTileColored))
                        continue;
                    
                    try {
                        if (parent.isStructure() && parent.getStructure().hasStructureColor()) {
                            LittleStructure structure = parent.getStructure();
                            if (structure.getStructureColor() != color) {
                                double volume = structure.getPercentVolume();
                                colorVolume += volume;
                                gained.add(ColorIngredient.getColors(color, structure.getDefaultColor(), volume));
                                if (!simulate) {
                                    addRevert(structure.getStructureColor(), te.getPos(), context, Arrays.asList(intersecting));
                                    structure.paint(color);
                                }
                            }
                            continue structure_loop;
                        }
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {
                        continue structure_loop;
                    }
                    
                    if (!LittleTileColored.needsToBeRecolored(tile, color))
                        continue;
                    
                    doneSomething = true;
                    
                    if (!tile.equalsBox(intersecting)) {
                        if (simulate) {
                            double volume = 0;
                            List<LittleBox> cutout = new ArrayList<>();
                            LittleBoxReturnedVolume returnedVolume = new LittleBoxReturnedVolume();
                            tile.cutOut(boxes, cutout, returnedVolume);
                            for (LittleBox box2 : cutout) {
                                colorVolume += box2.getPercentVolume(context);
                                volume += box2.getPercentVolume(context);
                            }
                            if (returnedVolume.has()) {
                                colorVolume += returnedVolume.getPercentVolume(context);
                                volume += returnedVolume.getPercentVolume(context);
                            }
                            
                            gained.add(ColorIngredient.getColors(tile.getPreviewTile(), volume));
                            
                        } else {
                            List<LittleBox> cutout = new ArrayList<>();
                            List<LittleBox> newBoxes = tile.cutOut(boxes, cutout, null);
                            
                            if (newBoxes != null) {
                                addRevert(LittleTileColored.getColor(tile), te.getPos(), context, cutout);
                                
                                LittleTile tempTile = tile.copy();
                                LittleTile changedTile = LittleTileColored.setColor(tempTile, color);
                                if (changedTile == null)
                                    changedTile = tempTile;
                                
                                for (int i = 0; i < newBoxes.size(); i++) {
                                    LittleTile newTile = tile.copy();
                                    newTile.setBox(newBoxes.get(i));
                                    x.get(parent).add(newTile);
                                }
                                
                                for (int i = 0; i < cutout.size(); i++) {
                                    LittleTile newTile = changedTile.copy();
                                    newTile.setBox(cutout.get(i));
                                    x.get(parent).add(newTile);
                                }
                                
                                x.get(parent).remove(tile);
                            }
                        }
                    } else {
                        if (simulate) {
                            colorVolume += tile.getPercentVolume(context);
                            gained.add(ColorIngredient.getColors(tile.getPreviewTile(), tile.getPercentVolume(context)));
                        } else {
                            List<LittleBox> oldBoxes = new ArrayList<>();
                            oldBoxes.add(tile.getBox());
                            
                            addRevert(LittleTileColored.getColor(tile), te.getPos(), context, oldBoxes);
                            
                            LittleTile changedTile = LittleTileColored.setColor(tile, color);
                            if (changedTile != null) {
                                x.get(parent).add(changedTile);
                                x.get(parent).remove(tile);
                            }
                        }
                    }
                }
            }
        };
        
        if (simulate)
            te.updateTilesSecretly(consumer);
        else
            te.updateTiles(consumer);
        
        ColorIngredient toDrain = ColorIngredient.getColors(color);
        toDrain.scale(colorVolume);
        
        return gained.sub(toDrain);
    }
    
    @Override
    public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleBox> boxes, LittleGridContext context) throws LittleActionException {
        if (ColorUtils.getAlpha(color) < LittleTiles.CONFIG.getMinimumTransparency(player))
            throw new NotAllowedToPlaceColorException(player);
        
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
            
            List<BlockIngredientEntry> entries = new ArrayList<>();
            
            ColorIngredient gained = new ColorIngredient();
            
            ColorIngredient toDrain = action(te, boxes, gained, true, context);
            LittleIngredients gainedIngredients = new LittleIngredients(gained);
            LittleIngredients drainedIngredients = new LittleIngredients(toDrain);
            LittleInventory inventory = new LittleInventory(player);
            try {
                inventory.startSimulation();
                give(player, inventory, gainedIngredients);
                take(player, inventory, drainedIngredients);
            } finally {
                inventory.stopSimulation();
            }
            
            give(player, inventory, gainedIngredients);
            take(player, inventory, drainedIngredients);
            action(te, boxes, gained, false, context);
            
            te.combineTiles();
            
            if (toVanilla || !doneSomething)
                te.convertBlockToVanilla();
        }
    }
    
    @Override
    protected boolean action(EntityPlayer player) throws LittleActionException {
        revertList = new HashMapList<>();
        return super.action(player);
    }
    
    @Override
    public boolean canBeReverted() {
        return true;
    }
    
    @Override
    public LittleAction revert(EntityPlayer player) {
        List<LittleAction> actions = new ArrayList<>();
        for (Entry<Integer, ArrayList<LittleBoxes>> entry : revertList.entrySet()) {
            for (LittleBoxes boxes : entry.getValue()) {
                boxes.convertToSmallest();
                actions.add(new LittleActionColorBoxes(boxes, entry.getKey(), true));
            }
        }
        return new LittleActions(actions.toArray(new LittleAction[0]));
    }
    
    @Override
    public LittleAction flip(Axis axis, LittleBoxAbsolute box) {
        LittleActionColorBoxes action = new LittleActionColorBoxes();
        action.color = color;
        action.toVanilla = toVanilla;
        return assignFlip(action, axis, box);
    }
    
    public static class LittleActionColorBoxesFiltered extends LittleActionColorBoxes {
        
        public TileSelector selector;
        
        public LittleActionColorBoxesFiltered(LittleBoxes boxes, int color, boolean toVanilla, TileSelector selector) {
            super(boxes, color, toVanilla);
            this.selector = selector;
        }
        
        public LittleActionColorBoxesFiltered() {
            
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
        
        @Override
        public LittleAction flip(Axis axis, LittleBoxAbsolute box) {
            LittleActionColorBoxesFiltered action = new LittleActionColorBoxesFiltered();
            action.selector = selector;
            action.color = color;
            action.toVanilla = toVanilla;
            return assignFlip(action, axis, box);
        }
    }
}
