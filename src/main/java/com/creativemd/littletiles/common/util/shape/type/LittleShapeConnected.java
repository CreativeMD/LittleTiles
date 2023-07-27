package com.creativemd.littletiles.common.util.shape.type;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.shape.ShapeSelection;
import com.creativemd.littletiles.common.util.shape.ShapeSelection.ShapeSelectPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class LittleShapeConnected extends LittleShapeSelectable {
    
    public LittleShapeConnected() {
        super(1);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        for (ShapeSelectPos pos : selection) {
            if (pos.result.isComplete()) {
                ConnectedBlock block = new ConnectedBlock(pos.result.te, pos.result.tile, selection.getContext());
                boxes = block.start(boxes, pos.result.tile, selection.inside ? null : pos.pos.facing);
            } else
                addBox(boxes, selection.inside, selection.getContext(), pos.ray.getBlockPos(), pos.pos.facing);
        }
    }
    
    @Override
    public void addExtraInformation(NBTTagCompound nbt, List<String> list) {}
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {}
    
    @Override
    public void rotate(NBTTagCompound nbt, Rotation rotation) {
        
    }
    
    @Override
    public void flip(NBTTagCompound nbt, Axis axis) {
        
    }
    
    private static final ConnectedBlock EMPTY = new ConnectedBlock(null, null, null);
    
    private static class ConnectedBlock {
        
        private final TileEntityLittleTiles parent;
        private final List<LittleBox> potential;
        private final LittleGridContext aimedContext;
        private ConnectedBlock[] neighborCache = new ConnectedBlock[6];
        
        public ConnectedBlock(TileEntityLittleTiles te, LittleTile startTile, LittleGridContext aimedContext) {
            parent = te;
            potential = new ArrayList<>();
            this.aimedContext = aimedContext;
            if (te != null)
                for (LittleTile tile : te.noneStructureTiles())
                    if (tile != startTile && tile.canBeCombined(startTile) && startTile.canBeCombined(tile))
                        potential.add(tile.getBox());
        }
        
        private void addBox(LittleBoxes boxes, LittleBox box, EnumFacing facing) {
            LittleShapeSelectable.addBox(boxes, facing == null, aimedContext, parent.noneStructureTiles(), box, facing);
        }
        
        public LittleBoxes start(LittleBoxes boxes, LittleTile startTile, EnumFacing facing) {
            HashMap<BlockPos, ConnectedBlock> blocks = new HashMap<>();
            blocks.put(parent.getPos(), this);
            addBox(boxes, startTile.getBox().copy(), facing);
            performSearchIn(boxes, blocks, startTile, true, parent.getContext(), startTile.getBox().copy(), facing);
            return boxes;
        }
        
        public void performSearchIn(LittleBoxes boxes, HashMap<BlockPos, ConnectedBlock> blocks, LittleTile startTile, boolean start, LittleGridContext other, LittleBox otherBox, EnumFacing insideFace) {
            LittleGridContext context = parent.getContext();
            List<LittleBox> added = new ArrayList<>();
            int index = 0;
            while (index <= added.size()) {
                for (Iterator<LittleBox> iterator = potential.iterator(); iterator.hasNext();) {
                    LittleBox box = iterator.next();
                    if (index == 0 ? box.doesTouch(context, other, otherBox) : box.doesTouch(added.get(index - 1))) {
                        LittleBox copy = box.copy();
                        addBox(boxes, copy, insideFace);
                        added.add(box.copy());
                        iterator.remove();
                    }
                }
                index++;
            }
            
            if (start)
                added.add(otherBox);
            
            for (LittleBox box : added) {
                for (int i = 0; i < neighborCache.length; i++) {
                    EnumFacing facing = EnumFacing.byIndex(i);
                    if (box.isFaceAtEdge(context, facing)) {
                        if (neighborCache[i] == null) {
                            BlockPos pos = parent.getPos().offset(facing);
                            ConnectedBlock block = blocks.get(pos);
                            if (block == null) {
                                TileEntity te = parent.getWorld().getTileEntity(pos);
                                if (te instanceof TileEntityLittleTiles)
                                    block = new ConnectedBlock((TileEntityLittleTiles) te, startTile, aimedContext);
                                else
                                    block = EMPTY;
                                blocks.put(pos, block);
                            }
                            neighborCache[i] = block;
                        }
                        
                        ConnectedBlock block = neighborCache[i];
                        if (block.isEmpty())
                            continue;
                        
                        LittleBox copyBox = box.copy();
                        copyBox.sub(context.size * facing.getXOffset(), context.size * facing.getYOffset(), context.size * facing
                            .getZOffset());
                        
                        LittleGridContext used = context;
                        if (block.getContext().size > context.size) {
                            copyBox.convertTo(context, block.getContext());
                            used = block.getContext();
                        }
                        
                        block.performSearchIn(boxes, blocks, startTile, false, used, copyBox, insideFace);
                    }
                    
                }
            }
        }
        
        public LittleGridContext getContext() {
            return parent.getContext();
        }
        
        public boolean isEmpty() {
            return potential.isEmpty();
        }
    }
    
}
