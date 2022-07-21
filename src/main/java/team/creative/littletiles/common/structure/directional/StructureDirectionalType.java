package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.structure.relative.StructureRelative;

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
        registerType(Facing.class, new StructureDirectionalType<Facing>() {
            @Override
            public Facing read(Tag nbt) {
                if (nbt instanceof IntTag)
                    return Facing.values()[((IntTag) nbt).getAsInt()];
                return null;
            }
            
            @Override
            public Tag write(Facing value) {
                return IntTag.valueOf(value.ordinal());
            }
            
            @Override
            public Facing move(Facing value, LittleVecGrid offset) {
                return value;
            }
            
            @Override
            public Facing mirror(Facing value, LittleGrid context, Axis axis, LittleVec doubledCenter) {
                return axis.mirror(value);
            }
            
            @Override
            public Facing rotate(Facing value, LittleGrid context, Rotation rotation, LittleVec doubledCenter) {
                return rotation.rotate(value);
            }
            
            @Override
            public Facing getDefault() {
                return Facing.EAST;
            }
        });
        registerType(Axis.class, new StructureDirectionalType<Axis>() {
            
            @Override
            public Axis read(Tag nbt) {
                if (nbt instanceof IntTag)
                    return Axis.values()[((IntTag) nbt).getAsInt()];
                return null;
            }
            
            @Override
            public Tag write(Axis value) {
                return IntTag.valueOf(value.ordinal());
            }
            
            @Override
            public Axis move(Axis value, LittleVecGrid offset) {
                return value;
            }
            
            @Override
            public Axis mirror(Axis value, LittleGrid context, Axis axis, LittleVec doubledCenter) {
                return value;
            }
            
            @Override
            public Axis rotate(Axis value, LittleGrid context, Rotation rotation, LittleVec doubledCenter) {
                return rotation.rotate(value);
            }
            
            @Override
            public Axis getDefault() {
                return Axis.X;
            }
            
        });
        registerType(StructureRelative.class, new StructureDirectionalType<StructureRelative>() {
            
            @Override
            public StructureRelative read(Tag nbt) {
                if (nbt instanceof IntArrayTag)
                    return new StructureRelative(((IntArrayTag) nbt).getAsIntArray());
                return null;
            }
            
            @Override
            public Tag write(StructureRelative value) {
                return new IntArrayTag(value.write());
            }
            
            @Override
            public StructureRelative move(StructureRelative value, LittleVecGrid offset) {
                value.move(offset);
                return value;
            }
            
            @Override
            public StructureRelative mirror(StructureRelative value, LittleGrid context, Axis axis, LittleVec doubledCenter) {
                value.mirror(context, axis, doubledCenter);
                return value;
            }
            
            @Override
            public StructureRelative rotate(StructureRelative value, LittleGrid context, Rotation rotation, LittleVec doubledCenter) {
                value.rotate(context, rotation, doubledCenter);
                return value;
            }
            
            @Override
            public LittleGrid getGrid(StructureRelative value) {
                return value.getGrid();
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
            public LittlePlaceBoxRelative getPlaceBox(StructureRelative value, LittleGroup previews, StructureDirectionalField field) {
                return value.getPlaceBox(previews, field);
            }
            
            @Override
            public StructureRelative getDefault() {
                return new StructureRelative(new LittleBox(0, 0, 0, 1, 1, 1), LittleGrid.defaultGrid());
            }
            
        });
        registerType(Vec3f.class, new StructureDirectionalType<Vec3f>() {
            
            @Override
            public Vec3f read(Tag nbt) {
                if (nbt instanceof IntArrayTag) {
                    int[] array = ((IntArrayTag) nbt).getAsIntArray();
                    if (array.length == 3)
                        return new Vec3f(Float.intBitsToFloat(array[0]), Float.intBitsToFloat(array[1]), Float.intBitsToFloat(array[2]));
                }
                return null;
            }
            
            @Override
            public Tag write(Vec3f value) {
                return new IntArrayTag(new int[] { Float.floatToIntBits(value.x), Float.floatToIntBits(value.y), Float.floatToIntBits(value.z) });
            }
            
            @Override
            public Vec3f move(Vec3f value, LittleVecGrid offset) {
                return value;
            }
            
            @Override
            public Vec3f mirror(Vec3f value, LittleGrid context, Axis axis, LittleVec doubledCenter) {
                axis.mirror(value);
                return value;
            }
            
            @Override
            public Vec3f rotate(Vec3f value, LittleGrid context, Rotation rotation, LittleVec doubledCenter) {
                rotation.transform(value);
                return value;
            }
            
            @Override
            public Vec3f getDefault() {
                return new Vec3f();
            }
        });
    }
    
    public abstract T read(Tag nbt);
    
    public abstract Tag write(T value);
    
    public abstract T move(T value, LittleVecGrid vec);
    
    public abstract T mirror(T value, LittleGrid context, Axis axis, LittleVec doubledCenter);
    
    public abstract T rotate(T value, LittleGrid context, Rotation rotation, LittleVec doubledCenter);
    
    public abstract T getDefault();
    
    public LittleGrid getGrid(T value) {
        return null;
    }
    
    public void convertToSmallest(T value) {
        
    }
    
    public void advancedScale(T value, int from, int to) {
        
    }
    
    public LittlePlaceBoxRelative getPlaceBox(T value, LittleGroup group, StructureDirectionalField field) {
        return null;
    }
    
}
