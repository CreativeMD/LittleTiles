package com.creativemd.littletiles.common.tile.math.vec;

import java.security.InvalidParameterException;

import com.creativemd.littletiles.common.util.grid.IGridBased;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LittleAbsoluteVec implements IGridBased {
    
    protected BlockPos pos;
    protected LittleVecContext contextVec;
    
    public LittleAbsoluteVec(String name, NBTTagCompound nbt) {
        int[] array = nbt.getIntArray(name);
        if (array.length == 3) // Loading vec
        {
            LittleVec vec = new LittleVec(name, nbt);
            LittleGridContext context = LittleGridContext.get();
            this.pos = vec.getBlockPos(context);
            this.contextVec = new LittleVecContext(new LittleVec(vec.x - pos.getX() * context.size, vec.y - pos.getY() * context.size, vec.z - pos.getZ() * context.size), context);
        } else if (array.length == 7) {
            this.pos = new BlockPos(array[0], array[1], array[2]);
            this.contextVec = new LittleVecContext(new LittleVec(array[4], array[5], array[6]), LittleGridContext.get(array[3]));
        } else
            throw new InvalidParameterException("No valid coords given " + nbt);
    }
    
    public LittleAbsoluteVec(RayTraceResult result, LittleGridContext context) {
        long x = context.toGridAccurate(result.hitVec.x);
        long y = context.toGridAccurate(result.hitVec.y);
        long z = context.toGridAccurate(result.hitVec.z);
        this.pos = new BlockPos((int) Math.floor(context.toVanillaGrid(x)), (int) Math.floor(context.toVanillaGrid(y)), (int) Math.floor(context.toVanillaGrid(z)));
        this.contextVec = new LittleVecContext(new LittleVec((int) (x - context.toGridAccurate(pos.getX())), (int) (y - context.toGridAccurate(pos.getY())), (int) (z - context
            .toGridAccurate(pos.getZ()))), context);
    }
    
    public LittleAbsoluteVec(BlockPos pos, LittleGridContext context) {
        this(pos, new LittleVecContext(new LittleVec(0, 0, 0), context));
    }
    
    public LittleAbsoluteVec(BlockPos pos, LittleGridContext context, LittleVec vec) {
        this(pos, new LittleVecContext(vec, context));
    }
    
    public LittleAbsoluteVec(BlockPos pos, LittleVecContext contextVec) {
        this.pos = pos;
        this.contextVec = contextVec;
    }
    
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public int getSmallestContext() {
        return contextVec.getSmallestContext();
    }
    
    @Override
    public void convertToSmallest() {
        this.contextVec.convertToSmallest();
    }
    
    @Override
    public void convertTo(LittleGridContext to) {
        this.contextVec.convertTo(to);
    }
    
    public LittleVec getRelative(BlockPos pos) {
        LittleVec newVec = new LittleVec(getContext(), this.pos.subtract(pos));
        newVec.add(this.contextVec.vec);
        return newVec;
    }
    
    public LittleVecContext getRelative(LittleAbsoluteVec pos) {
        forceContext(pos);
        LittleVecContext newVec = new LittleVecContext(new LittleVec(getContext(), this.pos.subtract(pos.pos)), getContext());
        newVec.vec.add(this.contextVec.vec);
        newVec.vec.sub(pos.contextVec.vec);
        
        pos.convertToSmallest();
        convertToSmallest();
        return newVec;
    }
    
    public void add(LittleAbsoluteVec pos) {
        this.pos = this.pos.add(pos.pos);
        ensureContext(pos, () -> this.contextVec.vec.add(pos.contextVec.vec));
    }
    
    public void sub(LittleAbsoluteVec pos) {
        this.pos = this.pos.subtract(pos.pos);
        ensureContext(pos, () -> this.contextVec.vec.sub(pos.contextVec.vec));
    }
    
    public void add(Vec3i vec) {
        pos = pos.add(vec);
    }
    
    public void sub(Vec3i vec) {
        pos = pos.subtract(vec);
    }
    
    public void add(LittleVecContext vec) {
        contextVec.add(vec);
    }
    
    public void sub(LittleVecContext vec) {
        contextVec.sub(vec);
    }
    
    public void removeInternalBlockOffset() {
        LittleGridContext context = getContext();
        // Larger
        if (contextVec.vec.x >= context.size) {
            int amount = contextVec.vec.x / context.size;
            contextVec.vec.x -= amount * context.size;
            pos = pos.add(amount, 0, 0);
        }
        if (contextVec.vec.y >= context.size) {
            int amount = contextVec.vec.y / context.size;
            contextVec.vec.y -= amount * context.size;
            pos = pos.add(0, amount, 0);
        }
        if (contextVec.vec.z >= context.size) {
            int amount = contextVec.vec.z / context.size;
            contextVec.vec.z -= amount * context.size;
            pos = pos.add(0, 0, amount);
        }
        
        // Smaller
        if (contextVec.vec.x < 0) {
            int amount = (int) Math.ceil(Math.abs(contextVec.vec.x / (double) context.size));
            contextVec.vec.x += amount * context.size;
            pos = pos.add(-amount, 0, 0);
        }
        if (contextVec.vec.y < 0) {
            int amount = (int) Math.ceil(Math.abs(contextVec.vec.y / (double) context.size));
            contextVec.vec.y += amount * context.size;
            pos = pos.add(0, -amount, 0);
        }
        if (contextVec.vec.z < 0) {
            int amount = (int) Math.ceil(Math.abs(contextVec.vec.z / (double) context.size));
            contextVec.vec.z += amount * context.size;
            pos = pos.add(0, 0, -amount);
        }
    }
    
    public LittleAbsoluteVec copy() {
        return new LittleAbsoluteVec(pos, contextVec.copy());
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
        return pos.getX() + contextVec.getPosX();
    }
    
    public double getPosY() {
        return pos.getY() + contextVec.getPosY();
    }
    
    public double getPosZ() {
        return pos.getZ() + contextVec.getPosZ();
    }
    
    public Vec3d getVec3d() {
        return new Vec3d(getPosX(), getPosY(), getPosZ());
    }
    
    public void writeToNBT(String name, NBTTagCompound nbt) {
        nbt.setIntArray(name, new int[] { pos.getX(), pos.getY(), pos.getZ(), contextVec.context.size, contextVec.vec.x, contextVec.vec.y, contextVec.vec.z });
    }
    
    @Override
    public LittleGridContext getContext() {
        return contextVec.getContext();
    }
    
    @Override
    public int hashCode() {
        return pos.hashCode();
    }
    
    public LittleVecContext getVecContext() {
        return contextVec;
    }
    
    public LittleVec getVec() {
        return contextVec.getVec();
    }
    
    public void setVecContext(LittleVecContext vec) {
        this.contextVec = vec;
    }
    
    @Deprecated
    public void overwriteContext(LittleGridContext context) {
        contextVec.overwriteContext(context);
    }
    
    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof LittleAbsoluteVec) {
            LittleAbsoluteVec pos = (LittleAbsoluteVec) paramObject;
            LittleGridContext newContext = LittleGridContext.max(getContext(), pos.getContext());
            
            int multiplier = newContext.size / getContext().size;
            long thisX = this.pos.getX() * newContext.size + this.contextVec.vec.x * multiplier;
            long thisY = this.pos.getY() * newContext.size + this.contextVec.vec.y * multiplier;
            long thisZ = this.pos.getZ() * newContext.size + this.contextVec.vec.z * multiplier;
            
            multiplier = newContext.size / pos.getContext().size;
            long otherX = pos.pos.getX() * newContext.size + pos.contextVec.vec.x * multiplier;
            long otherY = pos.pos.getY() * newContext.size + pos.contextVec.vec.y * multiplier;
            long otherZ = pos.pos.getZ() * newContext.size + pos.contextVec.vec.z * multiplier;
            
            return thisX == otherX && thisY == otherY && thisZ == otherZ;
        }
        return super.equals(paramObject);
    }
    
    @Override
    public String toString() {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos
            .getZ() + ",grid:" + contextVec.context.size + "," + contextVec.vec.x + "," + contextVec.vec.y + "," + contextVec.vec.z + "]";
    }
}
