package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.Field;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.structure.LittleStructure;

public class StructureDirectionalField {
    
    public final Field field;
    public final String key;
    public final String saveKey;
    public final StructureDirectional annotation;
    public final StructureDirectionalType type;
    private Object defaultValue;
    
    public StructureDirectionalField(Class structureClass, Field field, StructureDirectional annotation) {
        this.field = field;
        this.key = field.getName();
        this.saveKey = annotation.saveKey().isEmpty() ? key : annotation.saveKey();
        this.annotation = annotation;
        this.type = StructureDirectionalType.getType(structureClass, field);
    }
    
    public void set(LittleStructure structure, Object value) {
        try {
            field.set(structure, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object get(LittleStructure structure) {
        try {
            return field.get(structure);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object createAndSet(LittleStructure structure, CompoundTag nbt) {
        Object relative = create(structure, nbt);
        set(structure, relative);
        return relative;
    }
    
    public void setDefault(Object value) {
        this.defaultValue = value;
    }
    
    public Object create(LittleStructure structure, CompoundTag nbt) {
        Object value = type.read(this, structure, nbt.get(saveKey));
        if (value == null)
            return getDefault(structure);
        return value;
    }
    
    public Object createTemporary(CompoundTag nbt) {
        return create(null, nbt);
    }
    
    public void save(CompoundTag nbt, Object value) {
        nbt.put(saveKey, type.write(this, value));
    }
    
    public Object move(Object value, LittleVecGrid vec) {
        return type.move(this, value, vec);
    }
    
    public Object mirror(Object value, LittleGrid context, Axis axis, LittleVec doubledCenter) {
        return type.mirror(this, value, context, axis, doubledCenter);
    }
    
    public Object rotate(Object value, LittleGrid context, Rotation rotation, LittleVec doubledCenter) {
        return type.rotate(this, value, context, rotation, doubledCenter);
    }
    
    public LittleGrid getGrid(Object value) {
        return type.getGrid(this, value);
    }
    
    public void convertToSmallest(Object value) {
        type.convertToSmallest(value);
    }
    
    public void advancedScale(Object value, int from, int to) {
        type.advancedScale(value, from, to);
    }
    
    public LittlePlaceBoxRelative getPlaceBox(Object value, LittleGroup group) {
        return type.getPlaceBox(value, group, this);
    }
    
    public Object getDefault(LittleStructure structure) {
        return type.getDefault(this, structure, defaultValue);
    }
}
