package team.creative.littletiles.common.math.vec;

import java.security.InvalidParameterException;

import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleVecGrid implements IGridBased {
    
    protected LittleVec vec;
    protected LittleGrid grid;
    
    public LittleVecGrid() {
        this(new LittleVec(0, 0, 0), LittleGrid.min());
    }
    
    public LittleVecGrid(String name, CompoundTag nbt) {
        int[] array = nbt.getIntArray(name);
        if (array.length == 3) {
            LittleVec vec = new LittleVec(name, nbt);
            this.grid = LittleGrid.defaultGrid();
            this.vec = new LittleVec(vec.x, vec.y, vec.z);
        } else if (array.length == 4) {
            this.vec = new LittleVec(array[0], array[1], array[2]);
            this.grid = LittleGrid.get(array[3]);
        } else
            throw new InvalidParameterException("No valid coords given " + nbt);
    }
    
    public LittleVecGrid(LittleVec vec, LittleGrid grid) {
        this.vec = vec;
        this.grid = grid;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        vec.convertTo(grid, to);
        this.grid = to;
    }
    
    @Override
    public int getSmallest() {
        return vec.getSmallest(grid);
    }
    
    public void add(LittleVecGrid vec) {
        sameGrid(vec, () -> this.vec.add(vec.vec));
    }
    
    public void add(BlockPos pos) {
        this.vec.add(pos, grid);
    }
    
    public void sub(LittleVecGrid vec) {
        sameGrid(vec, () -> this.vec.sub(vec.vec));
    }
    
    public void sub(BlockPos pos) {
        this.vec.sub(pos, grid);
    }
    
    public LittleVecGrid copy() {
        return new LittleVecGrid(vec.copy(), grid);
    }
    
    public BlockPos getBlockPos() {
        return vec.getBlockPos(grid);
    }
    
    public double getPosX() {
        return vec.getPosX(grid);
    }
    
    public double getPosY() {
        return vec.getPosY(grid);
    }
    
    public double getPosZ() {
        return vec.getPosZ(grid);
    }
    
    public LittleVec getVec() {
        return vec;
    }
    
    public Vec3d getVec3d() {
        return vec.getVec(grid);
    }
    
    public Vector3d getVector() {
        return vec.getVector(grid);
    }
    
    public LittleVec getVec(LittleGrid grid) {
        if (grid == this.grid)
            return vec.copy();
        LittleVec newVec = vec.copy();
        newVec.convertTo(this.grid, grid);
        return newVec;
    }
    
    public void writeToNBT(String name, CompoundTag nbt) {
        nbt.putIntArray(name, new int[] { vec.x, vec.y, vec.z, grid.count });
    }
    
    @Override
    public int hashCode() {
        return vec.hashCode();
    }
    
    @Deprecated
    public void overwriteGrid(LittleGrid grid) {
        this.grid = grid;
    }
    
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof LittleVecGrid) {
            LittleVecGrid otherVec = (LittleVecGrid) paramObject;
            LittleGrid oldContext = grid;
            LittleGrid oldContextPos = otherVec.grid;
            
            if (getGrid() != otherVec.getGrid()) {
                if (getGrid().count > otherVec.getGrid().count)
                    otherVec.convertTo(getGrid());
                else
                    convertTo(otherVec.getGrid());
            }
            
            boolean equal = grid == otherVec.grid && vec.equals(otherVec.vec);
            
            otherVec.convertTo(oldContextPos);
            convertTo(oldContext);
            
            return equal;
        }
        
        return super.equals(paramObject);
    }
    
    @Override
    public String toString() {
        return "[" + vec.x + "," + vec.y + "," + vec.z + ",grid:" + grid.count + "]";
    }
}
