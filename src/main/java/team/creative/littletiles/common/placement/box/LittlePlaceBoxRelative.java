package team.creative.littletiles.common.placement.box;

import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;

import net.minecraft.core.BlockPos;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.Placement.PlacementBlock;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittlePlaceBoxRelative extends LittlePlaceBox {
    
    public StructureDirectionalField relativeType;
    public StructureRelative relative;
    
    public LittlePlaceBoxRelative(LittleBox box, StructureRelative relative, StructureDirectionalField relativeType) {
        super(box.copy());
        this.relative = relative;
        this.relativeType = relativeType;
    }
    
    @Override
    public List<LittleRenderBox> getRenderBoxes(LittleGrid grid) {
        List<LittleRenderBox> cubes = super.getRenderBoxes(grid);
        for (LittleRenderBox cube : cubes)
            cube.color = relativeType.annotation.color();
        return cubes;
    }
    
    @Override
    public void place(Placement placement, PlacementBlock block, LittleStructure structure) throws LittleActionException {
        relative.setBox(BlockPos.ZERO, box.copy(), block.getGrid());
        relative.add(block.pos.subtract(structure.getPos()));
        relativeType.set(structure, relative);
    }
    
}
