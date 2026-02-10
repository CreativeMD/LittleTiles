package team.creative.littletiles.common.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class PlacementPosition extends LittleVecAbsolute {
    
    public final Facing facing;
    
    public PlacementPosition(PlacementPosition position) {
        super(position.pos, position.gridVec);
        this.facing = position.facing;
    }
    
    public PlacementPosition(BlockPos pos, LittleVecGrid vec, Facing facing) {
        super(pos, vec);
        this.facing = facing;
    }
    
    public PlacementPosition(LittleVecAbsolute vec, Facing facing) {
        super(vec.getPos(), vec.getVecGrid());
        this.facing = facing;
    }
    
    public PlacementPosition(BlockPos pos, LittleGrid context, LittleVec vec, Facing facing) {
        super(pos, context, vec);
        this.facing = facing;
    }
    
    public PlacementPosition(BlockHitResult result, LittleGrid context) {
        super(result, context);
        this.facing = Facing.get(result.getDirection());
    }
    
    public void assign(LittleVecAbsolute pos) {
        this.pos = pos.getPos();
        this.gridVec = pos.getVecGrid();
    }
    
    public void subVec(LittleVec vec) {
        getVec().add(vec);
        removeInternalBlockOffset();
    }
    
    public void addVec(LittleVec vec) {
        getVec().sub(vec);
        removeInternalBlockOffset();
    }
    
    @Override
    public PlacementPosition copy() {
        return new PlacementPosition(pos, gridVec.copy(), facing);
    }
    
    public void mirror(Axis axis, LittleBoxAbsolute box) {
        box.sameGrid(gridVec, () -> {
            LittleVec doubledCenter = box.getDoubledCenter(pos);
            long temp = gridVec.getVec().get(axis) * 2 - doubledCenter.get(axis);
            gridVec.getVec().set(axis, (int) ((doubledCenter.get(axis) - temp) / 2));
        });
        
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
            if (obj instanceof PlacementPosition p)
                return p.facing == facing;
            else
                return true;
        return false;
    }
    
    public LittleBoxAbsolute toAbsoluteBox() {
        return new LittleBoxAbsolute(pos, new LittleBox(gridVec.getVec().x, gridVec.getVec().y, gridVec.getVec().z, gridVec.getVec().x + 1, gridVec.getVec().y + 1, gridVec
                .getVec().z + 1), getGrid());
    }
}
