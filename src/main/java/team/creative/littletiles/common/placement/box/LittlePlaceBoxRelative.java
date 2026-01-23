package team.creative.littletiles.common.placement.box;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittlePlaceBoxRelative extends LittlePlaceBox {
    
    public StructureDirectionalField relativeType;
    @Nullable
    public StructureRelative relative;
    
    public LittlePlaceBoxRelative(LittleBox box, @Nullable StructureRelative relative, StructureDirectionalField relativeType) {
        super(box.copy());
        this.relative = relative;
        this.relativeType = relativeType;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public LittleRenderBox getRenderBox(LittleGrid grid, LittleVec vec) {
        LittleRenderBox cube = super.getRenderBox(grid, vec);
        cube.color = relativeType.annotation.color();
        return cube;
    }
    
    @Override
    public void place(Placement placement, LittleGrid grid, BlockPos pos, LittleStructure structure) throws LittleActionException {
        if (relative != null) {
            relative.setBox(BlockPos.ZERO, box.copy(), grid);
            relative.add(pos.subtract(structure.getStructurePos()));
            relativeType.set(structure, relative);
        }
    }
    
}
