package team.creative.littletiles.common.math.vec;

import java.security.InvalidParameterException;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.HitResult;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleVecAbsolute implements IGridBased {
    
    protected BlockPos pos;
    protected LittleVecGrid gridVec;
    
    public LittleVecAbsolute(String name, CompoundTag nbt) {
        int[] array = nbt.getIntArray(name);
        if (array.length == 7) {
            this.pos = new BlockPos(array[0], array[1], array[2]);
            this.gridVec = new LittleVecGrid(new LittleVec(array[4], array[5], array[6]), LittleGrid.get(array[3]));
        } else
            throw new InvalidParameterException("No valid coords given " + nbt);
    }
    
    public LittleVecAbsolute(HitResult result, LittleGrid grid) {
        long x = grid.toGridAccurate(result.getLocation().x);
        long y = grid.toGridAccurate(result.getLocation().y);
        long z = grid.toGridAccurate(result.getLocation().z);
        this.pos = new BlockPos((int) Math.floor(grid.toVanillaGrid(x)), (int) Math.floor(grid.toVanillaGrid(y)), (int) Math.floor(grid.toVanillaGrid(z)));
        this.gridVec = new LittleVecGrid(new LittleVec((int) (x - grid.toGridAccurate(pos.getX())), (int) (y - grid.toGridAccurate(pos.getY())), (int) (z - grid
                .toGridAccurate(pos.getZ()))), grid);
    }
    
    public LittleVecAbsolute(BlockPos pos, LittleGrid context) {
        this(pos, new LittleVecGrid(new LittleVec(0, 0, 0), context));
    }
    
    public LittleVecAbsolute(BlockPos pos, LittleGrid context, LittleVec vec) {
        this(pos, new LittleVecGrid(vec, context));
    }
    
    public LittleVecAbsolute(BlockPos pos, LittleVecGrid gridVec) {
        this.pos = pos;
        this.gridVec = gridVec;
    }
    
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    @Override
    public int getSmallest() {
        return gridVec.getSmallest();
    }
    
    @Override
    public LittleGrid getGrid() {
        return gridVec.getGrid();
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        this.gridVec.convertTo(to);
    }
    
    public LittleVec getRelative(BlockPos pos) {
        LittleVec newVec = new LittleVec(getGrid(), this.pos.subtract(pos));
        newVec.add(this.gridVec.vec);
        return newVec;
    }
    
    @SuppressWarnings("deprecation")
    public LittleVecGrid getRelative(LittleVecAbsolute pos) {
        forceSameGrid(pos);
        LittleVecGrid newVec = new LittleVecGrid(new LittleVec(getGrid(), this.pos.subtract(pos.pos)), getGrid());
        newVec.vec.add(this.gridVec.vec);
        newVec.vec.sub(pos.gridVec.vec);
        
        pos.convertToSmallest();
        convertToSmallest();
        return newVec;
    }
    
    @Override
    public void convertToSmallest() {
        removeInternalBlockOffset();
        IGridBased.super.convertToSmallest();
    }
    
    public void add(LittleVecAbsolute pos) {
        this.pos = this.pos.offset(pos.pos);
        sameGrid(pos, () -> this.gridVec.vec.add(pos.gridVec.vec));
    }
    
    public void sub(LittleVecAbsolute pos) {
        this.pos = this.pos.subtract(pos.pos);
        sameGrid(pos, () -> this.gridVec.vec.sub(pos.gridVec.vec));
    }
    
    public void add(Vec3i vec) {
        pos = pos.offset(vec);
    }
    
    public void sub(Vec3i vec) {
        pos = pos.subtract(vec);
    }
    
    public void add(LittleVecGrid vec) {
        gridVec.add(vec);
    }
    
    public void sub(LittleVecGrid vec) {
        gridVec.sub(vec);
    }
    
    public void removeInternalBlockOffset() {
        LittleGrid context = getGrid();
        // Larger
        if (gridVec.vec.x >= context.count) {
            int amount = gridVec.vec.x / context.count;
            gridVec.vec.x -= amount * context.count;
            pos = pos.offset(amount, 0, 0);
        }
        if (gridVec.vec.y >= context.count) {
            int amount = gridVec.vec.y / context.count;
            gridVec.vec.y -= amount * context.count;
            pos = pos.offset(0, amount, 0);
        }
        if (gridVec.vec.z >= context.count) {
            int amount = gridVec.vec.z / context.count;
            gridVec.vec.z -= amount * context.count;
            pos = pos.offset(0, 0, amount);
        }
        
        // Smaller
        if (gridVec.vec.x < 0) {
            int amount = (int) Math.ceil(Math.abs(gridVec.vec.x / (double) context.count));
            gridVec.vec.x += amount * context.count;
            pos = pos.offset(-amount, 0, 0);
        }
        if (gridVec.vec.y < 0) {
            int amount = (int) Math.ceil(Math.abs(gridVec.vec.y / (double) context.count));
            gridVec.vec.y += amount * context.count;
            pos = pos.offset(0, -amount, 0);
        }
        if (gridVec.vec.z < 0) {
            int amount = (int) Math.ceil(Math.abs(gridVec.vec.z / (double) context.count));
            gridVec.vec.z += amount * context.count;
            pos = pos.offset(0, 0, -amount);
        }
    }
    
    public LittleVecAbsolute copy() {
        return new LittleVecAbsolute(pos, gridVec.copy());
    }
    
    public double getVanillaGrid(Axis axis) {
        switch (axis) {
        case X:
            return getPosX();
        case Y:
            return getPosY();
        case Z:
            return getPosZ();
        default:
            return 0;
        }
    }
    
    public double getPosX() {
        return pos.getX() + gridVec.getPosX();
    }
    
    public double getPosY() {
        return pos.getY() + gridVec.getPosY();
    }
    
    public double getPosZ() {
        return pos.getZ() + gridVec.getPosZ();
    }
    
    public Vec3d getVec3d() {
        return new Vec3d(getPosX(), getPosY(), getPosZ());
    }
    
    public void writeToNBT(String name, CompoundTag nbt) {
        nbt.putIntArray(name, new int[] { pos.getX(), pos.getY(), pos.getZ(), gridVec.grid.count, gridVec.vec.x, gridVec.vec.y, gridVec.vec.z });
    }
    
    @Override
    public int hashCode() {
        return pos.hashCode();
    }
    
    public LittleVecGrid getVecGrid() {
        return gridVec;
    }
    
    public LittleVec getVec() {
        return gridVec.getVec();
    }
    
    public void setVecContext(LittleVecGrid vec) {
        this.gridVec = vec;
    }
    
    @Deprecated
    public void overwriteGrid(LittleGrid grid) {
        gridVec.overwriteGrid(grid);
    }
    
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof LittleVecAbsolute) {
            LittleVecAbsolute pos = (LittleVecAbsolute) paramObject;
            LittleGrid newContext = LittleGrid.max(getGrid(), pos.getGrid());
            
            int multiplier = newContext.count / getGrid().count;
            long thisX = this.pos.getX() * newContext.count + this.gridVec.vec.x * multiplier;
            long thisY = this.pos.getY() * newContext.count + this.gridVec.vec.y * multiplier;
            long thisZ = this.pos.getZ() * newContext.count + this.gridVec.vec.z * multiplier;
            
            multiplier = newContext.count / pos.getGrid().count;
            long otherX = pos.pos.getX() * newContext.count + pos.gridVec.vec.x * multiplier;
            long otherY = pos.pos.getY() * newContext.count + pos.gridVec.vec.y * multiplier;
            long otherZ = pos.pos.getZ() * newContext.count + pos.gridVec.vec.z * multiplier;
            
            return thisX == otherX && thisY == otherY && thisZ == otherZ;
        }
        return super.equals(paramObject);
    }
    
    @Override
    public String toString() {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ",grid:" + gridVec.grid.count + "," + gridVec.vec.x + "," + gridVec.vec.y + "," + gridVec.vec.z + "]";
    }
}
