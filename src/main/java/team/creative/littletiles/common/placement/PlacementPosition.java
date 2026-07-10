package team.creative.littletiles.common.placement;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecAbsolute;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class PlacementPosition implements IGridBased {
    
    protected final LittleBoxAbsolute box;
    public final Facing facing;
    
    public PlacementPosition(LittleBoxAbsolute box, Facing facing) {
        this.box = box;
        this.facing = facing;
    }
    
    public PlacementPosition(BlockPos pos, LittleVecGrid vec, Facing facing) {
        this.box = new LittleBoxAbsolute(pos, new LittleBox(vec.getVec()), vec.getGrid());
        this.facing = facing;
    }
    
    public PlacementPosition(LittleVecAbsolute vec, Facing facing) {
        this(vec.getPos(), vec.getVecGrid(), facing);
    }
    
    public PlacementPosition(BlockPos pos, LittleGrid grid, LittleVec vec, Facing facing) {
        this.box = new LittleBoxAbsolute(pos, new LittleBox(vec), grid);
        this.facing = facing;
    }
    
    public PlacementPosition(BlockHitResult result, LittleGrid grid) {
        this(new LittleVecAbsolute(result, grid), Facing.get(result.getDirection()));
    }
    
    public PlacementPosition copy() {
        return new PlacementPosition(box.copy(), facing);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlacementPosition p)
            return p.box.equals(box) && p.facing == facing;
        return false;
    }
    
    public double getVanillaGrid(Axis axis) {
        return switch (axis) {
            case X -> box.getMinPosX();
            case Y -> box.getMinPosY();
            case Z -> box.getMinPosZ();
        };
    }
    
    public LittleBoxAbsolute box() {
        return box;
    }
    
    public AABB getBox() {
        return box.toAABB();
    }
    
    public ABB getBB() {
        return box.toABB();
    }
    
    public double getMinPosX() {
        return box.getMinPosX();
    }
    
    public double getMinPosY() {
        return box.getMinPosY();
    }
    
    public double getMinPosZ() {
        return box.getMinPosZ();
    }
    
    public LittleVec getRelative(BlockPos pos) {
        LittleVec newVec = new LittleVec(getGrid(), box.pos.subtract(pos));
        newVec.add(box.box.getMinVec());
        return newVec;
    }
    
    public LittleBox getRelative(BlockPos pos, LittleGrid target) {
        LittleVec newVec = new LittleVec(getGrid(), box.pos.subtract(pos));
        LittleBox box = this.box.box.copy(); // TODO Test if that actually works
        box.add(newVec);
        return box;
    }
    
    public BlockPos getPos() {
        return box.pos;
    }
    
    public LittleVecGrid getMin() {
        return new LittleVecGrid(box.box.getMinVec(), box.grid);
    }
    
    public BlockPos getMinPos() {
        return box.getMinPos();
    }
    
    @Override
    public LittleGrid getGrid() {
        return box.getGrid();
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        box.convertTo(to);
    }
    
    @Override
    public int getSmallest() {
        return box.getSmallest();
    }
    
    public void move(LittleVecGrid vec) {
        box.move(vec);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void move(LittleGrid grid, Facing facing) {
        LittleVecGrid vec = new LittleVecGrid(new LittleVec(facing), grid);
        if (Screen.hasControlDown())
            vec.getVec().scale(grid.count);
        box.move(vec);
    }
    
}
