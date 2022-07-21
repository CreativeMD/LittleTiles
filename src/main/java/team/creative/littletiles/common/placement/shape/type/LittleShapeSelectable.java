package team.creative.littletiles.common.placement.shape.type;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.placement.shape.LittleShape;

public abstract class LittleShapeSelectable extends LittleShape {
    
    public LittleShapeSelectable(int pointsBeforePlacing) {
        super(pointsBeforePlacing);
    }
    
    @Override
    public boolean requiresNoOverlap() {
        return true;
    }
    
    public static void addBox(LittleBoxes boxes, boolean inside, LittleGrid grid, IParentCollection parent, LittleBox box, Facing facing) {
        if (inside)
            boxes.addBox(parent.getGrid(), parent.getPos(), box.copy());
        else {
            
            box = box.copy();
            if (parent.getGrid().count > grid.count)
                grid = parent.getGrid();
            else
                box.convertTo(parent.getGrid(), grid);
            
            if (facing.positive) {
                int min = box.getMax(facing.axis);
                box.setMin(facing.axis, min);
                box.setMax(facing.axis, min + 1);
            } else {
                int max = box.getMin(facing.axis);
                box.setMin(facing.axis, max - 1);
                box.setMax(facing.axis, max);
            }
            boxes.addBox(grid, parent.getPos(), box);
        }
    }
    
    public void addBox(LittleBoxes boxes, boolean inside, LittleGrid grid, BlockPos pos, Facing facing) {
        LittleBox box = new LittleBox(0, 0, 0, grid.count, grid.count, grid.count);
        
        if (inside)
            boxes.addBox(grid, pos, box);
        else {
            if (facing.positive)
                box.setMax(facing.axis, 1);
            else
                box.setMin(facing.axis, grid.count - 1);
            boxes.addBox(grid, pos.relative(facing.toVanilla()), box);
        }
        
    }
    
}
