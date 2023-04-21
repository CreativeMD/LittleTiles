package team.creative.littletiles.common.structure.animation;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public class PhysicalState {
    
    private Vec3d offset;
    private Vec3d rotation;
    
    public PhysicalState() {
        offset = new Vec3d();
        rotation = new Vec3d();
    }
    
    public PhysicalState(CompoundTag nbt) {
        this.offset = new Vec3d(nbt.getDouble("oX"), nbt.getDouble("oY"), nbt.getDouble("oZ"));
        this.rotation = new Vec3d(nbt.getDouble("rX"), nbt.getDouble("rY"), nbt.getDouble("rZ"));
    }
    
    public boolean isAligned() {
        return offset.x == 0 && offset.y == 0 && offset.z == 0 && rotation.x % 360 == 0 && rotation.y % 360 == 0 && rotation.z % 360 == 0;
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        if (offset.x != 0)
            nbt.putDouble("oX", offset.x);
        if (offset.y != 0)
            nbt.putDouble("oY", offset.y);
        if (offset.z != 0)
            nbt.putDouble("oZ", offset.z);
        
        if (rotation.x != 0)
            nbt.putDouble("rX", rotation.x);
        if (rotation.y != 0)
            nbt.putDouble("rY", rotation.y);
        if (rotation.z != 0)
            nbt.putDouble("rZ", rotation.z);
        return nbt;
    }
    
    public double offX() {
        return offset.x;
    }
    
    public double offY() {
        return offset.y;
    }
    
    public double offZ() {
        return offset.z;
    }
    
    public double rotX() {
        return rotation.x;
    }
    
    public double rotY() {
        return rotation.y;
    }
    
    public double rotZ() {
        return rotation.z;
    }
    
    public void off(Facing facing, double value) {
        offset.set(facing.axis, facing.offset() * value);
    }
    
    public void off(double x, double y, double z) {
        offset.x = x;
        offset.y = y;
        offset.z = z;
    }
    
    public void offX(double value) {
        offset.x = value;
    }
    
    public void offY(double value) {
        offset.y = value;
    }
    
    public void offZ(double value) {
        offset.z = value;
    }
    
    public void rot(double x, double y, double z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }
    
    public void rotX(double value) {
        rotation.x = value;
    }
    
    public void rotY(double value) {
        rotation.y = value;
    }
    
    public void rotZ(double value) {
        rotation.z = value;
    }
    
    public Vec3d rotation() {
        return rotation.copy();
    }
    
    public Vec3d offset() {
        return offset.copy();
    }
    
    public double rot(Axis axis) {
        return rotation.get(axis);
    }
    
    public void rot(Axis axis, double value) {
        rotation.set(axis, value);
    }
    
    public double off(Axis axis) {
        return offset.get(axis);
    }
    
    public void off(Axis axis, double value) {
        offset.set(axis, value);
    }
    
    public void mirror(Axis axis) {
        axis.mirror(offset);
        axis.mirror(rotation);
    }
    
    public void rotate(Rotation rotation) {
        rotation.transform(this.offset);
        rotation.transform(this.rotation);
    }
    
    @Override
    public String toString() {
        return "off" + offset + ",rot" + rotation;
    }
    
}
