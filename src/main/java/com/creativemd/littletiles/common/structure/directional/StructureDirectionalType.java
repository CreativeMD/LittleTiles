package com.creativemd.littletiles.common.structure.directional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public abstract class StructureDirectionalType<T> {
    
    private static HashMap<Class, StructureDirectionalType> types = new HashMap<>();
    private static List<Function<Field, StructureDirectionalType>> specialFactories = new ArrayList<>();
    
    public static StructureDirectionalType getType(Field field) {
        StructureDirectionalType type = types.get(field.getType());
        if (type != null)
            return type;
        
        for (Function<Field, StructureDirectionalType> factory : specialFactories) {
            type = factory.apply(field);
            if (type != null)
                return type;
        }
        
        throw new RuntimeException("No registered directional type for " + field.getType() + ", " + field.getName());
    }
    
    public static StructureDirectionalType getSubType(Class clazz) {
        StructureDirectionalType type = types.get(clazz);
        if (type != null)
            return type;
        throw new RuntimeException("No registered directional type for " + clazz + ", " + clazz.getName());
    }
    
    public static void register(Function<Field, StructureDirectionalType> factory) {
        specialFactories.add(factory);
    }
    
    public static <T> void register(Class<T> clazz, StructureDirectionalType<T> type) {
        if (types.containsKey(clazz))
            throw new IllegalArgumentException("Type already exists. " + clazz);
        
        types.put(clazz, type);
    }
    
    static {
        register(x -> {
            if (List.class.isAssignableFrom(x.getType()))
                return new StructureDirectionalType<List>() {
                    
                    private final StructureDirectionalType subType;
                    
                    {
                        ParameterizedType type = (ParameterizedType) x.getGenericType();
                        subType = getSubType((Class) type.getActualTypeArguments()[0]);
                    }
                    
                    @Override
                    public List read(StructureDirectionalField field, LittleStructure structure, NBTBase nbt) {
                        List list = structure != null ? (List) field.get(structure) : new ArrayList<>();
                        list.clear();
                        if (nbt instanceof NBTTagList) {
                            NBTTagList tag = (NBTTagList) nbt;
                            for (int i = 0; i < tag.tagCount(); i++) {
                                Object object = subType.read(field, structure, tag.get(i));
                                if (object != null)
                                    list.add(object);
                            }
                        }
                        return list;
                    }
                    
                    @Override
                    public NBTBase write(StructureDirectionalField field, List value) {
                        NBTTagList list = new NBTTagList();
                        for (int i = 0; i < value.size(); i++) {
                            NBTBase tag = subType.write(field, value.get(i));
                            if (tag != null)
                                list.appendTag(tag);
                        }
                        return list;
                    }
                    
                    @Override
                    public List move(StructureDirectionalField field, List value, LittleGridContext context, LittleVec offset) {
                        for (int i = 0; i < value.size(); i++)
                            subType.move(field, value.get(i), context, offset);
                        return value;
                    }
                    
                    @Override
                    public List mirror(StructureDirectionalField field, List value, LittleGridContext grid, Axis axis, LittleVec doubledCenter) {
                        for (int i = 0; i < value.size(); i++)
                            subType.mirror(field, value.get(i), grid, axis, doubledCenter);
                        return value;
                    }
                    
                    @Override
                    public List rotate(StructureDirectionalField field, List value, LittleGridContext grid, Rotation rotation, LittleVec doubledCenter) {
                        for (int i = 0; i < value.size(); i++)
                            subType.rotate(field, value.get(i), grid, rotation, doubledCenter);
                        return value;
                    }
                    
                    @Override
                    public Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue) {
                        List value = (List) field.get(structure);
                        value.clear();
                        if (defaultValue != null && defaultValue instanceof List) {
                            List list = (List) defaultValue;
                            value.addAll(list);
                        }
                        return value;
                    }
                    
                };
            return null;
        });
        
        register(EnumFacing.class, new StructureDirectionalTypeSimple<EnumFacing>() {
            @Override
            public EnumFacing read(NBTBase nbt) {
                if (nbt instanceof NBTTagInt)
                    return EnumFacing.VALUES[((NBTTagInt) nbt).getInt()];
                return null;
            }
            
            @Override
            public NBTBase write(EnumFacing value) {
                return new NBTTagInt(value.ordinal());
            }
            
            @Override
            public EnumFacing move(EnumFacing value, LittleGridContext context, LittleVec offset) {
                return value;
            }
            
            @Override
            public EnumFacing mirror(EnumFacing value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
                if (axis == value.getAxis())
                    return value.getOpposite();
                return value;
            }
            
            @Override
            public EnumFacing rotate(EnumFacing value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
                return RotationUtils.rotate(value, rotation);
            }
            
            @Override
            public EnumFacing getDefault() {
                return EnumFacing.EAST;
            }
        });
        register(Axis.class, new StructureDirectionalTypeSimple<Axis>() {
            
            @Override
            public Axis read(NBTBase nbt) {
                if (nbt instanceof NBTTagInt)
                    return Axis.values()[((NBTTagInt) nbt).getInt()];
                return null;
            }
            
            @Override
            public NBTBase write(Axis value) {
                return new NBTTagInt(value.ordinal());
            }
            
            @Override
            public Axis move(Axis value, LittleGridContext context, LittleVec offset) {
                return value;
            }
            
            @Override
            public Axis mirror(Axis value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
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
        register(StructureRelative.class, new StructureDirectionalTypeSimple<StructureRelative>() {
            
            @Override
            public StructureRelative read(NBTBase nbt) {
                if (nbt instanceof NBTTagIntArray)
                    return new StructureRelative(((NBTTagIntArray) nbt).getIntArray());
                return null;
            }
            
            @Override
            public NBTBase write(StructureRelative value) {
                return new NBTTagIntArray(value.write());
            }
            
            @Override
            public StructureRelative move(StructureRelative value, LittleGridContext context, LittleVec offset) {
                value.move(context, offset);
                return value;
            }
            
            @Override
            public StructureRelative mirror(StructureRelative value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
                value.flip(context, axis, doubledCenter);
                return value;
            }
            
            @Override
            public StructureRelative rotate(StructureRelative value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
                value.rotate(context, rotation, doubledCenter);
                return value;
            }
            
            @Override
            public LittleGridContext getContext(StructureRelative value) {
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
        register(Vector3f.class, new StructureDirectionalTypeSimple<Vector3f>() {
            
            @Override
            public Vector3f read(NBTBase nbt) {
                if (nbt instanceof NBTTagIntArray) {
                    int[] array = ((NBTTagIntArray) nbt).getIntArray();
                    if (array.length == 3)
                        return new Vector3f(Float.intBitsToFloat(array[0]), Float.intBitsToFloat(array[1]), Float
                                .intBitsToFloat(array[2]));
                }
                return null;
            }
            
            @Override
            public NBTBase write(Vector3f value) {
                return new NBTTagIntArray(new int[] { Float.floatToIntBits(value.x), Float.floatToIntBits(value.y), Float
                        .floatToIntBits(value.z) });
            }
            
            @Override
            public Vector3f move(Vector3f value, LittleGridContext context, LittleVec offset) {
                return value;
            }
            
            @Override
            public Vector3f mirror(Vector3f value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
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
    
    public abstract T read(StructureDirectionalField field, LittleStructure structure, NBTBase nbt);
    
    public abstract NBTBase write(StructureDirectionalField field, T value);
    
    public abstract T move(StructureDirectionalField field, T value, LittleGridContext context, LittleVec offset);
    
    public abstract T mirror(StructureDirectionalField field, T value, LittleGridContext context, Axis axis, LittleVec doubledCenter);
    
    public abstract T rotate(StructureDirectionalField field, T value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter);
    
    public abstract Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue);
    
    public LittleGridContext getContext(StructureDirectionalField field, T value) {
        return null;
    }
    
    public void convertToSmallest(T value) {
        
    }
    
    public void advancedScale(T value, int from, int to) {
        
    }
    
    public PlacePreview getPlacePreview(T value, LittlePreviews previews, StructureDirectionalField field) {
        return null;
    }
    
    public static abstract class StructureDirectionalTypeSimple<T> extends StructureDirectionalType<T> {
        
        @Override
        public T read(StructureDirectionalField field, LittleStructure structure, NBTBase nbt) {
            return read(nbt);
        }
        
        public abstract T read(NBTBase nbt);
        
        @Override
        public NBTBase write(StructureDirectionalField field, T value) {
            return write(value);
        }
        
        public abstract NBTBase write(T value);
        
        @Override
        public T move(StructureDirectionalField field, T value, LittleGridContext context, LittleVec offset) {
            return move(value, context, offset);
        }
        
        public abstract T move(T value, LittleGridContext context, LittleVec offset);
        
        @Override
        public T mirror(StructureDirectionalField field, T value, LittleGridContext grid, Axis axis, LittleVec doubledCenter) {
            return mirror(value, grid, axis, doubledCenter);
        }
        
        public abstract T mirror(T value, LittleGridContext grid, Axis axis, LittleVec doubledCenter);
        
        @Override
        public T rotate(StructureDirectionalField field, T value, LittleGridContext grid, Rotation rotation, LittleVec doubledCenter) {
            return rotate(value, grid, rotation, doubledCenter);
        }
        
        public abstract T rotate(T value, LittleGridContext grid, Rotation rotation, LittleVec doubledCenter);
        
        @Override
        public Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue) {
            if (defaultValue != null)
                return defaultValue;
            return getDefault();
        }
        
        public abstract T getDefault();
        
        @Override
        public LittleGridContext getContext(StructureDirectionalField field, T value) {
            return getContext(value);
        }
        
        public LittleGridContext getContext(T value) {
            return null;
        }
    }
}
