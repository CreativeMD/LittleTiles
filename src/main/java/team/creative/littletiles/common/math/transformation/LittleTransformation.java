package team.creative.littletiles.common.math.transformation;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class LittleTransformation {
    
    public BlockPos center;
    
    public int rotX;
    public int rotY;
    public int rotZ;
    
    public LittleVec doubledRotationCenter;
    
    public LittleVecGrid offset;
    
    public LittleTransformation(int[] array) {
        if (array.length != 13)
            throw new IllegalArgumentException("Invalid array when creating door transformation!");
        
        center = new BlockPos(array[0], array[1], array[2]);
        rotX = array[3];
        rotY = array[4];
        rotZ = array[5];
        doubledRotationCenter = new LittleVec(array[6], array[7], array[8]);
        offset = new LittleVecGrid(new LittleVec(array[9], array[10], array[11]), LittleGrid.get(array[12]));
    }
    
    public LittleTransformation(BlockPos center, int rotX, int rotY, int rotZ, LittleVec doubledRotationCenter, LittleVecGrid offset) {
        this.center = center;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.doubledRotationCenter = doubledRotationCenter;
        this.offset = offset;
    }
    
    public LittleTransformation(BlockPos center, Rotation rotation) {
        this.center = center;
        this.rotX = rotation.axis == Axis.X ? (rotation.clockwise ? 1 : -1) : 0;
        this.rotY = rotation.axis == Axis.Y ? (rotation.clockwise ? 1 : -1) : 0;
        this.rotZ = rotation.axis == Axis.Z ? (rotation.clockwise ? 1 : -1) : 0;
        this.doubledRotationCenter = new LittleVec(0, 0, 0);
        this.offset = new LittleVecGrid();
    }
    
    public Rotation getRotation(Axis axis) {
        switch (axis) {
        case X:
            if (rotX == 0)
                return null;
            return Rotation.getRotation(axis, rotX > 0);
        case Y:
            if (rotY == 0)
                return null;
            return Rotation.getRotation(axis, rotY > 0);
        case Z:
            if (rotZ == 0)
                return null;
            return Rotation.getRotation(axis, rotZ > 0);
        }
        return null;
    }
    
    public BlockPos transform(BlockPos pos) {
        pos = pos.subtract(center);
        if (rotX != 0) {
            Rotation rotation = getRotation(Axis.X);
            for (int i = 0; i < Math.abs(rotX); i++)
                pos = rotation.transform(pos);
        }
        if (rotY != 0) {
            Rotation rotation = getRotation(Axis.Y);
            for (int i = 0; i < Math.abs(rotY); i++)
                pos = rotation.transform(pos);
        }
        if (rotZ != 0) {
            Rotation rotation = getRotation(Axis.Z);
            for (int i = 0; i < Math.abs(rotZ); i++)
                pos = rotation.transform(pos);
        }
        
        pos = pos.offset(center);
        
        if (offset != null)
            pos = pos.offset(offset.getBlockPos());
        return pos;
    }
    
    public void transform(LittleGroupAbsolute previews) {
        if (rotX != 0) {
            Rotation rotation = getRotation(Axis.X);
            for (int i = 0; i < Math.abs(rotX); i++)
                previews.group.rotate(rotation, doubledRotationCenter);
        }
        if (rotY != 0) {
            Rotation rotation = getRotation(Axis.Y);
            for (int i = 0; i < Math.abs(rotY); i++)
                previews.group.rotate(rotation, doubledRotationCenter);
        }
        if (rotZ != 0) {
            Rotation rotation = getRotation(Axis.Z);
            for (int i = 0; i < Math.abs(rotZ); i++)
                previews.group.rotate(rotation, doubledRotationCenter);
        }
        
        if (offset != null)
            previews.group.move(offset);
    }
    
    public int[] array() {
        return new int[] { center.getX(), center.getY(), center.getZ(), rotX, rotY, rotZ, doubledRotationCenter.x, doubledRotationCenter.y, doubledRotationCenter.z, offset
                .getVec().x, offset.getVec().y, offset.getVec().z, offset.getGrid().count };
    }
    
    @Override
    public String toString() {
        return "center:" + center.getX() + "," + center.getY() + "," + center.getZ() + ";rotation:" + rotX + "," + rotY + "," + rotZ + ";offset:" + offset.getVec().x + "," + offset
                .getVec().y + "," + offset.getVec().z + ";context:" + offset.getGrid();
    }
}
