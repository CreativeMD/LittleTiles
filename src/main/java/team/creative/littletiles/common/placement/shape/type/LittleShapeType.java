package team.creative.littletiles.common.placement.shape.type;

import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;

public class LittleShapeType extends LittleShapeSelectable<Void> {
    
    public LittleShapeType() {
        super(1);
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, Void config) {
        for (ShapePosition pos : selection) {
            if (pos.result.isComplete()) {
                if (pos.result.parent.isStructure())
                    continue;
                
                LittleTile tile = pos.result.tile;
                for (LittleTile toDestroy : pos.result.parent)
                    if (tile.is(toDestroy))
                        for (LittleBox box : toDestroy)
                            addBox(boxes, selection.inside, selection.grid, pos.result.parent, box, pos.facing);
                        
            } else
                addBox(boxes, selection.inside, selection.grid, pos.ray.getBlockPos(), pos.facing);
        }
    }
}
