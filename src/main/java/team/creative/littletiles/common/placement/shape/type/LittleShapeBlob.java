package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.config.BrushSizeShapeConfig;
import team.creative.littletiles.common.placement.shape.config.HollowThicknessConfig;

public class LittleShapeBlob extends LittleShape<BrushSizeShapeConfig> {
    
    protected LittleBoxes[] sizeCache = new LittleBoxes[BrushSizeShapeConfig.BRUSH_MAX];
    protected HollowThicknessConfig sphereConfig = new HollowThicknessConfig();
    
    public LittleShapeBlob() {
        super(1);
    }
    
    public LittleBoxes get(BrushSizeShapeConfig config) {
        var boxes = sizeCache[config.size - 1];
        if (boxes == null) {
            List<ShapePosition> positions = new ArrayList<>();
            positions.add(new ShapePosition(new PlacementPosition(BlockPos.ZERO, LittleGrid
                    .overallDefault(), new LittleVec(-config.size, -config.size, -config.size), null), null, null));
            positions.add(new ShapePosition(new PlacementPosition(BlockPos.ZERO, LittleGrid
                    .overallDefault(), new LittleVec(config.size, config.size, config.size), null), null, null));
            sizeCache[config.size - 1] = boxes = ShapeRegistry.DRAG_SPHERE.build(new ShapeSelection(null, LittleGrid.overallDefault(), positions, false), sphereConfig);
        }
        return boxes;
    }
    
    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, BrushSizeShapeConfig config) {
        for (ShapePosition pos : selection) {
            LittleBoxes shape = get(config);
            for (LittleBox box : shape.all()) {
                LittleBox toAdd = box.copy();
                toAdd.add(pos.getVec());
                boxes.addBox(selection.grid, pos.getPos(), toAdd);
            }
        }
    }
    
}
