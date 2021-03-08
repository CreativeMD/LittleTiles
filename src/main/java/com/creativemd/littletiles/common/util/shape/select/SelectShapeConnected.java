package com.creativemd.littletiles.common.util.shape.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.block.BlockTile.TEResult;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SelectShapeConnected extends SelectShape {
    
    public SelectShapeConnected() {
        super("connected");
    }
    
    @Override
    public boolean rightClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
        return false;
    }
    
    @Override
    public boolean leftClick(EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
        return true;
    }
    
    @Override
    public LittleBoxes getHighlightBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
        return getBoxes(world, pos, player, nbt, result, context);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<GuiControl> getCustomSettings(NBTTagCompound nbt, LittleGridContext context) {
        return new ArrayList<>();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCustomSettings(GuiParent gui, NBTTagCompound nbt, LittleGridContext context) {
        
    }
    
    @Override
    public LittleBoxes getBoxes(World world, BlockPos pos, EntityPlayer player, NBTTagCompound nbt, RayTraceResult result, LittleGridContext context) {
        TEResult te = BlockTile.loadTeAndTile(world, pos, player);
        
        LittleBoxes boxes;
        if (te.isComplete()) {
            
            if (te.parent.isStructure())
                return new LittleBoxes(te.te.getPos(), te.te.getContext());
            
            boolean secondMode = LittleAction.isUsingSecondMode(player);
            
            ConnectedBlock block = new ConnectedBlock(te.te, te.tile);
            boxes = block.start(te.tile);
        } else {
            boxes = new LittleBoxes(pos, context);
            boxes.add(new LittleBox(0, 0, 0, context.size, context.size, context.size));
        }
        
        return boxes;
    }
    
    @Override
    public void deselect(EntityPlayer player, NBTTagCompound nbt, LittleGridContext context) {
        
    }
    
    @Override
    public void addExtraInformation(World world, NBTTagCompound nbt, List<String> list, LittleGridContext context) {
        
    }
    
    private static final ConnectedBlock EMPTY = new ConnectedBlock(null, null);
    
    private static class ConnectedBlock {
        
        private final TileEntityLittleTiles parent;
        private final List<LittleBox> potential;
        private ConnectedBlock[] neighborCache = new ConnectedBlock[6];
        
        public ConnectedBlock(TileEntityLittleTiles te, LittleTile startTile) {
            parent = te;
            potential = new ArrayList<>();
            if (te != null)
                for (LittleTile tile : te.noneStructureTiles())
                    if (tile != startTile && tile.canBeCombined(startTile) && startTile.canBeCombined(tile))
                        potential.add(tile.getBox());
        }
        
        public LittleBoxes start(LittleTile startTile) {
            HashMap<BlockPos, ConnectedBlock> blocks = new HashMap<>();
            blocks.put(parent.getPos(), this);
            LittleBoxes boxes = new LittleBoxes(parent.getPos(), parent.getContext());
            boxes.addBox(parent.getContext(), parent.getPos(), startTile.getBox().copy());
            performSearchIn(boxes, blocks, startTile, true, parent.getContext(), startTile.getBox().copy());
            return boxes;
        }
        
        public void performSearchIn(LittleBoxes boxes, HashMap<BlockPos, ConnectedBlock> blocks, LittleTile startTile, boolean start, LittleGridContext other, LittleBox otherBox) {
            LittleGridContext context = parent.getContext();
            List<LittleBox> added = new ArrayList<>();
            int index = 0;
            while (index <= added.size()) {
                for (Iterator<LittleBox> iterator = potential.iterator(); iterator.hasNext();) {
                    LittleBox box = iterator.next();
                    if (index == 0 ? box.doesTouch(context, other, otherBox) : box.doesTouch(added.get(index - 1))) {
                        LittleBox copy = box.copy();
                        boxes.addBox(parent.getContext(), parent.getPos(), copy);
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
                    EnumFacing facing = EnumFacing.getFront(i);
                    if (box.isFaceAtEdge(context, facing)) {
                        if (neighborCache[i] == null) {
                            BlockPos pos = parent.getPos().offset(facing);
                            ConnectedBlock block = blocks.get(pos);
                            if (block == null) {
                                TileEntity te = parent.getWorld().getTileEntity(pos);
                                if (te instanceof TileEntityLittleTiles)
                                    block = new ConnectedBlock((TileEntityLittleTiles) te, startTile);
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
                        copyBox.sub(context.size * facing.getFrontOffsetX(), context.size * facing.getFrontOffsetY(), context.size * facing
                            .getFrontOffsetZ());
                        
                        LittleGridContext used = context;
                        if (block.getContext().size > context.size) {
                            copyBox.convertTo(context, block.getContext());
                            used = block.getContext();
                        }
                        
                        block.performSearchIn(boxes, blocks, startTile, false, used, copyBox);
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
