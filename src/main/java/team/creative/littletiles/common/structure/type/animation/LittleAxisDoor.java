package team.creative.littletiles.common.structure.type.animation;

import java.util.function.BiFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelativeAxis;
import team.creative.littletiles.common.structure.animation.PhysicalState;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.directional.StructureDirectionalType;
import team.creative.littletiles.common.structure.directional.StructureDirectionalType.StructureDirectionalTypeSimple;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittleAxisDoor extends LittleDoor {
    
    public static void load() {
        StructureDirectionalType.register(LittleAxisDoorRotation.class, new StructureDirectionalTypeSimple<LittleAxisDoorRotation>() {
            
            @Override
            public LittleAxisDoorRotation read(Tag nbt) {
                if (nbt instanceof CompoundTag tag) {
                    Axis axis = Axis.values()[tag.getInt("a")];
                    if (tag.contains("d"))
                        return new LittleAxisDoorRotationFixed(axis, tag.getDouble("d"));
                    return new LittleAxisDoorRotationDirection(axis, tag.getBoolean("c"));
                }
                return null;
            }
            
            @Override
            public Tag write(LittleAxisDoorRotation value) {
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("a", value.axis.ordinal());
                if (value instanceof LittleAxisDoorRotationFixed fixed)
                    nbt.putDouble("d", fixed.degree);
                else
                    nbt.putBoolean("c", ((LittleAxisDoorRotationDirection) value).clockwise);
                return nbt;
            }
            
            @Override
            public LittleAxisDoorRotation move(LittleAxisDoorRotation value, LittleVecGrid vec) {
                return value;
            }
            
            @Override
            public LittleAxisDoorRotation mirror(LittleAxisDoorRotation value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
                value.mirror(axis);
                return value;
            }
            
            @Override
            public LittleAxisDoorRotation rotate(LittleAxisDoorRotation value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
                value.rotate(rotation);
                return value;
            }
            
            @Override
            public LittleAxisDoorRotation getDefault() {
                return new LittleAxisDoorRotationDirection(Axis.Y, true);
            }
        });
    }
    
    @StructureDirectional
    public LittleAxisDoorRotation rotation;
    
    public LittleAxisDoor(LittleStateStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    public static abstract class LittleAxisDoorRotation {
        
        public Axis axis;
        
        public LittleAxisDoorRotation(Axis axis) {
            this.axis = axis;
        }
        
        public abstract void mirror(Axis axis);
        
        public void rotate(Rotation rotation) {
            axis = rotation.rotate(axis);
            rotateInternal(rotation);
        }
        
        protected abstract void rotateInternal(Rotation rotation);
        
        public abstract void apply(PhysicalState state);
    }
    
    public static class LittleAxisDoorRotationDirection extends LittleAxisDoorRotation {
        
        public boolean clockwise;
        
        public LittleAxisDoorRotationDirection(Axis axis, boolean clockwise) {
            super(axis);
            this.clockwise = clockwise;
        }
        
        @Override
        public void mirror(Axis axis) {
            if (this.axis == axis)
                clockwise = !clockwise;
        }
        
        @Override
        protected void rotateInternal(Rotation rotation) {
            clockwise = Rotation.getRotation(axis, clockwise).rotate(rotation).clockwise;
        }
        
        @Override
        public void apply(PhysicalState state) {
            state.rot(axis, clockwise ? 90 : -90);
        }
        
    }
    
    public static class LittleAxisDoorRotationFixed extends LittleAxisDoorRotation {
        
        public double degree;
        
        public LittleAxisDoorRotationFixed(Axis axis, double degree) {
            super(axis);
            this.degree = degree;
        }
        
        @Override
        public void mirror(Axis axis) {
            if (this.axis == axis)
                degree = -degree;
        }
        
        @Override
        protected void rotateInternal(Rotation rotation) {
            degree = Rotation.getRotation(axis, degree > 0 ? true : false).rotate(rotation).clockwise ? Math.abs(degree) : -Math.abs(degree);
        }
        
        @Override
        public void apply(PhysicalState state) {
            state.rot(axis, degree);
        }
    }
    
    public static class LittleDoorTypeAxisCenter extends LittleDoorType {
        
        public <T extends LittleDoor> LittleDoorTypeAxisCenter(String id, Class<T> structureClass, BiFunction<? extends LittleStateStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
            super(id, structureClass, factory, attribute);
        }
        
        @Override
        protected LittlePlaceBoxRelative getPlaceBox(Object value, StructureDirectionalField type, LittleGroup previews) {
            if (type.key.equals("center"))
                return new LittlePlaceBoxRelativeAxis(((StructureRelative) value).getBox(), (StructureRelative) value, type, Axis.values()[previews.getStructureTag().getCompound(
                    "rotation").getInt("a")]);
            return super.getPlaceBox(value, type, previews);
        }
    }
    
}
