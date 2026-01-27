package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.creativecore.common.util.math.vec.Vec3f;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.animation.AnimationState;
import team.creative.littletiles.common.structure.animation.AnimationStateDirected;
import team.creative.littletiles.common.structure.animation.AnimationTransition;
import team.creative.littletiles.common.structure.directional.StructureDirectionalType.StructureDirectionalTypeSimple;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class StructureDirectionalTypes {
    
    public static void init() {
        StructureDirectionalType.register((x, y) -> {
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
                    public List transform(StructureDirectionalField field, List value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                        for (int i = 0; i < value.size(); i++)
                            subType.transform(field, value.get(i), grid, matrix, doubledCenter);
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
        
        StructureDirectionalType.register(Facing.class, new StructureDirectionalTypeSimple<Facing>() {
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
            public Facing transform(Facing value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                return value.transform(matrix);
            }
            
            @Override
            public Facing getDefault() {
                return Facing.EAST;
            }
        });
        StructureDirectionalType.register(Axis.class, new StructureDirectionalTypeSimple<Axis>() {
            
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
            public Axis transform(Axis value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                return value.transform(matrix);
            }
            
            @Override
            public Axis getDefault() {
                return Axis.X;
            }
            
        });
        StructureDirectionalType.register(StructureRelative.class, new StructureDirectionalTypeSimple<StructureRelative>() {
            
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
            public StructureRelative transform(StructureRelative value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                value.transform(grid, matrix, doubledCenter);
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
        StructureDirectionalType.register(Vec3f.class, new StructureDirectionalTypeSimple<Vec3f>() {
            
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
            public Vec3f transform(Vec3f value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                matrix.transform(value);
                return value;
            }
            
            @Override
            public Vec3f getDefault() {
                return new Vec3f();
            }
        });
        StructureDirectionalType.register(AnimationTransition.class, new StructureDirectionalTypeSimple<AnimationTransition>() {
            
            @Override
            public AnimationTransition read(Tag nbt) {
                if (nbt instanceof CompoundTag c)
                    return new AnimationTransition(c);
                return null;
            }
            
            @Override
            public Tag write(AnimationTransition value) {
                return value.save();
            }
            
            @Override
            public AnimationTransition move(AnimationTransition value, LittleVecGrid vec) {
                return value;
            }
            
            @Override
            public AnimationTransition transform(AnimationTransition value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                value.timeline.transform(matrix);
                return value;
            }
            
            @Override
            public AnimationTransition getDefault() {
                throw new UnsupportedOperationException();
            }
            
        });
        
        StructureDirectionalType.register(AnimationState.class, new StructureDirectionalTypeSimple<AnimationState>() {
            
            @Override
            public AnimationState read(Tag nbt) {
                if (nbt instanceof CompoundTag c)
                    return new AnimationState(c);
                return null;
            }
            
            @Override
            public Tag write(AnimationState value) {
                return value.save();
            }
            
            @Override
            public AnimationState move(AnimationState value, LittleVecGrid vec) {
                return value;
            }
            
            @Override
            public AnimationState transform(AnimationState value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                value.transform(matrix);
                return value;
            }
            
            @Override
            public AnimationState getDefault() {
                throw new UnsupportedOperationException();
            }
            
        });
        StructureDirectionalType.register(AnimationStateDirected.class, new StructureDirectionalTypeSimple<AnimationStateDirected>() {
            
            @Override
            public AnimationStateDirected read(Tag nbt) {
                if (nbt instanceof CompoundTag c)
                    return new AnimationStateDirected(c);
                return null;
            }
            
            @Override
            public Tag write(AnimationStateDirected value) {
                return value.save();
            }
            
            @Override
            public AnimationStateDirected move(AnimationStateDirected value, LittleVecGrid vec) {
                return value;
            }
            
            @Override
            public AnimationStateDirected transform(AnimationStateDirected value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                value.transform(matrix);
                return value;
            }
            
            @Override
            public AnimationStateDirected getDefault() {
                throw new UnsupportedOperationException();
            }
            
        });
        
        StructureDirectionalType.register(LittleGroup.class, new StructureDirectionalType<LittleGroup>() {
            
            @Override
            public LittleGroup read(StructureDirectionalField field, LittleStructure structure, Tag nbt) {
                if (nbt instanceof CompoundTag c)
                    return LittleGroup.load(c);
                return null;
            }
            
            @Override
            public Tag write(StructureDirectionalField field, LittleGroup value) {
                if (value.isEmptyIncludeChildren())
                    return new CompoundTag();
                return LittleGroup.save(value);
            }
            
            @Override
            public LittleGroup move(StructureDirectionalField field, LittleGroup value, LittleVecGrid vec) {
                value.move(vec);
                return value;
            }
            
            @Override
            public LittleGroup transform(StructureDirectionalField field, LittleGroup value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
                value.convertTo(grid);
                value.transform(matrix, doubledCenter);
                return value;
            }
            
            @Override
            public Object getDefault(StructureDirectionalField field, LittleStructure structure, Object defaultValue) {
                return new LittleGroup();
            }
            
            @Override
            public LittlePlaceBoxRelative getPlaceBox(LittleGroup value, LittleGroup group, StructureDirectionalField field) {
                if (value.isEmptyIncludeChildren())
                    return null;
                return new LittlePlaceBoxRelative(value.getSurroundingBox(), null, field);
            }
            
            @Override
            public void convertToSmallest(LittleGroup value) {
                value.convertToSmallest();
            }
            
            @Override
            public void advancedScale(LittleGroup value, int from, int to) {
                value.advancedScale(from, to);
            }
            
            @Override
            public LittleGrid getGrid(StructureDirectionalField field, LittleGroup value) {
                return value.getGrid();
            }
        });
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
    
}
