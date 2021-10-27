package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.ShapeSelection;
import team.creative.littletiles.common.placement.shape.ShapeSelection.ShapeSelectPos;

public class LittleShapeConnected extends LittleShapeSelectable {
    
    public LittleShapeConnected() {
        super(1);
    }
    
    @Override
    protected void addBoxes(LittleBoxes boxes, ShapeSelection selection, boolean lowResolution) {
        for (ShapeSelectPos pos : selection) {
            if (pos.result.isComplete()) {
                ConnectedBlock block = new ConnectedBlock(pos.result.be, pos.result.tile, selection.getGrid());
                boxes = block.start(boxes, pos.result.tile, selection.inside ? null : pos.pos.facing);
            } else
                addBox(boxes, selection.inside, selection.getGrid(), pos.ray.getBlockPos(), pos.pos.facing);
        }
    }
    
    @Override
    public void addExtraInformation(CompoundTag nbt, List<Component> list) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<GuiControl> getCustomSettings(CompoundTag nbt, LittleGrid grid) {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void saveCustomSettings(GuiParent gui, CompoundTag nbt, LittleGrid grid) {}
    
    @Override
    public void rotate(CompoundTag nbt, Rotation rotation) {}
    
    @Override
    public void mirror(CompoundTag nbt, Axis axis) {}
    
    private static final ConnectedBlock EMPTY = new ConnectedBlock(null, null, null);
    
    private static class ConnectedBlock {
        
        private final BETiles parent;
        private final List<LittleBox> potential;
        private final LittleGrid aimedContext;
        private ConnectedBlock[] neighborCache = new ConnectedBlock[6];
        
        public ConnectedBlock(BETiles be, LittleTile startTile, LittleGrid aimedContext) {
            parent = be;
            potential = new ArrayList<>();
            this.aimedContext = aimedContext;
            if (be != null)
                for (LittleTile tile : be.noneStructureTiles())
                    if (tile != startTile && tile.canBeCombined(startTile) && startTile.canBeCombined(tile))
                        potential.add(tile.getBox());
        }
        
        private void addBox(LittleBoxes boxes, LittleBox box, EnumFacing facing) {
            LittleShapeSelectable.addBox(boxes, facing == null, aimedContext, parent.noneStructureTiles(), box, facing);
        }
        
        public LittleBoxes start(LittleBoxes boxes, LittleTile startTile, Facing facing) {
            HashMap<BlockPos, ConnectedBlock> blocks = new HashMap<>();
            blocks.put(parent.getBlockPos(), this);
            addBox(boxes, startTile.getBox().copy(), facing);
            performSearchIn(boxes, blocks, startTile, true, parent.getGrid(), startTile.getBox().copy(), facing);
            return boxes;
        }
        
        public void performSearchIn(LittleBoxes boxes, HashMap<BlockPos, ConnectedBlock> blocks, LittleTile startTile, boolean start, LittleGridContext other, LittleBox otherBox, EnumFacing insideFace) {
            LittleGrid context = parent.getGrid();
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
                    Facing facing = Facing.get(i);
                    if (box.isFaceAtEdge(context, facing)) {
                        if (neighborCache[i] == null) {
                            BlockPos pos = parent.getBlockPos().relative(facing.toVanilla());
                            ConnectedBlock block = blocks.get(pos);
                            if (block == null) {
                                BlockEntity be = parent.getLevel().getBlockEntity(pos);
                                if (be instanceof BETiles)
                                    block = new ConnectedBlock((BETiles) te, startTile, aimedContext);
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
                        copyBox.sub(context.size * facing.getFrontOffsetX(), context.size * facing.getFrontOffsetY(), context.size * facing.getFrontOffsetZ());
                        
                        LittleGrid used = context;
                        if (block.getGrid().count > context.size) {
                            copyBox.convertTo(context, block.getGrid());
                            used = block.getGrid();
                        }
                        
                        block.performSearchIn(boxes, blocks, startTile, false, used, copyBox, insideFace);
                    }
                    
                }
            }
        }
        
        public LittleGrid getGrid() {
            return parent.getGrid();
        }
        
        public boolean isEmpty() {
            return potential.isEmpty();
        }
    }
    
}
