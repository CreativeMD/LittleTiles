package team.creative.littletiles.common.math.vec;

import java.security.InvalidParameterException;

import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.math.vec.VectorUtils;
import team.creative.littletiles.common.grid.LittleGrid;

public class LittleVec {
    
    public static final LittleVec ZERO = new LittleVec(0, 0, 0);
    
    public int x;
    public int y;
    public int z;
    
    public LittleVec(String name, CompoundTag nbt) {
        int[] array = nbt.getIntArray(name);
        if (array.length == 3)
            set(array[0], array[1], array[2]);
        else
            throw new InvalidParameterException("No valid coords given " + nbt);
    }
    
    public LittleVec(LittleGrid grid, BlockHitResult result) {
        this(grid, result.getLocation(), Facing.get(result.getDirection()));
    }
    
    public LittleVec(LittleGrid grid, Vec3 vec, Facing facing) {
        this(grid, vec);
        if (facing.positive && !grid.isAtEdge(VectorUtils.get(facing.axis, vec)))
            set(facing.axis, get(facing.axis) + 1);
    }
    
    public LittleVec(LittleGrid grid, Vec3 vec) {
        this.x = grid.toGrid(vec.x);
        this.y = grid.toGrid(vec.y);
        this.z = grid.toGrid(vec.z);
    }
    
    public LittleVec(LittleGrid grid, Vec3d vec) {
        this.x = grid.toGrid(vec.x);
        this.y = grid.toGrid(vec.y);
        this.z = grid.toGrid(vec.z);
    }
    
    public LittleVec(LittleGrid grid, Vector3d vec) {
        this.x = grid.toGrid(vec.x);
        this.y = grid.toGrid(vec.y);
        this.z = grid.toGrid(vec.z);
    }
    
    public LittleVec(Facing facing) {
        switch (facing) {
        case EAST:
            set(1, 0, 0);
            break;
        case WEST:
            set(-1, 0, 0);
            break;
        case UP:
            set(0, 1, 0);
            break;
        case DOWN:
            set(0, -1, 0);
            break;
        case SOUTH:
            set(0, 0, 1);
            break;
        case NORTH:
            set(0, 0, -1);
            break;
        default:
            set(0, 0, 0);
            break;
        }
    }
    
    public LittleVec(int x, int y, int z) {
        set(x, y, z);
    }
    
    public LittleVec(LittleGrid grid, Vec3i vec) {
        this(grid.toGrid(vec.getX()), grid.toGrid(vec.getY()), grid.toGrid(vec.getZ()));
    }
    
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void set(LittleGrid grid, Vec3i vec) {
        set(grid.toGrid(vec.getX()), grid.toGrid(vec.getY()), grid.toGrid(vec.getZ()));
    }
    
    public int getSmallest(LittleGrid grid) {
        int size = LittleGrid.min().count;
        size = Math.max(size, grid.getMinGrid(x));
        size = Math.max(size, grid.getMinGrid(y));
        size = Math.max(size, grid.getMinGrid(z));
        return size;
    }
    
    public void convertTo(LittleGrid from, LittleGrid to) {
        if (from.count > to.count) {
            int ratio = from.count / to.count;
            x /= ratio;
            y /= ratio;
            z /= ratio;
        } else {
            int ratio = to.count / from.count;
            x *= ratio;
            y *= ratio;
            z *= ratio;
        }
    }
    
    public BlockPos getBlockPos(LittleGrid grid) {
        return new BlockPos((int) Math.floor(grid.toVanillaGrid(x)), (int) Math.floor(grid.toVanillaGrid(y)), (int) Math.floor(grid.toVanillaGrid(z)));
    }
    
    public Vec3d getVec(LittleGrid grid) {
        return new Vec3d(grid.toVanillaGrid(x), grid.toVanillaGrid(y), grid.toVanillaGrid(z));
    }
    
    public Vector3d getVector(LittleGrid grid) {
        return new Vector3d(grid.toVanillaGrid(x), grid.toVanillaGrid(y), grid.toVanillaGrid(z));
    }
    
    public double getPosX(LittleGrid grid) {
        return grid.toVanillaGrid(x);
    }
    
    public double getPosY(LittleGrid grid) {
        return grid.toVanillaGrid(y);
    }
    
    public double getPosZ(LittleGrid grid) {
        return grid.toVanillaGrid(z);
    }
    
    public void add(Facing facing) {
        set(facing.axis, get(facing.axis) + facing.offset());
    }
    
    public void add(LittleVec vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }
    
    public void add(BlockPos pos, LittleGrid grid) {
        this.x += grid.toGrid(pos.getX());
        this.y += grid.toGrid(pos.getY());
        this.z += grid.toGrid(pos.getZ());
    }
    
    public void sub(Facing facing) {
        set(facing.axis, get(facing.axis) - facing.offset());
    }
    
    public void sub(LittleVec vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
    }
    
    public void sub(BlockPos pos, LittleGrid grid) {
        this.x -= grid.toGrid(pos.getX());
        this.y -= grid.toGrid(pos.getY());
        this.z -= grid.toGrid(pos.getZ());
    }
    
    public void flip(Axis axis) {
        set(axis, -get(axis));
    }
    
    public void rotateVec(Rotation rotation) {
        int tempX = x;
        int tempY = y;
        int tempZ = z;
        this.x = rotation.getMatrix().getX(tempX, tempY, tempZ);
        this.y = rotation.getMatrix().getY(tempX, tempY, tempZ);
        this.z = rotation.getMatrix().getZ(tempX, tempY, tempZ);
    }
    
    public double distanceTo(LittleVec vec) {
        return Math.sqrt(Math.pow(vec.x - this.x, 2) + Math.pow(vec.y - this.y, 2) + Math.pow(vec.z - this.z, 2));
    }
    
    @Override
    public boolean equals(Object object) {
        if (object instanceof LittleVec)
            return x == ((LittleVec) object).x && y == ((LittleVec) object).y && z == ((LittleVec) object).z;
        return super.equals(object);
    }
    
    public LittleVec copy() {
        return new LittleVec(x, y, z);
    }
    
    public void writeToNBT(String name, CompoundTag nbt) {
        nbt.putIntArray(name, new int[] { x, y, z });
    }
    
    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }
    
    public void invert() {
        set(-x, -y, -z);
    }
    
    public void scale(int factor) {
        x *= factor;
        y *= factor;
        z *= factor;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public void setX(int value) {
        x = value;
    }
    
    public void setY(int value) {
        y = value;
    }
    
    public void setZ(int value) {
        z = value;
    }
    
    public int get(Axis axis) {
        switch (axis) {
        case X:
            return x;
        case Y:
            return y;
        case Z:
            return z;
        }
        return 0;
    }
    
    public void set(Axis axis, int value) {
        switch (axis) {
        case X:
            x = value;
            break;
        case Y:
            y = value;
            break;
        case Z:
            z = value;
            break;
        }
    }
    
    public int getVolume() {
        return x * y * z;
    }
    
    /** @return the volume in percent to a size of a normal block */
    public double getPercentVolume(LittleGrid context) {
        return getVolume() / (double) (context.count3d);
    }
    
    public LittleVec calculateInvertedCenter() {
        return new LittleVec((int) Math.ceil(this.x / 2D), (int) Math.ceil(this.y / 2D), (int) Math.ceil(this.z / 2D));
    }
    
    public LittleVec calculateCenter() {
        return new LittleVec((int) Math.floor(this.x / 2D), (int) Math.floor(this.y / 2D), (int) Math.floor(this.z / 2D));
    }
    
    public LittleVec max(LittleVec size) {
        this.x = Math.max(this.x, size.x);
        this.y = Math.max(this.y, size.y);
        this.z = Math.max(this.z, size.z);
        return this;
    }
    
    public Axis getLongestAxis() {
        if (Math.abs(x) > Math.abs(y))
            if (Math.abs(x) > Math.abs(z))
                return Axis.X;
            else
                return Axis.Z;
        else if (Math.abs(z) > Math.abs(y))
            return Axis.Z;
        return Axis.Y;
        
    }
    
}
