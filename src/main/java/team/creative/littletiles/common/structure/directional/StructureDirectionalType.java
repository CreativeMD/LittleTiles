package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.structure.LittleStructure;

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
    
    static {
        StructureDirectionalTypes.init();
    }
    
    public abstract T read(StructureDirectionalField field, LittleStructure structure, Tag nbt);
    
    public abstract Tag write(StructureDirectionalField field, T value);
    
    public abstract T move(StructureDirectionalField field, T value, LittleVecGrid vec);
    
    public abstract T transform(StructureDirectionalField field, T value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter);
    
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
        public T transform(StructureDirectionalField field, T value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter) {
            return transform(value, grid, matrix, doubledCenter);
        }
        
        public abstract T transform(T value, LittleGrid grid, IntMatrix3c matrix, LittleVec doubledCenter);
        
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
