package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
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
            if (pos.result.isComplete())
                new ConnectedSearch(pos.result, selection.inside ? null : pos.pos.facing, selection.getGrid()).start(boxes);
            else
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
    
    private static class ConnectedSearch {
        
        public final LittleElement element;
        public final LittleBox box;
        public final LittleGrid aimedGrid;
        public final HashMap<BlockPos, ConnectedBlock> blocks = new HashMap<>();
        public final Facing facing;
        public final ConnectedBlock origin;
        private final MutableBlockPos pos = new MutableBlockPos();
        
        public ConnectedSearch(LittleTileContext context, Facing facing, LittleGrid aimedGrid) {
            this.element = context.tile;
            this.box = context.box;
            this.facing = facing;
            this.aimedGrid = aimedGrid;
            this.origin = new ConnectedBlock(context.parent.getBE(), this);
            blocks.put(context.parent.getPos(), origin);
        }
        
        public void addBox(LittleBoxes boxes, ConnectedBlock block, LittleBox box, Facing facing) {
            LittleShapeSelectable.addBox(boxes, facing == null, aimedGrid, block.parent.noneStructureTiles(), box, facing);
        }
        
        public LittleBoxes start(LittleBoxes boxes) {
            addBox(boxes, origin, box, facing);
            origin.performSearchIn(boxes, this, true, origin.parent.getGrid(), box, facing);
            return boxes;
        }
        
        public ConnectedBlock get(BlockPos pos, Facing facing) {
            this.pos.set(pos);
            this.pos.move(facing.toVanilla());
            ConnectedBlock block = blocks.get(this.pos);
            if (block == null) {
                BlockEntity be = origin.parent.getLevel().getBlockEntity(this.pos);
                if (be instanceof BETiles t)
                    block = new ConnectedBlock(t, this);
                else
                    block = EMPTY;
                blocks.put(this.pos.immutable(), block);
            }
            return block;
        }
        
    }
    
    private static final ConnectedBlock EMPTY = new ConnectedBlock(null, null);
    
    private static class ConnectedBlock {
        
        private final BETiles parent;
        private final List<LittleBox> potential;
        
        public ConnectedBlock(BETiles be, ConnectedSearch search) {
            this.parent = be;
            if (be != null) {
                potential = new ArrayList<>();
                for (LittleTile tile : be.noneStructureTiles())
                    if (tile.is(search.element))
                        for (LittleBox box : tile)
                            if (box != search.box)
                                potential.add(box);
            } else
                potential = Collections.EMPTY_LIST;
            
        }
        
        public void performSearchIn(LittleBoxes boxes, ConnectedSearch search, boolean start, LittleGrid other, LittleBox otherBox, Facing insideFace) {
            LittleGrid context = parent.getGrid();
            List<LittleBox> added = new ArrayList<>();
            int index = 0;
            while (index <= added.size()) {
                for (Iterator<LittleBox> iterator = potential.iterator(); iterator.hasNext();) {
                    LittleBox box = iterator.next();
                    if (index == 0 ? box.doesTouch(context, other, otherBox) : box.doesTouch(added.get(index - 1))) {
                        LittleBox copy = box.copy();
                        search.addBox(boxes, this, copy, insideFace);
                        added.add(box.copy());
                        iterator.remove();
                    }
                }
                index++;
            }
            
            if (start)
                added.add(otherBox);
            
            for (LittleBox box : added) {
                for (int i = 0; i < Facing.VALUES.length; i++) {
                    Facing facing = Facing.get(i);
                    if (box.isFaceAtEdge(context, facing)) {
                        
                        ConnectedBlock block = search.get(parent.getBlockPos(), facing);
                        if (block.isEmpty())
                            continue;
                        
                        LittleBox copyBox = box.copy();
                        copyBox.sub(context.count * facing.offset(Axis.X), context.count * facing.offset(Axis.Y), context.count * facing.offset(Axis.Z));
                        
                        LittleGrid used = context;
                        if (block.getGrid().count > context.count) {
                            copyBox.convertTo(context, block.getGrid());
                            used = block.getGrid();
                        }
                        
                        block.performSearchIn(boxes, search, false, used, copyBox, insideFace);
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
