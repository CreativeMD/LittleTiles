package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3f;
import team.creative.creativecore.common.util.math.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;

public abstract class StructureDirectionalType<T> {
    
    private static HashMap<Class, StructureDirectionalType> types = new HashMap<>();
    
    public static StructureDirectionalType getType(Field field) {
        StructureDirectionalType type = types.get(field.getType());
        if (type == null)
            throw new RuntimeException("No registered directional type for " + field.getType() + ", " + field.getName());
        return type;
    }
    
    public static <T> void registerType(Class<T> clazz, StructureDirectionalType<T> type) {
        if (types.containsKey(clazz))
            throw new IllegalArgumentException("Type already exists. " + clazz);
        
        types.put(clazz, type);
    }
    
    static {
        registerType(Direction.class, new StructureDirectionalType<Direction>() {
            @Override
            public Direction read(INBT nbt) {
                if (nbt instanceof IntNBT)
                    return Direction.values()[((IntNBT) nbt).getAsInt()];
                return null;
            }
            
            @Override
            public INBT write(Direction value) {
                return IntNBT.valueOf(value.ordinal());
            }
            
            @Override
            public Direction move(Direction value, LittleGridContext context, LittleVec offset) {
                return value;
            }
            
            @Override
            public Direction flip(Direction value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
                if (axis == value.getAxis())
                    return value.getOpposite();
                return value;
            }
            
            @Override
            public Direction rotate(Direction value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
                return RotationUtils.rotate(value, rotation);
            }
            
            @Override
            public Direction getDefault() {
                return Direction.EAST;
            }
        });
        registerType(Axis.class, new StructureDirectionalType<Axis>() {
            
            @Override
            public Axis read(INBT nbt) {
                if (nbt instanceof IntNBT)
                    return Axis.values()[((IntNBT) nbt).getAsInt()];
                return null;
            }
            
            @Override
            public INBT write(Axis value) {
                return IntNBT.valueOf(value.ordinal());
            }
            
            @Override
            public Axis move(Axis value, LittleGridContext context, LittleVec offset) {
                return value;
            }
            
            @Override
            public Axis flip(Axis value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
                return value;
            }
            
            @Override
            public Axis rotate(Axis value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
                return RotationUtils.rotate(value, rotation);
            }
            
            @Override
            public Axis getDefault() {
                return Axis.X;
            }
            
        });
        registerType(StructureRelative.class, new StructureDirectionalType<StructureRelative>() {
            
            @Override
            public StructureRelative read(INBT nbt) {
                if (nbt instanceof IntArrayNBT)
                    return new StructureRelative(((IntArrayNBT) nbt).getAsIntArray());
                return null;
            }
            
            @Override
            public INBT write(StructureRelative value) {
                return new IntArrayNBT(value.write());
            }
            
            @Override
            public StructureRelative move(StructureRelative value, LittleGridContext context, LittleVec offset) {
                value.move(context, offset);
                return value;
            }
            
            @Override
            public StructureRelative flip(StructureRelative value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
                value.flip(context, axis, doubledCenter);
                return value;
            }
            
            @Override
            public StructureRelative rotate(StructureRelative value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
                value.rotate(context, rotation, doubledCenter);
                return value;
            }
            
            @Override
            public LittleGrid getContext(StructureRelative value) {
                return value.getContext();
            }
            
            @Override
            public void convertToSmallest(StructureRelative value) {
                value.convertToSmallest();
            }
            
            @Override
            public void advancedScale(StructureRelative value, int from, int to) {
                value.advancedScale(from, to);
            }
            
            @Override
            public PlacePreview getPlacePreview(StructureRelative value, LittlePreviews previews, StructureDirectionalField field) {
                return value.getPlacePreview(previews, field);
            }
            
            @Override
            public StructureRelative getDefault() {
                return new StructureRelative(new LittleBox(0, 0, 0, 1, 1, 1), LittleGridContext.get());
            }
            
        });
        registerType(Vector3f.class, new StructureDirectionalType<Vector3f>() {
            
            @Override
            public Vector3f read(INBT nbt) {
                if (nbt instanceof IntArrayNBT) {
                    int[] array = ((IntArrayNBT) nbt).getAsIntArray();
                    if (array.length == 3)
                        return new Vector3f(Float.intBitsToFloat(array[0]), Float.intBitsToFloat(array[1]), Float.intBitsToFloat(array[2]));
                }
                return null;
            }
            
            @Override
            public INBT write(Vector3f value) {
                return new IntArrayNBT(new int[] { Float.floatToIntBits(value.x), Float.floatToIntBits(value.y), Float.floatToIntBits(value.z) });
            }
            
            @Override
            public Vector3f move(Vector3f value, LittleGridContext context, LittleVec offset) {
                return value;
            }
            
            @Override
            public Vector3f flip(Vector3f value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
                RotationUtils.flip(value, axis);
                return value;
            }
            
            @Override
            public Vector3f rotate(Vector3f value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
                RotationUtils.rotate(value, rotation);
                return value;
            }
            
            @Override
            public Vector3f getDefault() {
                return new Vector3f();
            }
        });
    }
    
    public abstract T read(INBT nbt);
    
    public abstract INBT write(T value);
    
    public abstract T move(T value, LittleGridContext context, LittleVec offset);
    
    public abstract T flip(T value, LittleGridContext context, Axis axis, LittleVec doubledCenter);
    
    public abstract T rotate(T value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter);
    
    public abstract T getDefault();
    
    public LittleGrid getContext(T value) {
        return null;
    }
    
    public void convertToSmallest(T value) {
        
    }
    
    public void advancedScale(T value, int from, int to) {
        
    }
    
    public PlacePreview getPlacePreview(T value, LittlePreviews previews, StructureDirectionalField field) {
        return null;
    }
    
}
