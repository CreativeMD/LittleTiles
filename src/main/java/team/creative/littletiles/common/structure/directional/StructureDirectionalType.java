package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
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
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public abstract class StructureDirectionalType<T> {
    
    private static HashMap<Class, StructureDirectionalType> types = new HashMap<>();
    private static List<BiFunction<Field, Class, StructureDirectionalType>> specialFactories = new ArrayList<>();
    
    public static StructureDirectionalType getType(Class origin, Field field) {
        StructureDirectionalType type = types.get(field.getType());
        if (type != null)
            return type;
        
        for (BiFunction<Field, Class, StructureDirectionalType> factory : specialFactories) {
            type = factory.apply(field, origin);
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
    
    public static <T> void register(Class<T> clazz, StructureDirectionalType<T> type) {
        if (types.containsKey(clazz))
            throw new IllegalArgumentException("Type already exists. " + clazz);
        
        types.put(clazz, type);
    }
    
    public static void register(BiFunction<Field, Class, StructureDirectionalType> factory) {
        specialFactories.add(factory);
    }
    
    private static int searchIndex(Class clazz, TypeVariable toFind) {
        var typeParamters = clazz.getTypeParameters();
        for (int i = 0; i < typeParamters.length; i++)
            if (typeParamters[i] == toFind) {
                return i;
            }
        
        throw new IllegalArgumentException("Type parameter " + toFind + " could not be found in " + clazz);
    }
    
    private static Class searchType(Class origin, Class clazz, TypeVariable toFind) {
        List<Class> classTree = new ArrayList<>();
        classTree.add(origin);
        Class temp = origin;
        while (temp.getSuperclass() != null) {
            var newTemp = temp.getSuperclass();
            if (newTemp == clazz)
                break;
            else if (newTemp == null)
                throw new IllegalArgumentException(origin + " does not extend " + clazz);
            classTree.add(newTemp);
            temp = newTemp;
        }
        
        int paramIndex = searchIndex(clazz, toFind);
        
        int index = classTree.size() - 1;
        while (index >= 0) {
            ParameterizedType genericType = (ParameterizedType) classTree.get(index).getGenericSuperclass(); // has to parameterized as it does extend origin
            var actual = genericType.getActualTypeArguments()[paramIndex];
            if (actual instanceof Class c)
                return c;
            if (actual instanceof TypeVariable t) {
                paramIndex = searchIndex(classTree.get(index), t);
                index--;
            }
        }
        
        throw new IllegalArgumentException("Could not find valid class type of " + toFind + " in " + clazz);
    }
    
    static {
        register((x, y) -> {
            if (List.class.isAssignableFrom(x.getType()))
                return new StructureDirectionalType<List>() {
                    
                    private final StructureDirectionalType subType;
                    
                    {
                        ParameterizedType type = (ParameterizedType) x.getGenericType();
                        var actualType = type.getActualTypeArguments()[0];
                        if (actualType instanceof Class c)
                            subType = getSubType(c);
                        else if (actualType instanceof TypeVariable t)
                            subType = getSubType(searchType(y, (Class) t.getGenericDeclaration(), t));
                        else
                            throw new IllegalArgumentException("Could not find subtype of " + x);
                    }
                    
                    @Override
                    public List read(StructureDirectionalField field, LittleStructure structure, Tag nbt) {
                        List list = structure != null ? (List) field.get(structure) : new ArrayList<>();
                        list.clear();
                        if (nbt instanceof ListTag tag) {
                            for (int i = 0; i < tag.size(); i++) {
                                Object object = subType.read(field, structure, tag.get(i));
                                if (object != null)
                                    list.add(object);
                            }
                        }
                        return list;
                    }
                    
                    @Override
                    public Tag write(StructureDirectionalField field, List value) {
                        ListTag list = new ListTag();
                        for (int i = 0; i < value.size(); i++) {
                            Tag tag = subType.write(field, value.get(i));
                            if (tag != null)
                                list.add(tag);
                        }
                        return list;
                    }
                    
                    @Override
                    public List move(StructureDirectionalField field, List value, LittleVecGrid vec) {
                        for (int i = 0; i < value.size(); i++)
                            subType.move(field, value.get(i), vec);
                        return value;
                    }
                    
                    @Override
                    public List mirror(StructureDirectionalField field, List value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
                        for (int i = 0; i < value.size(); i++)
                            subType.mirror(field, value.get(i), grid, axis, doubledCenter);
                        return value;
                    }
                    
                    @Override
                    public List rotate(StructureDirectionalField field, List value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
                        for (int i = 0; i < value.size(); i++)
                            subType.rotate(field, value.get(i), grid, rotation, doubledCenter);
                        return value;
                    }
                    
                    @Override
                    public Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue) {
                        List value = (List) field.get(structure);
                        value.clear();
                        if (defaultValue != null && defaultValue instanceof List list)
                            value.addAll(list);
                        return value;
                    }
                    
                };
            return null;
        });
        
        register(Facing.class, new StructureDirectionalTypeSimple<Facing>() {
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
            public Facing mirror(Facing value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
                return axis.mirror(value);
            }
            
            @Override
            public Facing rotate(Facing value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
                return rotation.rotate(value);
            }
            
            @Override
            public Facing getDefault() {
                return Facing.EAST;
            }
        });
        register(Axis.class, new StructureDirectionalTypeSimple<Axis>() {
            
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
            public Axis mirror(Axis value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
                return value;
            }
            
            @Override
            public Axis rotate(Axis value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
                return rotation.rotate(value);
            }
            
            @Override
            public Axis getDefault() {
                return Axis.X;
            }
            
        });
        register(StructureRelative.class, new StructureDirectionalTypeSimple<StructureRelative>() {
            
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
            public StructureRelative mirror(StructureRelative value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
                value.mirror(grid, axis, doubledCenter);
                return value;
            }
            
            @Override
            public StructureRelative rotate(StructureRelative value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
                value.rotate(grid, rotation, doubledCenter);
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
                return new StructureRelative(new LittleBox(0, 0, 0, 1, 1, 1), LittleGrid.MIN);
            }
            
        });
        register(Vec3f.class, new StructureDirectionalTypeSimple<Vec3f>() {
            
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
            public Vec3f mirror(Vec3f value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
                axis.mirror(value);
                return value;
            }
            
            @Override
            public Vec3f rotate(Vec3f value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
                rotation.transform(value);
                return value;
            }
            
            @Override
            public Vec3f getDefault() {
                return new Vec3f();
            }
        });
    }
    
    public abstract T read(StructureDirectionalField field, LittleStructure structure, Tag nbt);
    
    public abstract Tag write(StructureDirectionalField field, T value);
    
    public abstract T move(StructureDirectionalField field, T value, LittleVecGrid vec);
    
    public abstract T mirror(StructureDirectionalField field, T value, LittleGrid grid, Axis axis, LittleVec doubledCenter);
    
    public abstract T rotate(StructureDirectionalField field, T value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter);
    
    public abstract Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue);
    
    public LittleGrid getGrid(StructureDirectionalField field, T value) {
        return null;
    }
    
    public void convertToSmallest(T value) {}
    
    public void advancedScale(T value, int from, int to) {}
    
    public LittlePlaceBoxRelative getPlaceBox(T value, LittleGroup group, StructureDirectionalField field) {
        return null;
    }
    
    public static abstract class StructureDirectionalTypeSimple<T> extends StructureDirectionalType<T> {
        
        @Override
        public T read(StructureDirectionalField field, LittleStructure structure, Tag nbt) {
            return read(nbt);
        }
        
        public abstract T read(Tag nbt);
        
        @Override
        public Tag write(StructureDirectionalField field, T value) {
            return write(value);
        }
        
        public abstract Tag write(T value);
        
        @Override
        public T move(StructureDirectionalField field, T value, LittleVecGrid vec) {
            return move(value, vec);
        }
        
        public abstract T move(T value, LittleVecGrid vec);
        
        @Override
        public T mirror(StructureDirectionalField field, T value, LittleGrid grid, Axis axis, LittleVec doubledCenter) {
            return mirror(value, grid, axis, doubledCenter);
        }
        
        public abstract T mirror(T value, LittleGrid grid, Axis axis, LittleVec doubledCenter);
        
        @Override
        public T rotate(StructureDirectionalField field, T value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
            return rotate(value, grid, rotation, doubledCenter);
        }
        
        public abstract T rotate(T value, LittleGrid grid, Rotation rotation, LittleVec doubledCenter);
        
        @Override
        public Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue) {
            if (defaultValue != null)
                return defaultValue;
            return getDefault();
        }
        
        public abstract T getDefault();
        
        @Override
        public LittleGrid getGrid(StructureDirectionalField field, T value) {
            return getGrid(value);
        }
        
        public LittleGrid getGrid(T value) {
            return null;
        }
        
    }
    
}
